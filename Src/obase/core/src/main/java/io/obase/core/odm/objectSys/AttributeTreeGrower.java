/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性树生长器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 11:36:31
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ComplexAttribute;

import java.util.List;

/**
 * 属性树生长器
 */
public class AttributeTreeGrower implements IAttributeTreeDownwardVisitor {
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
        //读取属性
        Attribute attr = subTree.getAttribute();

        if (!attr.getIsComplex()) {
            outParentState.realValue = null;
            outPreVisitState.realValue = null;
        }

        if (attr instanceof ComplexAttribute) {
            List<Attribute> attrs = ((ComplexAttribute) attr).getComplexType().getAttributes();

            if (attrs != null && attrs.size() > 0) {
                for (Attribute subAttr : attrs) {
                    AttributeTree sub = new AttributeTree(subAttr);
                    subTree.addSubTree(sub);
                }
            }
        }


        outParentState.realValue = null;
        outPreVisitState.realValue = null;
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
        //Nothing To Do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing To Do
    }
}
