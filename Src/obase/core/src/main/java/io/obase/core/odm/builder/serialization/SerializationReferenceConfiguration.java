/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的引用配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:45:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.core.odm.serialization.SerializationElement;
import io.obase.core.odm.serialization.SerializationReference;

/**
 * 序列化实体的引用配置
 *
 * @param <TStructural> 序列化实体类型
 */
public class SerializationReferenceConfiguration<TStructural> extends SerializationTypeElementConfiguration<TStructural> {

    /**
     * 引用是多重的还是单值的
     */
    private final boolean multiple;

    /**
     * 属性名称
     */
    private final String name;

    /**
     * 初始化序列化实体的引用配置
     *
     * @param multiple 属性名称
     * @param name     引用是多重的还是单值的
     */
    public SerializationReferenceConfiguration(boolean multiple, String name) {
        super(null);
        this.multiple = multiple;
        this.name = name;
    }

    /**
     * 创建对应的序列化元素
     *
     * @return 序列化元素
     */
    @Override
    public SerializationElement create() {
        //创建序列化引用
        SerializationReference result = new SerializationReference(this.multiple, this.name, this.getValueType());
        result.setValueGetter(this.valueGetter);
        result.setValueSetter(this.valueSetter);

        return result;
    }
}
