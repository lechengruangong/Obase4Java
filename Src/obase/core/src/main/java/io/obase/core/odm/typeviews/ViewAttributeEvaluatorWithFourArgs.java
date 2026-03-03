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
 * 有四个源的视图属性的求值器
 *
 * @param <TAgent1> 第一个源的代理属性的类型
 * @param <TAgent2> 第二个源的代理属性的类型
 * @param <TAgent3> 第三个源的代理属性的类型
 * @param <TAgent4> 第四个源的代理属性的类型
 */
public class ViewAttributeEvaluatorWithFourArgs<TAgent1, TAgent2, TAgent3, TAgent4> extends ViewAttributeEvaluator {

    /**
     * 表达式
     */
    private final LambdaExpression lambdaExpression;

    /**
     * 构造有四个源的视图属性的求值器
     *
     * @param lambdaExpression 表达式
     */
    public ViewAttributeEvaluatorWithFourArgs(LambdaExpression lambdaExpression) {

        this.lambdaExpression = lambdaExpression;
    }

    /**
     * 根据属性源代理的值计算视图属性的值。
     *
     * @param agentValues 属性源代理的值构成的序列
     * @return 计算结果
     */
    @Override
    public Object evaluate(Object[] agentValues) {
        return this.lambdaExpression.compile().invoke(new Object[]{agentValues[0], agentValues[1], agentValues[2], agentValues[3]});
    }
}
