/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:36:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.ITextSerializer;

/**
 * 序列化设值器
 */
public class SerializedValueSetter implements IValueSetter {

    /**
     * 基础设值器
     */
    private final IValueSetter baseSetter;

    /**
     * 序列化器
     */
    private final ITextSerializer serializer;

    /**
     * 反序列化后的类型
     */
    private final Class<?> valueType;

    /**
     * 初始化序列化设值器
     *
     * @param baseSetter 基础设值器
     * @param serializer 序列化器
     * @param valueType  反序列化后的类型
     */
    public SerializedValueSetter(IValueSetter baseSetter, ITextSerializer serializer, Class<?> valueType) {
        this.baseSetter = baseSetter;
        this.serializer = serializer;
        this.valueType = valueType;
    }

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    @Override
    public EValueSettingMode getMode() {
        return this.baseSetter.getMode();
    }

    /**
     * 获取基础设值器
     *
     * @return 基础设值器
     */
    public IValueSetter getBaseSetter() {
        return this.baseSetter;
    }

    /**
     * 为对象设值
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    @Override
    public void setValue(Object obj, Object value) {
        //按照字符串处理
        String stringValue = value.toString();
        //反序列化
        Object realObj = this.serializer.deserialize(stringValue, this.valueType);
        //设置值
        this.baseSetter.setValue(obj, realObj);
    }
}
