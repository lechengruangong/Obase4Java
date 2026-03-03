/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：方法触发器,调用方法作为触发条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 15:00:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * 方法触发器
 */
public class MethodTrigger implements IBehaviorTrigger {

    /**
     * 一个MethodInfo实例，该实例表示触发对象行为的方法
     */
    private final Method methodInfo;

    /**
     * 创建方法触发器实例
     *
     * @param method 方法
     */
    public MethodTrigger(Method method) {
        this.methodInfo = method;
    }

    /**
     * 获取一个MethodInfo实例，该实例表示触发对象行为的方法。
     *
     * @return MethodInfo实例
     */
    public Method getMethodInfo() {
        return this.methodInfo;
    }


    /**
     * 获取触发器标识，该标识在当前类型的所有触发器中是唯一的。
     *
     * @return 触发器标识
     */
    @Override
    public String getUniqueId() {
        return this.methodInfo.getName() + Arrays.stream(this.methodInfo.getGenericParameterTypes()).map(Type::getTypeName).collect(Collectors.joining("_"));
    }

    /**
     * 生成一个方法，该方法用于在指定的类中重写当前触发器
     *
     * @param type 要重写当前触发器的类
     * @return 触发的方法
     */
    @Override
    public Method override(Class<?> type) {

        Class<?>[] parameters = Arrays.stream(this.methodInfo.getParameters()).map(p -> p.getAnnotatedType().getClass()).toArray(Class<?>[]::new);
        try {
            return type.getMethod(this.methodInfo.getName(), parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取方法" + this.methodInfo.getName(), e);
        }
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
     * 返回触发器实例的哈希代码。
     *
     * @return 哈希码
     */
    @Override
    public int getHashCode() {
        return this.methodInfo.hashCode();
    }

    /**
     * 比较二者是否相等
     *
     * @param other 与此实例进行比较的触发器实例
     * @return 是否相等
     */
    @Override
    public boolean equal(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        MethodTrigger that = (MethodTrigger) other;
        return Objects.equals(this.methodInfo, that.methodInfo);
    }

}
