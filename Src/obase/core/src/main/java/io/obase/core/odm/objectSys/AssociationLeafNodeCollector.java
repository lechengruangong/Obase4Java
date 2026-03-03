/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联叶子节点收集器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 16:55:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

import java.util.ArrayList;
import java.util.List;

/**
 * 关联叶子节点收集器
 */
public class AssociationLeafNodeCollector implements IAssociationTreeDownwardVisitorWithResult<AssociationTreeNode[]> {

    /**
     * 收集结果
     */
    private final List<AssociationTreeNode> result = new ArrayList<>();

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
        outParentState.realValue = outPreVisitState.realValue = null;
        if (subTree.getNode().getChildren().length > 0) return true;
        this.result.add(subTree.getNode());
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
    public AssociationTreeNode[] getResult() {
        return this.result.toArray(new AssociationTreeNode[0]);
    }
}
