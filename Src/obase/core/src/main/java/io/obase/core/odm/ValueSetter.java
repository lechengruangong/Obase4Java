/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：设值器的基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:55:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ActionWithTwoArg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.obase.core.common.Utils.makeDelegateValueSetter;

/**
 * 为设值器提供基础实现
 */
public abstract class ValueSetter implements IValueSetter {
    /**
     * 为指定字段所代表的元素创建设值器实例
     *
     * @param field 字段
     * @return 字段设值器
     */
    public static ValueSetter create(Field field) {
        return new FieldValueSetter(field);
    }

    /**
     * 创建一个设值器实例，该设值器通过调用指定的委托为元素设值
     *
     * @param method 设值方法
     * @param mode   设值模式
     * @return 设值器
     */
    public static ValueSetter create(Method method, EValueSettingMode mode) {
        Class<?> parameterType = method.getParameterTypes()[0];

        //包装类转换
        if (parameterType.isPrimitive()) {
            switch (parameterType.getName()) {
                case "short":
                    parameterType = Short.class;
                    break;
                case "int":
                    parameterType = Integer.class;
                    break;
                case "long":
                    parameterType = Long.class;
                    break;
                case "byte":
                    parameterType = Byte.class;
                    break;
                case "char":
                    parameterType = Character.class;
                    break;
                case "float":
                    parameterType = Float.class;
                    break;
                case "double":
                    parameterType = Double.class;
                    break;
                case "boolean":
                    parameterType = Boolean.class;
                    break;
            }
        }

        return makeDelegateValueSetter(method, parameterType);
    }

    /**
     * 创建一个设值器实例，该设值器通过调用指定的委托为元素设值
     *
     * @param setValue  设值委托
     * @param mode      设值模式
     * @param <TObject> 要设置的类型
     * @param <TValue>  值的类型
     * @return 设值器实例
     */
    public static <TObject, TValue> ValueSetter create(ActionWithTwoArg<TObject, TValue> setValue, EValueSettingMode mode) {
        return new DelegateValueSetter<>(setValue, mode, null);
    }

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    public abstract EValueSettingMode getMode();

    /**
     * 调用SetValueCore
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    public void setValue(Object obj, Object value) {
        this.setValueCore(obj, value);
    }

    /**
     * 执行为对象设值的核心逻辑。由派生类实现
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    protected abstract void setValueCore(Object obj, Object value);
}
