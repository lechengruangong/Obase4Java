/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：数值条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:39:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 数值条件
 *
 * @param <TNumeric>
 */
public class NumericCriteria<TNumeric> extends SimpleCriteria<TNumeric> {

    /**
     * 创建简单数值条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public NumericCriteria(String field, ERelationOperator relationOperator, TNumeric value) {
        super(field, relationOperator, value);
    }

    /**
     * 创建简单数值条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public NumericCriteria(String source, String field, ERelationOperator relationOperator, TNumeric value) {
        super(source, field, relationOperator, value);
    }
}
