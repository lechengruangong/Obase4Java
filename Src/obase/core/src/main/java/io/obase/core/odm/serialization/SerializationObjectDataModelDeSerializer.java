/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型对象反序列化器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:16:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.util.List;

/**
 * 序列化对象数据模型对象反序列化器
 */
public class SerializationObjectDataModelDeSerializer {

    /**
     * 序列化对象数据模型对象
     */
    private final SerializationObjectDataModel model;

    /**
     * 初始化序列化对象数据模型对象反序列化器
     *
     * @param model 模型
     */
    public SerializationObjectDataModelDeSerializer(SerializationObjectDataModel model) {
        this.model = model;
    }

    /**
     * 序列化对象数据模型的反序列化方法
     * 最终返回反序列化后的对象集合 其顺序与传入的Dto集合中根对象的顺序一致
     *
     * @param wrapper Dto的包装对象
     * @return 反序列化后的对象集合
     */
    public List<Object> deSerialize(SerializationDataTransferObjectWrapper wrapper) {
        return null;
    }
}
