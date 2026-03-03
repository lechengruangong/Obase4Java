/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图元素添加器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 14:52:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.expression.Expression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.TypeView;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * 视图元素添加器，根据类的成员（字段或属性访问器）及其绑定的表达式创建视图元素，并添加到目标视图。
 * 实施说明
 * 本类为建造器模式指挥者，通过调度具体建造者完成建造。需要时才创建建造者；一旦创建即寄存以供下次使用。
 */
public class ViewElementAdder {

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 为其创建并添加元素的类型视图，简称目标视图
     */
    private final TypeView typeView;


    /**
     * 视图属性建造器
     */
    private ViewAttributeBuilder viewAttributeBuilder;

    /**
     * 视图复杂属性建造器
     */
    private ViewComplexAttributeBuilder viewComplexAttributeBuilder;

    /**
     * 视图引用建造器
     */
    private ViewReferenceBuilder viewReferenceBuilder;

    /**
     * 创建ViewElementAdder实例
     *
     * @param typeView 目标视图
     * @param model    对象数据模型
     */
    public ViewElementAdder(TypeView typeView, ObjectDataModel model) {
        this.typeView = typeView;
        this.model = model;
    }

    /**
     * 创建视图元素并将其添加到目标视图
     *
     * @param member       依据其创建元素的类成员
     * @param expression   类成员的绑定表达式
     * @param paraBindings 形参绑定
     * @return 添加的类型元素
     */
    public TypeElement addElement(Member member, Expression expression, ParameterBinding[] paraBindings) {
        Class<?> memberType = null;
        if (member instanceof Field) {
            Field fieldInfo = (Field) member;
            memberType = fieldInfo.getType();
        } else if (member instanceof Method) {
            Method method = (Method) member;
            memberType = method.getReturnType();
        } else if (member == null) {
            //没有Member 则表示在构造函数内初始化
            memberType = expression.getType();
        }
        if (memberType != null && Iterable.class.isAssignableFrom(memberType)) {
            if (expression instanceof MemberExpression) {
                MemberExpression memberExpression = (MemberExpression) expression;
                memberType = (Class<?>) ((ParameterizedType) memberExpression.getMemberMethod().getGenericReturnType()).getActualTypeArguments()[0];
            } else {
                memberType = (Class<?>) ((ParameterizedType) (memberType.getGenericSuperclass())).getActualTypeArguments()[0];
            }

        }

        StructuralType structType = this.model.getStructuralType(memberType);
        ViewElementBuilder builder;

        if (structType instanceof ReferringType) {
            //视图引用
            if (this.viewReferenceBuilder == null) this.viewReferenceBuilder = new ViewReferenceBuilder(this.model);
            builder = this.viewReferenceBuilder;
        } else if (structType instanceof ComplexType) {
            //复杂属性
            if (this.viewComplexAttributeBuilder == null)
                this.viewComplexAttributeBuilder = new ViewComplexAttributeBuilder(this.model);
            builder = this.viewComplexAttributeBuilder;
        } else {
            //视图属性
            if (this.viewAttributeBuilder == null) this.viewAttributeBuilder = new ViewAttributeBuilder(this.model);
            builder = this.viewAttributeBuilder;
        }

        builder.instantiate(member, expression, this.typeView.getExtension(), paraBindings);
        builder.setTargetField(member);
        builder.setMultiple(expression);
        builder.setValueGetter(member, expression);
        builder.setValueSetter(member, expression);
        TypeElement element = builder.getElement();
        this.typeView.addElement(element);
        return element;
    }
}

