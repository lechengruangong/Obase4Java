/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：以查询结果集作为值域的IN运算条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:09:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.ELogicalOperator;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 以查询结果集作为值域的IN运算（广义）表示的条件
 */
public class InSelectCriteria implements ICriteria {

    /**
     * 作为IN运算（广义）左操作数的表达式
     */
    private final Expression left;

    /**
     * 一个查询表达式，其查询结果将作为IN运算（广义）的值域
     */
    private final QuerySql valueSetSql;

    /**
     * 广义IN运算符
     */
    private EInOperator operator = EInOperator.IN;

    /**
     * 创建InSelectCriteria的实例，指定IN运算的左操作数和生成值域的查询Sql语句。默认运算符为IN
     *
     * @param expression 表达式
     * @param valueSet   子查询
     */
    public InSelectCriteria(Expression expression, QuerySql valueSet) {
        this.valueSetSql = valueSet;
        this.left = expression;
    }

    /**
     * 创建InSelectCriteria的实例，指定作为IN运算左操作数的字段和生成值域的查询Sql语句。默认运算符为IN
     *
     * @param field    字段
     * @param valueSet 子查询
     */
    public InSelectCriteria(Field field, QuerySql valueSet) {
        this(Expression.field(field), valueSet);
    }

    /**
     * 创建InSelectCriteria的实例，指定作为IN运算左操作数的字段的名称，同时指定生成值域的查询Sql语句。默认运算符为IN
     *
     * @param field    字段
     * @param valueSet 子查询
     */
    public InSelectCriteria(String field, QuerySql valueSet) {
        this(new Field(field), valueSet);
    }

    /**
     * 创建InSelectCriteria的实例，指定作为IN运算左操作数的字段的名称及其所在的源的名称，同时指定生成值域的查询Sql语句。默认运算符为IN。
     *
     * @param field    字段
     * @param source   源
     * @param valueSet 子查询
     */
    public InSelectCriteria(String field, String source, QuerySql valueSet) {
        this(new Field(source, field), valueSet);
    }

    /**
     * 创建InSelectCriteria的实例，指定作为IN运算左操作数的字段的名称及其所在的源，同时指定生成值域的查询Sql语句。默认运算符为IN。
     *
     * @param field    字段
     * @param source   源
     * @param valueSet 子查询
     */
    public InSelectCriteria(String field, ISource source, QuerySql valueSet) {
        this(new Field((MonomerSource) source, field), valueSet);
    }

    /**
     * 获取广义IN运算符。默认值为IN
     *
     * @return 广义IN运算符
     */
    public EInOperator getOperator() {
        return this.operator;
    }

    /**
     * 设置广义IN运算符
     *
     * @param operator 广义IN运算符
     */
    public void setOperator(EInOperator operator) {
        this.operator = operator;
    }

    /**
     * 获取IN运算（广义）的左操作数
     *
     * @return 左操作数
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * 获取生成IN运算（广义）值域的查询Sql语句
     *
     * @return 查询Sql语句
     */
    public QuerySql getValueSetSql() {
        return this.valueSetSql;
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
        this.left.accept(visitor);
    }

    /**
     * 针对指定的数据源类型，生成条件实例的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        switch (this.getOperator()) {
            case IN:
                return " " + this.getLeft().toString(sourceType) + " IN (" + this.getValueSetSql().toSql(sourceType) + ")";
            case NOTIN:
                return " " + this.getLeft().toString(sourceType) + " NOT IN (" + this.getValueSetSql().toSql(sourceType) + ")";
            default:
                throw new IllegalArgumentException("未知的IN类型: " + this.getOperator());
        }
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

        //每个部分的参数集合
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = new ObjectReferencePack<>();
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = new ObjectReferencePack<>();
        leftSqlParameter.realValue = new ArrayList<>();
        rightSqlParameter.realValue = new ArrayList<>();

        //字符串
        String resultStr;
        switch (this.getOperator()) {
            case IN:
                resultStr = " " + this.getLeft().toString(sourceType, leftSqlParameter, creator) + " IN (" + this.getValueSetSql().toSql(sourceType, rightSqlParameter, creator) + ")";
                break;
            case NOTIN:
                resultStr = " " + this.getLeft().toString(sourceType, leftSqlParameter, creator) + " NOT IN (" + this.getValueSetSql().toSql(sourceType, rightSqlParameter, creator) + ")";
                break;
            default:
                throw new IllegalArgumentException("未知的IN类型: " + this.getOperator());
        }

        //最终的集合
        sqlParameters.realValue = new ArrayList<>();
        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
        sqlParameters.realValue.addAll(rightSqlParameter.realValue);

        DataParameterSorter.sort(sqlParameters.realValue);

        return resultStr;
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
