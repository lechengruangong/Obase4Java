/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询链访问者.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:39:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 定义在遍历查询链过程中访问查询运算的规范
 *
 * @param <TArg>    访问操作参数的类型
 * @param <TResult> 访问操作返回值类型
 */
public abstract class QueryOpVisitorWithArgs<TArg, TResult> extends QueryOpVisitorWithResult<TResult> {

    /**
     * 获取访问操作参数
     *
     * @return 访问操作参数
     */
    public abstract TArg getArgument();

    /**
     * 设置访问操作参数
     *
     * @param arg 访问操作参数
     */
    public abstract void setArgument(TArg arg);
}
