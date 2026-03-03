/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:35:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.expression.*;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.AssociationExpressionGenerator;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeExpressionGenerator;
import io.obase.core.odm.objectSys.AttributeTreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * 视图属性。
 * 视图属性来源于源（或源扩展）的一个属性，或一个或多个属性的算术运算。
 * 视图属性属于简单属性。
 */
public class ViewAttribute extends Attribute implements ITypeViewElement {

    /**
     * 属性绑定，一个表达式，说明视图属性的来源。
     */
    private final Expression binding;

    /**
     * 邮戳锁
     */
    private final Object lockObj = new Object();

    /**
     * 视图属性的源
     */
    private final ViewAttributeSource[] sources;

    /**
     * 求值器，用于根据属性绑定表达式计算属性的值
     */
    private ViewAttributeEvaluator evaluator;

    /**
     * (寄存)该值指示视图属性是否为直观属性
     */
    private Boolean isIntuitive;

    /**
     * 影子元素
     */
    private ViewAttribute shadow;

    /**
     * 创建表示非直观属性的ViewAttribute实例
     *
     * @param name    属性名称
     * @param binding 属性绑定
     * @param sources 属性源
     */
    public ViewAttribute(String name, Expression binding, ViewAttributeSource sources) {
        super(binding.getType(), name);

        this.binding = binding;
        ViewAttributeSource[] viewAttributeSource = new ViewAttributeSource[1];
        viewAttributeSource[0] = sources;
        this.sources = viewAttributeSource;
        this.setTargetField(name);
    }

    /**
     * 创建表示非直观属性的ViewAttribute实例
     *
     * @param name    属性名称
     * @param binding 属性绑定
     * @param sources 属性源
     */
    public ViewAttribute(String name, Expression binding, ViewAttributeSource[] sources) {
        super(binding.getType(), name);

        this.binding = binding;
        this.sources = sources;
    }

    /**
     * 创建表示直观属性的ViewAttribute实例
     *
     * @param name          属性名称
     * @param attributeNode 属性树节点，代表构成属性源的属性
     * @param extensionNode 构成属性源的扩展树节点，未指定表示根节点
     */
    public ViewAttribute(String name, AttributeTreeNode attributeNode, AssociationTreeNode extensionNode) {
        this(name, attributeNode.getAttribute(), extensionNode);
    }

    /**
     * 创建表示直观属性的ViewAttribute实例
     *
     * @param name          属性名称
     * @param attribute     构成属性源的属性，须为顶级属性，不接受子属性
     * @param extensionNode 构成属性源的扩展树节点，未指定表示根节点
     */
    public ViewAttribute(String name, Attribute attribute, AssociationTreeNode extensionNode) {
        super(attribute.getDataType(), name);

        Class<?> decType = attribute.getHostType().getClrType();

        MemberExpression representor = null;
        Property property = ObaseIntrospector.getObaseBeanProperties(decType).stream().filter(p -> p.getName().equals(attribute.getName())).findFirst().orElse(null);
        if (property != null) {
            ParameterExpression parExp = Expression.parameter(property.getGetterMethod().getName(), decType);
            representor = Expression.member(parExp, property.getGetterMethod(), parExp, parExp.getType());
        }

        this.binding = representor;
        ViewAttributeSource[] sources = new ViewAttributeSource[1];
        if (extensionNode == null) {
            sources[0] = new ViewAttributeSource(attribute, representor);
        } else {
            sources[0] = new ViewAttributeSource(extensionNode, attribute, representor);
        }
        this.sources = sources;
        this.isIntuitive = true;
        this.setTargetField(attribute.getTargetField());
    }

    /**
     * 获取属性绑定
     *
     * @return 属性绑定
     */
    public Expression getBinding() {
        return this.binding;
    }

    /**
     * 获取属性求值器
     *
     * @return 属性求值器
     */
    public ViewAttributeEvaluator getEvaluator() {
        synchronized (this.lockObj) {
            if (this.evaluator != null) return this.evaluator;
            LambdaExpression lambda = this.generateAgentExpression();
            this.evaluator = ViewAttributeEvaluator.create(lambda);
            return this.evaluator;
        }
    }

