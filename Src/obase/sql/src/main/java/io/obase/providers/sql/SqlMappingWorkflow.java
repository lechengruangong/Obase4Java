/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于SQL源的映射工作流.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:54:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.ELogicalOperator;
import io.obase.core.IMappingWorkflow;
import io.obase.core.MappingFilter;
import io.obase.core.mapping.pipeline.PostExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.PreExecuteCommandEventArgs;
import io.obase.core.odm.*;
import io.obase.core.saving.NothingUpdatedException;
import io.obase.providers.sql.sqlobject.*;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 特定于SQL源的映射工作流
 */
public class SqlMappingWorkflow implements IMappingWorkflow {

    /**
     * 寄存器（寄存代表映射筛选器片段）
     */
    private final List<ICriteria> segments = new ArrayList<>();

    /**
     * SQL语句执行器
     */
    private final ISqlExecutor sqlExecutor;

    /**
     * 用于级联删除的SQL语句
     */
    private Stack<ChangeSql> cascadedSqls = new Stack<>();

    /**
     * 用于持久化工作流的SQL语句
     */
    private ChangeSql changeSql;

    /**
     * 是否已设置修改类型
     */
    private boolean hasSetChangeType;

    /**
     * 创建SqlMappingWorkflow实例
     *
     * @param sqlExecutor Sql执行器
     */
    public SqlMappingWorkflow(ISqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        this.changeSql = new ChangeSql();
    }

    /**
     * 开始跟踪修改
     */
    @Override
    public void begin() {
        this.cascadedSqls.clear();
        this.changeSql = new ChangeSql();
    }

    /**
     * 接受本次工作流的存储源名称（如数据库表名）
     *
     * @param targetSource 存储源名称
     * @return 工作流
     */
    @Override
    public IMappingWorkflow setSource(String targetSource) {
        this.changeSql.setSource(new SimpleSource(targetSource));
        return this;
    }

    /**
     * 指示本次工作流将向存储源插入新对象
     *
     * @return 工作流
     */
    @Override
    public IMappingWorkflow forInserting() {
        this.changeSql.setChangeType(EChangeType.Insert);
        this.changeSql.setSqlType(ESqlType.Insert);
        this.hasSetChangeType = true;
        return this;
    }

    /**
     * 指示本次工作流将修改存储源中已有的对象
     *
     * @return 工作流
     */
    @Override
    public IMappingWorkflow forUpdating() {
        this.changeSql.setChangeType(EChangeType.Update);
        this.changeSql.setSqlType(ESqlType.Update);
        this.hasSetChangeType = true;
        return this;
    }

    /**
     * 指示本次工作流将删除存储源中的对象
     *
     * @return 工作流
     */
    @Override
    public IMappingWorkflow forDeleting() {
        this.changeSql.setChangeType(EChangeType.Delete);
        this.changeSql.setSqlType(ESqlType.Delete);
        this.hasSetChangeType = true;
        return this;
    }

    /**
     * 设置指定域（如数据库表的字段）的值
     *
     * @param field 字段
     * @param value 值
     * @return 工作流
     */
    @Override
    public IMappingWorkflow setField(String field, Object value) {
        if (this.changeSql != null)
            this.changeSql.overwriteField(field, value);
        return this;
    }

    /**
     * 对指定域（如数据库表的字段）的值施加一个增量
     *
     * @param field     字段
     * @param increment 值
     * @return 工作流
     */
    @Override
    public IMappingWorkflow increaseField(String field, Object increment) {
        //构造语句
        Field fieldExp = new Field(field);
        FieldExpression exp = Expression.field(fieldExp);
        ConstantExpression incrementExp = Expression.constant(increment, Long.class);
        ArithmeticExpression arithmetic = Expression.add(exp, incrementExp);
        if (this.changeSql != null)
            this.changeSql.overwriteField(field, arithmetic);
        return this;
    }

    /**
     * 指示本次工作流应当忽略指定域（如数据库表的字段），如果已跟踪到了该域的修改，应当将其排除
     *
     * @param field 字段
     * @return 工作流
     */
    @Override
    public IMappingWorkflow ignoreField(String field) {
        if (this.changeSql != null)
            this.changeSql.removeFieldSetter(field);
        return this;
    }

