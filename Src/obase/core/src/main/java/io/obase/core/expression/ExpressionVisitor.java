/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式访问器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 16:32:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 抽象的表达式访问器 用于访问表达式树
 */
public abstract class ExpressionVisitor {

    /**
     * 访问表达式树方法
     *
     * @param node 表达式树节点
     * @return 访问结果
     */
    public Expression visit(Expression node) {

        if (node == null)
            return null;
        return node.accept(this);
    }

    /**
     * 默认的访问二元表达式
     * 先访问左端 然后访问右端 最后返回右端的访问结果
     *
     * @param binaryExpression 二元表达式
     * @return 二元表达式的右端
     */
    protected Expression visitBinary(BinaryExpression binaryExpression) {
        //默认的访问逻辑
        this.visit(binaryExpression.getLeft());
        this.visit(binaryExpression.getRight());
        return this.visit(binaryExpression.getRight());
    }

    /**
     * 默认的访问常量表达式
     * 直接返回自身
     *
     * @param constantExpression 常量表达式
     * @return 常量表达式自身
     */
    protected Expression visitConstant(ConstantExpression constantExpression) {
        return constantExpression;
    }

    /**
     * 默认的访问Lambda表达式
     * 先访问Body 然后挨个访问Parameter
     * 最后返回自身
     *
     * @param lambdaExpression Lambda表达式
     * @return 自身
     */
    protected Expression visitLambda(LambdaExpression lambdaExpression) {
        //默认的访问逻辑
        this.visit(lambdaExpression.getBody());
        if (lambdaExpression.getParameters() != null && lambdaExpression.getParameters().length > 0) {
            ParameterExpression[] parameterExpressions = lambdaExpression.getParameters();
            for (ParameterExpression parameterExpression : parameterExpressions) {
                this.visit(parameterExpression);
            }
        }
        return lambdaExpression;
    }

    /**
     * 默认的访问参数表达式
     * 直接返回自身
     *
     * @param parameterExpression 参数表达式
     * @return 常量表达式自身
     */
    protected Expression visitParameter(ParameterExpression parameterExpression) {
        //默认的访问逻辑
        return parameterExpression;
    }

    /**
     * 默认的访问成员表达式
     * 访问成员表达式的Expression 而后返回自身
     *
     * @param memberExpression 成员表达式
     * @return 成员表达式自身
     */
    protected Expression visitMember(MemberExpression memberExpression) {
        //默认的访问逻辑
        this.visit(memberExpression.getExpression());
        return memberExpression;
    }

    /**
     * 默认的访问新建表达式
     * 先访问Argument 然后返回自身
     *
     * @param newExpression 新建表达式
     * @return 新建表达式自身
     */
    protected Expression visitNew(NewExpression newExpression) {
        //默认的访问逻辑
        if (newExpression.getArgument() != null && newExpression.getArgument().length > 0) {
            Expression[] expressions = newExpression.getArgument();
            for (Expression expression : expressions) {
                this.visit(expression);
            }
        }
        return newExpression;
    }

    /**
     * 默认的访问方法调用表达式
     * 先访问Object然后挨个访问Argument
     * 最后返回自身
     *
     * @param methodCallExpression Lambda表达式
     * @return 自身
     */
    protected Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        //默认的访问逻辑
        this.visit(methodCallExpression.getObject());
        if (methodCallExpression.getArgument() != null && methodCallExpression.getArgument().length > 0) {
            Expression[] expressions = methodCallExpression.getArgument();
            for (Expression expression : expressions) {
                this.visit(expression);
            }
        }
        return methodCallExpression;
    }

    /**
     * 默认访问一元表达式
     * 先访问Operand 先后返回自身
     *
     * @param unaryExpression 一元表达式
     * @return 一元表达式自身
     */
    protected Expression visitUnary(UnaryExpression unaryExpression) {
        this.visit(unaryExpression.getOperand());
        return unaryExpression;
    }
}
