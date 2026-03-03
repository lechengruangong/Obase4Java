/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：包含检测接口
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:05:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

/**
 * 包含检测接口
 *
 * @param <T>元素类型
 */
public interface IContains<T> {

    /**
     * 是否包含元素
     *
     * @param item 元素
     * @return 包含返回True 否则返回False
     */
    boolean contains(T item);
}
