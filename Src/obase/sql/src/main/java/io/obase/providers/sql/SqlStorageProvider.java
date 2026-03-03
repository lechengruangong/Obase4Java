/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于SQL源的存储提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:48:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IMappingWorkflow;
import io.obase.core.IStorageProvider;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.common.EIsolationLevel;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.mapping.pipeline.PostExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.PreExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.OpExecutor;
import io.obase.core.query.QueryContext;
import io.obase.core.query.QueryOp;
import io.obase.core.query.oop.OopPipelineBuilder;
import io.obase.core.saving.IAmbientTransactionable;
import io.obase.core.saving.ITransactionable;
import io.obase.core.saving.NothingUpdatedException;
import io.obase.providers.sql.common.SqlUtils;
import io.obase.providers.sql.rop.*;
import io.obase.providers.sql.sqlobject.*;
import org.apache.commons.lang3.time.StopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 特定于SQL源的存储提供程序。
 * 实施说明
 * 对ExecutePipeline方法的实现：
 * (1)穿越运算管道取出最后一个节点（RopTerminator），设置包含树；
 * (2)执行运算管道，然后从关系运算上下文取出QuerySql，执行Sql语句；
 * (3)使用结果读取器（Query.Rop.ResultReader）工厂生产相应读取器，从Sql执行结果中读取对象实例，参见顺序图“Rop/查询执行器/执行查询”。
 */
public class SqlStorageProvider implements ITransactionable, IStorageProvider, IAmbientTransactionable {

    /**
     * SQL语句执行器
     */
    private final ISqlExecutor sqlExecutor;

    /**
     * 本地事务是否开始
     */
    private boolean transactionBegun;

