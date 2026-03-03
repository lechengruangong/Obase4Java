/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：可计数接口,提供统计集合或序列中元素个数的机制.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:12:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

/**
 * 提供统计集合或序列中元素个数的机制
 */
public interface ICountable {

    /**
     * 获取一个值，该值指示集合或序列是否支持统计元素个数的操作
     *
     * @return 可以计数返回True 否则返回False
     */
    boolean getCanCount();

    /**
     * 获取元素个数
     *
     * @return 元素个数
     * @throws UnsupportedOperationException 不支持计数
     */
    long getCount() throws UnsupportedOperationException;
}
