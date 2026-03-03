/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型视图构造器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:26:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.expression.EExpressionType;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;

/**
 * 类型视图构造器工厂，根据视图表达式创建特定的构造器实例
 */
public class TypeViewBuilderFactory {

    /**
     * 创建类型视图构造器实例
     *
     * @param viewExp 视图表达式
     * @return 视图构造器实例
     */
    public ITypeViewBuilder create(Expression viewExp) {
        if (viewExp instanceof LambdaExpression) {
            LambdaExpression lambda = (LambdaExpression) viewExp;
            if (lambda.getBody().getExpressionType() == EExpressionType.New) {
                return new NewExpressionBasedBuilder();
            } else if (lambda.getBody().getExpressionType() == EExpressionType.MemberInit)
                return new MemberInitExpressionBasedBuilder();
        }

        throw new IllegalArgumentException("创建类型视图构造器实例时表达式不合法" + viewExp);
    }
}
