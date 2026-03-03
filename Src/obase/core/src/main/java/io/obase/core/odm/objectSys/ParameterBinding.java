/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：形参绑定.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:10:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.expression.Expression;
import io.obase.core.expression.ParameterExpression;

/**
 * 形参绑定
 */
public class ParameterBinding {

    /**
     * 作为形参值的表达式
     */
    private final Expression expression;

    /**
     * lambda表达式的形式参数
     */
    private final ParameterExpression parameter;

    /**
     * 形参指代，表明该形参指代的内容，如查询源中的单个对象、查询源序列等。
     */
    private final EParameterReferring referring;

    /**
     * 创建ParameterBinding实例
     *
     * @param parameter  形参
     * @param referring  形参指代
     * @param expression 作为形参取值的表达式
     */
    public ParameterBinding(ParameterExpression parameter, EParameterReferring referring,
                            Expression expression) {
        this.parameter = parameter;
        this.expression = expression;
        this.referring = referring;
    }

    /**
     * 创建ParameterBinding实例，在该绑定中，形式参数指代查询源中的单个对象或值。
     *
     * @param parameter  形参
     * @param expression 作为形参取值的表达式
     */
    public ParameterBinding(ParameterExpression parameter, Expression expression) {
        this(parameter, EParameterReferring.Single, expression);
    }

    /**
     * 获取作为绑定目标的表达式
     *
     * @return 获取作为绑定目标的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 获取该绑定的形式参数
     *
     * @return 获取该绑定的形式参数
     */
    public ParameterExpression getParameter() {
        return this.parameter;
    }

    /**
     * 获取形参指代
     *
     * @return 获取形参指代
     */
    public EParameterReferring getReferring() {
        return this.referring;
    }
}
