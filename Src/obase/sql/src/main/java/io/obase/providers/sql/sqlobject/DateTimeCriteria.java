/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：日期时间条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:23:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import java.time.LocalDateTime;

/**
 * 日期时间条件
 */
public class DateTimeCriteria extends SimpleCriteria<LocalDateTime> {

    /**
     * 创建简单日期时间条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public DateTimeCriteria(String field, ERelationOperator relationOperator, LocalDateTime value) {
        super(field, relationOperator, value);
    }

    /**
     * 创建简单日期时间条件实例
     *
     * @param source           源
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public DateTimeCriteria(String source, String field, ERelationOperator relationOperator, LocalDateTime value) {
        super(source, field, relationOperator, value);
    }
}
