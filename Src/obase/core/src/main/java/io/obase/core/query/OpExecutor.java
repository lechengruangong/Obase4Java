/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：运算执行器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:16:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 为运算执行器定义了基础实现
 */
public abstract class OpExecutor {

    /**
     * 查询运算管道中的下一个执行器
     */
    protected OpExecutor next;

    /**
     * 要执行的查询运算
     */
    protected QueryOp queryOp;

    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    protected OpExecutor(QueryOp queryOp, OpExecutor next) {
        this.queryOp = queryOp;
        this.next = next;
    }

    /**
     * 获取运算管道中的下一个执行器
     *
     * @return 获取运算管道中的下一个执行器
     */
    public OpExecutor getNext() {
        return this.next;
    }

    /**
     * 要执行的查询运算
     *
     * @return 要执行的查询运算
     */
    public QueryOp getQueryOp() {
        return this.queryOp;
    }
}
