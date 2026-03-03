/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：以表达式表示的条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:15:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.ELogicalOperator;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 以表达式表示的条件
 */
public class ExpressionCriteria implements ICriteria {

    /**
     * 表示条件的表达式
     */
    protected Expression expression;

    /**
     * 使用指定的布尔表达式创建ExpressionCriteria的实例
     *
     * @param expression 一个表示条件的布尔表达式
     */
    public ExpressionCriteria(Expression expression) {
        this.expression = expression;
    }

    /**
     * 获取表示条件的表达式
     *
     * @return 条件的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 将当前条件与另一条件执行逻辑与运算，得出一个新条件
     *
     * @param other 另一个条件
     * @return 逻辑与后得到的条件
     */
    @Override
    public ICriteria and(ICriteria other) {
        if (other == null)
            return this;
        return new ComplexCriteria(this, other, ELogicalOperator.And);
    }

    /**
     * 将当前条件与另一条件执行逻辑或运算，得出一个新条件
     *
     * @param other 另一个条件
     * @return 逻辑或后得到的条件
     */
    @Override
    public ICriteria or(ICriteria other) {
        if (other == null)
            return this;
        return new ComplexCriteria(this, other, ELogicalOperator.Or);
    }

    /**
     * 对当前条件执行逻辑非运算，得出一个新条件
     *
     * @return 逻辑非后得到的条件
     */
    @Override
    public ICriteria not() {
        //如果是In操作 则翻转为NotIn
        if (this.expression instanceof InExpression) {
            InExpression inExpression = (InExpression) this.expression;
            inExpression.flipOverOperator();
            return this;
        }

        return new ComplexCriteria(this, null, ELogicalOperator.Not);
    }

    /**
     * 将表达式访问者引导至条件内部的表达式
     * 特别约定：仅引导至直接包含的表达式，规避通过其它对象间接包含的表达式（如InSelectCriteria中作为值域的子查询所包含的表达式）。
     *
     * @param visitor 要引导的表达式访问者
     */
    @Override
    public void guideExpressionVisitor(ExpressionVisitor visitor) {
        this.getExpression().accept(visitor);
    }

    /**
     * 针对指定的数据源类型，生成条件实例的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        if (sourceType == EDataSource.SqlServer) {
            if (this.getExpression() instanceof FieldExpression) {
                Expression exp = Expression.equal(this.getExpression(), new ConstantExpression(true));
                return exp.toString(sourceType);
            }

            if (this.getExpression() instanceof ConstantExpression) {
                ConstantExpression constantExpression = (ConstantExpression) this.getExpression();
                if (constantExpression.getValue() instanceof Boolean) {
                    return "1=1";
                }
            }

        }
        return this.getExpression().toString(sourceType);
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        if (sourceType == EDataSource.SqlServer) {
            if (this.getExpression() instanceof FieldExpression) {
                Expression exp = Expression.equal(this.getExpression(), new ConstantExpression(true));
                return exp.toString(sourceType, sqlParameters, creator);
            }

            if (this.getExpression() instanceof ConstantExpression) {
                ConstantExpression constantExpression = (ConstantExpression) this.getExpression();
                if (constantExpression.getValue() instanceof Boolean) {
                    sqlParameters.realValue = new ArrayList<>();
                    return "1=1";
                }
            }

        }

        return this.getExpression().toString(sourceType, sqlParameters, creator);
    }

    /**
     * 使用默认数据源和参数化的方式将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        return this.toString(EDataSource.SqlServer, sqlParameters, creator);
    }
}
