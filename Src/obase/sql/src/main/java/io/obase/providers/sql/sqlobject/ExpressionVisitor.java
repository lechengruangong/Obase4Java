/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示针对表达式树的访问者.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 12:01:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 表示针对表达式树的访问者
 */
public abstract class ExpressionVisitor {

    /**
     * 将要访问的表达式调度到此类中更专用的访问方法之一
     *
     * @param expression 要访问的表达式
     * @return Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     */
    public Expression visit(Expression expression) {
        if (expression == null)
            return null;
        switch (expression.getNodeType()) {
            case Add:
            case Subtract:
            case Multiply:
            case Divide:
            case Power:
            case Equal:
            case NotEqual:
            case LessThan:
            case LessThanOrEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case Like:
            case In:
            case NotIn:
            case AndAlso:
            case BitAnd:
            case BitOr:
            case BitXor:
            case OrElse:
            case LeftShift:
            case RightShift:
                return this.visitBinary((BinaryExpression) expression);
            case BitNot:
            case Not:
                return this.visitNot((UnaryExpression) expression);
            case Function:
                return this.visitFunction((FunctionExpression) expression);
            case Constant:
                return this.visitConstant((ConstantExpression) expression);
            case Field:
                return this.visitField((FieldExpression) expression);
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + expression.getNodeType());
        }
    }

    /**
     * 访问常量表达式
     *
     * @param constant 要访问的表达式
     * @return Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     */
    protected Expression visitConstant(ConstantExpression constant) {
        return constant;
    }

    /**
     * 访问字段表达式
     *
     * @param field 要访问的表达式
     * @return Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     */
    protected Expression visitField(FieldExpression field) {
        return field;
    }

    /**
     * Expression
     *
     * @param not 要访问的表达式
     * @return Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     */
    protected Expression visitNot(UnaryExpression not) {
        Expression exp = this.visit(not.getOperand());
        if (exp != not.getOperand())
            return Expression.not(exp);
        return not;
    }

    /**
     * 访问函数表达式
     *
     * @param func Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     * @return 访问结果
     */
    protected Expression visitFunction(FunctionExpression func) {
        boolean isModify = false;
        Expression[] arguments = new Expression[func.getArguments().length];
        for (int i = 0; i < func.getArguments().length; i++) {
            Expression arg = this.visit(func.getArguments()[i]);
            if (arg != func.getArguments()[i])
                isModify = true;
            arguments[i] = arg;
        }

        if (isModify) return Expression.function(func.getFunctionName(), arguments);
        return func;
    }

    /**
     * 访问二元表达式
     *
     * @param binary 二元表达式
     * @return 访问结果
     */
    protected Expression visitBinary(BinaryExpression binary) {
        Expression left = this.visit(binary.getLeft());
        Expression right = this.visit(binary.getRight());
        if (left != binary.getLeft() || right != binary.getRight()) {
            switch (binary.getNodeType()) {
                case Add:
                    return Expression.add(left, right);
                case Subtract:
                    return Expression.subtract(left, right);
                case Multiply:
                    return Expression.multiply(left, right);
                case Divide:
                    return Expression.divide(left, right);
                case Power:
                    return Expression.power(left, right);
                case Equal:
                    return Expression.equal(left, right);
                case NotEqual:
                    return Expression.notEqual(left, right);
                case LessThan:
                    return Expression.lessThan(left, right);
                case LessThanOrEqual:
                    return Expression.lessThanOrEqual(left, right);
                case GreaterThan:
                    return Expression.greaterThan(left, right);
                case GreaterThanOrEqual:
                    return Expression.greaterThanOrEqual(left, right);
                case AndAlso:
                    return Expression.andAlso(left, right);
                case OrElse:
                    return Expression.orElse(left, right);
                case Like:
                    return Expression.like(left, ((LikeExpression) binary).getPattern());
                case In:
                    return Expression.in(left, ((InExpression) binary).getValueSet());
                default:
                    throw new IllegalArgumentException("未知的表达式类型: " + binary.getNodeType());
            }
        }

        return binary;
    }
}
