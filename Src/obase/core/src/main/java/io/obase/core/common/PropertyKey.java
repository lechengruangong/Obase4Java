/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase的内省属性的缓存键.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-16 17:01:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.util.Objects;

/**
 * Obase的内省属性的缓存键
 */
public class PropertyKey {

    /**
     * 当前类型
     */
    private final Class<?> beanClass;

    /**
     * 基类
     */
    private final Class<?> superClass;

    /**
     * 初始化Obase的内省属性的缓存键
     *
     * @param beanClass  当前类型
     * @param superClass 基类
     */
    PropertyKey(Class<?> beanClass, Class<?> superClass) {
        this.beanClass = beanClass;
        this.superClass = superClass;
    }

    /**
     * 获取当前类型
     *
     * @return 当前类型
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    /**
     * 获取基类
     *
     * @return 基类
     */
    public Class<?> getSuperClass() {
        return this.superClass;
    }

    /**
     * 重写比较方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        PropertyKey that = (PropertyKey) o;
        return this.beanClass.equals(that.beanClass) && this.superClass.equals(that.superClass);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.beanClass, this.superClass);
    }
}
