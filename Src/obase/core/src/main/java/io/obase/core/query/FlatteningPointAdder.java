/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：平展点添加器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:44:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.IAssociationTreeUpwardVisitor;

import java.util.Arrays;

/**
 * 作为一个关联树访问者，为退化路径极限分解得到的基础路径和附加路径添加平展点
 */
public class FlatteningPointAdder implements IAssociationTreeUpwardVisitor {

    /**
     * 附加路径
     */
    private final AtrophyPath attachingPath;

    /**
     * 基础路径
     */
    private final AtrophyPath basePath;

    /**
     * 被分解的退化路径
     */
    private final AtrophyPath decomposedPath;

    /**
     * 创建FlatteningPointAdder实例
     *
     * @param decomposedPath 被分解的退化路径
     * @param basePath       基础路径
     * @param attachingPath  附加路径
     */
    public FlatteningPointAdder(AtrophyPath decomposedPath, AtrophyPath basePath, AtrophyPath attachingPath) {

        this.decomposedPath = decomposedPath;
        this.basePath = basePath;
        this.attachingPath = attachingPath;
    }

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
        AssociationTreeNode currentNode;
        AtrophyPath targetPath;

        if (childState != null) {
            Object[] tempChildState = (Object[]) childState;

            currentNode = ((AssociationTreeNode) tempChildState[0]).getParent();
            if (currentNode == null) currentNode = this.basePath.getAssociationPath();
            targetPath = (AtrophyPath) tempChildState[1];
        } else {
            currentNode = this.attachingPath.getAssociationPath();
            targetPath = this.attachingPath;
        }

        Object[] value = new Object[2];
        value[0] = currentNode;
        value[1] = targetPath;
        outChildState.realValue = value;
        outPreVisitState.realValue = null;
        //获取被分解路径的平展点
        AssociationTreeNode[] points = this.decomposedPath.getFlatteningPoints();
        //添加平展点
        if (Arrays.asList(points).contains(subTree.getNode())) targetPath.addFlatteningPoint(currentNode);
        return false;
    }

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object childState, Object preVisitState) {

    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {

    }
}
