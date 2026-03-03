/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：时间段条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:46:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import java.time.LocalTime;

/**
 * 时间条件
 */
public class TimeSpanCriteria extends SimpleCriteria<LocalTime> {
    /**
     * 创建简单条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param localTime        参考值
     */
    protected TimeSpanCriteria(String field, ERelationOperator relationOperator, LocalTime localTime) {
        super(field, relationOperator, localTime);
    }

    /**
     * 创建简单条件实例
     *
     * @param field            字段
     * @param relationOperator 关系运算符
     * @param localTime        参考值
     */
    protected TimeSpanCriteria(Field field, ERelationOperator relationOperator, LocalTime localTime) {
        super(field, relationOperator, localTime);
    }

    /**
     * 创建简单条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param localTime        参考值
     */
    protected TimeSpanCriteria(String source, String field, ERelationOperator relationOperator, LocalTime localTime) {
        super(source, field, relationOperator, localTime);
    }
}
