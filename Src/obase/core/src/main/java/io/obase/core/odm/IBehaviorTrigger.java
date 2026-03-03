/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象行为触发器接口,提供代理类的触发器重写规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 16:26:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 对象行为触发器接口。（代理类才有触发器）
 */
public interface IBehaviorTrigger {

    /**
     * 获取触发器标识，该标识在当前类型的所有触发器中是唯一的。
     *
     * @return 触发器标识
     */
    String getUniqueId();

    /**
     * 生成一个方法，该方法用于在指定的类中重写当前触发器
     *
     * @param type 要重写当前触发器的类
     * @return 指定的类中重写当前触发器的方法
     */
    Method override(Class<?> type);

    /**
     * 使用反射发出调用触发器的基础实现
     *
     * @param superCall 原方法
     */
    void callBase(Callable<?> superCall);

    /**
     * 返回触发器实例的哈希代码
     *
     * @return 哈希代码
     */
    int getHashCode();

    /**
     * 返回一个值，该值指示此实例是否与指定的对象相等。
     *
     * @param other 与此实例进行比较的触发器实例
     * @return 是否与指定的对象相等
     */
    boolean equal(Object other);
}
