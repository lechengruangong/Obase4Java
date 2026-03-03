/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序结果读取器接口,包含普通版本和泛型版本.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:16:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;

import java.util.Comparator;

/**
 * 泛型版本的排序结果读取器
 * 作为泛型版本的排序结果封装排序延迟执行逻辑，并提供对结果再次排序的方法
 *
 * @param <T> 集合元素类型
 */
public interface IOrderedReaderGeneric<T> extends IOrderedReader, IForwardReaderGeneric<T> {

    /**
     * 获取对其执行排序操作的源序列
     *
     * @return 对其执行排序操作的源序列
     */
    IForwardReaderGeneric<T> getSource();

    /**
     * 使用指定的比较器，采用延迟执行策略对排序结果执行递进排序（升序）
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的只进读取器
     */
    IOrderedReaderGeneric<T> thenBy(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器，采用延迟执行策略对排序结果执行递进排序（升序）
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TObject>   排序键
     * @return 代表排序结果的只进读取器
     */
    <TObject> IOrderedReaderGeneric<T> thenBy(FunctionWithOneArg<T, TObject> keySelector);

    /**
     * 使用指定的比较器，采用延迟执行策略对排序结果执行递进排序（降序
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的只进读取器
     */
    IOrderedReaderGeneric<T> thenByDescending(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器，采用延迟执行策略对排序结果执行递进排序（降序）
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TObject>   排序键
     * @return 代表排序结果的只进读取器
     */
    <TObject> IOrderedReaderGeneric<T> thenByDescending(FunctionWithOneArg<T, TObject> keySelector);
}

