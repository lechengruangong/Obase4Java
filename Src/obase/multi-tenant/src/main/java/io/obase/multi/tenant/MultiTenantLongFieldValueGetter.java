/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户long类型主键字段取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:57:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.odm.IValueGetter;

import java.lang.reflect.Field;

/**
 * 多租户long类型主键字段取值器
 */
public class MultiTenantLongFieldValueGetter implements IValueGetter {


    /**
     * 表示要取其值的字段
     */
    private final Field fieldInfo;

    /**
     * 目标类型
     */
    private final Class<?> targetType;

    /**
     * 宿主上下文类
     */
    private final Class<?> hostContextType;

    /**
     * 创建FieldValueSetter实例
     *
     * @param fieldInfo       要取其值的字段
     * @param targetType      目标类型
     * @param hostContextType 宿主上下文类
     */
    public MultiTenantLongFieldValueGetter(Field fieldInfo, Class<?> targetType, Class<?> hostContextType) {
        this.fieldInfo = fieldInfo;
        this.targetType = targetType;
        this.hostContextType = hostContextType;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        if (obj.getClass().equals(this.targetType)) {
            try {
                return this.fieldInfo.get(obj);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("获取多租户字段值失败", e);
            }
        }

        Object value = MultiTenantExtensions.getTenantId(this.hostContextType);

        return Long.parseLong(value.toString());
    }
}
