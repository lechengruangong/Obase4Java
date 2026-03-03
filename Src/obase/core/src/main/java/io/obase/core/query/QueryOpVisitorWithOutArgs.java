/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询链访问者.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:40:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 定义在遍历查询链过程中访问查询运算的规范，并提供基础实现
 *
 * @param <TResult> 访问操作返回值类型
 * @param <TOut>    访问操作输出参数的类型
 */
public abstract class QueryOpVisitorWithOutArgs<TResult, TOut> extends QueryOpVisitorWithResult<TResult> {

    /**
     * 访问操作的输出参数值
     */
    protected TOut outArgument;

    /**
     * 获取访问操作的输出参数值
     *
     * @return 访问操作的输出参数值
     */
    public TOut getOutArgument() {
        return this.outArgument;
    }
}
