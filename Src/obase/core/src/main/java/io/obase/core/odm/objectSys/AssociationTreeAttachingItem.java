/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：附加树及其附加节点和附加引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 15:02:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.ReferenceElement;

/**
 * 表示附加树及其附加节点和附加引用
 */
public class AssociationTreeAttachingItem {

    /**
     * 附加节点。附加节点是基础树上的一个节点，在分解前的树中，它是附加树根的父节点。
     */
    private final AssociationTreeNode attachingNode;

    /**
     * 附加引用。附加引用是附加节点代表类型的一个引用元素，在分解前的树中，它是附加树根节点代表的引用元素。
     */
    private final ReferenceElement attachingReference;

    /**
     * 附加树
     */
    private final AssociationTree attachingTree;

    /**
     * 创建AssociationTreeAttachingItem实例
     *
     * @param attachingTree 附加树
     * @param attachingNode 附加节点
     * @param attachingRef  附加引用
     */
    public AssociationTreeAttachingItem(AssociationTree attachingTree, AssociationTreeNode attachingNode,
                                        ReferenceElement attachingRef) {
        this.attachingTree = attachingTree;
        this.attachingNode = attachingNode;
        this.attachingReference = attachingRef;
    }

    /**
     * 获取附加节点
     *
     * @return 获取附加节点
     */
    public AssociationTreeNode getAttachingNode() {
        return this.attachingNode;
    }

    /**
     * 获取附加引用
     *
     * @return 获取附加引用
     */
    public ReferenceElement getAttachingReference() {
        return this.attachingReference;
    }

    /**
     * 获取附加树
     *
     * @return 获取附加树
     */
    public AssociationTree getAttachingTree() {
        return this.attachingTree;
    }
}
