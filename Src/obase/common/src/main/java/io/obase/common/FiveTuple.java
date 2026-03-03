/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：五元组
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:49:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 五元组
 *
 * @param <T1> 第一个元素类型
 * @param <T2> 第二个元素类型
 * @param <T3> 第三个元素类型
 * @param <T4> 第四个元素类型
 * @param <T5> 第五个元素类型
 */
public class FiveTuple<T1, T2, T3, T4, T5> extends FourTuple<T1, T2, T3, T4> {

    /**
     * 第五个元素
     */
    private final T5 item5;

    /**
     * 初始化五元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param item3 第三个元素
     * @param item4 第四个元素
     * @param item5 第五个元素
     */
    public FiveTuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
        super(item1, item2, item3, item4);
        this.item5 = item5;
    }

    /**
     * 获取第五个元素
     *
     * @return 第五个元素
     */
    public T5 getItem5() {
        return this.item5;
    }

    /**
     * 获取元组的泛型参数类型集合
     *
     * @return 元组的泛型参数类型集合
     */
    @Override
    public Class<?>[] getGenericClasses() {
        List<Class<?>> result = Arrays.stream(super.getGenericClasses()).collect(Collectors.toList());
        result.add(this.item5.getClass());
        return result.toArray(new Class<?>[0]);
    }

    /**
     * 获取元组的所有元素
     *
     * @return 元组的所有元素
     */
    @Override
    public Object[] getItems() {
        List<Object> result = Arrays.stream(super.getItems()).collect(Collectors.toList());
        result.add(this.item5);
        return result.toArray(new Object[0]);
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
        if (!super.equals(o)) return false;
        FiveTuple<?, ?, ?, ?, ?> fiveTuple = (FiveTuple<?, ?, ?, ?, ?>) o;
        return Objects.equals(this.item5, fiveTuple.item5);
    }

    /**
     * 重写hashCode
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.item5);
    }
}
