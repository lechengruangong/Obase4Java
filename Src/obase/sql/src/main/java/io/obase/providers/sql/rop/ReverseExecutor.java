/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：反序运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 17:31:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;

/**
 * 反序运算执行器
 */
public class ReverseExecutor extends RopExecutor {

    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    public ReverseExecutor(QueryOp queryOp, OpExecutorWithContext<RopContext> next) {
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
        //设置反序
        ropContext.getResultSql().reverse();

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
