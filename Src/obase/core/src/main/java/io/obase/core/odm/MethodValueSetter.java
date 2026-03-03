/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：方法设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 12:19:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.obase.core.common.Utils.convertDbValue;

/**
 * 方法设值器
 */
public class MethodValueSetter implements IValueSetter {

    /**
     * 方法信息
     */
    private final Method methodInfo;

    /**
     * 初始化方法设值器
     *
     * @param methodInfo 方法信息
     */
    public MethodValueSetter(Method methodInfo) {
        this.methodInfo = methodInfo;
    }

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    @Override
    public EValueSettingMode getMode() {
        return EValueSettingMode.Assignment;
    }

    /**
     * 为对象设值
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    @Override
    public void setValue(Object obj, Object value) {
        //目标对象和值对象空判断
        if (obj == null || value == null) return;
        Class<?> tValueType = Arrays.stream(this.methodInfo.getParameterTypes()).findFirst().orElse(null);
        if (tValueType == null)
            throw new RuntimeException("无法获取设值方法" + this.methodInfo.getName() + "的参数值.");
        value = convertDbValue(value, tValueType);

        this.methodInfo.setAccessible(true);
        try {
            this.methodInfo.invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法用方法" + this.methodInfo.getName() + "设置值.", e);
        }
    }
}
