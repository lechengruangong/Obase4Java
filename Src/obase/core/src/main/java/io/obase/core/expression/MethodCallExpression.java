/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：方法调用表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:13:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 方法调用表达式
 */
public class MethodCallExpression extends Expression {

    /**
     * 参数列表
     */
    private final Expression[] argument;

    /**
     * 调用的方法
     */
    private final Method method;

    /**
     * 所属的对象的表达式
     */
    private final Expression object;

    /**
     * 构造方法调用表达式
     *
     * @param argument 参数
     * @param method   方法
     * @param object   所属的对象
     */
    MethodCallExpression(Expression[] argument, Method method, Expression object) {
        this.argument = argument;
        this.method = method;
        this.object = object;
        this.expressionType = EExpressionType.Call;
        this.type = method.getReturnType();
    }

    /**
     * 获取所属的对象的表达式
     *
     * @return 属的对象的表达式
     */
    public Expression getObject() {
        return this.object;
    }

    /**
     * 获取调用的方法
     *
     * @return 调用的方法
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public Expression[] getArgument() {
        return this.argument;
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
            if (this.getArgument() != null && this.getArgument().length > 0) {
                Object[] objs = new Object[0];
                List<Object> objectList = new ArrayList<>();
                for (Expression expression : this.getArgument()) {
                    objectList.add(expression.calculate(getter));
                }
                objectList.toArray(objs);
                return this.getMethod().invoke(this.getObject().calculate(getter), objs);
            }

            return this.getMethod().invoke(this.getObject().calculate(getter));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("计算表达式错误,无法调用函数", e);
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
        return visitor.visitMethodCall(this);
    }

    /**
     * 转换为字符串表示
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "MethodCallExpression{" +
                "expressionType=" + this.expressionType +
                ", type=" + this.type +
                ", argument=" + Arrays.toString(this.argument) +
                ", method=" + this.method +
                ", object=" + this.object +
                '}';
    }
}
