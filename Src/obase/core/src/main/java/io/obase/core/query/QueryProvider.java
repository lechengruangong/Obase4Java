/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询提供程序基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:02:36
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.ObjectContext;
import io.obase.core.common.EventHandler;
import io.obase.core.expression.Expression;
import io.obase.core.mapping.pipeline.IQueryPipeline;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;

/**
 * 为查询提供程序提供基础实现
 */
public abstract class QueryProvider implements IQueryPipeline {

    /**
     * 数据模型
     */
    protected final ObjectDataModel model;
    /**
     * 上下文
     */
    protected final ObjectContext context;
    /**
     * 附加委托
     */
    protected IAttachObject attachObject;
    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     */
    private EventHandler<QueryEventArgs> iQueryPipelinePreExecuteCommand;
    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     */
    private EventHandler<QueryEventArgs> iQueryPipelinePostExecuteCommand;
    /**
     * 为BeginQuery事件附加或移除事件处理程序
     */
    private EventHandler<QueryEventArgs> beginQuery;
    /**
     * 为EndQuery事件附加或移除事件处理程序
     */
    private EventHandler<QueryEventArgs> endQuery;

    /**
     * 创建QueryProvider
     *
     * @param model 对象数据模型
     */
    protected QueryProvider(ObjectDataModel model, IAttachObject attachObject, ObjectContext context) {
        this.model = model;
        this.attachObject = attachObject;
        this.context = context;
    }

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    @Override
    public EventHandler<QueryEventArgs> getIQueryPipelinePreExecuteCommand() {

        if (this.iQueryPipelinePreExecuteCommand == null)
            this.iQueryPipelinePreExecuteCommand = new EventHandler<>();
        return this.iQueryPipelinePreExecuteCommand;
    }

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    @Override
    public EventHandler<QueryEventArgs> getIQueryPipelinePostExecuteCommand() {
        if (this.iQueryPipelinePostExecuteCommand == null)
            this.iQueryPipelinePostExecuteCommand = new EventHandler<>();
        return this.iQueryPipelinePostExecuteCommand;
    }

    /**
     * 为BeginQuery事件附加或移除事件处理程序
     *
     * @return BeginQuery事件
     */
    @Override
    public EventHandler<QueryEventArgs> getBeginQuery() {
        if (this.beginQuery == null)
            this.beginQuery = new EventHandler<>();
        return this.beginQuery;
    }

    /**
     * 为EndQuery事件附加或移除事件处理程序
     *
     * @return EndQuery事件
     */
    @Override
    public EventHandler<QueryEventArgs> getEndQuery() {
        if (this.endQuery == null)
            this.endQuery = new EventHandler<>();
        return this.endQuery;
    }

    /**
     * 获取模型
     *
     * @return 模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 获取附加对象委托
     *
     * @return 附加对象委托
     */
    public IAttachObject getAttachObject() {
        return this.attachObject;
    }

    /**
     * 执行查询
     *
     * @param query     要执行的查询
     * @param including 相对于查询源的包含树，指示在查询链中显式或隐含的包含运算外额外执行的包含运算。值为null表示不执行额外的包含运算
     * @return 执行查询的结果
     */
    public Object execute(QueryOp query, AssociationTree including) {
        QueryContext context = new QueryContext(query, null);

        if (this.beginQuery != null)
            this.beginQuery.publishEvent(new QueryEventArgs(this, context));

        Object result = this.execute(query, including, null);

        if (this.endQuery != null)
            this.endQuery.publishEvent(new QueryEventArgs(this, context));

        if (Iterable.class.isAssignableFrom(query.getResultType()) && this.model.getObjectType(query.getResultType()) != null) {
            ObjectReferencePack<Object> pack = new ObjectReferencePack<>();
            pack.realValue = result;
            this.attachObject.attachObject(pack, true);
        }

        return result;
    }

    /**
     * 执行查询
     *
     * @param query      要执行的查询。值为null表示取出查询源中的所有对象
     * @param including  相对于查询源的包含树，指示在查询链中显式或隐含的包含运算外额外执行的包含运算。值为null表示不执行额外的包含运算
     * @param expression 查询表达式。调用方须自行确保该表达式与query参数指定的查询等效。
     * @return 执行查询的结果
     */
    private Object execute(QueryOp query, AssociationTree including, Expression expression) {
        QueryContext context = new QueryContext(query, expression);
        this.execute(including, context, query);

        return context.getResult();
    }

    /**
     * 执行查询
     *
     * @param including 包含树
     * @param context   查询上下文
     * @param query     查询运算
     */
    protected abstract void execute(AssociationTree including, QueryContext context, QueryOp query);

    /**
     * 由查询执行器触发 将执行Sql事件前抛到上层
     *
     * @param args 查询事件数据
     */
    public void OnPreExecuteSql(QueryEventArgs args) {
        if (this.iQueryPipelinePreExecuteCommand != null)
            this.iQueryPipelinePreExecuteCommand.publishEvent(args);
    }

    /**
     * 由查询执行器触发 将执行Sql事件后抛到上层
     *
     * @param args 查询事件数据
     */
    public void OnPostExecuteSql(QueryEventArgs args) {
        if (this.iQueryPipelinePostExecuteCommand != null)
            this.iQueryPipelinePostExecuteCommand.publishEvent(args);
    }

    /**
     * 根据表达式和查询链执行运算
     *
     * @param expression 表达式
     * @param query      查询运算表示的查询链
     * @param <TResult>  结果类型或结果元素类型
     * @return 运算结果
     */
    public <TResult> TResult execute(Expression expression, QueryOp query, Class<?> sourceType) {

        //是空的 没解析 为空查询
        if (query == null) {
            query = QueryOp.every(sourceType, this.model, null);
        }

        //如果全是Include操作 则需要拼接一个NonQuery
        int includeCount = 0;
        int allopCount = 0;
        QueryOp currentQuery = query;
        while (currentQuery != null) {
            if (currentQuery.getName() == EQueryOpName.Include) {
                includeCount++;
            }
            allopCount++;
            currentQuery = currentQuery.getNext();
        }
        if (includeCount == allopCount) {
            query = QueryOp.every(sourceType, this.model, query);
        }

        QueryContext context = new QueryContext(query, expression);

        if (this.beginQuery != null)
            this.beginQuery.publishEvent(new QueryEventArgs(this, context));

        Object result;
        if (context.getHasCanceled()) {
            result = context.getResult();
        } else {
            query = context.getQuery();
            result = this.execute(query, null, expression);
        }

        if (this.endQuery != null)
            this.endQuery.publishEvent(new QueryEventArgs(this, context));

        return (TResult) result;
    }
}