    /**
     * 为当前工作流新增一个映射筛选器，该筛选器与已存在的筛选器进行逻辑“与”运算。
     *
     * @return 新增的映射筛选器
     */
    @Override
    public MappingFilter and() {
        return new MappingFilter(this, ELogicalOperator.And, this::filterReady, this::segmentReady);
    }

    /**
     * 为当前工作流新增一个映射筛选器，该筛选器与已存在的筛选器进行逻辑“或”运算
     *
     * @return 新增的映射筛选器
     */
    @Override
    public MappingFilter or() {
        return new MappingFilter(this, ELogicalOperator.Or, this::filterReady, this::segmentReady);
    }

    /**
     * 级联删除，即从基点类型开始沿关联关系递归删除。实施者制定具体的级联规则
     *
     * @param initType 基点类型
     */
    @Override
    public void deleteCascade(ObjectType initType) {
        Stack<ChangeSql> result = new Stack<>();
        SimpleSource source = new SimpleSource(initType.getTargetTable());

        ObjectReferencePack<List<ChangeSql>> sqls = new ObjectReferencePack<>();
        sqls.realValue = new ArrayList<>();
        if (initType instanceof EntityType) {
            EntityType entityType = (EntityType) initType;
            this.joinAssociation(sqls, source, source, entityType, "", 1);
        }

        if (initType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) initType;
            this.joinAssociationEnd(sqls, source, source, associationType, "", "", 1);
        }

        for (ChangeSql sql : sqls.realValue) {

            if (this.sqlExecutor.getSourceType() == EDataSource.Sqlite || this.sqlExecutor.getSourceType() == EDataSource.PostgreSql) {
                if (sql.getCriteria() instanceof InSelectCriteria) {
                    InSelectCriteria selectCriteria = (InSelectCriteria) sql.getCriteria();
                    selectCriteria.getValueSetSql().setCriteria(this.changeSql.getCriteria());
                } else {
                    sql.setCriteria(this.changeSql.getCriteria());
                }
            } else {
                sql.setCriteria(this.changeSql.getCriteria());
            }
            result.push(sql);
        }

