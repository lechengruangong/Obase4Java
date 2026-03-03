/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:39:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import io.obase.core.common.Property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 成员表达式
 */
public class MemberExpression extends Expression {

    /**
     * 成员方法 Get
     */
    private final Method memberMethod;

    /**
     * 成员所属的对象所属对象的表达式
     */
    private final Expression expression;

    /**
     * 成员名称 即字段名或Property的GetName
     */
    private final String memberName;

    /**
     * 成员的属性
     */
    private final Property property;

    /**
     * 宿主表达式
     */
    private final Expression host;

    /**
     * 宿主类型
     */
    private final Class<?> hostType;

    /**
     * 构造一个成员访问表达式
     *
     * @param memberMethod 访问的方法
     * @param expression   所属对象的表达式
     * @param memberName   成员名称
     * @param property     成员的属性
     * @param host         宿主表达式
     * @param hostType     宿主表达式的类型
     */
    MemberExpression(Method memberMethod, Expression expression, String memberName, Property property, Expression host, Class<?> hostType) {
        this.memberMethod = memberMethod;
        this.expression = expression;
        this.memberName = memberName;
        this.host = host;
        this.expressionType = EExpressionType.MemberAccess;
        this.type = memberMethod.getReturnType();
        this.property = property;
        this.hostType = hostType;
        if (hostType != null && this.host != null) {
            if (hostType != this.host.getType()) {
                this.host.type = hostType;
            }
        }
    }

    /**
     * 获取成员所属的对象所属对象的表达式
     *
     * @return 成员所属的对象所属对象的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 获取成员方法 Get
     *
     * @return 成员方法 Get
     */
    public Method getMemberMethod() {
        return this.memberMethod;
    }

    /**
     * 获取成员名称 即字段名或Property的GetName
     *
     * @return 成员名称
     */
    public String getMemberName() {
        return this.memberName;
    }

    /**
     * 成员的属性
     *
     * @return 成员的属性
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * 获取宿主
     *
     * @return 宿主
     */
    public Expression getHost() {
        return this.host;
    }

    /**
     * 获取宿主类型
     *
     * @return 宿主类型
     */
    public Class<?> getHostType() {
        return this.hostType;
    }

    /**
     * 获取表达式类型
     *
     * @return 表达式类型
     */
    @Override
    public EExpressionType getExpressionType() {
        return this.expressionType;
    }

    /**
     * 获取表达式返回的类型
     *
     * @return 表达式返回的类型
     */
    @Override
    public Class<?> getType() {
        return this.type;
    }

    /**
     * 计算表达式的值
     *
     * @param getter 参数值获取器
     * @return 计算后的结果
     */
    @Override
    public Object calculate(IArgumentGetter getter) {
        try {
            return this.memberMethod.invoke(getter.get("instance"));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("计算表达式失败,无法获取方法的值", e);
        }
    }

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitMember(this);
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "MemberExpression{" +
                "expressionType=" + this.expressionType +
                ", type=" + this.type +
                ", memberMethod=" + this.memberMethod +
                ", expression=" + this.expression +
                '}';
    }
}
