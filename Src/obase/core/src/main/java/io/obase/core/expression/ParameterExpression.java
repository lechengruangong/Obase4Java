/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：参数表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 14:51:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 参数表达式
 */
public class ParameterExpression extends Expression {

    /**
     * 参数的名称
     */
    private final String name;

    /**
     * 实际参数
     */
    private final Object obj;
    /**
     * 是否为宿主参数
     */
    private final boolean isHost;
    /**
     * 参数在解析时的索引
     */
    private int index = 0;

    /**
     * 构造一个参数表达式
     *
     * @param name   参数名
     * @param obj    实际参数
     * @param type   类型
     * @param isHost 是否为宿主参数
     */
    ParameterExpression(String name, Object obj, Class<?> type, boolean isHost) {
        this.name = name;
        this.obj = obj;
        this.type = type;
        this.expressionType = EExpressionType.Parameter;
        this.isHost = isHost;
    }

    /**
     * 获取参数名称
     *
     * @return 参数名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取实参
     *
     * @return 实参
     */
    public Object getObj() {
        return this.obj;
    }

    /**
     * 获取解析实参时的索引
     *
     * @return 解析实参时的索引
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * 设置解析实参时的索引
     *
     * @param index 索引值
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 返回是否为宿主参数
     *
     * @return 是否为宿主参数
     */
    public boolean getIsHost() {
        return this.isHost;
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
        return getter.get(this.getName());
    }

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitParameter(this);
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "ParameterExpression{" +
                "expressionType=" + this.expressionType +
                ", type=" + this.type +
                ", name='" + this.name + '\'' +
                '}';
    }
}
