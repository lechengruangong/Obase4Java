/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图在源扩展树上的平展节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:43:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.objectSys.AssociationTreeNode;

/**
 * 表示视图在源扩展树上的平展节点以及代表该节点的形参。
 */
public class ViewFlatteningPoint {

    /**
     * 源扩展树上的一个节点，表示在该节点上实施平展
     */
    private final AssociationTreeNode extensionNode;

    /**
     * 平展形参，即在表达式（如视图属性的绑定表达式）中代表平展点的形式参数。
     */
    private final ParameterExpression flatteningParameter;

    /**
     * 创建ViewFlatteningPoint实例
     *
     * @param extensionNode  源扩展树上的节点
     * @param flatteningPara 平展形参
     */
    public ViewFlatteningPoint(AssociationTreeNode extensionNode, ParameterExpression flatteningPara) {
        this.extensionNode = extensionNode;
        this.flatteningParameter = flatteningPara;
    }

    /**
     * 获取源扩展树的节点，该节点为平展节点
     *
     * @return 获取源扩展树的节点，该节点为平展节点
     */
    public AssociationTreeNode getExtensionNode() {
        return this.extensionNode;
    }

    /**
     * 获取平展形参。平展形参是在表达式（如视图属性的绑定表达式）中代表平展点的形式参数。
     *
     * @return 获取平展形参。平展形参是在表达式（如视图属性的绑定表达式）中代表平展点的形式参数。
     */
    public ParameterExpression getFlatteningParameter() {
        return this.flatteningParameter;
    }
}