/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员表达式提取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:21:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.expression.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 成员表达式提取器
 */
public class MemberExpressionExtractor extends ExpressionVisitor {

    /**
     * 子树求值器
     */
    private final SubTreeEvaluator subTreeEvaluator;

    /**
     * 提取出的成员表达式
     */
    private List<MemberExpression> memberExpressions;

    /**
     * 创建MemberExpressionExtractor实例
     *
     * @param subTreeEvaluator 子树求值器
     */
    public MemberExpressionExtractor(SubTreeEvaluator subTreeEvaluator) {
        this.subTreeEvaluator = subTreeEvaluator;
    }

    /**
     * 获取提取出的成员表达式
     *
     * @return 提取出的成员表达式
     */
    public MemberExpression[] getMemberExpressions() {
        return this.memberExpressions.toArray(new MemberExpression[0]);
    }

    /**
     * 要指定表达式中提取成员表达式
     *
     * @param expression 要从中提取成员表达式的表达式
     * @return 抽取结果
     */
    @Deprecated
    public List<MemberExpression> extractMember(Expression expression) {
        Expression exp = this.subTreeEvaluator.evaluate(expression);
        this.memberExpressions = new ArrayList<>();
        List<MemberExpression> temp = new ArrayList<>();

        switch (exp.getExpressionType()) {
            case MemberAccess:
                temp.add((MemberExpression) exp);
                break;
            case UnaryPlus:
                if (exp instanceof UnaryExpression) {
                    temp.addAll(this.extractMember(((UnaryExpression) exp).getOperand()));
                }
                break;
            case Lambda:
                if (exp instanceof LambdaExpression) {
                    temp.addAll(this.extractMember(((LambdaExpression) exp).getBody()));
                }
                break;
            case Call: {
                if (exp instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = (MethodCallExpression) exp;
                    if (methodCallExpression.getMethod().getName().equalsIgnoreCase("contains")) {
                        if (methodCallExpression.getObject() instanceof MemberExpression)
                            temp.addAll(this.extractMember(methodCallExpression.getObject()));
                        if (methodCallExpression.getArgument()[0] instanceof MemberExpression)
                            temp.addAll(this.extractMember(methodCallExpression.getArgument()[0]));
                    } else {
                        temp.addAll(this.extractMember(methodCallExpression.getObject()));
                    }
                }
                break;
            }
            case New:
                if (exp instanceof NewExpression) {
                    NewExpression newEx = (NewExpression) exp;
                    for (Expression item : newEx.getArgument()) {
                        temp.addAll(this.extractMember(item));
                    }
                    break;
                }
                break;
            case Not: {
                if (exp instanceof UnaryExpression) {
                    temp.addAll(this.extractMember(((UnaryExpression) exp).getOperand()));
                }
                break;
            }
            default:
                if (exp instanceof BinaryExpression) {
                    BinaryExpression binary = (BinaryExpression) exp;
                    temp.addAll(this.extractMember(binary.getLeft()));
                    temp.addAll(this.extractMember(binary.getRight()));
                }
                break;
        }
        this.memberExpressions.addAll(temp);
        return this.memberExpressions;
    }
}
