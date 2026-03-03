/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树复制器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 11:46:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

/**
 * 关联树复制器,作为一个关联树向下访问者执行复制关联树的操作
 */
public class AssociationTreeCloner implements IAssociationTreeDownwardVisitorWithResult<AssociationTreeNode> {

    /**
     * 遍历关联树的结果
     */
    private AssociationTreeNode result;

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
        AssociationTreeNode cloneAlone = subTree.getNode().cloneAlone();
        this.result = cloneAlone;
        outParentState.realValue = cloneAlone;
        outPreVisitState.realValue = null;
        return true;
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

    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {

    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 获取遍历关联树的结果
     */
    @Override
    public AssociationTreeNode getResult() {
        return this.result;
    }
}
