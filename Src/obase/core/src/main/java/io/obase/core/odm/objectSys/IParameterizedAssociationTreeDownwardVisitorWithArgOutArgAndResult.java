/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向下遍历关联树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 15:03:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 定义在向下遍历关联树过程中对子树实施访问的规范，该遍历操作接收一个参数，返回一个结果并以输出参数返回另一结果。
 *
 * @param <TArg>    遍历操作参数的类型
 * @param <TResult> 遍历操作返回结果的类型
 * @param <TOut>    输出参数的类型
 */
public interface IParameterizedAssociationTreeDownwardVisitorWithArgOutArgAndResult<TArg, TResult, TOut>
        extends IAssociationTreeDownwardVisitorWithOutArg<TResult, TOut>, IParameterizedAssociationTreeDownwardVisitorWithArgAndResult<TArg, TResult> {
}

