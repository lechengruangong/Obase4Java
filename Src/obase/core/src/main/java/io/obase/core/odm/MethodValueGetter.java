/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：方法取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 12:18:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法取值器
 */
public class MethodValueGetter implements IValueGetter {

    /**
     * 方法信息
     */
    private final Method methodInfo;

    /**
     * 初始化方法取值器
     *
     * @param methodInfo 方法信息
     */
    public MethodValueGetter(Method methodInfo) {
        this.methodInfo = methodInfo;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        this.methodInfo.setAccessible(true);
        try {
            return this.methodInfo.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法用方法" + this.methodInfo.getName() + "设置值.", e);
        }
    }
}
