/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式计算器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 14:35:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * 表达式计算器
 */
public class ExpressionFunc implements SerializedFunction<Object, Object>, SerializedPredicate<Object>, IFunc {

    /**
     * 表达式
     */
    private final Expression expression;

    /**
     * 构造表达式计算器
     */
    public ExpressionFunc(Expression expression) {
        this.expression = expression;
    }

    /**
     * 表示方法的调用
     *
     * @param args 参数
     * @return 结果
     */
    @Override
    public Object invoke(Object[] args) {
        Map<String, Object> map = new HashMap<>();
        //处理Lambda表达式
        if (this.expression instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) this.expression;
            if (lambdaExpression.getParameters() != null && lambdaExpression.getParameters().length > 0)
                for (ParameterExpression parameter : lambdaExpression.getParameters()) {
                    //参数分为两种 一种是Host参数 一种是普通参数
                    //Host参数就是Lambda表达式的主体代表的参数
                    if (parameter.getIsHost()) {
                        map.put("instance", args[0]);
                        map.put(parameter.getName(), args[parameter.getIndex()]);
                    } else {
                        map.put(parameter.getName(), parameter.getObj());
                    }
                }
        }
        //用默认的参数提取器计算结果
        return this.expression.calculate(new ArgumentGetter(map));
    }

    /**
     * 重写Function的方法
     *
     * @param o the function argument
     * @return the function result
     */
    @Override
    public Object apply(Object o) {
        return this.invoke(new Object[]{o});
    }

    /**
     * 重写Predicate的方法
     *
     * @param o the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(Object o) {
        return (boolean) this.invoke(new Object[]{o});
    }
}
