/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：作为排序结果的只进阅读器,实现有序的只进读取.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 16:49:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 作为排序结果的只进阅读器，实现延迟执行机制
 *
 * @param <T> 集合元素的类型
 */
public class OrderedReader<T> implements IOrderedReaderGeneric<T>, IReversible<T>, IContains<T>, ICountable {

    /**
     * 源序列
     */
    private final IForwardReaderGeneric<T> source;
    /**
     * 延迟执行的顺序
     */
    private ItemOrder<T> delayedOrder;
    /**
     * 排序器
     */
    private IItemSorter<T> sorter = new DefaultItemSorter<>();
    /**
     * 固有顺序
     */
    private ItemOrder<T> conNaturalOrder;
    /**
     * 获取读取器当前位置的元素。
     * 当读取器位于“只进”流的起始位置或结束位置时，返回null。
     */
    private T current;
    /**
     * 源序列的HugeSet包装
     */
    private HugeSet<T> hugeSetSource;

    /**
     * 是否需要排序
     * 当被构造未读取前 执行了OrderBy ThenBy重设了排序方式之后 需要在Read时排序
     */
    private boolean needToSort = true;

    /**
     * 作为排序结果的集合。
     */
    private HugeSet<T> resultSet;

    /**
     * 使用已指明固有顺序的源序列、排序规则创建OrderedReader实例，该实例使用默认排序策略延迟执行排序操作
     *
     * @param source          要对其进行排序的源序列
     * @param conNaturalOrder 源序列的固有顺序
     * @param delayedOrder    待执行的排序规则
     */
    public OrderedReader(IForwardReaderGeneric<T> source, ItemOrder<T> conNaturalOrder, ItemOrder<T> delayedOrder) {
        this.source = source;
        this.conNaturalOrder = conNaturalOrder;
        this.delayedOrder = delayedOrder;

        //处理HugeSet包装的源序列
        this.setHugeSetSource();
    }

    /**
     * 使用指定的源序列、排序规则创建OrderedReader实例，该实例使用默认排序策略延迟执行排序操作。
     *
     * @param source       要对其进行排序的源序列
     * @param delayedOrder 待执行的排序规则
     */
    public OrderedReader(IForwardReaderGeneric<T> source, ItemOrder<T> delayedOrder) {
        this.source = source;
        this.delayedOrder = delayedOrder;

        //处理HugeSet包装的源序列
        this.setHugeSetSource();
    }

    /**
     * 使用已指明固有顺序的源序列、排序规则创建OrderedReader实例，该实例使用指定的排序策略延迟执行排序操作。
     *
     * @param source          要对其进行排序的源序列
     * @param conNaturalOrder 源序列的固有顺序
     * @param delayedOrder    待执行的排序规则
     * @param sorter          按一定策略执行排序操作的排序器
     */
    public OrderedReader(IForwardReaderGeneric<T> source, ItemOrder<T> conNaturalOrder, ItemOrder<T> delayedOrder,
                         IItemSorter<T> sorter) {
        this.source = source;
        this.conNaturalOrder = conNaturalOrder;
        this.delayedOrder = delayedOrder;
        this.sorter = sorter;

        //处理HugeSet包装的源序列
        this.setHugeSetSource();
    }

    /**
     * 使用指定的源序列、排序规则创建OrderedReader实例，该实例使用指定的排序策略延迟执行排序操作
     *
     * @param source       要对其进行排序的源序列
     * @param delayedOrder 待执行的排序规则
     * @param sorter       按一定策略执行排序操作的排序器
     */
    public OrderedReader(IForwardReaderGeneric<T> source, ItemOrder<T> delayedOrder, IItemSorter<T> sorter) {
        this.source = source;
        this.delayedOrder = delayedOrder;
        this.sorter = sorter;
        //处理HugeSet包装的源序列
        this.setHugeSetSource();
    }

    /**
     * 是否包含元素
     *
     * @param item 元素
     * @return 包含返回True 否则返回False
     */
    @Override
    public boolean contains(T item) {
        return this.hugeSetSource.contains(item);
    }

    /**
     * 获取一个值，该值指示集合或序列是否支持统计元素个数的操作
     *
     * @return 可以计数返回True 否则返回False
     */
    @Override
    public boolean getCanCount() {
        return true;
    }

