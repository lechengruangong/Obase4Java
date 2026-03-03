/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Property-Get触发器,触发延迟加载.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:15:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Property;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Property-Get触发器
 *
 * @param <TObject> 目标的类型
 * @param <TValue>  值的类型
 */
public class PropertyGetTrigger<TObject, TValue> implements IBehaviorTrigger {


    /**
     * 属性访问器名称
     */
    private final String propertyName;

    /**
     * 重写方法
     */
    private final Method getMethod;

    /**
     * 使用PropertyInfo创建Property-Set触发器实例
     *
     * @param property 表示一个Property，该Property包含一个Set方法，该方法为触发器
     */
    public PropertyGetTrigger(Property property) {
        this.getMethod = property.getGetterMethod();
        this.propertyName = property.getName();
    }

    /**
     * 获取属性访问器名称
     *
     * @return 获取属性访问器名称
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * 获取触发器标识，该标识在当前类型的所有触发器中是唯一的。
     *
     * @return 触发器标识
     */
    @Override
    public String getUniqueId() {
        return this.propertyName;
    }

    /**
     * 生成一个方法，该方法用于在指定的类中重写当前触发器
     *
     * @param type 要重写当前触发器的类
     * @return 触发的方法
     */
    @Override
    public Method override(Class<?> type) {
        return this.getMethod;
    }

    /**
     * 使用反射发出调用触发器的基础实现
     *
     * @param superCall 原方法
     */
    @Override
    public void callBase(Callable<?> superCall) {
        try {
            superCall.call();
        } catch (Exception e) {
            throw new RuntimeException("调用原方法出错,请参考内部异常", e);
        }
    }

    /**
     * 返回触发器实例的哈希代码
     *
     * @return 哈希代码
     */
    @Override
    public int getHashCode() {
        return Objects.hash(this.propertyName, this.getMethod);
    }

    /**
     * 返回一个值，该值指示此实例是否与指定的对象相等。
     *
     * @param other 与此实例进行比较的触发器实例
     * @return 是否与指定的对象相等
     */
    @Override
    public boolean equal(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        PropertyGetTrigger<?, ?> that = (PropertyGetTrigger<?, ?>) other;
        return this.propertyName.equals(that.propertyName) && this.getMethod.equals(that.getMethod);
    }

    /**
     * 重写比较函数
     *
     * @param o 另一个镀锡
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        PropertyGetTrigger<?, ?> that = (PropertyGetTrigger<?, ?>) o;
        return this.propertyName.equals(that.propertyName) && this.getMethod.equals(that.getMethod);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.propertyName, this.getMethod);
    }
}

