/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：附加视图及其附加节点和附加引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 16:03:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.core.odm.objectSys.AssociationTreeNode;

/**
 * 表示对视图实施极限分解后得到的附加视图及其附加节点和附加引用
 */
public class TypeViewAttachingItem {

    /**
     * 附加节点。附加节点是基础视图源扩展树上的一个节点，在分解前的扩展树中，它是附加视图源扩展树根的父节点。
     */
    private final AssociationTreeNode attachingNode;

    /**
     * 附加引用。附加视图是基础视图上的一个引用，它锚定于附加节点，绑定到附加视图源扩展树根节点在分解前的扩展树中所代表的引用元素。
     */
    private final ViewReference attachingReference;

    /**
     * 附加视图。
     */
    private final TypeView attachingView;

    /**
     * 创建TypeViewAttachingItem实例
     *
     * @param attachingView 附加视图。
     * @param attachingNode 附加节点。
     * @param attachingRef  附加引用。
     */
    public TypeViewAttachingItem(TypeView attachingView, AssociationTreeNode attachingNode,
                                 ViewReference attachingRef) {
        this.attachingView = attachingView;
        this.attachingNode = attachingNode;
        this.attachingReference = attachingRef;
    }

    /**
     * 获取附加节点
     *
     * @return 附加节点
     */
    public AssociationTreeNode getAttachingNode() {
        return this.attachingNode;
    }

    /**
     * 获取附加视图
     *
     * @return 附加视图
     */
    public TypeView getAttachingView() {
        return this.attachingView;
    }

    /**
     * 获取附加引用
     *
     * @return 附加引用
     */
    public ViewReference getAttachingReference() {
        return this.attachingReference;
    }
}
