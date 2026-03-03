/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：键序基读取器接口,沿键序列读取各个键对应的值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 16:30:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.ObjectReferencePack;

/**
 * 键序基读取器，沿键序列读取各个键对应的值。
 * 该键序列中的键称为基键
 *
 * @param <Key> 键的类型
 * @param <T>   值的类型
 */
public interface IKeySequenceBasedReader<Key, T> extends IForwardReaderGeneric<T> {

    /**
     * 获取基键序列
     *
     * @return 基键序列
     */
    IForwardReaderGeneric<Key> getKeys();

    /**
     * 将读取器向前移动到下一个元素。
     * 刚被初始化或重置时，读取器位于只进流的起始位置，即第一个元素之前；读取结束后位置流的结束位置，即最后一个元素之后。
     *
     * @param key 内含一个键元素 表示移动后当前位置的基键
     * @return 如果移动成功返回true，如果已位于最后一个元素返回false
     */
    boolean read(ObjectReferencePack<Key> key);

    /**
     * 检查是否所有基键都能在基础集合中找到
     *
     * @return 如果所有基键都能在基础集合中找到，则返回true；否则返回false
     */
    boolean existenceCheck();

    /**
     * 获取基础集合中缺失的基键
     *
     * @return 缺失的基键
     */
    IForwardReaderGeneric<Key> getAbsent();
}
