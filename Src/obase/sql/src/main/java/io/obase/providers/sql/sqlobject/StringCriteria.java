/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字符串条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:41:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 字符串条件
 */
public class StringCriteria extends SimpleCriteria<String> {

    /**
     * 创建简单字符串条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public StringCriteria(String field, ERelationOperator relationOperator, String value) {
        super(field, relationOperator, value);
    }

    /**
     * 创建简单字符串条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public StringCriteria(String source, String field, ERelationOperator relationOperator, String value) {
        super(source, field, relationOperator, value);
    }
}
