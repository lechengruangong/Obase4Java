/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：运算执行器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:49:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 为查询运算执行器提供模板化实现
 * 按执行方式对运算进行分类，目前已知的有两种。一种为称对象运算，即对内存中的对象集执行操作；一种称为关系运算，即对关系数据库中的关系实例执行操作。
 * 根据运算方式不同，可以将运算执行器分为对象运算执行器和关系运算执行器。相应地，运算管道也可以分为对象运算管道和关系运算管道。
 * 本类为运算执行器定义了基础实现，根据具体的运算方式可以定义具体的运算执行器。
 *
 * @param <TContext> 运算上下文的类型
 */
public abstract class OpExecutorWithContext<TContext> extends OpExecutor {

    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    protected OpExecutorWithContext(QueryOp queryOp, OpExecutorWithContext<TContext> next) {
        super(queryOp, next);
    }

    /**
     * 执行运算
     *
     * @param context 运算上下文
     */
    public abstract void execute(TContext context);
}
