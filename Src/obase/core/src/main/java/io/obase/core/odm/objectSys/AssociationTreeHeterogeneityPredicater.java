/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树异构判断器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 12:07:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.query.StorageHeterogeneityPredicationProvider;

/**
 * 作为一个关联树向下访问者断言关联树是否为异构的。
 * [定义]如果关联树中存在任意一个节点，其映射源与根节点映射源分属不同的存储服务，则称该关联树为异构的。
 * 警告
 * 如果关联树根为TypeViewNode，不会检测该视图是否为异构，直接依据其终极源（考虑视图嵌套情形）判定根节点的存储标记。
 * 实施说明
 * 采用显式接口实现，相关方法定义为私有。
 */
public class AssociationTreeHeterogeneityPredicater implements IAssociationTreeDownwardVisitorWithResult<Boolean> {

    /**
     * 关联树异构断言提供程序
     */
    private final HeterogeneityPredicationProvider provider;

    /**
     * 指示关联树是否为异构的
     */
    private boolean heterogeneous;

    /**
     * 构造作为一个关联树向下访问者断言关联树是否为异构的
     *
     * @param provider 关联树异构断言提供程序
     */
    public AssociationTreeHeterogeneityPredicater(HeterogeneityPredicationProvider provider) {
        this.provider = provider == null ? new StorageHeterogeneityPredicationProvider() : provider;
    }

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
        outParentState.realValue = null;
        outPreVisitState.realValue = null;
        if (this.heterogeneous) return false;
        if (subTree.getIsRoot()) {
            this.provider.registerRoot(subTree.getNode());
            return true;
        }

        boolean result = this.provider.compare(subTree.getNode());
        if (result) {
            return true;
        }

        this.heterogeneous = true;
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
        //nothing to do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //nothing to do
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 获取遍历关联树的结果
     */
    @Override
    public Boolean getResult() {
        return this.heterogeneous;
    }
}
