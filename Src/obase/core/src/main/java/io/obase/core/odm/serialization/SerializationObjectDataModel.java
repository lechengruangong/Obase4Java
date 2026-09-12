/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型,此模型全局应只有一个.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 12:19:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 序列化对象数据模型
 */
public class SerializationObjectDataModel {

    /**
     * 保护structuralTypes的锁对象
     */
    private final Object structuralTypesSyncRoot = new Object();

    /**
     * clr类型与模型字典
     * 使用LinkedHashMap加锁保护，既保证线程安全，又保持类型的插入顺序
     */
    private final LinkedHashMap<Class<?>, SerializationEntity> structuralTypes = new LinkedHashMap<>();

    /**
     * 获取模型类型集合
     *
     * @return 模型类型集合
     */
    public List<SerializationEntity> getTypes() {
        synchronized (this.structuralTypesSyncRoot) {
            return new ArrayList<>(this.structuralTypes.values());
        }
    }

    /**
     * 向模型添加类型
     *
     * @param modelType 要添加到模型中的类型
     */
    public void addType(SerializationEntity modelType) {
        if (modelType == null) throw new IllegalArgumentException("modelType不能为null");
        synchronized (this.structuralTypesSyncRoot) {
            //覆盖原有的类型
            this.structuralTypes.put(modelType.getClrType(), modelType);
        }
    }

    /**
     * 获取指定CLR类型的模型类型
     *
     * @param type CLR类型
     * @return 模型类型 不存在则返回空
     */
    public SerializationEntity getTypeOrNull(Class<?> type) {
        synchronized (this.structuralTypesSyncRoot) {
            if (type != null)
                return this.structuralTypes.getOrDefault(type, null);
        }
        return null;
    }
}
