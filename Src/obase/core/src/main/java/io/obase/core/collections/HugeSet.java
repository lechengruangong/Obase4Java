/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：巨量集合,为数据特别庞大的元素集提供存储与访问机制.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 15:49:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;

import java.io.Closeable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 巨量集合，为数据特别庞大的元素集提供存储与访问机制。
 * HugeSet提供两个存储区，内存存储区和后备存储区。新加入的元素将被放入内存存储区，该存储区用完后自动将元素转存至后备存储区，并且清空内存存储区以接受新的元素
 * HugeSet实现IForwardReader，调用方可以使用此接口访问集合的”只进“流。
 * 从后备存储区读取元素实现了“预读”机制，一次性从后备存储区批量读取元素放入内存缓冲区，从而减少IO操作。
 *
 * @param <T> 集合元素的类型
 */
public class HugeSet<T> extends ForwardReader<T> implements IContains<T>, IReversible<T>, Closeable {

    /**
     * 后备存储提供程序
     */
    private final IBackupStorageProvider<T> backupStorage;

    /**
     * 内存存储区容量
     */
    private final int memoryCapacity;

    /**
     * 内存存储区
     */
    private final Queue<T> memorySet;

    /**
     * 总个数
     */
    private int count;

    /**
     * 获取读取器当前位置的元素。
     * 当读取器位于“只进”流的起始位置或结束位置时，返回null
     */
    private T current;

    /**
     * 内存存储区当前元素数
     */
    private int memoryCount;

    /**
     * 读取的索引 从-1开始
     */
    private int readIndex = -1;

    /**
     * 使用默认的后备存储提供程序创建HugeSet实例，同时设置内存存储区容量为默认值。
     */
    public HugeSet() {
        this.backupStorage = new FileStorageProvider<>();
        this.memorySet = new LinkedList<>();
        this.memoryCount = 0;
        this.memoryCapacity = 65536;
    }

    /**
     * 使用默认的后备存储提供程序创建HugeSet实例，同时指定内存存储区容量。
     *
     * @param memoryCapacity 内存存储区容量
     */
    public HugeSet(int memoryCapacity) {
        this.backupStorage = new FileStorageProvider<>();
        this.memorySet = new LinkedList<>();
        this.memoryCount = 0;
        this.memoryCapacity = memoryCapacity;
    }

    /**
     * 使用指定的后备存储提供程序创建HugeSet实例，同时设置内存存储区容量为默认值。
     *
     * @param backupStorage 后备存储提供程序
     */
    public HugeSet(IBackupStorageProvider<T> backupStorage) {
        this.backupStorage = backupStorage;
        this.memorySet = new LinkedList<>();
        this.memoryCount = 0;
        this.memoryCapacity = 65536;
    }

    /**
     * 使用指定的后备存储提供程序创建HugeSet实例，同时指定内存存储区容量。
     *
     * @param memoryCapacity 内存存储区容量
     * @param backupStorage  后备存储提供程序
     */
    public HugeSet(int memoryCapacity, IBackupStorageProvider<T> backupStorage) {
        this.backupStorage = backupStorage;
        this.memorySet = new LinkedList<>();
        this.memoryCount = 0;
        this.memoryCapacity = memoryCapacity;
    }

    /**
     * 获取集合中元素的个数
     *
     * @return 集合中元素的个数
     */
    public long getCount() {
        return this.count;
    }

    /**
     * 获取一个值，该值指示“只进”读取器是否可重置
     *
     * @return 是否可重置
     */
    @Override
    public boolean getResettable() {
        return true;
    }

    /**
     * 获取读取器当前位置的元素
     *
     * @return 当读取器位于“只进”流的起始位置或结束位置时，返回null
     */
    @Override
    public T getCurrent() {
        return this.current;
    }

    /**
     * 是否包含元素
     *
     * @param item 元素
     * @return 包含返回True 否则返回False
     */
    @Override
    public boolean contains(T item) {
        if (this.memorySet.contains(item)) return true;

        return this.backupStorage.contains(item);
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        this.releaseUnmanagedResources();
    }

    /**
     * 释放资源
     */
    private void releaseUnmanagedResources() {
        //暂时没有可释放的
    }

    /**
     * 创建一个从当前巨量集合中反序读取元素的只进读取器
     *
     * @return 生成的只进读取器
     */
    public IForwardReaderGeneric<T> reverse() {
        return new ReverselyReader<>(this);
    }

