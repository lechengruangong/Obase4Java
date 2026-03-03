/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户的字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:37:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.common.Utils;
import io.obase.core.odm.EValueSettingMode;
import io.obase.core.odm.IValueSetter;
import io.obase.core.odm.StructuralType;

import java.lang.reflect.Field;

/**
 * 多租户的字段设值器
 */
public class MultiTenantFieldValueSetter implements IValueSetter {

    /**
     * 表示要设值的字段
     */
    private final Field fieldInfo;

    /**
     * 目标的结构化类型
     */
    private final StructuralType structuralType;

    /**
     * 多租户的字段设值器
     *
     * @param fieldInfo  设值的字段
     * @param targetType 结构化类型
     */
    public MultiTenantFieldValueSetter(Field fieldInfo, StructuralType targetType) {
        this.fieldInfo = fieldInfo;
        this.structuralType = targetType;
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
        Class<?> currentType = obj.getClass();
        if (this.structuralType.getRebuildingType().equals(currentType))
            this.setValueCore(this.fieldInfo, obj, value);
        else
            this.setDerivedValue(this.structuralType, currentType, obj, value);
    }

    /**
     * 实际设值
     *
     * @param fieldInfo 字段
     * @param obj       对象
     * @param value     值
     */
    private void setValueCore(Field fieldInfo, Object obj, Object value) {
        if (value == null || obj == null)
            return;

        Class<?> tValueType = fieldInfo.getType();

        value = Utils.convertDbValue(value, tValueType);
        try {
            fieldInfo.setAccessible(true);
            fieldInfo.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("设置多租户字段值失败.", e);
        }
    }

    /**
     * 查找所有的子类型 以确定具体的值
     *
     * @param structuralType 结构化类型
     * @param currentType    当前的运行时类型
     * @param obj            对象
     * @param value          值
     */
    private void setDerivedValue(StructuralType structuralType, Class<?> currentType, Object obj, Object value) {
        for (StructuralType derivedType : structuralType.getDerivedTypes()) {
            if (derivedType.getRebuildingType().equals(currentType)) {
                try {
                    this.setValueCore(derivedType.getRebuildingType().getField(this.fieldInfo.getName()), obj, value);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("设置多租户字段值失败.", e);
                }
                return;
            }

            this.setDerivedValue(derivedType, currentType, obj, value);
        }
    }
}