        this.cascadedSqls = result;
    }

    /**
     * 提交工作流
     *
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     */
    @Override
    public void commit(ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        //有级联删除
        if (this.cascadedSqls.size() > 0) {
            for (ChangeSql changeSql : this.cascadedSqls) {
                if (preExecutionCallback != null) {
                    preExecutionCallback.invoke(new PreExecuteCommandEventArgs(changeSql, this));
                }

                ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
                sqlParameters.realValue = new ArrayList<>();
                String sqlStr = changeSql.toSql(this.sqlExecutor.getSourceType(), sqlParameters, this.sqlExecutor.createParameterCreator());
                int affectCount;
                StopWatch watch = new StopWatch();
                watch.start();

                try {
                    affectCount = this.sqlExecutor.execute(sqlStr, sqlParameters.realValue.toArray(new DataParameter[0]));
                    watch.stop();
                    if (postExecutionCallback != null)
                        postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), affectCount));
                } catch (NothingUpdatedException ex) {
                    watch.stop();
                    affectCount = 0;
                    if (postExecutionCallback != null)
                        postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), affectCount));
                } catch (Exception ex) {
                    watch.stop();
                    if (postExecutionCallback != null)
                        postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), ex));
                    throw ex;
                }
            }
        }

        if (this.changeSql != null && this.hasSetChangeType) {

            ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
            sqlParameters.realValue = new ArrayList<>();

            //触发事件
            if (preExecutionCallback != null) {
                preExecutionCallback.invoke(new PreExecuteCommandEventArgs(this.changeSql, this));
            }

            //转为Sql字符串
            String sqlStr = this.changeSql.toSql(this.sqlExecutor.getSourceType(), sqlParameters, this.sqlExecutor.createParameterCreator());

            //执行Sql
            StopWatch watch = new StopWatch();

            try {
                watch.start();
                //执行sql返回自增值
                this.sqlExecutor.execute(sqlStr, sqlParameters.realValue.toArray(new DataParameter[0]));
                watch.stop();
                if (postExecutionCallback != null)
                    postExecutionCallback.invoke(
                            new PostExecuteCommandEventArgs(this, this.changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), 1));
            } catch (Exception ex) {
                watch.stop();
                if (postExecutionCallback != null)
                    postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, this.changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), ex));
                if (this.cascadedSqls != null && this.cascadedSqls.size() > 0 && this.sqlExecutor.getSourceType() == EDataSource.Sqlite &&
                        ex instanceof NothingUpdatedException) {
                    //Sqlite会将级联删除处理为子查询 导致常规删除语句未能删除 此种情况的NothingUpdatedException应忽略
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * 提交工作流
     *
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @param identity              返回存储服务为新对象生成的标识
     */
    @Override
    public void commit(ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback, ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback, ObjectReferencePack<Object> identity) {
        if (this.changeSql != null) {
            //触发事件
            if (preExecutionCallback != null) {
                preExecutionCallback.invoke(new PreExecuteCommandEventArgs(this.changeSql, this));
            }

            ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
            sqlParameters.realValue = new ArrayList<>();
            //转为Sql字符串
            String sqlStr = this.changeSql.toSql(this.sqlExecutor.getSourceType(), sqlParameters, this.sqlExecutor.createParameterCreator());

            //自增获取
            String getNewIdentityStr = this.sqlExecutor.getSourceType() == EDataSource.SqlServer || this.sqlExecutor.getSourceType() == EDataSource.Sqlite
                    ? ";select last_insert_rowid();"
                    : ";select @@identity;";

            //执行Sql
            StopWatch watch = new StopWatch();

            try {
                watch.start();
                //执行sql返回自增值
                identity.realValue = this.sqlExecutor.executeScalar(sqlStr + getNewIdentityStr, sqlParameters.realValue.toArray(new DataParameter[0]));
                watch.stop();
                if (postExecutionCallback != null)
                    postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, this.changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), 1));
            } catch (Exception ex) {
                watch.stop();
                if (postExecutionCallback != null)
                    postExecutionCallback.invoke(new PostExecuteCommandEventArgs(this, this.changeSql, (int) watch.getTime(TimeUnit.MILLISECONDS), ex));
                throw ex;
            }
        } else {
            identity.realValue = null;
        }
    }

    /**
     * 映射筛选器制作完成时回调
     *
     * @param operator 操作符
     */
    private void filterReady(ELogicalOperator operator) {
        ICriteria criteria;
        if (this.segments.size() == 0) return;
        if (this.segments.size() == 1) {
            criteria = this.segments.get(0);
        } else {
            criteria = new ComplexCriteria(this.segments.get(0), this.segments.get(1));
            for (int i = 2; i < this.segments.size(); i++)
                criteria = criteria.and(this.segments.get(i));
        }
        this.segments.clear();

        if (this.changeSql.getCriteria() == null) {
            this.changeSql.setCriteria(criteria);
            return;
        }

        switch (operator) {
            case And:
                this.changeSql.setCriteria(this.changeSql.getCriteria().and(criteria));
                break;
            case Or:
                this.changeSql.setCriteria(this.changeSql.getCriteria().or(criteria));
                break;
        }
    }

    /**
     * 映射筛选器片段制作完成时回调
     *
     * @param field 字段
     * @param val   值
     */
    private void segmentReady(String field, Object val) {
        String sourceName = ((SimpleSource) this.changeSql.getSource()).getName();

        if (val == null) {
            this.segments.add(new StringCriteria(sourceName, field, ERelationOperator.Equal, null));
        } else if (val instanceof String) {
            String str = (String) val;
            this.segments.add(new StringCriteria(sourceName, field, ERelationOperator.Equal, str));
        } else if (val instanceof Character) {
            Character character = (Character) val;
            this.segments.add(new CharCriteria(sourceName, field, ERelationOperator.Equal, character));
        } else if (val instanceof Date) {
            Date date = (Date) val;
            this.segments.add(new DateTimeCriteria(sourceName, field, ERelationOperator.Equal, date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        } else {
            try {
                Constructor<?> constructor = NumericCriteria.class.getConstructor(String.class, String.class, ERelationOperator.class, Object.class);
                ICriteria n = (ICriteria) constructor.newInstance(sourceName, field, ERelationOperator.Equal, val);
                this.segments.add(n);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                     NoSuchMethodException e) {
                throw new IllegalArgumentException("无法创建数字条件,请参照内部异常.", e);
            }

        }
    }

    /**
     * 遍历指定实体型的关联引用，连接其映射表，同时返回Delete-SQL集合
     *
     * @param deletionSql  Delete-SQL集合
     * @param leftSource   Join运算的左操作数
     * @param entitySource 当左操作数为连接源时，指定具体参与连接运算的简单源；当左操作数为简单源时，为左操作数自身
     * @param entityType   要与其关联引用的映射源进行连接的实体型
     * @param aliasRoot    别名根，用于与关联引用名称串联生成右操作数（即关联引用的映射表）的别名。默认值为空字符串
     * @param currentLevel 连接层级。默认值为1
     */
    private void joinAssociation(ObjectReferencePack<List<ChangeSql>> deletionSql, ISource leftSource, ISource entitySource,
                                 EntityType entityType, String aliasRoot, int currentLevel) {
        deletionSql.realValue = new ArrayList<>();
        if (currentLevel <= 3) {
            for (AssociationReference re : entityType.getAssociationReferences()) {
                ISource newSource = leftSource;
                String alias = aliasRoot + "_" + re.getName();
                ISource assoSource = entitySource;
                //如果执行器不为Sqlite 此处尝试处理为InnerJoin
                if (this.sqlExecutor.getSourceType() != EDataSource.Sqlite && this.sqlExecutor.getSourceType() != EDataSource.PostgreSql) {
                    if (!re.getLeftAsAssociationTable()) {
                        assoSource = new SimpleSource(re.getAssociationType().getTargetTable(), alias);
                        AssociationEnd end = re.getAssociationType().getAssociationEnd(re.getLeftEnd());
                        ICriteria c = this.generateJoinCriteria(assoSource, entitySource, end);
                        newSource = newSource.innerJoin(assoSource, c);
                        if (re.getAssociationType().getIndependent()) {
                            ChangeSql sql = new ChangeSql(newSource, EChangeType.Delete);
                            sql.setTargetSource((SimpleSource) assoSource);
                            deletionSql.realValue.add(sql);
                        }
                    }
                }

                ObjectReferencePack<List<ChangeSql>> sqls = new ObjectReferencePack<>();
                sqls.realValue = new ArrayList<>();
                this.joinAssociationEnd(sqls, newSource, assoSource, re.getAssociationType(), re.getLeftEnd(), alias,
                        currentLevel + 1);
                deletionSql.realValue.addAll(sqls.realValue);
            }
        }
    }

    /**
     * 遍历指定关联型的端，连接其映射表，同时返回Delete-SQL集合
     *
     * @param deletionSql  Delete-SQL集合
     * @param leftSource   Join运算的左操作数
     * @param assocSource  当左操作数为连接源时，指定具体参与连接运算的简单源；当左操作数为简单源时，为左操作数自身
     * @param assoType     要与其关联端的映射源进行连接的关联型
     * @param excludedEnd  要排除的关联端。默认值为空字符串，表示不排除任何关联端
     * @param aliasRoot    别名根，用于与关联引用名称串联生成右操作数（即关联引用的映射表）的别名。默认值为空字符串
     * @param currentLevel 连接层级。默认值为1
     */
    private void joinAssociationEnd(ObjectReferencePack<List<ChangeSql>> deletionSql, ISource leftSource, ISource assocSource,
                                    AssociationType assoType, String excludedEnd, String aliasRoot, int currentLevel) {
        deletionSql.realValue = new ArrayList<>();
        List<AssociationEnd> ends = assoType.getAssociationEnds();
        ends.sort((o1, o2) -> {
            if (o1.isCompanionEnd() ^ o2.isCompanionEnd()) {
                return o1.isCompanionEnd() ? -1 : 1;
            } else {
                return 0;
            }
        });

        for (AssociationEnd end : ends) {
            if (!end.getName().equalsIgnoreCase(excludedEnd)) {
                //为聚合的端处理级联删除
                if (end.getIsAggregated()) {
                    ISource newSource = leftSource;
                    String alias = aliasRoot + "_" + end.getName();
                    ISource endSource = assocSource;

                    //非伴随端 直接连接
                    if (!assoType.isCompanionEnd(end)) {
                        endSource = new SimpleSource(end.getEntityType().getTargetTable(), alias);
                        ICriteria c = this.generateJoinCriteria(assocSource, endSource, end);
                        newSource = newSource.innerJoin(endSource, c);
                    }
                    //非Sqlite 处理为InnerJoin
                    if (this.sqlExecutor.getSourceType() == EDataSource.MySql || this.sqlExecutor.getSourceType() == EDataSource.Oracle || this.sqlExecutor.getSourceType() == EDataSource.SqlServer) {
                        //创建删除Sql语句
                        ChangeSql sql = new ChangeSql(newSource, EChangeType.Delete);
                        sql.setTargetSource((SimpleSource) endSource);
                        //加入级联删除
                        deletionSql.realValue.add(sql);
                    } else {
                        assocSource = new SimpleSource(end.getEntityType().getTargetTable(), end.getEntityType().getTargetTable());
                        //处理为子查询
                        List<Expression> fieldExps = new ArrayList<>();
                        //处理键属性映射
                        for (AssociationEndMapping mapping : end.getMappings()) {
                            Field field = new Field((MonomerSource) assocSource, mapping.getTargetField());
                            FieldExpression fieldExp = new FieldExpression(field);
                            fieldExps.add(fieldExp);
                        }
                        //连接所有键属性
                        FunctionExpression funcExp = new FunctionExpression("concat", fieldExps.toArray(new Expression[0]));
                        ExpressionColumn column = new ExpressionColumn();
                        column.setExpression(funcExp);
                        SelectionSet selectionSet = new SelectionSet(column);

                        //连接其他的端
                        List<AssociationEnd> otherEnds = ends.stream().filter(p -> !p.equals(end)).collect(Collectors.toList());
                        for (AssociationEnd other : otherEnds) {
                            //构造joinedSource
                            SimpleSource otherEndSource = new SimpleSource(other.getEntityType().getTargetTable(), other.getEntityType().getTargetTable());
                            SimpleSource currentAssSource = new SimpleSource(end.getEntityType().getTargetTable(), end.getEntityType().getTargetTable());
                            ICriteria c = this.generateJoinCriteria(currentAssSource, otherEndSource, other);
                            //左连接
                            newSource = otherEndSource.leftJoin(currentAssSource, c);
                        }

                        //创建子查询
                        QuerySql querySql = new QuerySql(newSource);
                        querySql.setSelectionSet(selectionSet);

                        //创建删除Sql语句
                        InSelectCriteria criteria = new InSelectCriteria(funcExp, querySql);
                        ChangeSql changeSql = new ChangeSql(assocSource, EChangeType.Delete, criteria);
                        changeSql.setTargetSource((SimpleSource) endSource);
                        //加入级联删除
                        deletionSql.realValue.add(changeSql);
                    }

                    ObjectReferencePack<List<ChangeSql>> sqls = new ObjectReferencePack<>();
                    sqls.realValue = new ArrayList<>();
                    this.joinAssociation(sqls, newSource, endSource, end.getEntityType(), alias,
                            currentLevel + 1);
                    deletionSql.realValue.addAll(sqls.realValue);
                }
            }
        }
    }

    /**
     * 生成连接条件
     *
     * @param assocSource 关联型的映射表
     * @param endSource   关联端的映射表
     * @param end         要连接的关联端
     * @return 连接条件
     */
    private ICriteria generateJoinCriteria(ISource assocSource, ISource endSource, AssociationEnd end) {
        ICriteria c = null;
        for (AssociationEndMapping mapping : end.getMappings()) {
            Attribute endAttr = end.getEntityType().getAttribute(mapping.getKeyAttribute());
            FieldCriteria segment = new FieldCriteria(assocSource, mapping.getTargetField(), ERelationOperator.Equal, endSource,
                    endAttr.getTargetField());
            c = c == null ? segment : c.and(segment);
        }

        return c;
    }
}
