/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：布尔条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:47:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 布尔条件
 */
public class BoolCriteria extends SimpleCriteria<Boolean> {

    /**
     * 创建简单布尔条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public BoolCriteria(String field, ERelationOperator relationOperator, boolean value) {
        super(field, relationOperator, value);
    }

    /**
     * 创建简单布尔条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    public BoolCriteria(String source, String field, ERelationOperator relationOperator, boolean value) {
        super(source, field, relationOperator, value);
    }
}
