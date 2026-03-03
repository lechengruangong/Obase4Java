/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员赋值,用字段赋值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:42:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import io.obase.core.common.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 成员赋值,用字段赋值
 */
public class MemberAssignment extends MemberBinding {

    /**
     * 设置成员方法
     */
    private final Method setMethod;

    /**
     * 绑定类型
     */
    private final EMemberBindingType type;

    /**
     * 表达式
     */
    private final Expression expression;

    /**
     * 代表的字段
     */
    private final Field field;

    /**
     * 构造成员赋值
     *
     * @param expression 表达式
     * @param method     设置成员方法
     */
    public MemberAssignment(Expression expression, Method method) {
        this.expression = expression;
        this.setMethod = method;
        this.type = EMemberBindingType.Assignment;
        this.field = Utils.getFieldIncludeSuperclass(method.getDeclaringClass(), method.getName().toLowerCase().replace("set", "_"));
    }

    /**
     * 获取对应的字段
     *
     * @return 字段
     */
    public Field getField() {
        return this.field;
    }

    /**
     * 获取表达式
     *
     * @return 表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 获取绑定类型
     *
     * @return 绑定类型
     */
    @Override
    public EMemberBindingType getBidingType() {
        return this.type;
    }

    /**
     * 获取成员设置方法
     *
     * @return 成员设置方法
     */
    @Override
    public Method getMemberSetMethod() {
        return this.setMethod;
    }
}
