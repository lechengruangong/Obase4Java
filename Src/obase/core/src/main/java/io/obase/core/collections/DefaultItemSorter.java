/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的元素排序器,实现排序器接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 16:56:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

/**
 * 默认的元素排序器
 *
 * @param <T> 元素类型
 */
public class DefaultItemSorter<T> implements IItemSorter<T> {
    /**
     * 执行排序
     *
     * @param source    源序列
     * @param rules     排序规则
     * @param resultSet 结果集
     */
    @Override
    public void sort(IForwardReaderGeneric<T> source, ItemOrder<T> rules, HugeSet<T> resultSet) {
        //是正序还是倒序
        //分别使用只进读取器的排序方法处理 并且追加到结果集中
        IForwardReaderGeneric<T> orderedReader;
        if (!rules.getDescending()) {
            orderedReader = rules.getKeySelector() != null
                    ? source.orderBy(rules.getComparator()).thenBy(rules.getKeySelector())
                    : source.orderBy(rules.getComparator());
        } else {
            orderedReader = rules.getKeySelector() != null
                    ? source.orderByDescending(rules.getComparator()).thenByDescending(rules.getKeySelector())
                    : source.orderByDescending(rules.getComparator());
        }
        resultSet.append(orderedReader);
    }
}
