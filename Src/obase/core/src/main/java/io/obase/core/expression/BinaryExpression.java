/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：二元表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 16:26:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 二元表达式
 */
public class BinaryExpression extends Expression {

    /**
     * 表达式左边
     */
    private final Expression left;

    /**
     * 表达式右边
     */
    private final Expression right;


    /**
     * 构造一个二元表达式
     *
     * @param left           左侧
     * @param right          右侧
     * @param expressionType 表达式类型
     * @param type           表达式的返回值类型
     */
    BinaryExpression(Expression left, Expression right, EExpressionType expressionType, Class<?> type) {
        this.left = left;
        this.right = right;
        this.expressionType = expressionType;
        this.type = type;
    }

    /**
     * 获取表达式左侧
     *
     * @return 表达式左侧
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * 获取表达式右侧
     *
     * @return 表达式右侧
     */
    public Expression getRight() {
        return this.right;
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
        //只能支持相等和不相等运算
        //其余运算无法直接应用其运算符
        switch (this.getExpressionType()) {
            case Equal:
                return this.left.calculate(getter).equals(this.right.calculate(getter));
            case NotEqual:
                return !this.left.calculate(getter).equals(this.right.calculate(getter));
            default:
                throw new IllegalArgumentException("计算失败,不支持的二元表达式类型" + this.getExpressionType());
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
        return visitor.visitBinary(this);
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "BinaryExpression{" +
                "left=" + this.left +
                ", right=" + this.right +
                ", expressionType=" + this.expressionType +
                ", type=" + this.type +
                '}';
    }
}
