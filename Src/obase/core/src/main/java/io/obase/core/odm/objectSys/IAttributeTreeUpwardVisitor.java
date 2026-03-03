/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向上遍历属性树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:26:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

/**
 * 定义在向上遍历属性树过程中对子树实施访问的规范
 */
public interface IAttributeTreeUpwardVisitor {

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    boolean preVisit(AttributeTree subTree, Object childState, ObjectReferencePack<Object> outChildState,
                     ObjectReferencePack<Object> outPreVisitState);

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    void postVisit(AttributeTree subTree, Object childState, Object preVisitState);

    /**
     * 重置访问者
     */
    void reset();
}
