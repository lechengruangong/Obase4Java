/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：具体类型区别属性的字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:15:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Field;

import static io.obase.core.common.Utils.convertDbValue;

/**
 * 具体类型区别属性的字段设值器
 */
public class ConcreteTypeSignFiledSetter implements IValueSetter {

    /**
     * 定义的字段名称
     */
    private final String fieldName;

    /**
     * 具体类型区别属性的字段设值器
     *
     * @param fieldName 字段名
     */
    public ConcreteTypeSignFiledSetter(String fieldName) {
        this.fieldName = fieldName;
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
        if (obj != null) {
            Field fieldInfo;
            try {
                fieldInfo = obj.getClass().getField(this.fieldName);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("无法获取" + obj.getClass().getName() + "的具体类型判别字段" + this.fieldName, e);
            }
            //目标对象和值对象空判断
            if (value == null) return;
            Class<?> tValueType = fieldInfo.getType();
            value = convertDbValue(value, tValueType);

            fieldInfo.setAccessible(true);
            try {
                fieldInfo.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("无法获取为" + obj.getClass().getName() + "的具体类型判别字段" + this.fieldName + "设置值", e);
            }
        }
    }
}
