/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：子树求值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:17:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.expression.EExpressionType;
import io.obase.core.expression.Expression;
import io.obase.core.expression.ExpressionVisitor;
import io.obase.core.expression.LambdaExpression;

import java.util.HashSet;
import java.util.Set;

/**
 * 子树求值器，用于对表达式中可求值的子树进行计算求值，以简化表达式
 */
public class SubTreeEvaluator {

    /**
     * 可求值子树的候选集，构成嵌套关系的两个子树可共存于候选集
     */
    private final Set<Expression> candidates;

    /**
     * 构造SubTreeEvaluator的 新实例
     *
     * @param wholeTree 将要对其各级子树尝试求值的整棵表达式树
     */
    public SubTreeEvaluator(Expression wholeTree) {
        this.candidates = new Nominator().nominate(wholeTree);
    }

    /**
     * 尝试对指定的表达式求值。如果该表达式可求值则计算其值，并返回以其结果为值的常量表达式，否则返回表达式本身。
     * 建议调用方从表达式树根节点开始沿叶子方向逐级尝试求值，这样可做到对构成父子关系的多个表达式一次性完成求值。
     *
     * @param subTree 要尝试求值的子树
     * @return 求得的值
     */
    public Expression evaluate(Expression subTree) {
        if (subTree.getExpressionType() == EExpressionType.Constant) return subTree;

        if (!this.candidates.contains(subTree)) return subTree;

        LambdaExpression lambdaExpression = Expression.lambda(null, subTree);
        Object obj = lambdaExpression.compile().invoke(new Object[0]);
        return Expression.constant(obj);
    }

    /**
     * 可求值表达式提取者，负责对指定表达式树的各级子树进行评估，将可求值子树提名为候选者
     */
    private static class Nominator extends ExpressionVisitor {
        /**
         * 提名
         */
        private Set<Expression> candidates;

        /**
         * 是否已计算
         */
        private boolean cannotBeEvaluated;

        /**
         * 访问表达式树方法
         *
         * @param expression 表达式树节点
         * @return 访问结果
         */
        @Override
        public Expression visit(Expression expression) {
            if (expression != null) {
                boolean savedCannotBeEvaluated = this.cannotBeEvaluated;
                this.cannotBeEvaluated = false;
                super.visit(expression);
                if (this.cannotBeEvaluated) {
                    if (expression.getExpressionType() == EExpressionType.Quote) {
                        this.candidates.add(expression);
                        this.cannotBeEvaluated = false;
                    }
                } else {
                    if (expression.getExpressionType() != EExpressionType.Parameter) {
                        this.candidates.add(expression);
                        this.cannotBeEvaluated = false;
                    } else {
                        this.cannotBeEvaluated = true;
                    }
                }

                this.cannotBeEvaluated |= savedCannotBeEvaluated;
            }

            return expression;
        }

        /**
         * 从指定的表达式树中提名可求值子树。注：对构造父子关系的多个表达式分别提名
         *
         * @param wholeTree 对其子树进行提名的整棵表达式树
         * @return 子树值集合
         */
        public Set<Expression> nominate(Expression wholeTree) {
            this.candidates = new HashSet<>();
            this.visit(wholeTree);
            return this.candidates;
        }
    }
}
