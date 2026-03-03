/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：一元表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:19:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 一元表达式
 */
public class UnaryExpression extends Expression {

    /**
     * 操作数
     */
    private final Expression operand;

    /**
     * 如果是一个转换表达式 此字段有值
     */
    private Class<?> convertType;

    /**
     * 构造一元表达式
     *
     * @param operand        操作数
     * @param expressionType 类型
     */
    UnaryExpression(Expression operand, EExpressionType expressionType) {
        this.operand = operand;
        this.expressionType = expressionType;
        this.type = operand.getType();
    }

    /**
     * 获取操作数
     *
     * @return 操作数
     */
    public Expression getOperand() {
        return this.operand;
    }

    /**
     * 获取转换类型
     *
     * @return 转换类型
     */
    public Class<?> getConvertType() {
        return this.convertType;
    }

    /**
     * 设置转换类型
     *
     * @param convertType 转换类型
     */
    public void setConvertType(Class<?> convertType) {
        this.convertType = convertType;
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
        throw new IllegalArgumentException("计算失败,不支持的一元表达式类型" + this.getExpressionType());
    }

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitUnary(this);
    }
}
