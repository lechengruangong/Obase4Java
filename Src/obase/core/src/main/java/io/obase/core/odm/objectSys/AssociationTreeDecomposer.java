/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树极限分解器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 15:01:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.ReferenceElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 作为一个关联树向下访问者对关联树实施极限分解，访问操作以是否克隆附加树为输入参数，以基础树为返回值，以附加项集合为输出参数。
 * [定义]从关联树中移除任意一棵子树，把该子树视为另一棵关联树，这一过程则为关联树分解。
 * 移除子树后剩余部分仍然是一棵关联树，称为基础树，该子树称为附加树。
 * 附加树的根节点在原树中的父节点称为该附加树的附加节点，在原树中代表的引用元素称为该附加树的附加引用。
 * [定义]如果一个分解方案对一棵关联树实施一次或连续实施多次分解，使得基础树是同构的而且其包含尽可能多的节点，则称该分解方案为关联树的极限分解。
 * 警告
 * 如果关联树根为TypeViewNode，不会检测该视图是否为异构，直接依据其终极源（考虑视图嵌套情形）判定根节点的存储标记。
 * 警告
 * 不会事先检查关联树是否为异构的，如果关联树不是异构的，将克隆整棵树作为基础树。建议执行分解操作前先确保关联树是异构的。
 */
public class AssociationTreeDecomposer implements IParameterizedAssociationTreeDownwardVisitorWithArgOutArgAndResult<Boolean, AssociationTree,
        AssociationTreeAttachingItem[]> {


    /**
     * 实施极限分解得到的附加树（未复制）的根节点。
     */
    private final List<AssociationTreeAttachingItem> attachingItems = new ArrayList<>();

    /**
     * 关联树异构断言提供程序
     */
    private final HeterogeneityPredicationProvider provider;


    /**
     * 实施极限分解得到的基础树
     */
    private AssociationTreeNode baseTree;

    /**
     * 指示是否复制附加树，即依次克隆附加树的节点生成一棵新树。
     */
    private boolean cloningAttachingTree;

    /**
     * 构造作为一个关联树向下访问者实施极限分解
     *
     * @param provider 关联树异构断言提供程序
     */
    public AssociationTreeDecomposer(HeterogeneityPredicationProvider provider) {
        this.provider = provider;
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
        outParentState.realValue = outPreVisitState.realValue = null;
        if (parentState == null) {
            //克隆根节点。
            AssociationTreeNode cloneAlone = subTree.getNode().cloneAlone();
            this.baseTree = cloneAlone;
            this.provider.registerRoot(subTree.getRoot().getNode());//寄存根节点关注特性
            outParentState.realValue = cloneAlone;//克隆根节点。
            return true;
        }

        boolean result = this.provider.compare(subTree.getNode());
        if (!result) {
            this.newAttachingItem(subTree, (AssociationTreeNode) parentState); //创建并寄存附加项。
            return false;
        }

        outParentState.realValue = subTree.getNode().cloneAlone(); //克隆当前节点。
        ((AssociationTreeNode) parentState).addChild((ObjectTypeNode) outParentState.realValue, null); //添加为子节点。
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
     * 获取输出参数的值
     *
     * @return 获取输出参数的值
     */
    @Override
    public AssociationTreeAttachingItem[] getOutArgument() {
        return this.attachingItems.toArray(new AssociationTreeAttachingItem[0]);
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 获取遍历关联树的结果
     */
    @Override
    public AssociationTree getResult() {
        return this.baseTree.asTree();
    }

    /**
     * 为即将开始的遍历操作设置参数
     *
     * @param argument 参数值
     */
    @Override
    public void setArgument(Boolean argument) {
        this.cloningAttachingTree = argument;
    }

    /**
     * 创建并寄存附加项
     *
     * @param attachingTree 附加关联树树（未复制
     * @param attachingNode 附加节点
     */
    private void newAttachingItem(AssociationTree attachingTree, AssociationTreeNode attachingNode) {
        //获取附加引用
        ReferenceElement attachingReference = attachingNode.getRepresentedType().getReferenceElement(attachingTree.getElement().getName());
        AssociationTreeAttachingItem attachingItem;
        if (this.cloningAttachingTree) {
            AssociationTree cloneAttachingTree = attachingTree.accept(new AssociationTreeCloner()).asTree();
            //创建附加项
            attachingItem = new AssociationTreeAttachingItem(cloneAttachingTree, attachingNode, attachingReference);

        } else {
            //创建附加项
            attachingItem = new AssociationTreeAttachingItem(attachingTree, attachingNode, attachingReference);
        }
        this.attachingItems.add(attachingItem);
        //确保引用键
        attachingReference.getReferringKey(false);
        //确保参考键
        attachingReference.getReferredKey(false);
    }
}
