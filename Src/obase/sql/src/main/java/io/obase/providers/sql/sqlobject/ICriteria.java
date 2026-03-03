/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示条件，如筛选条件、连接条件等.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:17:07
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 表示条件，如筛选条件、连接条件等
 */
public interface ICriteria {

    /**
     * 将当前条件与另一条件执行逻辑与运算，得出一个新条件
     *
     * @param other 另一个条件
     * @return 逻辑与后得到的条件
     */
    ICriteria and(ICriteria other);

    /**
     * 将当前条件与另一条件执行逻辑或运算，得出一个新条件
     *
     * @param other 另一个条件
     * @return 逻辑或后得到的条件
     */
    ICriteria or(ICriteria other);

    /**
     * 对当前条件执行逻辑非运算，得出一个新条件
     *
     * @return 逻辑非后得到的条件
     */
    ICriteria not();

    /**
     * 将表达式访问者引导至条件内部的表达式
     * 特别约定：仅引导至直接包含的表达式，规避通过其它对象间接包含的表达式（如InSelectCriteria中作为值域的子查询所包含的表达式）。
     *
     * @param visitor 要引导的表达式访问者
     */
    void guideExpressionVisitor(ExpressionVisitor visitor);

    /**
     * 针对指定的数据源类型，生成条件实例的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    String toString(EDataSource sourceType);

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);

    /**
     * 使用默认数据源和参数化的方式将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);
}
