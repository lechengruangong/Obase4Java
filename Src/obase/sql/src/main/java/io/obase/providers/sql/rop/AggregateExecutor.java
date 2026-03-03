/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：聚合运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 14:49:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.PrimitiveType;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.EAggregationFunction;

/**
 * 聚合运算执行器
 */
public class AggregateExecutor extends RopExecutor {

    /**
     * 聚合类型
     */
    private final EAggregationFunction aggregationType;

    /**
     * 聚合结果的类型
     */
    private final Class<?> resultType;

    /**
     * 构造AggregateExecutor的新实例
     *
     * @param queryOp         查询运算
     * @param aggregationType 聚合类型
     * @param resultType      聚合结果的类型
     * @param next            运算管道中的下一个执行器
     */
    public AggregateExecutor(QueryOp queryOp, EAggregationFunction aggregationType, Class<?> resultType,
                             OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.aggregationType = aggregationType;
        this.resultType = resultType;
    }

    /**
     * 获取聚合类型
     *
     * @return 聚合类型
     */
    public EAggregationFunction getAggregationType() {
        return this.aggregationType;
    }

    /**
     * 获取聚合结果的类型
     *
     * @return 聚合结果的类型
     */
    public Class<?> getResultType() {
        return this.resultType;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        ropContext.getResultSql().setAggregation(this.aggregationType);

        ropContext.setResultType(PrimitiveType.fromType(this.resultType), false, this.next instanceof RopTerminator);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
