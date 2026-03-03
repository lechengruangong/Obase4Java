/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员初始化表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:52:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 成员初始化表达式
 */
public class MemberInitExpression extends Expression {

    /**
     * 绑定的新建表达式
     */
    private final NewExpression newExpression;

    /**
     * 要赋值的成员
     */
    private final MemberBinding[] memberBindings;

    /**
     * 构造MemberInit表达式
     *
     * @param expression     绑定的新建表达式
     * @param memberBindings 要赋值的成员
     */
    public MemberInitExpression(NewExpression expression, MemberBinding[] memberBindings) {
        this.memberBindings = memberBindings;
        this.expressionType = EExpressionType.MemberInit;
        this.newExpression = expression;
    }

    /**
     * 获取赋值表达式
     *
     * @return 赋值表达式
     */
    public NewExpression getNewExpression() {
        return this.newExpression;
    }

    /**
     * 获取绑定
     *
     * @return 成员绑定
     */
    public MemberBinding[] getMemberBindings() {
        return this.memberBindings;
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
        //直接返回构造函数的结果即可 成员赋值绑定在JAVA中不存在
        return this.newExpression.calculate(getter);
    }
}
