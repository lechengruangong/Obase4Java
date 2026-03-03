/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除字段取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:02:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.StructuralType;

import java.lang.reflect.Field;

/**
 * 逻辑删除字段取值器
 */
public class LogicDeletionFieldValueGetter implements IValueGetter {

    /**
     * 表示要取其值的字段
     */
    private final Field fieldInfo;

    /**
     * 目标的结构化类型
     */
    private final StructuralType structuralType;

    /**
     * 创建FieldValueSetter实例
     *
     * @param fieldInfo  要取其值的字段
     * @param targetType 目标类型
     */
    public LogicDeletionFieldValueGetter(Field fieldInfo, StructuralType targetType) {
        this.fieldInfo = fieldInfo;
        this.structuralType = targetType;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        Class<?> currentType = obj.getClass();
        StructuralType structuralType = this.structuralType;
        //和自己相同 直接返回对象里保存的值
        if (structuralType.getRebuildingType().equals(currentType)) {
            try {
                return this.fieldInfo.get(obj);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("无法获取逻辑删除字段的值.", e);
            }
        }

        //向下寻找
        return this.getDerivedValue(structuralType, currentType, obj);
    }

    /**
     * 查找所有的子类型 以确定具体的值
     *
     * @param structuralType 结构化类型
     * @param currentType    当前运行时类型
     * @param obj            对象
     * @return 字段的值
     */
    private Object getDerivedValue(StructuralType structuralType, Class<?> currentType, Object obj) {
        for (StructuralType derivedType : structuralType.getDerivedTypes()) {
            if (derivedType.getRebuildingType().equals(currentType)) {
                try {
                    return derivedType.getRebuildingType().getField(this.fieldInfo.getName()).get(obj);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new IllegalArgumentException("无法获取逻辑删除字段的值.", e);
                }
            }
            this.getDerivedValue(derivedType, currentType, obj);
        }

        return false;
    }
}
