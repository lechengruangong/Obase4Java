/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：去重运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 16:23:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;

/**
 * 去重运算执行器
 */
public class DistinctExecutor extends RopExecutor {

    /**
     * 构造DistinctExecutor的新实例
     *
     * @param queryOp 查询运算
     * @param next    运算管道中的下一个执行器
     */
    public DistinctExecutor(QueryOp queryOp, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        if (ropContext.getResultSql().getTakeNumber() > 0)
            ropContext.acceptResult();
        ropContext.getResultSql().setDistinct(true);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
