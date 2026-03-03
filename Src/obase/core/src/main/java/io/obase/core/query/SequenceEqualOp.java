/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示序列相等运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 14:12:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import java.util.Comparator;

/**
 * 序列相等操作
 */
public class SequenceEqualOp extends QueryOp {

    /**
     * 相等比较器，用于测试来自两个序列的元素是否相等
     */
    private final Comparator<?> comparer;

    /**
     * 参与比较的另一序列
     */
    private final Iterable<?> other;

    /**
     * 创建SequenceEqualOp实例
     *
     * @param other    参与比较的另一序列
     * @param comparer 相等比较器，用于测试来自两个序列的元素是否相等
     */
    SequenceEqualOp(Iterable<?> other, Comparator<?> comparer, Class<?> sourceType) {
        super(EQueryOpName.SequenceEqual, sourceType);

        this.other = other;
        this.comparer = comparer;
    }

    /**
     * 获取相等比较器，该比较器用于测试来自两个序列的元素是否相等
     *
     * @return 相等比较器
     */
    public Comparator<?> getComparer() {
        return this.comparer;
    }

    /**
     * 获取参与比较的另一序列
     *
     * @return 参与比较的另一序列
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
