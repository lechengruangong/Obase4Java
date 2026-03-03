/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：常量表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 12:06:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 常量表达式
 */
public class ConstantExpression extends Expression {

    /**
     * 值
     */
    private final Object value;

    /**
     * 构造一个常量表达式
     *
     * @param value 值
     */
    ConstantExpression(Object value) {
        this.value = value;
        this.expressionType = EExpressionType.Constant;
        this.type = value == null ? null : value.getClass();
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Object getValue() {
        return this.value;
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
        //处理包装类 转换为基础类型
        if (this.type != null) {
            if (Integer.class.equals(this.type)) {
                return int.class;
            } else if (Long.class.equals(this.type)) {
                return long.class;
            } else if (Short.class.equals(this.type)) {
                return short.class;
            } else if (Byte.class.equals(this.type)) {
                return byte.class;
            } else if (Float.class.equals(this.type)) {
                return float.class;
            } else if (Double.class.equals(this.type)) {
                return double.class;
            } else if (Boolean.class.equals(this.type)) {
                return boolean.class;
            } else if (Character.class.equals(this.type)) {
                return char.class;
            }
        }
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
        return this.value;
    }

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitConstant(this);
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "ConstantExpression{" +
                "value=" + this.value +
                ", expressionType=" + this.expressionType +
                ", type=" + this.type +
                '}';
    }
}
