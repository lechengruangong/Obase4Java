/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：用于兼容对象引用传递的包装器
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:55:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

import java.util.Objects;

/**
 * 用于兼容对象引用传递的包装器
 * 需要注意的是 此种方式也可以传递out参数 但本质上是ref参数
 */
public class ObjectReferencePack<T> {

    /**
     * 真实值作为一个字段存在
     */
    public T realValue;

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
        ObjectReferencePack<?> pack = (ObjectReferencePack<?>) o;
        return this.realValue.equals(pack.realValue);
    }

    /**
     * 重写hashCode
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.realValue);
    }
}
