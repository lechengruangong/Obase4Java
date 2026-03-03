/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Lambda表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 14:51:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lambda表达式
 */
public class LambdaExpression extends Expression {

    /**
     * 表达式体
     */
    private final Expression body;
    /**
     * 参数列表
     */
    private ParameterExpression[] parameters;

    /**
     * 构造Lambda表达式
     *
     * @param parameters 参数列表
     * @param body       表达式体
     */
    LambdaExpression(ParameterExpression[] parameters, Expression body) {
        this.parameters = parameters;
        this.body = body;
        this.expressionType = EExpressionType.Lambda;
        this.type = body.getType();
    }

    /**
     * 获取表达式体
     *
     * @return 表达式体
     */
    public Expression getBody() {
        return this.body;
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public ParameterExpression[] getParameters() {
        return this.parameters;
    }

    /**
     * 编译方法 还原表达式所代表的方法
     *
     * @return 返回一个表达式计算器对象
     */
    public IFunc compile() {
        return new ExpressionFunc(this);
    }

    /**
     * 添加参数表达式
     *
     * @param parameterExpression 参数表达式
     */
    public void addParameter(ParameterExpression parameterExpression) {
        List<ParameterExpression> parameterExpressionList = new ArrayList<>(Arrays.asList(this.parameters));
        parameterExpressionList.add(parameterExpression);
        this.parameters = parameterExpressionList.toArray(new ParameterExpression[0]);
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
        return this.body.calculate(getter);
    }

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitLambda(this);
    }

    @Override
    public String toString() {
        return "LambdaExpression{" +
                "expressionType=" + this.expressionType +
                ", type=" + this.type +
                ", parameters=" + Arrays.toString(this.parameters) +
                ", body=" + this.body +
                '}';
    }
}
