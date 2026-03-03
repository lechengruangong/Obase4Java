/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向上遍历关联树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 17:43:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 定义在向上遍历关联树过程中对子树实施访问的规范，该遍历操作会返回一个结果。
 *
 * @param <TArg> 遍历操作参数的类型
 */
public interface IParameterizedAssociationTreeUpwardVisitor<TArg> extends IAssociationTreeUpwardVisitor {

    /**
     * 为即将开始的遍历操作设置参数
     *
     * @param argument 参数值
     */
    void setArgument(TArg argument);
}
