/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：只进读取器接口,包含普通版本和泛型版本.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:29:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;

import java.util.Comparator;

/**
 * 泛型版本的读取集合“只进”流的方法，并定义一种延迟执行机制对集合元素进行排序
 *
 * @param <T>
 */
public interface IForwardReaderGeneric<T> extends IForwardReader {

    /**
     * 获取读取器当前位置的元素
     * 当读取器位于“只进”流的起始位置或结束位置时，返回null
     *
     * @return 当前位置的元素
     */
    T getCurrent();

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 包含指定个数元素的读取器 可能小于要求的个数
     */
    IForwardReaderGeneric<T> take(int count);

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count       要读取的元素个数
     * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
     * @return 包含指定个数元素的读取器 具体个数等于resultCount
     */
    IForwardReaderGeneric<T> take(int count, ObjectReferencePack<Integer> resultCount);

    /**
     * 使用指定的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    IOrderedReaderGeneric<T> orderBy(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TObject>   排序键
     * @return 代表排序结果的新的只进读取器
     */
    <TObject> IOrderedReaderGeneric<T> orderBy(FunctionWithOneArg<T, TObject> keySelector);

    /**
     * 使用指定的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    IOrderedReaderGeneric<T> orderByDescending(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TObject>   排序键
     * @return 代表排序结果的新的只进读取器
     */
    <TObject> IOrderedReaderGeneric<T> orderByDescending(FunctionWithOneArg<T, TObject> keySelector);
}
