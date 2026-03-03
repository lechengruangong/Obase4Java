/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：最小堆,堆排序中用于表示排序节点的数据结构.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 16:39:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToIntFunction;

/**
 * 表示一个最小堆 即一棵完全二叉树 且所有非叶结点的值均不大于其子女的值
 *
 * @param <T> 元素类型
 */
public class MinHeap<T> {

    /**
     * 默认的容量
     */
    private static final int DefaultCapacity = 5000;

    /**
     * 比较器 用于比较堆内元素
     */
    private final Comparator<T> comparator;


    /**
     * 堆内当前的元素数量
     */
    private int count;

    /**
     * 堆元素集合
     */
    private T[] items;

    /**
     * 构造一个最小堆 并指定 堆内元素比较器 容量
     *
     * @param comparator 堆内元素比较器
     * @param capacity   容量
     */
    public MinHeap(Comparator<T> comparator, int capacity) {
        if (capacity < 0) throw new ArrayIndexOutOfBoundsException("堆容量不能小于0.");
        this.items = (T[]) new Object[capacity];
        this.comparator = comparator;
    }

    /**
     * 构造一个最小堆 并指定 堆内元素比较器
     *
     * @param comparator 堆内元素比较器
     */
    public MinHeap(Comparator<T> comparator) {
        this(comparator, DefaultCapacity);
    }

    /**
     * 构造一个最小堆 并指定 堆内元素比较方法
     *
     * @param keyExtractor 堆内元素比较方法
     */
    public MinHeap(ToIntFunction<? super T> keyExtractor) {
        this(Comparator.comparingInt(keyExtractor), DefaultCapacity);
    }

    /**
     * 获取堆内当前的元素数量
     *
     * @return 堆内当前的元素数量
     */
    public int getCount() {
        return this.count;
    }

    /**
     * 增加元素到堆 并从后往前依次对各结点为根的子树进行筛选 直至成为最小堆
     *
     * @param value 元素
     * @return 是否成功
     */
    public boolean enqueue(T value) {
        //存储空间已满
        if (this.count == this.items.length)
            //扩容至两倍
            this.resizeItemStore(this.items.length * 2);

        this.items[this.count++] = value;
        int position = this.bubbleUp(this.count - 1);

        return position == 0;
    }

    /**
     * 从堆内取出元素 并从前往后依次对各结点为根的子树进行筛选 直至成为最小堆
     *
     * @param shrink 取出元素后是否尝试收缩空间
     * @return 元素
     */
    public T dequeue(Boolean shrink) {
        if (this.count == 0) throw new IllegalArgumentException("已无元素可取出.");
        T result = this.items[0];
        if (this.count == 1) {
            this.count = 0;
            this.items[0] = null;
        } else {
            --this.count;
            //取序列最后的元素放在堆顶
            this.items[0] = this.items[this.count];
            this.items[this.count] = null;
            // 维护堆的结构
            this.bubbleDown();
        }

        //是非收缩空间
        if (shrink) this.shrinkStore();
        return result;
    }

    /**
     * 用适当的方式比较两个对象
     *
     * @param x 对象1
     * @param y 对象2
     * @return 比较结果
     */
    private int compare(T x, T y) {
        int result = 0;
        if (this.comparator != null)
            result = this.comparator.compare(x, y);

        return result;
    }

    /**
     * 从前往后依次对各结点为根的子树进行筛选，使之成为堆，直到序列最后的节点
     */
    private void bubbleDown() {
        int parent = 0;
        int leftChild = 1;
        while (leftChild < this.count) {
            // 找到子节点中较小的那个
            int rightChild = leftChild + 1;
            int bestChild = rightChild < this.count && this.compare(this.items[rightChild], this.items[leftChild]) < 0
                    ? rightChild
                    : leftChild;
            if (this.compare(this.items[bestChild], this.items[parent]) < 0) {
                // 如果子节点小于父节点, 交换子节点和父节点
                T temp = this.items[parent];
                this.items[parent] = this.items[bestChild];
                this.items[bestChild] = temp;
                parent = bestChild;
                leftChild = parent * 2 + 1;
            } else {
                break;
            }
        }
    }

    /**
     * 当元素增加时 重新调整堆内元素存储空间
     *
     * @param newSize 新大小
     */
    private void resizeItemStore(int newSize) {
        //不需要扩容 直接返回
        if (this.count < newSize || DefaultCapacity <= newSize) return;
        //扩容至指定的大小
        this.items = Arrays.copyOfRange(this.items, 0, this.count);
    }

    /**
     * 收缩存储空间
     */
    private void shrinkStore() {
        // 如果容量不足一半以上，默认容量会下降。
        if (this.items.length > DefaultCapacity && this.count < this.items.length >> 1) {
            int newSize = Math.max(
                    DefaultCapacity, (this.count / DefaultCapacity + 1) * DefaultCapacity);

            this.resizeItemStore(newSize);
        }
    }

    /**
     * 从后往前依次对各结点为根的子树进行筛选 使之成为堆 直到根结点
     *
     * @param startIndex 开始的索引
     * @return 索引
     */
    private int bubbleUp(int startIndex) {
        while (startIndex > 0) {
            //求出父节点Index
            int parent = (startIndex - 1) / 2;
            //如果子节点小于父节点，交换子节点和父节点
            if (this.compare(this.items[startIndex], this.items[parent]) < 0) {
                T temp = this.items[startIndex];
                this.items[startIndex] = this.items[parent];
                this.items[parent] = temp;
            } else {
                break;
            }

            startIndex = parent;
        }

        return startIndex;
    }
}