    /**
     * 向集合添加一个元素
     *
     * @param item 要添加的元素
     */
    public void append(T item) {
        //有余量
        if (this.memoryCount < this.memoryCapacity) {
            this.memorySet.offer(item);
            this.memoryCount++;
        } else {
            ArrayList<T> backUp = new ArrayList<>();
            while (this.memorySet.size() > 0) backUp.add(this.memorySet.poll());
            //存入后备
            this.backupStorage.append(backUp);
            //清空内存区
            this.memorySet.clear();
            this.memorySet.offer(item);
            this.memoryCount = 1;
        }

        this.count++;
    }

    /**
     * 向集合批量添加元素
     *
     * @param item 要添加的元素集
     */
    public void append(Iterable<T> item) {
        for (T i : item) this.append(i);
    }

    /**
     * 向集合批量添加元素
     *
     * @param item 要添加的元素集
     */
    public void append(IForwardReaderGeneric<T> item) {
        //挨个读出来
        while (item.read()) this.append(item.getCurrent());
    }

    /**
     * 将读取器向前移动到下一个元素。
     * 刚被初始化或重置时，读取器位于只进流的起始位置，即第一个元素之前；读取结束后位置流的结束位置，即最后一个元素之后。
     *
     * @return 如果移动成功返回true，如果已位于最后一个元素返回false。
     */
    @Override
    public boolean read() {

        //内存区无元素 返回false
        if (this.memorySet.size() == 0) return false;
        //当前索引在内存区内
        if (this.memorySet.size() > this.readIndex + 1) {
            this.readIndex++;
            this.current = (T) this.memorySet.toArray(new Object[0])[this.readIndex];
            return true;
        }

        T[] backReadResult = this.backupStorage.read(1);
        if (backReadResult != null) {
            //读取成功 移动索引
            this.readIndex++;
            this.current = backReadResult[0];
            return true;
        }

        //返回false
        this.current = null;
        return false;
    }

    /**
     * 从当前位置读取指定个数的元素。
     *
     * @param count       要读取的元素个数
     * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
     * @return 只进读取器
     */
    @Override
    public IForwardReaderGeneric<T> take(int count, ObjectReferencePack<Integer> resultCount) {
        HugeSet<T> result = new HugeSet<>();
        //读取指定个数
        int i = 0;
        while (this.read() && i < count) {
            result.append(this.getCurrent());
            i++;
        }

        //实际读取的数目
        resultCount.realValue = i;

        return result;
    }

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param comparator 排序比较器
     * @return 排序后的只进读取器
     */
    @Override
    public IOrderedReaderGeneric<T> orderBy(Comparator<?> comparator) {
        return new OrderedReader<>(this, new ItemOrder<>((Comparator<T>) comparator, false), (IItemSorter<T>) null);
    }

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      排序键
     * @return 排序后的只进读取器
     */
    @Override
    public <TKey> IOrderedReaderGeneric<T> orderBy(FunctionWithOneArg<T, TKey> keySelector) {
        return new OrderedReader<>(this, new ItemOrder<>(Comparator.comparingInt(o -> (Integer) keySelector.invoke(o)), false), (IItemSorter<T>) null);
    }

    /**
     * 用指定的排序键和默认的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param comparator 排序比较器
     * @return 排序后的只进读取器
     */
    @Override
    public IOrderedReaderGeneric<T> orderByDescending(Comparator<?> comparator) {
        return new OrderedReader<>(this, new ItemOrder<>((Comparator<T>) comparator, true), (IItemSorter<T>) null);
    }

    /**
     * 用指定的排序键和默认的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      排序键
     * @return 排序后的只进读取器
     */
    @Override
    public <TKey> IOrderedReaderGeneric<T> orderByDescending(FunctionWithOneArg<T, TKey> keySelector) {
        return new OrderedReader<>(this, new ItemOrder<>(Comparator.comparingInt(o -> (Integer) keySelector.invoke(o)), true), (IItemSorter<T>) null);
    }

    /**
     * 从当前位置读取指定个数的元素。
     *
     * @param count 要读取的元素个数
     * @return 只进读取器
     */
    @Override
    public IForwardReaderGeneric<T> take(int count) {
        HugeSet<T> result = new HugeSet<>();
        //读取指定个数
        int i = 0;
        while (this.read() && i < count) {
            result.append(this.getCurrent());
            i++;
        }

        return result;
    }

