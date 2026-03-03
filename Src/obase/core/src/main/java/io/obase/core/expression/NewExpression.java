/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：构造表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:53:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 构造表达式
 */
public class NewExpression extends Expression {

    /**
     * 构造函数信息
     */
    private Constructor<?> constructor;

    /**
     * 参数列表
     */
    private Expression[] argument;

    /**
     * 一并初始化的成员
     * JAVA中无此类语法 此项目仅为占位符
     */
    private Member[] members;

    /**
     * 构造一个构造函数表达式
     *
     * @param type 类型
     */
    NewExpression(Class<?> type) {
        this.type = type;
        this.expressionType = EExpressionType.New;
    }

    /**
     * 获取构造函数信息
     *
     * @return 构造函数信息
     */
    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    /**
     * 设值构造函数信息
     *
     * @param constructor 构造函数信息
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
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
     * 设值参数列表
     *
     * @param argument 参数列表
     */
    public void setArgument(Expression[] argument) {
        this.argument = argument;
    }

    /**
     * 获取一并初始化的成员
     *
     * @return 一并初始化的成员
     */
    public Member[] getMembers() {
        return this.members;
    }

    /**
     * 设置一并初始化的成员
     *
     * @param members 一并初始化的成员
     */
    public void setMembers(Member[] members) {
        this.members = members;
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
            //先计算参数 再使用构造器构造对象
            if (this.getArgument() != null && this.getArgument().length > 0) {
                Object[] objs = new Object[0];
                List<Object> objectList = new ArrayList<>();
                for (Expression expression : this.getArgument()) {
                    objectList.add(expression.calculate(getter));
                }
                objs = objectList.toArray(objs);
                return this.constructor.newInstance(objs);
            }

            return this.constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("计算表达式错误,无法新建对象", e);
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
        return visitor.visitNew(this);
    }

    /**
     * 返回字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "NewExpression{" +
                "expressionType=" + this.expressionType +
                ", type=" + this.type +
                ", constructor=" + this.constructor +
                ", argument=" + Arrays.toString(this.argument) +
                ", members=" + Arrays.toString(this.members) +
                '}';
    }
}
