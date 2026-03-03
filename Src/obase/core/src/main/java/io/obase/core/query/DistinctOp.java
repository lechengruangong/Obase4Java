/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Distinct运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:58:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import java.util.Comparator;

/**
 * 表示Distinct运算
 */
public class DistinctOp extends QueryOp {

    /**
     * 相等比较器，用于测试两个元素是否相等
     */
    private final Comparator<?> comparer;

    /**
     * 创建DistinctOp实例
     *
     * @param sourceType 查询源类型
     * @param comparer   相等比较器，用于测试两个元素是否相等
     */
    DistinctOp(Class<?> sourceType, Comparator<?> comparer) {
        super(EQueryOpName.Distinct, sourceType);
        this.comparer = comparer;
    }

    /**
     * 获取相等比较器，该比较器用于测试两个元素是否相等
     *
     * @return 相等比较器
     */
    public Comparator<?> getComparer() {
        return this.comparer;
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