    /**
     * 从集合只进流中预读指定数量的元素放入缓冲区。
     *
     * @param maxCount 最大个数
     * @return 结果数组
     */
    @Override
    protected T[] preRead(int maxCount) {
        //最大可预读数
        if (maxCount > this.getPreReadMax()) maxCount = this.getPreReadMax();

        ArrayList<T> result = new ArrayList<>();
        int i = 0;
        while (i < maxCount)
            if (this.read()) {
                //读入缓冲区
                this.preReadItems.offer(this.current);
                //返回值
                result.add(this.current);
                i++;
            } else {
                break;
            }

        T[] resultArray = (T[]) Array.newInstance(result.get(0).getClass(), result.size());
        return result.toArray(resultArray);
    }

    /**
     * 将读取器向前移动指定个数的元素。
     *
     * @param number 移动的个数
     * @return 实际跳过的个数
     */
    @Override
    protected int doSkipping(int number) {
        int realNumber = number;
        if (this.readIndex + number > this.count - 1) realNumber = this.count - this.readIndex - 1;
        this.readIndex += realNumber;
        return realNumber;
    }

    /**
     * 将读取器回退到只进流的起始位置（第一个元素之前）。
     * 在派生类中实现时，如果当前集合的只进流不可重置，则不执行任何操作。
     */
    @Override
    protected void doResetting() {
        this.readIndex = -1;
        this.backupStorage.reset();
    }

    /**
     * 关闭读取器
     */
    @Override
    protected void doClosing() {
        //没啥关的
    }

    /**
     * 巨量集合的反序读取器，用于从后往前读取指定巨量集合的元素
     *
     * @param <TItem>
     */
    private static class ReverselyReader<TItem> extends ForwardReader<TItem> implements ICountable {

        /**
         * 用于读取的集合
         */
        private final HugeSet<TItem> hugeSet;

        /**
         * 获取读取器当前位置的元素
         * 当读取器位于“只进”流的起始位置或结束位置时，返回null
         */
        private TItem current;

        /**
         * 倒序读取索引
         */
        private int reverselyReadIndex;

        /**
         * 构造一个巨量集合的反序读取器
         *
         * @param hugeSet 要读取的集合
         */
        public ReverselyReader(HugeSet<TItem> hugeSet) {
            this.hugeSet = hugeSet;
            this.hugeSet.backupStorage.reverselyReset();
            this.reverselyReadIndex = hugeSet.memorySet.size();
        }

        /**
         * 获取一个值，该值指示“只进”读取器是否可重置
         *
         * @return 是否可重置
         */
        @Override
        public boolean getResettable() {
            return this.hugeSet.getResettable();
        }

        /**
         * 获取读取器当前位置的元素
         *
         * @return 当前位置的元素
         */
        @Override
        public TItem getCurrent() {
            return this.current;
        }

        /**
         * 获取一个值，该值指示集合或序列是否支持统计元素个数的操作
         *
         * @return 集合或序列是否支持统计元素个数
         */
        public boolean getCanCount() {
            return true;
        }

        /**
         * 获取元素个数
         *
         * @return 元素个数
         * @throws UnsupportedOperationException 不支持统计元素个数操作
         */
        @Override
        public long getCount() throws UnsupportedOperationException {
            return this.hugeSet.getCount();
        }

        /**
         * 将读取器向前移动到下一个元素。
         * 刚被初始化或重置时，读取器位于只进流的起始位置，即第一个元素之前；读取结束后位置流的结束位置，即最后一个元素之后。
         *
         * @return 如果移动成功返回true，如果已位于最后一个元素返回false。
         */
        @Override
        public boolean read() {
            //先从后备区读取
            TItem[] backResult = this.hugeSet.backupStorage.reverselyRead(1);
            if (backResult != null) {
                this.current = backResult[0];
                return true;
            }

            //读取内存区
            if (this.reverselyReadIndex > 0) {
                this.reverselyReadIndex--;
                this.current = (TItem) this.hugeSet.memorySet.toArray()[this.reverselyReadIndex];
                return true;
            }

            //读完了
            this.current = null;
            return false;
        }

