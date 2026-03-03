/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提供访问对象上下文的快捷方式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:45:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.expression.SerializedPredicate;

import java.util.Map;

/**
 * 提供访问对象上下文的快捷方式，每次访问均会创建新的上下文实例
 */
public class ObaseGoStatic {

    /**
     * 应用程序域内默认的对象上下文类型
     */
    private static Class<?> defaultContextType;

    /**
     * 获取一个新的对象上下文。
     *
     * @return 新的对象上下文
     */
    public static ObjectContext getObjectContext() {
        if (ObaseGoStatic.defaultContextType == null)
            throw new IllegalArgumentException("未设置应用程序域内默认的对象上下文类型");
        try {
            return (ObjectContext) ObaseGoStatic.defaultContextType.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("无法创建对象上下文,请参考内部异常.", e);
        }
    }


    /**
     * 创建一个对象上下文，基于它根据传入的筛选条件删除对象。
     *
     * @param filterExpression 筛选条件
     * @param clazz            对象的类型
     * @param <T>              对象的类型
     * @return 受影响的行数
     */
    public static <T> int delete(SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return getObjectContext().createSet(clazz).delete(filterExpression, clazz);
    }

    /**
     * 创建一个对象上下文，基于它根据筛选条件为对象的属性设置新值。
     *
     * @param newValues        存储增量值的键值对集合，其中键为属性名称，值为属性的新值。
     * @param filterExpression 筛选条件
     * @param clazz            对象的类型
     * @param <T>              对象的类型
     * @return 受影响的行数
     */
    public static <T> int setAttributes(Map<String, Object> newValues, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return getObjectContext().createSet(clazz).setAttributes(newValues, filterExpression, clazz);
    }

    /**
     * 创建一个对象上下文，基于它根据筛选条件为对象的属性设置新值，其中新值为原值加上增量值。属性必须为数值类型。
     *
     * @param newValues        存储增量值的键值对集合，其中键为属性名称，值为增量值。
     * @param filterExpression 筛选条件
     * @param clazz            对象的类型
     * @param <T>              对象的类型
     * @return 受影响的行数
     */
    public static <T> int increaseAttributes(Map<String, Object> newValues, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return getObjectContext().createSet(clazz).increaseAttributes(newValues, filterExpression, clazz);
    }

    /**
     * 创建一个对象上下文，基于它对指定的新对象实施持久化。
     *
     * @param obj 要保存的对象
     * @param <T> 新对象的类型
     */
    public static <T> void saveNew(T obj) {
        ObjectContext context = getObjectContext();
        context.attach(obj);
        context.saveChanges();
    }

    /**
     * 设置应用程序域内默认的对象上下文类型
     *
     * @param defaultContextType 对象上下文类型
     * @param <TContext>         对象上下文类型
     */
    public static <TContext extends ObjectContext> void SetDefault(Class<TContext> defaultContextType) {
        if (ObaseGoStatic.defaultContextType != null)
            throw new UnsupportedOperationException("已设置默认的对象上下文类型" + ObaseGoStatic.defaultContextType.getName());

        ObaseGoStatic.defaultContextType = defaultContextType;
    }
}
