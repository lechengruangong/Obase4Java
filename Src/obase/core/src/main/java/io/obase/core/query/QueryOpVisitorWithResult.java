/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询链访问者.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:40:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 定义在遍历查询链过程中访问查询运算的规范，并提供基础实现。
 *
 * @param <TResult>
 */
public abstract class QueryOpVisitorWithResult<TResult> extends QueryOpVisitor {

    /**
     * 访问操作的结果
     */
    protected TResult result;

    /**
     * 获取访问操作的结果
     *
     * @return 获取访问操作的结果
     */
    public TResult getResult() {
        return this.result;
    }
}
