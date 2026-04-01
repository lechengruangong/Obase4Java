/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的属性配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:38:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.core.odm.serialization.SerializationAttribute;
import io.obase.core.odm.serialization.SerializationElement;

/**
 * 序列化实体的属性配置
 *
 * @param <TStructural> 序列化实体类型
 */
public class SerializationAttributeConfiguration<TStructural> extends SerializationTypeElementConfiguration<TStructural> {

    /**
     * 属性名称
     */
    private final String name;

    /**
     * 初始化序列化实体的属性配置
     *
     * @param name      属性名称
     * @param valueType 值类型
     */
    public SerializationAttributeConfiguration(String name, Class<?> valueType) {
        super(valueType);
        this.name = name;
    }


    /**
     * 创建对应的序列化元素
     *
     * @return 序列化元素
     */
    @Override
    public SerializationElement create() {
        //创建序列化属性
        SerializationAttribute result = new SerializationAttribute(this.name, this.getValueType());
        result.setValueGetter(this.valueGetter);
        result.setValueSetter(this.valueSetter);

        return result;
    }
}