    /**
     * 获取一个值，该指指示视图属性是否为直观属性。
     * 一个视图属性是直观，意即它直接以其源的值为值，不经过任何计算。
     * 直观属性必定是单源属性。
     *
     * @return 视图属性是否为直观属性
     */
    public boolean getIsIntuitive() {
        synchronized (this.lockObj) {
            if (this.isIntuitive == null) {
                boolean isIntuitive = false;
                if (this.getSourceSingle() && this.binding instanceof MemberExpression) //简单源 并且 绑定表达式为成员表达式
                {
                    MemberExpression member = (MemberExpression) this.binding;
                    StructuralType structuralType = this.getHostType().getModel().getStructuralType(member.getExpression().getType());
                    if (structuralType != null) {
                        isIntuitive = !structuralType.getAttribute(member.getMemberName()).getIsComplex();
                    }

                }
                this.isIntuitive = isIntuitive;
            }
            return this.isIntuitive;
        }
    }

    /**
     * 获取影子元素
     * 视图属性。
     * 视图属性来源于源（或源扩展）的一个属性，或一个或多个属性的算术运算。
     * 视图属性属于简单属性。
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
        this.shadow = (ViewAttribute) shadow;
    }

    /**
     * 获取一个值，该值指示属性是否为多源属性
     *
     * @return 属性是否为多源属性
     */
    public Boolean getSourceMultiple() {
        return this.sources.length > 1;
    }

    /**
     * 获取属性源
     *
     * @return 获取属性源
     */
    public ViewAttributeSource[] getSources() {
        return this.sources;
    }

    /**
     * 获取一个值，该值指示属性是否为单源属性
     *
     * @return 是否为单源属性
     */
    public boolean getSourceSingle() {
        return this.sources.length == 1;
    }


    /**
     * 生成在视图表达式中定义当前元素的表达式，它规定了该元素的锚点和绑定。
     *
     * @param sourcePara           代表视图源的形参
     * @param flatteningParaGetter 一个委托，用于获取代表指定平展点的形参。
     */
    @Override
    public Expression generateExpression(ParameterExpression sourcePara, FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter) {

        if (this.getIsIntuitive()) {
            ViewAttributeSource attrSource = this.sources[0];
            AssociationExpressionGenerator typeExpGenerator = new AssociationExpressionGenerator(sourcePara, flatteningParaGetter);
            LambdaExpression hostExp = attrSource.getExtensionNode().asTree().accept(typeExpGenerator);
            AttributeExpressionGenerator generator = new AttributeExpressionGenerator(hostExp);
            attrSource.getAttributeNode().asTree().accept(generator);
            return generator.getResult();
        }

        return this.binding;
    }

    /**
     * 生成代理表达式。
     * 代理表达式基于属性源代理计算属性的值，它以属性的绑定表达式为基本框架，以代表属性源代理的形参替换其中的属性源表达式。
     *
     * @return 代理表达式
     */
    private LambdaExpression generateAgentExpression() {
        AgentExpressionGenerator generator = new AgentExpressionGenerator();
        for (ViewAttributeSource attrSource : this.sources) {
            ParameterExpression parameter = Expression.parameter("", attrSource.getAttributeNode().getAttribute().getDataType());
            generator.addParameter(parameter, attrSource.getRepresentor());
        }

        this.binding.accept(generator);
        return (LambdaExpression) this.binding;
    }

    /**
     * 代理表达式生成器。
     * 代理表达式基于属性源代理计算属性的值，它以属性的绑定表达式为基本框架，以代表属性源代理的形参替换其中的属性源表达式。
     */
    private static class AgentExpressionGenerator extends ExpressionVisitor {

        /**
         * 代表属性源代理的形参，每个属性源表达式对应一个形参
         */
        private Map<MemberExpression, ParameterExpression> parameters;

        /**
         * 添加代表属性源代理的形参
         *
         * @param parameter 代表属性源代理的形参
         * @param sourceExp 属性源表达式
         */
        void addParameter(ParameterExpression parameter, MemberExpression sourceExp) {
            if (this.parameters == null) this.parameters = new HashMap<>();
            this.parameters.put(sourceExp, parameter);
        }

        /**
         * 默认的访问Lambda表达式
         * 先访问Body 然后挨个访问Parameter
         * 最后返回自身
         *
         * @param lambdaExpression Lambda表达式
         * @return 自身
         */
        @Override
        protected Expression visitLambda(LambdaExpression lambdaExpression) {
            Expression exp = this.visit(lambdaExpression.getBody());
            return Expression.lambda(this.parameters.values().toArray(new ParameterExpression[0]), exp);
        }

        /**
         * 默认的访问成员表达式
         * 访问成员表达式的Expression 而后返回自身
         *
         * @param memberExpression 成员表达式
         * @return 成员表达式自身
         */
        @Override
        protected Expression visitMember(MemberExpression memberExpression) {
            return this.parameters.get(memberExpression);
        }
    }
}