    /**
     * 创建SqlStorageProvider实例
     *
     * @param sqlExecutor Sql执行器
     */
    public SqlStorageProvider(ISqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    /**
     * 准备存储资源,如打开数据库连接
     */
    @Override
    public void prepareResource() {
        this.sqlExecutor.openConnection();
    }

    /**
     * 释放存储资源,如关闭数据库连接
     */
    @Override
    public void releaseResource() {
        this.sqlExecutor.closeConnection();
    }

    /**
     * 获取一个值，该值指示是否已开启本地事务
     *
     * @return 指示是否已开启本地事务
     */
    @Override
    public boolean getTransactionBegun() {
        this.transactionBegun = this.sqlExecutor.getTransactionBegun();
        return this.transactionBegun;
    }

    /**
     * 启动一个新的映射工作流
     *
     * @return 一个用于跟踪工作流的对象，它实现了IMappingWorkflow接口
     */
    @Override
    public IMappingWorkflow createMappingWorkflow() {
        return new SqlMappingWorkflow(this.sqlExecutor);
    }

    /**
     * 删除符合指定条件的对象
     *
     * @param objType               要删除的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    @Override
    public int delete(ObjectType objType, LambdaExpression filterExpression, ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        ObjectReferencePack<ISource> source = new ObjectReferencePack<>();
        ObjectReferencePack<ICriteria> criteria = new ObjectReferencePack<>();
        //构造Sql
        this.getSourceAndCriteria(filterExpression, objType.getModel(), source, criteria, objType.getClrType());
        ChangeSql sql = new ChangeSql(source.realValue, criteria.realValue);
        sql.setTargetSource(new SimpleSource(objType.getTargetTable()));
        if (source.realValue instanceof MonomerSource) {
            MonomerSource monomerSource = (MonomerSource) source.realValue;
            sql.setTargetSource(monomerSource);
        }

        return this.executeSql(preExecutionCallback, postExecutionCallback, sql);
    }

    /**
     * 搜索符合指定条件的对象，为其属性（部分或全部）设置新值。
     *
     * @param objType               要修改其属性的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param newValues             存储属性新值的字典
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    @Override
    public int setAttributes(ObjectType objType, LambdaExpression filterExpression, Map<String, Object> newValues, ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        //检查传入的集合
        for (String valuePair : newValues.keySet()) {
            //无法找到对应的属性名称
            if (objType.getAttributes().stream().allMatch(p -> (!Objects.equals(p.getTargetField(), valuePair) && !p.getTargetField().toLowerCase().equals(valuePair.toLowerCase(Locale.ROOT)))))
                throw new IllegalArgumentException("映射集合中的Key无法与对象中任意属性相对应");
            //要改动的属性名称为自增主键
            if (objType instanceof EntityType) {
                EntityType entityType = (EntityType) objType;
                if (entityType.getKeyFields().stream().anyMatch(p -> (Objects.equals(p, valuePair) || p.equals(valuePair.toLowerCase(Locale.ROOT)))) && entityType.getKeyIsSelfIncreased())
                    throw new IllegalArgumentException("不能更改自增主键的值");
            }
        }

        List<IFieldSetter> setters = new ArrayList<>();
        for (String key : newValues.keySet()) {
            Attribute attribute = objType.getAttribute(key);
            if (attribute == null)
                attribute = objType.getAttributes().stream().filter(p -> p.getTargetField().equalsIgnoreCase(key)).findFirst().orElse(null);
            if (attribute == null)
                throw new IllegalArgumentException("映射集合中的" + key + "无法找到对应的属性");
            IFieldSetter set = SqlUtils.getFieldSetter(attribute.getDataType(), attribute.getTargetField(), newValues.get(key), false, null);
            setters.add(set);
        }

        ChangeSql sql = this.getChangeSql(objType, filterExpression, setters);

        return this.executeSql(preExecutionCallback, postExecutionCallback, sql);
    }

    /**
     * 搜索符合指定条件的对象，为其属性（部分或全部）施加一个增量
     *
     * @param objType               要修改其属性的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param increaseValues        存储增量值的字典
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    @Override
    public int increaseAttributes(ObjectType objType, LambdaExpression filterExpression, Map<String, Object> increaseValues, ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        //检查传入的集合
        for (String valuePair : increaseValues.keySet()) {
            //要改动的属性名称为自增主键
            if (objType instanceof EntityType) {
                EntityType entityType = (EntityType) objType;
                if (entityType.getKeyFields().stream().anyMatch(p -> (Objects.equals(p, valuePair) || p.equals(valuePair.toLowerCase(Locale.ROOT)))) && entityType.getKeyIsSelfIncreased())
                    throw new IllegalArgumentException("不能更改自增主键的值");
            }
            //无法找到对应的属性名称
            if (objType.getAttributes().stream().allMatch(p -> (!Objects.equals(p.getTargetField(), valuePair) && !p.getTargetField().toLowerCase().equals(valuePair.toLowerCase(Locale.ROOT)))))
                throw new IllegalArgumentException("映射集合中的Key无法与对象中任意属性相对应");
        }

        List<IFieldSetter> setters = new ArrayList<>();
        for (String key : increaseValues.keySet()) {
            Attribute attribute = objType.getAttribute(key);
            if (!attribute.getDataType().isPrimitive())
                throw new IllegalArgumentException("无法为非值类型创建增量字段设值器");
            IFieldSetter set = SqlUtils.getFieldSetter(attribute.getDataType(), attribute.getTargetField(), increaseValues.get(key), true, null);
            setters.add(set);
        }

        ChangeSql sql = this.getChangeSql(objType, filterExpression, setters);

        return this.executeSql(preExecutionCallback, postExecutionCallback, sql);
    }

    /**
     * 生成修改Sql语句
     *
     * @param objType          对象类型
     * @param filterExpression 筛选表达式
     * @param setters          设值器集合
     * @return Sql语句
     */
    private ChangeSql getChangeSql(ObjectType objType, LambdaExpression filterExpression, List<IFieldSetter> setters) {
        ObjectReferencePack<ISource> source = new ObjectReferencePack<>();
        ObjectReferencePack<ICriteria> criteria = new ObjectReferencePack<>();
        //构造Sql
        this.getSourceAndCriteria(filterExpression, objType.getModel(), source, criteria, objType.getClrType());
        ChangeSql sql = new ChangeSql(source.realValue, criteria.realValue, setters);
        sql.setTargetSource(new SimpleSource(objType.getTargetTable()));
        if (source.realValue instanceof MonomerSource) {
            MonomerSource monomerSource = (MonomerSource) source.realValue;
            sql.setTargetSource(monomerSource);
        }
        return sql;
    }

    /**
     * 执行就地修改Sql
     *
     * @param preExecutionCallback  执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 执行存储指令（如SQL语句）后回调的方法
     * @param sql                   执行的Sql
     * @return 受影响的行数
     */
    private int executeSql(ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback, ChangeSql sql) {
        StopWatch watch = new StopWatch();
        int affectCount;

        //触发事件
        if (preExecutionCallback != null) {
            preExecutionCallback.invoke(new PreExecuteCommandEventArgs(sql, this));
        }

        //执行Sql
        try {
            ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
            sqlParameters.realValue = new ArrayList<>();
            String sqlStr = sql.toSql(this.sqlExecutor.getSourceType(), sqlParameters, this.sqlExecutor.createParameterCreator());
            watch.start();
            affectCount = this.sqlExecutor.execute(sqlStr, sqlParameters.realValue.toArray(new DataParameter[0]));
            watch.stop();
            if (postExecutionCallback != null) {
                postExecutionCallback.invoke(new PostExecuteCommandEventArgs(sql, this, affectCount, null));
            }
        } catch (NothingUpdatedException e) {
            watch.stop();
            affectCount = 0;
            if (postExecutionCallback != null) {
                postExecutionCallback.invoke(new PostExecuteCommandEventArgs(sql, this, affectCount, null));
            }
        } catch (Exception ex) {
            watch.stop();
            affectCount = 0;
            if (postExecutionCallback != null) {
                postExecutionCallback.invoke(new PostExecuteCommandEventArgs(sql, this, affectCount, ex));
            }
            throw ex;
        }
        return affectCount;
    }


    /**
     * 为指定的查询生成运算管道
     *
     * @param query             要执行的查询
     * @param complement        返回后续查询（或称后续链）
     * @param complementBuilder 返回用于生成补充运算管道的生成器
     * @return 操作执行器
     */
    @Override
    public OpExecutor generatePipeline(QueryOp query, ObjectReferencePack<QueryOp> complement, ObjectReferencePack<OopPipelineBuilder> complementBuilder) {
        //创建查询运算符管道
        RopPipelineBuilder builder = new RopPipelineBuilder(query == null ? null : query.getModel(), this.sqlExecutor.getSourceType());
        if (query != null)
            query.accept(builder);
        complement.realValue = builder.getOutArgument();
        complementBuilder.realValue = new ComplementaryPipelineBuilder();
        return builder.getPipeline();
    }

    /**
     * 执行运算管道
     *
     * @param pipeline              要执行的运算管道
     * @param resultIncluding       指定由运算管道加载的对象须包含的引用（相对于结果类型），必须是同构的
     * @param attachObject          用于在对象上下文中附加对象的委托
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @param attachRoot            是否作为根对象附加
     * @return 执行结果
     */
    @Override
    public Object executePipeline(OpExecutor pipeline, AssociationTree resultIncluding, IAttachObject attachObject, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, boolean attachRoot) {
        //（1）实例化RopContext，将参数including作为初始包含树；
        //（2）执行运算管道，然后从关系运算上下文取出QuerySql，执行Sql语句；
        //（3）使用结果读取器（Query.Rop.ResultReader）工厂生产相应读取器，从Sql执行结果中读取对象实例，参见顺序图“Rop/查询执行器/执行查询”。
        if (pipeline instanceof RopExecutor) {
            RopExecutor ropExecutor = (RopExecutor) pipeline;
            RopContext ropContext = new RopContext(ropExecutor.getQueryOp().getSourceType(), ropExecutor.getQueryOp().getModel(), this.sqlExecutor.getSourceType(), resultIncluding, null);
            ropExecutor.execute(ropContext);

            //查询结果的sql对象
            QuerySql resultSql = ropContext.getResultSql();
            if (preExecutionCallback != null) {
                QueryContext context = new QueryContext(ropExecutor.getQueryOp(), null);
                context.setCommand(resultSql);
                preExecutionCallback.invoke(new QueryEventArgs(this, context));
            }

            ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
            sqlParameters.realValue = new ArrayList<>();
            //生成sql语句
            String sql = resultSql.toSql(this.sqlExecutor.getSourceType(), sqlParameters, this.sqlExecutor.createParameterCreator());
            //查询结果是否为枚举
            boolean isEnum = ropContext.getResultIsEnum();
            Iterable<?> objs = null;
            Object result = null;
            StopWatch watch = new StopWatch();
            if (isEnum) {
                ResultReaderFactory resultReaderFactory = new ResultReaderFactory();

                //获取表达式树
                AssociationTree includingTree = ropContext.getIncluding();
                //获取查询结果类型
                TypeBase resultType = ropContext.getResultModelType();
                watch.start();

                try {
                    ResultSet dr = this.sqlExecutor.executeReader(sql, sqlParameters.realValue.toArray(new DataParameter[0]));
                    watch.stop();
                    if (postExecutionCallback != null) {
                        QueryContext context = new QueryContext(ropExecutor.getQueryOp(), null);
                        context.setCommand(resultSql);
                        context.setTimeConsumed((int) watch.getTime(TimeUnit.MILLISECONDS));
                        postExecutionCallback.invoke(new QueryEventArgs(this, context));
                    }
                    objs = resultReaderFactory.create(dr, resultType, includingTree, attachObject, true, this.sqlExecutor);
                } catch (Exception ex) {
                    if (ex.getCause() instanceof SQLException) {
                        this.releaseResource();
                    }
                    watch.stop();
                    if (postExecutionCallback != null) {
                        QueryContext context = new QueryContext(ropExecutor.getQueryOp(), null);
                        context.setTimeConsumed((int) watch.getTime(TimeUnit.MILLISECONDS));
                        context.setException(ex);
                        context.setCommand(resultSql);
                        context.setHasCanceled(true);
                        postExecutionCallback.invoke(new QueryEventArgs(this, context));
                    }
                    throw ex;
                }

            } else {
                watch.start();
                try {
                    result = this.sqlExecutor.executeScalar(sql, sqlParameters.realValue.toArray(new DataParameter[0]));
                    watch.stop();
                    if (postExecutionCallback != null) {
                        QueryContext context = new QueryContext(ropExecutor.getQueryOp(), null);
                        context.setTimeConsumed((int) watch.getTime(TimeUnit.MILLISECONDS));
                        context.setCommand(resultSql);
                        postExecutionCallback.invoke(new QueryEventArgs(this, context));
                    }
                } catch (Exception ex) {
                    watch.stop();
                    if (postExecutionCallback != null) {
                        QueryContext context = new QueryContext(ropExecutor.getQueryOp(), null);
                        context.setHasCanceled(true);
                        context.setCommand(resultSql);
                        context.setTimeConsumed((int) watch.getTime(TimeUnit.MILLISECONDS));
                        context.setException(ex);
                        postExecutionCallback.invoke(new QueryEventArgs(this, context));
                    }
                    throw ex;
                }
            }

            if (objs == null)
                return result;
            else
                return objs;
        }

        return null;
    }

    /**
     * 向受事务基础结构支持的事务登记
     */
    @Override
    public void enlistTransaction() {
        this.sqlExecutor.enlistTransaction();
        this.transactionBegun = true;
    }

    /**
     * 开始本地事务
     */
    @Override
    public void beginTransaction() {
        this.beginTransaction(EIsolationLevel.TRANSACTION_READ_COMMITTED);
    }

    /**
     * 开始本地事务
     *
     * @param isolationLevel 事务隔离级别
     */
    @Override
    public void beginTransaction(EIsolationLevel isolationLevel) {
        this.sqlExecutor.beginTransaction(isolationLevel);
        this.transactionBegun = true;
    }

    /**
     * 提交本地事务
     */
    @Override
    public void commitTransaction() {
        this.sqlExecutor.commitTransaction();
        this.transactionBegun = false;
    }

    /**
     * 回滚本地事务
     */
    @Override
    public void rollbackTransaction() {
        this.sqlExecutor.rollbackTransaction();
        this.transactionBegun = false;
    }

    /**
     * 根据过滤表达式生成源和条件
     *
     * @param filterExpression 过滤表达式
     * @param model            对象数据模型
     * @param source           解析出的对象源
     * @param finalCriteria    解析出的条件
     */
    private void getSourceAndCriteria(LambdaExpression filterExpression, ObjectDataModel model, ObjectReferencePack<ISource> source, ObjectReferencePack<ICriteria> finalCriteria, Class<?> clazz) {

        List<ParameterExpression> parameterExpressionList = Arrays.stream(filterExpression.getParameters()).filter(ParameterExpression::getIsHost).collect(Collectors.toList());
        if (parameterExpressionList.size() > 0)
            clazz = parameterExpressionList.get(0).getType();
        //构造当前类型的Rop查询
        RopContext context = new RopContext(clazz, model, this.sqlExecutor.getSourceType(), null, null);
        //处理条件
        SubTreeEvaluator tree = new SubTreeEvaluator(filterExpression);
        CriteriaExpressionParser criteriaParser = new CriteriaExpressionParser(model, tree, this.sqlExecutor.getSourceType(), null);
        ICriteria criteria = criteriaParser.parse(filterExpression);
        //用Where执行器解析
        WhereExecutor whereExecutor = new WhereExecutor(filterExpression, criteria, null);
        whereExecutor.execute(context);

        source.realValue = context.getResultSql().getSource();
        finalCriteria.realValue = context.getResultSql().getCriteria();
    }
}
