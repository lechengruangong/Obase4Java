/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树生长器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 17:38:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

/**
 * 关联树生长器
 * 执行关联树生长的访问者
 */
public class AssociationTreeGrower implements IParameterizedAssociationTreeDownwardVisitorWithArg<AssociationTree> {

    /**
     * 生成的关联树
     */
    private AssociationTree growingTree;

    /**
     * 前置访问，即在访问子级前执行操作。
     *
     * @param subTree          被访问的关联树子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        if (subTree.getParent() == null) {
            outParentState.realValue = this.growingTree.getNode();
            outPreVisitState.realValue = null;
            return true;
        }

        if (parentState instanceof AssociationTree) {
            AssociationTree associationTree = (AssociationTree) parentState;

            parentState = associationTree.getNode();
        }

        if (parentState instanceof AssociationTreeNode) {
            AssociationTreeNode associationNode = (AssociationTreeNode) parentState;
            ObjectTypeNode correspondingNode = associationNode.getChild(subTree.getElementName());
            if (correspondingNode != null) {
                outParentState.realValue = correspondingNode;
                outPreVisitState.realValue = null;
                return true;
            }

            //与自己相同的类型 不添加
            if (subTree.getNode() instanceof ObjectTypeNode) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) subTree.getNode();
                if (associationNode.getRepresentedType() != objectTypeNode.getRepresentedType()) {
                    ObjectTypeNode result = associationNode.addChild(objectTypeNode, null);
                    //如果不是因为同名 而是因为同类型而添加的
                    if (!result.getElementName().equals(subTree.getElementName())) {
                        outParentState.realValue = result;
                        outPreVisitState.realValue = null;
                        return true;
                    }
                }
            }

            outParentState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        }

        outParentState.realValue = null;
        outPreVisitState.realValue = null;
        return false;
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的关联树子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object parentState, Object preVisitState) {
        //Nothing to do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to do
    }

    /**
     * 为即将开始的遍历操作设置参数
     *
     * @param argument 参数值
     */
    @Override
    public void setArgument(AssociationTree argument) {
        this.growingTree = argument;
    }
}
