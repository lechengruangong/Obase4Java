/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向下遍历关联树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 15:05:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 定义在向下遍历关联树过程中对子树实施访问的规范，该遍历操作会返回一个结果并以输出参数返回另一结果
 *
 * @param <TResult> 遍历操作返回结果的类型
 * @param <TOut>    输出参数的类型
 */
public interface IParameterizedAssociationTreeDownwardVisitor<TResult, TOut> extends IAssociationTreeDownwardVisitorWithResult<TResult> {

    /**
     * 获取输出参数的值
     *
     * @return 获取输出参数的值
     */
    TOut getOutArgument();
}
