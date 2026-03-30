/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型对象序列化器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:03:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.util.List;

/**
 * 序列化对象数据模型对象序列化器
 */
public class SerializationObjectDataModelSerializer {

    /**
     * 序列化对象数据模型对象
     */
    private final SerializationObjectDataModel model;


    /**
     * 初始化序列化对象数据模型对象序列化器
     *
     * @param model 序列化对象数据模型对象
     */
    public SerializationObjectDataModelSerializer(SerializationObjectDataModel model) {
        this.model = model;
    }

    /**
     * 序列化对象数据模型的序列化方法
     * 最终返回Dto的包装对象
     *
     * @param list 要序列化的对象 无论是单值还是多值 都处理为List传入
     * @return Dto的包装对象
     */
    public SerializationDataTransferObjectWrapper serialize(List<Object> list) {
        return null;
    }
}
