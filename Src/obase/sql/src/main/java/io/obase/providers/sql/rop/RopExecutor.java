/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关系运算执行器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 11:54:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;

/**
 * 为关系运算执行器提供基础实现
 */
public abstract class RopExecutor extends OpExecutorWithContext<RopContext> {
    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    protected RopExecutor(QueryOp queryOp, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
    }
}
