/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序规则接口,描述集合中元素的顺序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:42:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;

import java.util.Comparator;

/**
 * 排序规则，描述集合中元素的顺序
 *
 * @param <T> 元素
 */
public class ItemOrder<T> {

    /**
     * 排序比较器
     */
    private final Comparator<T> comparator;

    /**
     * 指示是否使用降序
     */
    private final boolean descending;

    /**
     * 用于生成排序键的委托
     */
    private final FunctionWithOneArg<T, Object> keySelector;

    /**
     * 从序
     */
    private ItemOrder<T> subOrder;

    /**
     * 使用默认的比较器创建ItemOrder实例，并指示排序时是否使用降序
     *
     * @param descending 是否降序
     */
    public ItemOrder(boolean descending) {
        this.descending = descending;
        //默认的比较器 使用哈希码比较
        this.comparator = Comparator.comparingInt(Object::hashCode);
        this.keySelector = null;
        this.subOrder = null;
    }

    /**
     * 使用指定的比较器创建ItemOrder实例，并指示排序时是否使用降序
     *
     * @param comparator 比较器
     * @param descending 是否降序
     */
    public ItemOrder(Comparator<T> comparator, Boolean descending) {
        this.descending = descending;
        this.comparator = comparator;
        this.keySelector = null;
        this.subOrder = null;
    }

    /**
     * 使用指定的排序键和默认的比较器创建ItemOrder实例，并指示排序时是否使用降序
     *
     * @param keySelector 排序键
     * @param descending  是否降序
     */
    public ItemOrder(FunctionWithOneArg<T, Object> keySelector, Boolean descending) {
        this.descending = descending;
        this.comparator = null;
        this.keySelector = keySelector;
        this.subOrder = null;
    }

    /**
     * 获取排序键
     *
     * @return 排序键
     */
    public FunctionWithOneArg<T, Object> getKeySelector() {
        return this.keySelector;
    }

    /**
     * 获取排序比较器
     *
     * @return 排序比较器
     */
    public Comparator<T> getComparator() {
        return this.comparator;
    }

    /**
     * 获取一个值，该值指示是否使用降序
     *
     * @return 是否使用降序
     */
    public boolean getDescending() {
        return this.descending;
    }

    /**
     * 获取主序
     *
     * @return 主序
     */
    public ItemOrder<T> geMainOrder() {
        return this;
    }

    /**
     * 获取从序
     *
     * @return 从序
     */
    public ItemOrder<T> getSubOrder() {
        return this.subOrder;
    }

    /**
     * 设置从序
     *
     * @param subOrder 从序
     */
    public void setSubOrder(ItemOrder<T> subOrder) {
        this.subOrder = subOrder;
    }
}
