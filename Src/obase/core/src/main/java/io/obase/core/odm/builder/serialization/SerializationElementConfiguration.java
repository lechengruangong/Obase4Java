/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的类型元素配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:26:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.serialization.SerializationElement;

/**
 * 序列化实体的类型元素配置
 */
public abstract class SerializationElementConfiguration {

    /**
     * 类型元素的值类型
     */
    private final Class<?> valueType;

    /**
     * 类型元素的值获取器
     */
    protected IValueGetter valueGetter;

    /**
     * 初始化序列化实体的类型元素配置
     *
     * @param valueType 类型元素的值类型
     */
    protected SerializationElementConfiguration(Class<?> valueType) {
        this.valueType = valueType;
    }

    /**
     * 获取类型元素的值类型
     *
     * @return 类型元素的值类型
     */
    public Class<?> getValueType() {
        return this.valueType;
    }

    /**
     * 获取类型元素的值获取器
     *
     * @return 类型元素的值获取器
     */
    public IValueGetter getValueGetter() {
        return this.valueGetter;
    }

    /**
     * 创建对应的序列化元素
     *
     * @return 序列化元素
     */
    public abstract SerializationElement create();
}
