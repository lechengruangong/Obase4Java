/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：开始删除组事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 15:56:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.odm.ObjectType;

/**
 * 开始删除组事件数据类
 */
public class BeginDeletingGroupEventArgs extends DeletingGroupEventArgs {
    /**
     * 创建BeginDeletingGroupEventArgs实例
     *
     * @param source     源
     * @param objectType 对象组中对象的类型
     * @param objects    对象组中的对象
     */
    public BeginDeletingGroupEventArgs(Object source, ObjectType objectType, Object[] objects) {
        super(source, objectType, objects);
    }
}
