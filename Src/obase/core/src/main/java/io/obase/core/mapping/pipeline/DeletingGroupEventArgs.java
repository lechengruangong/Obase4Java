/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：与删除对象组相关的事件的数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 15:55:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.odm.ObjectType;

import java.util.EventObject;

/**
 * 与删除对象组相关的事件的数据类
 */
public class DeletingGroupEventArgs extends EventObject {

    /**
     * 要删除对象的类型
     */
    private final ObjectType objectType;

    /**
     * 要删除对象的集合
     */
    private final Object[] objects;

    /**
     * 创建DeletingGroupEventArgs实例
     *
     * @param source     源
     * @param objectType 对象组中对象的类型
     * @param objects    对象组中的对象
     */
    protected DeletingGroupEventArgs(Object source, ObjectType objectType, Object[] objects) {
        super(source);
        this.objectType = objectType;
        this.objects = objects;
    }

    /**
     * 获取要删除对象的类型
     *
     * @return 获取要删除对象的类型
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * 获取要删除对象的集合
     *
     * @return 获取要删除对象的集合
     */
    public Object[] getObjects() {
        return this.objects;
    }
}