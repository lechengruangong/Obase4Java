/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：有模型的序列化设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:10:25
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.ITextSerializer;
import io.obase.core.odm.serialization.SerializationDataTransferObjectWrapper;
import io.obase.core.odm.serialization.SerializationObjectDataModel;
import io.obase.core.odm.serialization.SerializationObjectDataModelDeSerializer;

import java.util.List;

/**
 * 有模型的序列化设值器
 */
public class SerializedModelValueSetter implements IValueSetter {

    /**
     * 基础设值器
     */
    private final IValueSetter baseValueSetter;

    /**
     * 所属的属性是否为多重的
     */
    private final boolean isAttitudeMultiple;

    /**
     * 序列化对象数据模型
     */
    private final SerializationObjectDataModel model;

    /**
     * 序列化器
     */
    private final ITextSerializer serializer;

    /**
     * 反序列化后的类型
     */
    private final Class<?> valueType;

    /**
     * 初始化有模型的序列化设值器
     *
     * @param baseValueSetter    基础设值器
     * @param isAttitudeMultiple 所属的属性是否为多重的
     * @param model              序列化对象数据模型
     * @param serializer         序列化器
     * @param valueType          反序列化后的类型
     */
    public SerializedModelValueSetter(IValueSetter baseValueSetter, boolean isAttitudeMultiple, SerializationObjectDataModel model, ITextSerializer serializer, Class<?> valueType) {
        this.baseValueSetter = baseValueSetter;
        this.isAttitudeMultiple = isAttitudeMultiple;
        this.model = model;
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
        return this.baseValueSetter.getMode();
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
        //创建反序列化器
        SerializationObjectDataModelDeSerializer deSerializer = new SerializationObjectDataModelDeSerializer(this.model);
        //反序列化后的对象集合
        List<Object> objects = deSerializer.deSerialize((SerializationDataTransferObjectWrapper) realObj);
        if (objects != null && objects.size() > 0) {
            //如果是多值的属性 直接设置 否则 设置首个
            if (this.isAttitudeMultiple) {
                this.baseValueSetter.setValue(obj, objects);
            } else {
                this.baseValueSetter.setValue(obj, objects.get(0));
            }
        }
    }
}
