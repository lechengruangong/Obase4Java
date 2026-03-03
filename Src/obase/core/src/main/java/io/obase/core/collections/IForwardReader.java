/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：只进读取器接口,包含普通版本和泛型版本.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:15:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.ObjectReferencePack;

import java.util.Comparator;

/**
 * 只进读取器
 * 提供读取集合“只进”流的方法，并定义一种延迟执行机制对集合元素进行排序。
 */
public interface IForwardReader {

    /**
     * 获取一个值，该值指示“只进”读取器是否可重置。
     *
     * @return 可以重置返回True 否则返回False
     */
    boolean getResettable();

    /**
     * 获取读取器当前位置的元素
     * 当读取器位于“只进”流的起始位置或结束位置时，返回null
     *
     * @return 当前元素
     */
    Object getCurrent();

    /**
     * 将读取器向前移动到下一个元素
     * 刚被初始化或重置时，读取器位于只进流的起始位置，即第一个元素之前；读取结束后位置流的结束位置，即最后一个元素之后
     *
     * @return 如果移动成功返回true，如果已位于最后一个元素返回false
     */
    boolean read();

    /**
     * 关闭读取器
     */
    void close();

    /**
     * 使用指定的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    IOrderedReader orderBy(Comparator<?> comparator);

    /**
     * 使用指定的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    IOrderedReader orderByDescending(Comparator<?> comparator);

    /**
     * 将读取器回退到只进流的起始位置（第一个元素之前）
     * 如果当前只进流不可重置则引发异常
     *
     * @throws UnsupportedOperationException 不支持重置
     */
    void reset() throws UnsupportedOperationException;

    /**
     * 将读取器向前移动指定个数的元素
     *
     * @param count 提升的元素个数
     * @return 实际移动数。当流中当前位置之后的元素数少于请求数时，实际移动数将小于请求数
     */
    int skip(int count);

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 包含指定个数元素的读取器 可能小于要求的个数
     */
    IForwardReader take(int count);

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count       要读取的元素个数
     * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
     * @return 包含指定个数元素的读取器 具体个数等于resultCount
     */
    IForwardReader take(int count, ObjectReferencePack<Integer> resultCount);
}
