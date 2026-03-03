/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：并发冲突异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:03:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.ObjectType;

/**
 * 为并发冲突异常提供基础实现
 */
public class ConcurrentConflictException extends RuntimeException {

    /**
     * 发生并发冲突的对象
     */
    private final Object object;

    /**
     * 发生并发冲突的对象的类型
     */
    private final ObjectType objectType;

    /**
     * 创建ConcurrentConflictException实例
     *
     * @param obj     发生并发冲突的对象
     * @param objType 发生并发冲突的对象的类型
     */
    protected ConcurrentConflictException(Object obj, ObjectType objType) {
        this.object = obj;
        this.objectType = objType;
    }

    /**
     * 获取发生并发冲突的对象
     *
     * @return 发生并发冲突的对象
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * 获取发生并发冲突的对象的类型
     *
     * @return 发生并发冲突的对象的类型
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * 获取发生并发冲突的对象的标识
     *
     * @return 发生并发冲突的对象的标识
     */
    public ObjectKey getObjectKey() {
        return ObjectSystemVisitor.getObjectKey(this.object, this.objectType);
    }
}
