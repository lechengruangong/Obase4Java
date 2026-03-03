/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：条件表达式解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 12:12:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.ELogicalOperator;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.*;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.sqlobject.ComplexCriteria;
import io.obase.providers.sql.sqlobject.ExpressionCriteria;
import io.obase.providers.sql.sqlobject.ICriteria;

import java.util.Arrays;

/**
 * 条件表达式解析器
 */
public class CriteriaExpressionParser extends ExpressionVisitor {

    /**
     * 数据对象模型
     */
    private final ObjectDataModel model;

    /**
     * 形参绑定
     */
    private final ParameterBinding[] parameterBindings;

    /**
     * 子树求值器
     */
    private final SubTreeEvaluator subTreeEvaluator;

    /**
     * 数据源类型
     */
    private final EDataSource targetSource;

    /**
     * 表达式翻译器
     */
    private final ExpressionTranslator translator;

    /**
     * 条件
     */
    private ICriteria criteria;

    /**
     * 构造CriteriaExpressionParser的新实例
     *
     * @param model             对象数据模型
     * @param subTreeEvaluator  子树求值器
     * @param targetSource      目标源类型
     * @param parameterBindings 形参绑定
     */
    public CriteriaExpressionParser(ObjectDataModel model, SubTreeEvaluator subTreeEvaluator,
                                    EDataSource targetSource, ParameterBinding[] parameterBindings) {
        this.model = model;
        this.subTreeEvaluator = subTreeEvaluator;
        this.targetSource = targetSource;
        this.parameterBindings = parameterBindings;
        this.translator = new ExpressionTranslator(model, subTreeEvaluator, null);
    }

    /**
     * 默认节点的翻译操作
     *
     * @param node 表达式
     */
    private void defaultTranslate(Expression node) {
        io.obase.providers.sql.sqlobject.Expression sqlExp = this.translator.translate(node);
        this.criteria = new ExpressionCriteria(sqlExp);
    }

    /**
     * 解析指定的条件表达式
     *
     * @param expression 表达式
     * @return 条件
     */
    public ICriteria parse(Expression expression) {
        expression = this.subTreeEvaluator.evaluate(expression);
        this.visit(expression);
        return this.criteria;
    }

    /**
     * 默认的访问Lambda表达式
     * 先访问Body 然后挨个访问Parameter
     * 最后返回自身
     *
     * @param lambdaExpression Lambda表达式
     * @return 自身
     */
    @Override
    protected Expression visitLambda(LambdaExpression lambdaExpression) {
        Expression body = this.subTreeEvaluator.evaluate(lambdaExpression.getBody());
        this.visit(body);
        this.defaultTranslate(lambdaExpression);
        return lambdaExpression;
    }

    /**
     * 默认的访问成员表达式
     * 访问成员表达式的Expression 而后返回自身
     *
     * @param memberExpression 成员表达式
     * @return 成员表达式自身
     */
    @Override
    protected Expression visitMember(MemberExpression memberExpression) {
        TypeBase hostType = null;
        String memberName = null;
        //如果是关联对象访问
        if (memberExpression.getExpression() instanceof MemberExpression) {
            //获取成员所属类型
            MemberExpression member = (MemberExpression) memberExpression.getExpression();
            hostType = this.model.getTypeOrNull(member.getHostType());
            memberName = member.getMemberName();
        }

        if (hostType != null) {
            //是否为实体型
            if (hostType instanceof EntityType) {
                EntityType entityType = (EntityType) hostType;
                AssociationReference assRef = entityType.getAssociationReference(memberName);
                if (assRef != null) {
                    this.criteria = new ExpressionCriteria(this.translator.translate(memberExpression));
                    return memberExpression;
                }
            }
            //是否为关联型
            if (hostType instanceof AssociationType) {
                AssociationType associationType = (AssociationType) hostType;
                AssociationEnd assEnd = associationType.getAssociationEnd(memberName);
                if (assEnd != null) {
                    this.criteria = new ExpressionCriteria(this.translator.translate(memberExpression));
                    return memberExpression;
                }
            }
        }

        //一般Member访问
        Expression exp = this.subTreeEvaluator.evaluate(memberExpression.getExpression());
        this.visit(exp);
        this.defaultTranslate(memberExpression);
        return memberExpression;
    }

