/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图引用建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 14:54:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.expression.Expression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.ViewReference;

import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;

/**
 * 视图引用建造器
 */
public class ViewReferenceBuilder extends ViewElementBuilder {

    /**
     * 创建ViewElementBuilder实例
     *
     * @param model 对象数据模型
     */
    public ViewReferenceBuilder(ObjectDataModel model) {
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
        if (expression instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) expression;

            String name = member.getName();
            AssociationTreeNode lastNode;
            if (memberExpression.getHost() instanceof MemberExpression) {
                lastNode = memberExpression.growAssociationTree(sourceExtension, this.model, paraBindings);
            } else {
                lastNode = sourceExtension.getNode();
            }
            Class<?> type = memberExpression.getMemberMethod().getDeclaringClass();
            if (Iterable.class.isAssignableFrom(type)) {
                type = (Class<?>) ((ParameterizedType) memberExpression.getMemberMethod().getGenericReturnType()).getActualTypeArguments()[0];
            }
            ReferringType referringType = this.model.getReferringType(type);
            ReferenceElement referenceElement = referringType.getReferenceElement(memberExpression.getMemberName());
            this.element = new ViewReference(referenceElement, name, lastNode);
        }
    }
}
