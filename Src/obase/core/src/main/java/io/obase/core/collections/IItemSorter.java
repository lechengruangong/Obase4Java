/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序器接口,提供按一定策略实施排序的方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:40:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

/**
 * 排序器，提供按一定策略实施排序的方法。
 *
 * @param <T> 元素类型
 */
public interface IItemSorter<T> {

    /**
     * 执行排序
     *
     * @param source    源序列
     * @param rules     排序规则
     * @param resultSet 结果集
     */
    void sort(IForwardReaderGeneric<T> source, ItemOrder<T> rules, HugeSet<T> resultSet);
}
