/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字符条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:20:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 字符条件
 */
public class CharCriteria extends SimpleCriteria<Character> {

    /**
     * 创建简单字符条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public CharCriteria(String field, ERelationOperator relationOperator, char value) {
        super(field, relationOperator, value);
    }

    /**
     * 创建简单字符条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public CharCriteria(String source, String field, ERelationOperator relationOperator, char value) {
        super(source, field, relationOperator, value);
    }
}
