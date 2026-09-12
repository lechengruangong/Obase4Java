/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体类型构造器参数配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:52:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.serialization.SerializationConstructorParameter;
import io.obase.core.odm.serialization.SerializationElement;

/**
 * 序列化实体类型构造器参数配置
 */
public class SerializationConstructorParameterConfiguration extends SerializationElementConfiguration {

    /**
     * 对应的构造参数索引
     * 从#0开始
     */
    private final String index;

    /**
     * 是否需要存储
     */
    private final boolean needStorage;

    /**
     * 值转换委托
     */
    private final FunctionWithOneArg<Object, Object> valueConvert;

    /**
     * 初始化序列化实体类型构造器参数配置
     *
     * @param index       对应的构造参数索引
     * @param needStorage 是否需要存储
     * @param valueGetter 取值器
     * @param valueType   值类型
     */
    public SerializationConstructorParameterConfiguration(String index, boolean needStorage, IValueGetter valueGetter, Class<?> valueType) {
        this(index, needStorage, valueGetter, valueType, null);
    }

    /**
     * 初始化序列化实体类型构造器参数配置
     *
     * @param index        对应的构造参数索引
     * @param needStorage  是否需要存储
     * @param valueGetter  取值器
     * @param valueType    值类型
     * @param valueConvert 值转换委托
     */
    public SerializationConstructorParameterConfiguration(String index, boolean needStorage, IValueGetter valueGetter, Class<?> valueType,
                                                          FunctionWithOneArg<Object, Object> valueConvert) {
        super(valueType);
        this.index = index;
        this.needStorage = needStorage;
        this.valueGetter = valueGetter;
        this.valueConvert = valueConvert;
    }

    /**
     * 创建对应的序列化元素
     *
     * @return 序列化元素
     */
    @Override
    public SerializationElement create() {
        //创建序列化实体类型构造器参数
        SerializationConstructorParameter result = new SerializationConstructorParameter(this.index, this.needStorage, this.getValueType(), this.valueConvert);
        result.setValueGetter(this.valueGetter);

        return result;
    }
}
