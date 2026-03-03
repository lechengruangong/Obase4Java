/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Set运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 14:19:32
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import java.util.Comparator;

/**
 * 表示Set运算
 */
public class SetOp extends QueryOp {

    /**
     * 相等比较器，用于测试来自于两个集合的元素是否相等
     */
    private final Comparator<?> comparator;

    /**
     * 集运算符
     */
    private final ESetOperator operator;

    /**
     * 参与运算的另一集合
     */
    private final Iterable<?> other;

    /**
     * 创建SetOp实例
     *
     * @param sourceType 查询源类型
     * @param operator   集运算符
     * @param other      参与运算的另一集合
     * @param comparer   相等比较器，用于测试来自于两个集合的元素是否相等
     */
    SetOp(Class<?> sourceType, ESetOperator operator, Iterable<?> other, Comparator<?> comparer) {
        super(EQueryOpName.Set, sourceType);
        this.comparator = comparer;
        this.operator = operator;
        this.other = other;
    }

    /**
     * 获取相等比较器，该比较器用于测试来自于两个集合的元素是否相等
     *
     * @return 相等比较器
     */
    public Comparator<?> getComparator() {
        return this.comparator;
    }

    /**
     * 获取集运算符
     *
     * @return 集运算符
     */
    public ESetOperator getOperator() {
        return this.operator;
    }

    /**
     * 获取参与运算的另一集合
     *
     * @return 参与运算的另一集合
     */
    public Iterable<?> getOther() {
        return this.other;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getSourceType();
    }
}
