/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：二元组
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:33:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

import java.util.Objects;

/**
 * 二元组
 *
 * @param <T1> 第一个元素类型
 * @param <T2> 第二个元素类型
 */
public class TwoTuple<T1, T2> implements Tuple {

    /**
     * 第一个元素
     */
    private final T1 item1;

    /**
     * 第二个元素
     */
    private final T2 item2;

    /**
     * 初始化二元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     */
    public TwoTuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    /**
     * 获取第一个元素
     *
     * @return 第一个元素
     */
    public T1 getItem1() {
        return this.item1;
    }

    /**
     * 获取第二个元素
     *
     * @return 第二个元素
     */
    public T2 getItem2() {
        return this.item2;
    }

    /**
     * 获取元组的泛型参数类型集合
     *
     * @return 元组的泛型参数类型集合
     */
    @Override
    public Class<?>[] getGenericClasses() {
        return new Class<?>[]{this.item1.getClass(), this.item2.getClass()};
    }

    /**
     * 获取元组的所有元素
     *
     * @return 元组的所有元素
     */
    @Override
    public Object[] getItems() {
        return new Object[]{this.item1, this.item2};
    }

    /**
     * 重写equals
     *
     * @param o 要比较的对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TwoTuple<?, ?> twoTuple = (TwoTuple<?, ?>) o;
        return Objects.equals(this.item1, twoTuple.item1) && Objects.equals(this.item2, twoTuple.item2);
    }

    /**
     * 重写hashCode
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.item1, this.item2);
    }
}
