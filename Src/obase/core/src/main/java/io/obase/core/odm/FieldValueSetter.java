/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 11:52:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Field;

import static io.obase.core.common.Utils.convertDbValue;

/**
 * 字段设值器，使用该设值器可以直接为表示元素的字段设置值
 */
public class FieldValueSetter extends ValueSetter {

    /**
     * 要为其设值的字段
     */
    private final Field fieldInfo;

    /**
     * 创建FieldValueSetter实例
     *
     * @param fieldInfo 要为其设值的字段
     */
    public FieldValueSetter(Field fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    /**
     * 获取设值模式
     *
     * @return 本属性总是返回Assignment
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
    protected void setValueCore(Object obj, Object value) {
        //目标对象和值对象空判断
        if (obj == null || value == null) return;
        Class<?> tValueType = this.fieldInfo.getType();
        value = convertDbValue(value, tValueType);

        this.fieldInfo.setAccessible(true);
        try {
            this.fieldInfo.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法为字段" + this.fieldInfo.getName() + "设置值.", e);
        }
    }
}
