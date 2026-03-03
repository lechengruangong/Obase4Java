/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图属性建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 14:40:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.Expression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.odm.typeviews.ViewAttributeSource;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

/**
 * 视图属性建造器
 */
public class ViewAttributeBuilder extends ViewElementBuilder {


    /**
     * 类成员绑定的表达式
     */
    private MemberExpression expression;

    /**
     * 创建ViewElementBuilder实例
     *
     * @param model 对象数据模型
     */
    public ViewAttributeBuilder(ObjectDataModel model) {
        super(model);
    }

    /**
     * 实例化类型元素，同时根据需要扩展视图源
     *
     * @param member          与元素对应的类成员
     * @param expression      类成员绑定的表达式
     * @param sourceExtension 视图源扩展树
     * @param paraBindings    形参绑定
     */
    @Override
    public void instantiate(Member member, Expression expression, AssociationTree sourceExtension, ParameterBinding[] paraBindings) {
        List<MemberExpression> memberExps = new MemberExpressionExtractor(new SubTreeEvaluator(expression)).extractMember(expression);
        List<ViewAttributeSource> sources = new ArrayList<>();

        for (MemberExpression memberExp : memberExps) {
            ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();
            AssociationTreeNode assTail = memberExp.growAssociationTree(sourceExtension, this.model, attrTail,
                    paraBindings);
            ViewAttributeSource source = new ViewAttributeSource(assTail, attrTail.realValue.getAttribute(), memberExp);
            sources.add(source);
            this.expression = memberExp;
        }

        this.element = new ViewAttribute(member.getName(), expression, sources.toArray(new ViewAttributeSource[0]));
    }

    /**
     * 设置映射字段
     *
     * @param member 与元素对应的类成员
     */
    @Override
    public void setTargetField(Member member) {
        StructuralType host = this.model.getStructuralType(this.expression.getExpression().getType());
        Attribute attr = host.getAttribute(this.expression.getMemberName());
        if (this.element instanceof Attribute) {
            Attribute attribute = (Attribute) this.element;
            if (member.getName().equalsIgnoreCase(attr.getTargetField()))
                attribute.setTargetField(member.getName());
            else
                attribute.setTargetField(attr.getTargetField());
        }
    }
}
