/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：IGroupingBy的默认实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:43:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.expression.IGroupingBy;

/**
 * IGroupingBy的默认实现
 *
 * @param <TKey>     键
 * @param <TElement> 元素
 */
public class GroupingBy<TKey, TElement> implements IGroupingBy<TKey, TElement> {

    /**
     * 键
     */
    private final TKey key;

    /**
     * 元素
     */
    private final TElement element;

    /**
     * 构造IGroupingBy的默认实现
     *
     * @param key     键
     * @param element 元素
     */
    public GroupingBy(TKey key, TElement element) {

        this.key = key;
        this.element = element;
    }

    /**
     * 获取分组键
     *
     * @return 分组键
     */
    @Override
    public TKey getKey() {
        return this.key;
    }

    /**
     * 获取元素
     *
     * @return 元素
     */
    @Override
    public TElement getElement() {
        return this.element;
    }
}
