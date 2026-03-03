/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为IN条件提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:34:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 为IN条件提供基础实现
 *
 * @param <TItem> 项类型
 */
public class InCriteria<TItem> extends SimpleCriteria<Iterable<TItem>> {

    /**
     * 关系运算符
     */
    private ERelationOperator operator;

    /**
     * 构造IN条件
     *
     * @param field            字段
     * @param relationOperator 关系操作符
     * @param value            集合
     */
    protected InCriteria(String field, ERelationOperator relationOperator, Iterable<TItem> value) {
        super(field, relationOperator, value);
    }

    /**
     * 构造IN条件
     *
     * @param source           源
     * @param field            字段
     * @param relationOperator 关系操作符
     * @param value            集合
     */
    protected InCriteria(String source, String field, ERelationOperator relationOperator, Iterable<TItem> value) {
        super(source, field, relationOperator, value);
    }

    /**
     * 获取关系运算符
     *
     * @return 关系运算符
     */
    @Override
    public ERelationOperator getOperator() {
        return this.operator;
    }

    /**
     * 设置关系运算符
     *
     * @param operator 关系运算符
     */
    @Override
    public void setOperator(ERelationOperator operator) {
        if (operator != ERelationOperator.In && operator != ERelationOperator.NotIn)
            throw new IllegalArgumentException("不支持的运算类型:" + operator);
        this.operator = operator;
    }
}
