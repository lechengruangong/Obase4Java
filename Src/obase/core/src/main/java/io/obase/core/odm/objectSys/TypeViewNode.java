/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代表类型视图的节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 16:59:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.typeviews.TypeView;

/**
 * 关联树中代表类型视图的节点
 */
public class TypeViewNode extends AssociationTreeNode {

    /**
     * 创建TypeViewNode实例
     *
     * @param viewType 类型视图
     */
    TypeViewNode(TypeView viewType) {
        super(viewType);
    }

    /**
     * 获取获取根节点
     *
     * @return 获取根节点
     */
    @Override
    public AssociationTreeNode getRoot() {
        return this;
    }

    /**
     * 获取当前节点的父级节点
     *
     * @return 获取当前节点的父级节点
     */
    @Override
    public AssociationTreeNode getParent() {
        return null;
    }

    /**
     * 设值当前节点的父级节点
     *
     * @param parent 设值当前节点的父级节点
     */
    @Override
    public void setParent(AssociationTreeNode parent) {

    }
}