        /**
         * 从当前位置读取指定个数的元素
         *
         * @param count 要读取的元素个数
         * @return 结果只进读取器
         */
        @Override
        public IForwardReaderGeneric<TItem> take(int count) {
            HugeSet<TItem> result = new HugeSet<>();
            //读取指定个数
            int i = 0;
            while (this.read() && i < count) {
                result.append(this.getCurrent());
                i++;
            }

            return result;
        }

        /**
         * 从当前位置读取指定个数的元素。
         *
         * @param count       要读取的元素个数
         * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
         * @return 结果只进读取器
         */
        @Override
        public IForwardReaderGeneric<TItem> take(int count, ObjectReferencePack<Integer> resultCount) {
            HugeSet<TItem> result = new HugeSet<>();
            //读取指定个数
            int i = 0;
            while (this.read() && i < count) {
                result.append(this.getCurrent());
                i++;
            }

            //实际读取的数目
            resultCount.realValue = i;

            return result;
        }

        /**
         * 从集合只进流中预读指定数量的元素放入缓冲区
         *
         * @param maxCount 最大个数
         * @return 读取到的元素的集合，未读到任何元素返回null。当流中当前位置之后的元素数小于请求数时，实际读取到的元素会小于请求数
         */
        @Override
        protected TItem[] preRead(int maxCount) {
            //最大可预读数
            if (maxCount > this.getPreReadMax()) maxCount = this.getPreReadMax();

            ArrayList<TItem> result = new ArrayList<>();
            int i = 0;
            while (i < maxCount)
                if (this.read()) {
                    //读入缓冲区
                    this.preReadItems.offer(this.current);
                    //返回值
                    result.add(this.current);
                    i++;
                } else {
                    break;
                }

            TItem[] resultArray = (TItem[]) Array.newInstance(result.get(0).getClass(), result.size());
            return result.toArray(resultArray);
        }

        /**
         * 将读取器向前移动指定个数的元素
         *
         * @param number 移动的个数
         * @return 实际移动数。当流中当前位置之后的元素数少于请求数时，实际移动数将小于请求数
         */
        @Override
        protected int doSkipping(int number) {
            int realNumber = number;
            if (this.reverselyReadIndex - number > -1)
                realNumber = this.hugeSet.memorySet.size() - this.reverselyReadIndex;
            this.reverselyReadIndex -= realNumber;
            return realNumber;
        }

        /**
         * 将读取器回退到只进流的起始位置（第一个元素之前）
         */
        @Override
        protected void doResetting() {
            this.hugeSet.reset();
        }

        /**
         * 关闭读取器
         */
        @Override
        protected void doClosing() {
            //没有需要特殊关闭的
        }

        /**
         * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
         *
         * @param keySelector 用于计算排序键的函数式接口
         * @param <TKey>      排序键
         * @return 排序后的只进读取器
         */
        @Override
        public <TKey> IOrderedReaderGeneric<TItem> orderBy(FunctionWithOneArg<TItem, TKey> keySelector) {
            return new OrderedReader<>(this, new ItemOrder<>(Comparator.comparingInt(o -> (Integer) keySelector.invoke(o)), false), (IItemSorter<TItem>) null);
        }

        /**
         * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行
         *
         * @param comparator 排序比较器
         * @return 排序后的只进读取器
         */
        @Override
        public IOrderedReaderGeneric<TItem> orderBy(Comparator<?> comparator) {
            return new OrderedReader<>(this, new ItemOrder<>((Comparator<TItem>) comparator, false), (IItemSorter<TItem>) null);
        }

        /**
         * 使用指定的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
         *
         * @param keySelector 用于计算排序键的函数式接口
         * @param <TKey>      排序键
         * @return 排序后的只进读取器
         */
        @Override
        public <TKey> IOrderedReaderGeneric<TItem> orderByDescending(FunctionWithOneArg<TItem, TKey> keySelector) {
            return new OrderedReader<>(this, new ItemOrder<>(Comparator.comparingInt(o -> (Integer) keySelector.invoke(o)), true), (IItemSorter<TItem>) null);
        }

        /**
         * 使用指定的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
         *
         * @param comparator 排序比较器
         * @return 排序后的只进读取器
         */
        @Override
        public IOrderedReaderGeneric<TItem> orderByDescending(Comparator<?> comparator) {
            return new OrderedReader<>(this, new ItemOrder<>((Comparator<TItem>) comparator, true), (IItemSorter<TItem>) null);
        }
    }
}
