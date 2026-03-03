/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提取运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:05:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;

/**
 * 提取运算执行器
 */
public class TakeExecutor extends RopExecutor {

    /**
     * 提取的数量
     */
    private final int count;

    /**
     * 构造TakeExecutor的新实例
     *
     * @param queryOp 查询运算
     * @param count   要提取的数量
     * @param next    运算管道中的下一个执行器
     */
    public TakeExecutor(QueryOp queryOp, int count, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.count = count;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        ropContext.getResultSql().setTakeNumber(this.count);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
