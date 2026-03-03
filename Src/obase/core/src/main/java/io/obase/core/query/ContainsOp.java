/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Contains运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:51:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import java.util.Comparator;

/**
 * 表示Contains运算
 */
public class ContainsOp extends QueryOp {

    /**
     * 相等比较器，用于测试序列中的元素与要查找的元素是否相等
     */
    private final Comparator<?> comparer;

    /**
     * 要在序列中查找的元素
     */
    private final Object item;

    /**
     * 创建ContainsOp实例
     *
     * @param item     要在序列中查找的元素
     * @param comparer 相等比较器，用于测试序列中的元素与要查找的元素是否相等
     */
    ContainsOp(Object item, Comparator<?> comparer, Class<?> sourceType) {
        super(EQueryOpName.Contains, sourceType);
        this.comparer = comparer;
        this.item = item;
    }

    /**
     * 获取相等比较器，该比较器用于测试序列中的元素与要查找的元素是否相等
     *
     * @return 相等比较器
     */
    public Comparator<?> getComparer() {
        return this.comparer;
    }

    /**
     * 获取要在序列中查找的元素
     *
     * @return 要在序列中查找的元素
     */
    public Object getItem() {
        return this.item;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return boolean.class;
    }
}