    /**
     * 获取元素个数
     *
     * @return 元素个数
     * @throws UnsupportedOperationException 不支持计数
     */
    @Override
    public long getCount() throws UnsupportedOperationException {
        return this.hugeSetSource.getCount();
    }

    /**
     * 获取一个值，该值指示“只进”读取器是否可重置。
     *
     * @return 可以重置返回True 否则返回False
     */
    @Override
    public boolean getResettable() {
        return this.hugeSetSource.getResettable();
    }

    /**
     * 获取读取器当前位置的元素
     *
     * @return 当前元素
     */
    @Override
    public T getCurrent() {
        return this.current;
    }

    /**
     * 对其执行排序操作的源序列
     *
     * @return 对其执行排序操作的源序列
     */
    @Override
    public IForwardReaderGeneric<T> getSource() {
        return this.hugeSetSource;
    }

    /**
     * 将读取器向前移动到下一个元素
     *
     * @return 如果移动成功返回true，如果已位于最后一个元素返回false
     */
    public boolean read() {
        //如果需要排序
        if (this.needToSort)
            //排序
            this.doSort();

        boolean result = this.resultSet.read();
        this.current = this.resultSet.getCurrent();
        return result;
    }

    /**
     * 关闭读取器
     */
    public void close() {
        this.source.close();
        this.hugeSetSource.close();
        if (this.resultSet != null)
            this.resultSet.close();
    }

    /**
     * 将读取器回退到只进流的起始位置（第一个元素之前）
     */
    public void reset() {
        if (this.resultSet != null) {
            this.resultSet.reset();
        }
    }

    /**
     * 将读取器向前移动指定个数的元素
     *
     * @param count 提升的元素个数
     * @return 实际移动数。当流中当前位置之后的元素数少于请求数时，实际移动数将小于请求数
     */
    public int skip(int count) {
        if (this.needToSort)
            this.sort();
        return this.resultSet.skip(count);
    }

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 只进读取器
     */
    public IForwardReaderGeneric<T> take(int count) {
        if (this.needToSort)
            this.sort();
        return this.resultSet.take(count);
    }

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count       要读取的元素个数
     * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
     * @return 只进读取器
     */
    public IForwardReaderGeneric<T> take(int count, ObjectReferencePack<Integer> resultCount) {
        if (this.needToSort)
            this.sort();
        return this.resultSet.take(count, resultCount);
    }

    /**
     * OrderReader的OrderBy会重设主排序conNaturalOrder
     *
     * @param comparator 排序比较器
     * @return 排序后的读取器
     */
    public IOrderedReaderGeneric<T> orderBy(Comparator<?> comparator) {
        this.needToSort = true;
        this.conNaturalOrder = new ItemOrder<>((Comparator<T>) comparator, false);

        return this;
    }

    /**
     * OrderReader的OrderBy会重设主排序conNaturalOrder
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      键类型
     * @return 排序后的读取器
     */
    public <TKey> IOrderedReaderGeneric<T> orderBy(FunctionWithOneArg<T, TKey> keySelector) {
        this.needToSort = true;
        this.conNaturalOrder = new ItemOrder<>((FunctionWithOneArg<T, Object>) keySelector, false);

        return this;
    }

    /**
     * OrderReader的OrderBy会重设主排序conNaturalOrder
     *
     * @param comparator 排序比较器
     * @return 排序后的读取器
     */
    public IOrderedReaderGeneric<T> orderByDescending(Comparator<?> comparator) {
        this.needToSort = true;
        this.conNaturalOrder = new ItemOrder<>((Comparator<T>) comparator, true);

        return this;
    }

    /**
     * OrderReader的OrderBy会重设主排序conNaturalOrder
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      键类型
     * @return 排序后的读取器
     */
    public <TKey> IOrderedReaderGeneric<T> orderByDescending(FunctionWithOneArg<T, TKey> keySelector) {
        this.needToSort = true;
        this.conNaturalOrder = new ItemOrder<>((FunctionWithOneArg<T, Object>) keySelector, true);

        return this;
    }

    /**
     * 调用ThenBy会重设延迟排序_delayedOrder
     *
     * @param comparator 比较器
     * @return 排序后的读取器
     */
    public IOrderedReaderGeneric<T> thenBy(Comparator<?> comparator) {
        this.needToSort = true;
        this.delayedOrder = new ItemOrder<>((Comparator<T>) comparator, false);

        return this;
    }

