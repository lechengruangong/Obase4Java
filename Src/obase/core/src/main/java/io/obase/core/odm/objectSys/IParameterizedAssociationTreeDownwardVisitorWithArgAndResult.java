/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向下遍历关联树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 15:03:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 定义在向下遍历关联树过程中对子树实施访问的规范，该遍历操作接收一个参数并返回一个结果。
 *
 * @param <TArg>    遍历操作参数的类型
 * @param <TResult> 遍历操作返回结果的类型
 */
public interface IParameterizedAssociationTreeDownwardVisitorWithArgAndResult<TArg, TResult>
        extends IAssociationTreeDownwardVisitorWithResult<TResult>, IParameterizedAssociationTreeDownwardVisitorWithArg<TArg> {
}
