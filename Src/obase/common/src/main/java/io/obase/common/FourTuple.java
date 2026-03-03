/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：四元组
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:44:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 四元组
 *
 * @param <T1> 第一个元素类型
 * @param <T2> 第二个元素类型
 * @param <T3> 第三个元素类型
 * @param <T4> 第四个元素类型
 */
public class FourTuple<T1, T2, T3, T4> extends ThreeTuple<T1, T2, T3> {

    /**
     * 第四个元素
     */
    private final T4 item4;

    /**
     * 初始化四元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param item3 第三个元素
     * @param item4 第四个元素
     */
    public FourTuple(T1 item1, T2 item2, T3 item3, T4 item4) {
        super(item1, item2, item3);
        this.item4 = item4;
    }

    /**
     * 获取第四个元素
     *
     * @return 第四个元素
     */
    public T4 getItem4() {
        return this.item4;
    }

    /**
     * 获取元组的泛型参数类型集合
     *
     * @return 元组的泛型参数类型集合
     */
    @Override
    public Class<?>[] getGenericClasses() {
        List<Class<?>> result = Arrays.stream(super.getGenericClasses()).collect(Collectors.toList());
        result.add(this.item4.getClass());
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
        result.add(this.item4);
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
        FourTuple<?, ?, ?, ?> fourTuple = (FourTuple<?, ?, ?, ?>) o;
        return Objects.equals(this.item4, fourTuple.item4);
    }


    /**
     * 重写hashCode
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.item4);
    }
}
