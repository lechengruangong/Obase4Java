/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性树的节点枚举器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 14:54:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性树节点枚举器，用于将树节点依次写入一个序列。
 * 使用者注意：写入顺序依赖于遍历算法，不承诺在后续版本维持此顺序。
 */
public class AttributeTreeNodeEnumerator implements IAttributeTreeDownwardVisitorWithResult<Iterable<AttributeTreeNode>> {

    /**
     * 指示是否忽略代表复杂属性节点
     */
    private boolean ignoreComplex = true;

    /**
     * 遍历属性树的结果
     */
    private Iterable<AttributeTreeNode> result;

    /**
     * 获取一个值，该值指示是否忽略代表复杂属性节点
     *
     * @return 是否忽略代表复杂属性节点
     */
    public boolean getIgnoreComplex() {
        return this.ignoreComplex;
    }

    /**
     * 设置是否忽略代表复杂属性节点
     *
     * @param ignoreComplex 是否忽略代表复杂属性节点
     */
    public void setIgnoreComplex(boolean ignoreComplex) {
        this.ignoreComplex = ignoreComplex;
    }

    /**
     * 前置访问，即在访问子级前执行操作
     *
     * @param subTree          被访问的子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     */
    @Override
    public void preVisit(AttributeTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        //当前节点
        AttributeTreeNode node = subTree.getNode();
        //flag
        outParentState.realValue = null;
        outPreVisitState.realValue = null;
        this.result = new ArrayList<>();
        //处理结果
        if (!(node.getAttribute().getIsComplex() && this.ignoreComplex))
            ((List<AttributeTreeNode>) this.result).add(node);
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AttributeTree subTree, Object parentState, Object preVisitState) {
        //Nothing to Do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to Do
    }

    /**
     * 获取遍历属性树的结果
     *
     * @return 获取遍历属性树的结果
     */
    @Override
    public Iterable<AttributeTreeNode> getResult() {
        return this.result;
    }
}
