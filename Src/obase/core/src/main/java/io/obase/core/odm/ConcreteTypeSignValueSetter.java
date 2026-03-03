/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型判别属性的设置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:23:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.HashMap;

/**
 * 类型判别属性的设置器
 */
public class ConcreteTypeSignValueSetter implements IValueSetter {

    /**
     * 判别标识集合1 内存代理类型
     */
    private final HashMap<Class<?>, Object> rebuildingTypeValues;

    /**
     * 判别标识集合1 内存Clr类型
     */
    private final HashMap<Class<?>, Object> clrTypeValues;
    /**
     * 实际的设值器
     */
    private final IValueSetter setter;

    /**
     * 类型判别属性的设置器
     *
     * @param values1 判别标识集合1 内存代理类型
     * @param values2 判别标识集合1 内存Clr类型
     */
    public ConcreteTypeSignValueSetter(HashMap<Class<?>, Object> values1, HashMap<Class<?>, Object> values2, IValueSetter setter) {
        this.rebuildingTypeValues = values1;
        this.clrTypeValues = values2;
        this.setter = setter;
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
        Object reavalue = null;
        if (this.rebuildingTypeValues.containsKey(obj.getClass()))
            reavalue = this.rebuildingTypeValues.get(obj.getClass());
        if (this.clrTypeValues.containsKey(obj.getClass()))
            reavalue = this.clrTypeValues.get(obj.getClass());
        if (this.setter != null)
            this.setter.setValue(obj, reavalue);
    }
}
