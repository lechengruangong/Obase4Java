/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示复杂条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:15:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.ELogicalOperator;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示复杂条件，该条件由两个条件通过逻辑运算得出
 */
public class ComplexCriteria implements ICriteria {

    /**
     * 条件列表
     */
    private final List<ICriteria> criterias;

    /**
     * 逻辑运算符
     */
    private ELogicalOperator logicalOperator = ELogicalOperator.And;

    /**
     * 创建复杂条件实例，该条件由两个条件通过逻辑与运算得出
     *
     * @param criteria1 第一个条件
     * @param criteria2 第二个条件
     */
    public ComplexCriteria(ICriteria criteria1, ICriteria criteria2) {
        this.criterias = new ArrayList<>();
        this.criterias.addAll(Arrays.asList(criteria1, criteria2));
    }

    /**
     * 创建复杂条件实例
     *
     * @param criteria1       第一个条件
     * @param criteria2       第二个条件
     * @param logicalOperator 逻辑操作符
     */
    public ComplexCriteria(ICriteria criteria1, ICriteria criteria2, ELogicalOperator logicalOperator) {
        this.criterias = new ArrayList<>();

        //分别添加
        if (criteria1 != null) this.criterias.add(criteria1);

        if (criteria2 != null) this.criterias.add(criteria2);

        this.logicalOperator = logicalOperator;
    }

    /**
     * 获取设置逻辑运算符
     *
     * @return 逻辑运算符
     */
    public ELogicalOperator getLogicalOperator() {
        return this.logicalOperator;
    }

    /**
     * 设置逻辑运算符
     *
     * @param logicalOperator 逻辑运算符
     */
    public void setLogicalOperator(ELogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
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
        this.criterias.forEach(c -> c.guideExpressionVisitor(visitor));
    }

    /**
     * 针对指定的数据源类型，生成条件实例的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        //此处的优化全部取消
        String logical = "";
        //Not 直接取反条件中的第一个
        if (this.logicalOperator == ELogicalOperator.Not) {
            if (this.criterias.size() > 1)
                throw new IllegalArgumentException("取反操作数不可大于1.");
            return " ( not ( " + this.criterias.get(0).toString(sourceType) + " ) )";
        }

        //不是Not 构造操作符
        switch (this.logicalOperator) {
            case And:
                logical = " and ";
                break;
            case Or:
                logical = " or ";
                break;
        }

        return "(" + this.criterias.stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(logical)) + ")";
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
        //此处的优化全部取消
        String logical = "";
        //Not 直接取反条件中的第一个
        if (this.logicalOperator == ELogicalOperator.Not) {
            if (this.criterias.size() > 1)
                throw new IllegalArgumentException("取反操作数不可大于1.");
            return " ( not ( " + this.criterias.get(0).toString(sourceType, sqlParameters, creator) + " ) )";
        }

        //不是Not 构造操作符
        switch (this.logicalOperator) {
            case And:
                logical = " and ";
                break;
            case Or:
                logical = " or ";
                break;
        }

        //最终的集合
        sqlParameters.realValue = new ArrayList<>();

        //每个条件都ToString
        List<String> resultStrList = new ArrayList<>();
        for (ICriteria criteria : this.criterias) {
            ObjectReferencePack<List<DataParameter>> parameters = new ObjectReferencePack<>();
            resultStrList.add("(" + criteria.toString(sourceType, parameters, creator) + ")");
            sqlParameters.realValue.addAll(parameters.realValue);
        }

        DataParameterSorter.sort(sqlParameters.realValue);

        return "(" + String.join(logical, resultStrList) + ")";
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
