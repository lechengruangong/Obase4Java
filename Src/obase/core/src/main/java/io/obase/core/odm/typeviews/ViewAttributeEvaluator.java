/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图属性求值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 16:58:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.core.expression.LambdaExpression;

/**
 * 为视图属性求值器提供基础实现。
 * 视图属性求值器基于代理表达式计算属性的值。
 * 代理表达式是基于属性源代理计算属性值的表达式。
 */
public abstract class ViewAttributeEvaluator {


    /**
     * 使用代表执行代理表达式的方法的委托创建视图属性求值器
     *
     * @param expression 表达式
     * @return 视图属性求值器
     */
    public static ViewAttributeEvaluator create(LambdaExpression expression) {
        switch (expression.getParameters().length) {
            case 1:
                return new ViewAttributeEvaluatorWithOneArg<>(expression);
            case 2:
                return new ViewAttributeEvaluatorWithTwoArgs<>(expression);
            case 3:
                return new ViewAttributeEvaluatorWithThreeArgs<>(expression);
            case 4:
                return new ViewAttributeEvaluatorWithFourArgs<>(expression);
            default:
                throw new IllegalArgumentException("创建视图属性求值器失败。委托参数个数不支持。");
        }
    }

    /**
     * 根据属性源代理的值计算视图属性的值。
     *
     * @param agentValues 属性源代理的值构成的序列
     * @return 计算结果
     */
    public abstract Object evaluate(Object[] agentValues);
}
