/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询源,如表.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:16:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 查询源，如表、视图等
 */
public interface ISource {

    /**
     * 获取一个值，该值指示源是否支持排序冒泡
     *
     * @return 是否支持排序冒泡
     */
    boolean getCanBubbleOrder();

    /**
     * 将当前源与另一源执行左连接运算，得出一个新源
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 左连接后的源
     */
    ISource leftJoin(ISource other, ICriteria criteria);

    /**
     * 将当前源与另一源执行右连接运算，得出一个新源
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 右连接后的源
     */
    ISource rightJoin(ISource other, ICriteria criteria);

    /**
     * 将当前源与另一源内执行连接运算，得出一个新源。
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 内连接后的源
     */
    ISource innerJoin(ISource other, ICriteria criteria);

    /**
     * 将当前查询源的排序规则提升为指定查询的排序规则
     *
     * @param query 指定的查询
     */
    void bubbleOrder(QuerySql query);

    /**
     * 针对指定的数据源类型，生成数据源实例的字符串表示形式，该字符串可用于From子句、Update子句和Insert Into子句。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    String toString(EDataSource sourceType);

    /**
     * 使用参数化的方式 默认的用途 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);
}
