/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 11:50:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Field;

/**
 * 字段取值器，使用该取值器可以直接获取表示属性的字段的值
 */
public class FieldValueGetter implements IValueGetter {

    /**
     * 表示要取其值的字段
     */
    private final Field fieldInfo;

    /**
     * 创建FieldValueSetter实例
     *
     * @param fieldInfo 要取其值的字段
     */
    public FieldValueGetter(Field fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    /**
     * 取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        this.fieldInfo.setAccessible(true);
        try {
            return this.fieldInfo.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法从字段" + this.fieldInfo.getName() + "获取值.", e);
        }
    }
}
