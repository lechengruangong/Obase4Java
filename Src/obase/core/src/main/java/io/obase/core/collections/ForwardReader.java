/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：只进读取器基础实现,提供只进读取器基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-23 10:25:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;

/**
 * 为采用预读机制的只进读取器提供基础实现
 *
 * @param <T> 集合元素的类型
 */
public abstract class ForwardReader<T> implements IForwardReaderGeneric<T> {

    /**
     * 存储预读取元素的缓冲区
     */
    protected final Queue<T> preReadItems;

    /**
     * 每次预读时最多读取的元素数
     */
    private int preReadMax;

    /**
     * 创建ForwardReader实例，并将预读上限数设置为默认值
     */
    protected ForwardReader() {
        this(256);
    }

    /**
     * 创建ForwardReader实例，并指定预读上限数
     *
     * @param preReadMax 每次预读时最多读取的元素数
     */
    private ForwardReader(int preReadMax) {
        this.preReadMax = preReadMax;
        this.preReadItems = new ArrayDeque<>();
    }

    /**
     * 获取每次预读时最多读取的元素数
     *
     * @return 每次预读时最多读取的元素数
     */
    public int getPreReadMax() {
        return this.preReadMax;
    }

    /**
     * 设置每次预读时最多读取的元素数
     *
     * @param preReadMax 每次预读时最多读取的元素数
     */
    public void setPreReadMax(int preReadMax) {
        this.preReadMax = preReadMax;
    }

    /**
     * 获取一个值，该值指示“只进”读取器是否可重置
     *
     * @return 是否可重置
     */
    public abstract boolean getResettable();

    /**
     * 获取读取器当前位置的元素
     *
     * @return 当读取器位于“只进”流的起始位置或结束位置时，返回null
     */
    public abstract T getCurrent();

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 包含元素的只进读取器
     */
    public abstract IForwardReaderGeneric<T> take(int count);

    /**
     * 从当前位置读取指定个数的元素
     *
     * @param count       要读取的元素个数
     * @param resultCount 内含一个整数 表示实际读取到的元素个数 当流中当前位置之后的元素数少于请求数时，实际读取数将小于请求数
     * @return 包含元素的只进读取器
     */
    public abstract IForwardReaderGeneric<T> take(int count, ObjectReferencePack<Integer> resultCount);

    /**
     * 使用指定的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    public abstract IOrderedReaderGeneric<T> orderBy(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（升序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      排序键的类型
     * @return 代表排序结果的新的只进读取器
     */
    public abstract <TKey> IOrderedReaderGeneric<T> orderBy(FunctionWithOneArg<T, TKey> keySelector);

    /**
     * 使用指定的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param comparator 排序比较器
     * @return 代表排序结果的新的只进读取器
     */
    public abstract IOrderedReaderGeneric<T> orderByDescending(Comparator<?> comparator);

    /**
     * 使用指定的排序键和默认的比较器生成一个代表排序（降序）结果的只进读取器，实际的排序操作将在结果流被首次读取时执行。
     *
     * @param keySelector 用于计算排序键的函数式接口
     * @param <TKey>      排序键的类型
     * @return 代表排序结果的新的只进读取器
     */
    public abstract <TKey> IOrderedReaderGeneric<T> orderByDescending(FunctionWithOneArg<T, TKey> keySelector);

    /**
     * 关闭读取器
     */
    public void close() {
        this.doClosing();
    }

    /**
     * 将读取器向前移动到下一个元素。
     * 刚被初始化或重置时，读取器位于只进流的起始位置，即第一个元素之前；读取结束后位置流的结束位置，即最后一个元素之后。
     *
     * @return 如果移动成功返回true，如果已位于最后一个元素返回false。
     */
    public abstract boolean read();

    /**
     * 将读取器向前移动指定个数的元素。
     *
     * @param count 提升的元素个数
     * @return 实际移动数。当流中当前位置之后的元素数少于请求数时，实际移动数将小于请求数。
     */
    public int skip(int count) {
        //在调用由派生类实现的DoSkipping方法时要注意，由于采用了预读策略，读取器的当前位置与基础流的当前位置是不一致的。
        //比预读的少
        if (this.preReadItems.size() > count) {
            //直接跳掉若干个预读的元素
            int i = 0;
            while (i < count) {
                this.preReadItems.poll();
                i++;
            }

            //返回0
            return 0;
        }

        //比预读的多 预读的跳掉
        int skipCount = this.doSkipping(count - this.preReadItems.size());

        this.preReadItems.clear();

        return skipCount;
    }

    /**
     * 将读取器回退到只进流的起始位置（第一个元素之前）
     *
     * @throws UnsupportedOperationException 如果当前只进流不可重置则引发异常
     */
    public void reset() {
        if (!this.getResettable()) throw new UnsupportedOperationException("当前只进流不可重置");

        this.doResetting();
    }

    /**
     * 枚举已预读到缓冲区的元素
     *
     * @return 已预读到缓冲区的枚举器
     */
    protected Iterator<T> enumeratePreRead() {
        return this.preReadItems.iterator();
    }

    /**
     * 从集合只进流中预读指定数量的元素放入缓冲区。
     *
     * @param maxCount 最大个数
     * @return 读取到的元素的集合，未读到任何元素返回null。当流中当前位置之后的元素数小于请求数时，实际读取到的元素会小于请求数
     */
    protected abstract T[] preRead(int maxCount);

    /**
     * 将读取器向前移动指定个数的元素
     *
     * @param number 移动的个数
     * @return 实际移动数。当流中当前位置之后的元素数少于请求数时，实际移动数将小于请求数。
     */
    protected abstract int doSkipping(int number);

    /**
     * 将读取器回退到只进流的起始位置（第一个元素之前）。
     * 在派生类中实现时，如果当前集合的只进流不可重置，则不执行任何操作。
     */
    protected abstract void doResetting();

    /**
     * 关闭读取器。
     */
    protected abstract void doClosing();
}
