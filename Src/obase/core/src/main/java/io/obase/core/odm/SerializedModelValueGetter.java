/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：有模型的序列化取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 13:48:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.ITextSerializer;
import io.obase.core.common.Utils;
import io.obase.core.odm.serialization.SerializationObjectDataModel;
import io.obase.core.odm.serialization.SerializationObjectDataModelSerializer;

import java.util.List;

/**
 * 有模型的序列化取值器
 */
public class SerializedModelValueGetter implements IValueGetter {

    /**
     * 基础取值器
     */
    private final IValueGetter baseValueGetter;

    /**
     * 序列化对象数据模型
     */
    private final SerializationObjectDataModel model;

    /**
     * 序列化器
     */
    private final ITextSerializer serializer;

    /**
     * 初始化有模型的序列化取值器
     *
     * @param baseValueGetter 基础取值器
     * @param serializer      序列化对象数据模型
     * @param model           序列化器
     */
    public SerializedModelValueGetter(IValueGetter baseValueGetter, ITextSerializer serializer, SerializationObjectDataModel model) {
        this.baseValueGetter = baseValueGetter;
        this.serializer = serializer;
        this.model = model;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        //取值
        Object value = this.baseValueGetter.getValue(obj);
        //转换为列表
        List<Object> targets = Utils.getObjectList(value);
        //创建序列化器
        SerializationObjectDataModelSerializer serializer = new SerializationObjectDataModelSerializer(this.model);
        //序列化
        return this.serializer.serialize(serializer.serialize(targets));
    }
}