    /**
     * 调用ThenBy会重设延迟排序_delayedOrder
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      键类型
     * @return 排序后的读取器
     */
    public <TKey> IOrderedReaderGeneric<T> thenBy(FunctionWithOneArg<T, TKey> keySelector) {
        this.needToSort = true;
        this.delayedOrder = new ItemOrder<>((FunctionWithOneArg<T, Object>) keySelector, false);

        return this;
    }

    /**
     * 调用ThenBy会重设延迟排序_delayedOrder
     *
     * @param comparator 比较器
     * @return 排序后的读取器
     */
    public IOrderedReaderGeneric<T> thenByDescending(Comparator<?> comparator) {
        this.needToSort = true;
        this.delayedOrder = new ItemOrder<>((Comparator<T>) comparator, true);

        return this;
    }

    /**
     * 调用ThenBy会重设延迟排序_delayedOrder
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      键类型
     * @return 排序后的读取器
     */
    public <TKey> IOrderedReaderGeneric<T> thenByDescending(FunctionWithOneArg<T, TKey> keySelector) {
        this.needToSort = true;
        this.delayedOrder = new ItemOrder<>((FunctionWithOneArg<T, Object>) keySelector, true);

        return this;
    }

    /**
     * 反序
     *
     * @return 反序后的纸巾读取器
     */
    public IForwardReaderGeneric<T> reverse() {
        if (this.needToSort) this.sort();
        return this.resultSet.reverse();
    }

    /**
     * 为HugeSet包装的源序列设值
     */
    private void setHugeSetSource() {
        if (this.source instanceof HugeSet) {
            this.hugeSetSource = (HugeSet<T>) this.source;
        } else {
            this.hugeSetSource = new HugeSet<>();
            this.hugeSetSource.append(this.source);
        }
    }

    /**
     * 排序
     */
    private void sort() {
        //准备集合
        this.resultSet = new HugeSet<>();
        this.hugeSetSource.reset();
        //调用排序器
        this.sorter.sort(this.hugeSetSource, this.conNaturalOrder, this.resultSet);

        this.needToSort = false;
    }

    /**
     * 真正执行排序
     */
    private void doSort() {
        //准备集合
        this.resultSet = new HugeSet<>();
        this.hugeSetSource.reset();

        //必须要有主序
        if (this.conNaturalOrder != null) {
            if (this.hugeSetSource.getCount() > 65536)
                this.memorySort(this.conNaturalOrder);
            else
                this.mergeHeapSort(this.conNaturalOrder);

            //有延迟排序 则再排一次
            if (this.delayedOrder != null) {
                if (this.hugeSetSource.getCount() > 65536)
                    this.memorySort(this.delayedOrder);
                else
                    this.mergeHeapSort(this.delayedOrder);
            }
        }

        this.needToSort = false;
    }

    /**
     * 内存排序
     *
     * @param order 元素排序规则
     */
    private void memorySort(ItemOrder<T> order) {
        //构造一个容器
        ArrayList<T> list = new ArrayList<>();
        while (this.hugeSetSource.read()) list.add(this.hugeSetSource.getCurrent());
        //排序
        if (order.getKeySelector() != null)
            list.sort(Comparator.comparingInt(o -> (int) order.getKeySelector().invoke(o)));
        else if (order.getComparator() != null)
            list.sort(order.getComparator());
        else
            list.sort((o1, o2) -> 0);

        //处理结果
        if (order.getDescending()) Collections.reverse(list);
        this.resultSet.append(list);
    }

    /**
     * 基于文件的归并堆排序
     *
     * @param order 元素排序规则
     */
    private void mergeHeapSort(ItemOrder<T> order) {
        //构造外存归并排序器
        MergeSortExecutor<T> mergeSorter;
        if (order.getComparator() != null)
            mergeSorter = new MergeSortExecutor<>(order.getComparator(), order.getDescending(), 100000);
        else
            mergeSorter = new MergeSortExecutor<>((o1, o2) -> 0, order.getDescending(), 100000);

        //放入待排序元素
        while (this.hugeSetSource.read()) mergeSorter.putIn(this.hugeSetSource.getCurrent());
        //结束放入
        mergeSorter.endPutIn();
        //读取结果
        ObjectReferencePack<Boolean> isSuccess = new ObjectReferencePack<>();
        isSuccess.realValue = true;
        while (isSuccess.realValue) {
            T result = mergeSorter.takeOut(isSuccess);
            if (isSuccess.realValue) this.resultSet.append(result);
        }
    }
}
