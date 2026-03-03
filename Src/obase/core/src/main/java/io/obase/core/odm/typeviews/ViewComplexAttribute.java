/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图复杂属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:37:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ComplexAttribute;
import io.obase.core.odm.ComplexType;
import io.obase.core.odm.EntityType;
import io.obase.core.odm.objectSys.*;

/**
 * 视图复杂属性。
 * 视图复杂属性来源于源（或源扩展）的一个复杂属性。
 */
public class ViewComplexAttribute extends ComplexAttribute implements ITypeViewElement {
    /**
     * 复杂属性的锚（或称锚点）。
     * 锚点是源扩展树上的一个节点，视图复杂属性即来源于该节点代表类型的某个复杂属性。
     */
    private final AssociationTreeNode anchor;

    /**
     * 复杂属性绑定。
     * 绑定是一个属性树节点，该节点所代表的复杂属性即是视图复杂属性的来源。
     */
    private final ComplexAttributeNode binding;

    /**
     * 影子元素
     */
    private ViewComplexAttribute shadow;

    /**
     * 创建ViewComplexAttribute实例
     *
     * @param name    属性名称
     * @param anchor  复杂属性锚
     * @param binding 复杂属性绑定
     */
    public ViewComplexAttribute(String name, AssociationTreeNode anchor, AttributeTreeNode binding) {
        super(anchor.getRepresentedType().getClrType(), name, new ComplexType(anchor.getRepresentedType().getClrType(), null));
        this.anchor = anchor;
        this.binding = new ComplexAttributeNode(binding.getAttribute());
    }

    /**
     * 实例化ViewComplexAttribute实例，该实例表示的视图复杂属性锚定于源扩展树根节点。
     *
     * @param name    属性名称
     * @param binding 复杂属性绑定
     */
    public ViewComplexAttribute(String name, AttributeTreeNode binding) {
        super(binding.getAttribute().getHostType().getClrType(), name, new ComplexType(binding.getAttribute().getHostType().getClrType(), null));
        this.anchor = new ObjectTypeNode(new EntityType(binding.getAttribute().getHostType().getClrType(), null), null);
        this.binding = new ComplexAttributeNode(binding.getAttribute());
    }

    /**
     * 获取影子元素
     *
     * @return 影子元素
     */
    @Override
    public ITypeViewElement getShadow() {
        return this.shadow;
    }

    /**
     * 设置影子元素
     *
     * @param shadow 影子元素
     */
    @Override
    public void setShadow(ITypeViewElement shadow) {
        this.shadow = (ViewComplexAttribute) shadow;
    }

    /**
     * 获取复杂属性的锚
     *
     * @return 复杂属性的锚
     */
    public AssociationTreeNode getAnchor() {
        return this.anchor;
    }

    /**
     * 获取复杂属性绑定
     *
     * @return 复杂属性绑定
     */
    public ComplexAttributeNode getBinding() {
        return this.binding;
    }

    /**
     * 生成在视图表达式中定义当前元素的表达式，它规定了该元素的锚点和绑定。
     *
     * @param sourcePara           代表视图源的形参
     * @param flatteningParaGetter 一个委托，用于获取代表指定平展点的形参。
     */
    @Override
    public Expression generateExpression(ParameterExpression sourcePara, FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter) {
        AssociationExpressionGenerator typeExpGenerator = new AssociationExpressionGenerator(sourcePara, flatteningParaGetter);
        LambdaExpression hostExp = this.anchor.asTree().accept(typeExpGenerator);
        AttributeExpressionGenerator generator = new AttributeExpressionGenerator(hostExp);
        this.binding.asTree().accept(generator);
        return generator.getResult();
    }
}