    /**
     * 默认的访问常量表达式
     * 直接返回自身
     *
     * @param constantExpression 常量表达式
     * @return 常量表达式自身
     */
    @Override
    protected Expression visitConstant(ConstantExpression constantExpression) {
        this.defaultTranslate(constantExpression);
        return constantExpression;
    }

    /**
     * 默认的访问方法调用表达式
     * 先访问Object然后挨个访问Argument
     * 最后返回自身
     *
     * @param methodCallExpression Lambda表达式
     * @return 自身
     */
    @Override
    protected Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        Expression objectValue;
        if (methodCallExpression.getObject() == null) {
            objectValue = this.subTreeEvaluator.evaluate(methodCallExpression.getArgument()[0]);
        } else {
            objectValue = this.subTreeEvaluator.evaluate(methodCallExpression.getObject());
        }

        if (methodCallExpression.getMethod().getName().equals("Contains") && objectValue.getExpressionType() == EExpressionType.Constant
                && objectValue instanceof ConstantExpression) {
            throw new IllegalArgumentException("暂时无法处理此种Contains" + Arrays.toString(this.parameterBindings) + this.targetSource.toString());
        } else {
            this.criteria = new ExpressionCriteria(this.translator.translate(methodCallExpression));
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
    @Override
    protected Expression visitUnary(UnaryExpression unaryExpression) {
        Expression operand = this.subTreeEvaluator.evaluate(unaryExpression.getOperand());
        this.visit(operand);
        if (operand.getExpressionType() == EExpressionType.Not) {
            if (operand.getType().equals(Boolean.class)) {
                this.criteria = this.criteria.not();
            } else {
                ExpressionTranslator translator = new ExpressionTranslator(this.model, this.subTreeEvaluator, null);
                io.obase.providers.sql.sqlobject.Expression sqlExp = translator.translate(unaryExpression);
                this.criteria = new ExpressionCriteria(sqlExp);
            }
        } else if (operand.getExpressionType() == EExpressionType.Convert) {
            //处理转换操作
            //分为两种情况 一种是普通属性的基础类型转换 一种是继承体系中的类型转换
            TypeBase memberType = this.model.getTypeOrNull(unaryExpression.getType());
            //访问的属性是StructuralType 判断为继承体系中的类型转换
            if (memberType instanceof StructuralType && operand instanceof MemberExpression) {
                MemberExpression member = (MemberExpression) operand;
                //获取成员所属类型
                TypeBase hostType = this.model.getTypeOrNull(member.getHostType());
                //是否为实体型
                if (hostType instanceof EntityType) {
                    EntityType entityType = (EntityType) hostType;
                    AssociationReference assRef = entityType.getAssociationReference(member.getMemberName());
                    if (assRef != null)
                        //直接返回上层 由上层处理
                        return unaryExpression;
                }

                //是否为关联型
                if (hostType instanceof AssociationType) {
                    AssociationType associationType = (AssociationType) hostType;
                    AssociationEnd assEnd = associationType.getAssociationEnd(member.getMemberName());
                    if (assEnd != null)
                        //直接返回上层 由上层处理
                        return unaryExpression;
                }
            } else {
                //都不是
                this.visit(operand);
            }
        }

        return unaryExpression;
    }

    /**
     * 默认的访问二元表达式
     * 先访问左端 然后访问右端 最后返回右端的访问结果
     *
     * @param binaryExpression 二元表达式
     * @return 二元表达式的右端
     */
    @Override
    protected Expression visitBinary(BinaryExpression binaryExpression) {
        Expression left = this.subTreeEvaluator.evaluate(binaryExpression.getLeft());
        this.visit(left);
        ICriteria leftCriteria = this.criteria;
        Expression right = this.subTreeEvaluator.evaluate(binaryExpression.getRight());
        this.visit(right);
        ICriteria rightCriteria = this.criteria;
        switch (binaryExpression.getExpressionType()) {
            case AndAlso:
                this.criteria = new ComplexCriteria(leftCriteria, rightCriteria, ELogicalOperator.And);
                break;
            case OrElse:
                this.criteria = new ComplexCriteria(leftCriteria, rightCriteria, ELogicalOperator.Or);
                break;
            default:
                ExpressionTranslator translator = new ExpressionTranslator(this.model, this.subTreeEvaluator, null);
                io.obase.providers.sql.sqlobject.Expression sqlExp = translator.translate(binaryExpression);
                this.criteria = new ExpressionCriteria(sqlExp);
                break;
        }

        return binaryExpression;
    }
}
