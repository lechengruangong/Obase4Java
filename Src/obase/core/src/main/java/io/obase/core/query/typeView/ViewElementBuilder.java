/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图元素建造器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 12:25:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.ParameterBinding;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * 为视图元素建造器提供基础实现
 */
public abstract class ViewElementBuilder {

    /**
     * 作为建造产品的类型元素
     */
    protected TypeElement element;

    /**
     * 对象数据模型
     */
    protected ObjectDataModel model;

    /**
     * 创建ViewElementBuilder实例
     *
     * @param model 对象数据模型
     */
    protected ViewElementBuilder(ObjectDataModel model) {
        this.model = model;
    }

    /**
     * 获取类型元素
     *
     * @return 类型元素
     */
    public TypeElement getElement() {
        return this.element;
    }

    /**
     * 实例化类型元素，同时根据需要扩展视图源
     *
     * @param member          与元素对应的类成员
     * @param expression      类成员绑定的表达式
     * @param sourceExtension 视图源扩展树
     * @param paraBindings    形参绑定
     */
    public abstract void instantiate(Member member, Expression expression, AssociationTree sourceExtension,
                                     ParameterBinding[] paraBindings);

    /**
     * 设置映射字段
     *
     * @param member 与元素对应的类成员
     */
    public void setTargetField(Member member) {
        if (this.element instanceof Attribute) {
            Attribute attribute = (Attribute) this.element;
            attribute.setTargetField(member.getName());
        }
    }

    /**
     * 设置多重性
     *
     * @param expression 类成员绑定的表达式
     */
    public void setMultiple(Expression expression) {
        if (this.element instanceof Attribute) {
            Attribute attribute = (Attribute) this.element;
            attribute.setIsMultiple(true);
        }
    }

    /**
     * 设置取值器
     *
     * @param member     与元素对应的类成员
     * @param expression 类成员绑定的表达式
     */
    public void setValueGetter(Member member, Expression expression) {
        if (member instanceof Field) {
            Field fieldInfo = (Field) member;
            this.element.setValueGetter(new FieldValueGetter(fieldInfo));
        } else if (member instanceof Method) {
            Method method = (Method) member;

            this.element.setValueGetter(Utils.makeDelegateValueGetter(method));
        }
    }

    /**
     * 设置设值器
     *
     * @param member     与元素对应的类成员
     * @param expression 类成员绑定的表达式
     */
    public void setValueSetter(Member member, Expression expression) {
        if (member instanceof Field) {
            Field fieldInfo = (Field) member;
            this.element.setValueSetter(new FieldValueSetter(fieldInfo));
        } else if (member instanceof Method) {
            Method method = (Method) member;
            this.element.setValueSetter(ValueSetter.create(method, EValueSettingMode.Assignment));
        }
    }
}
