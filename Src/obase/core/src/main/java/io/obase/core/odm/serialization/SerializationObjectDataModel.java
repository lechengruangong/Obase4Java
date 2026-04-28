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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * 序列化对象数据模型
 */
public class SerializationObjectDataModel {

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * clr类型与模型字典
     */
    private final HashMap<Class<?>, SerializationEntity> structuralTypes = new HashMap<>();

    /**
     * 获取模型类型集合
     *
     * @return 模型类型集合
     */
    public List<SerializationEntity> getTypes() {
        return new ArrayList<>(this.structuralTypes.values());
    }

    /**
     * 向模型添加类型
     *
     * @param modelType 要添加到模型中的类型
     */
    public void addType(SerializationEntity modelType) {
        long stamp = this.stampedLock.writeLock();
        //覆盖原有的类型
        this.structuralTypes.put(modelType.getClrType(), modelType);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取指定CLR类型的模型类型
     *
     * @param type CLR类型
     * @return 模型类型 不存在则返回空
     */
    public SerializationEntity getTypeOrNull(Class<?> type) {
        return this.structuralTypes.getOrDefault(type, null);
    }
}
