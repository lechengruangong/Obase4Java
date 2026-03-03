/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：分组后的结果接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 15:31:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 表示分组后的结果
 *
 * @param <TKey>     键
 * @param <TElement> 元素
 */
public interface IGroupingBy<TKey, TElement> {

    /**
     * 获取分组键
     *
     * @return 分组键
     */
    TKey getKey();

    /**
     * 获取元素
     *
     * @return 元素
     */
    TElement getElement();
}
