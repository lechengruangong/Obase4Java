/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结束删除组事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:24:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.odm.ObjectType;

/**
 * 结束删除组事件数据类
 */
public class EndDeletingGroupEventArgs extends DeletingGroupEventArgs {

    /**
     * 删除操作发生的异常，如果删除成功则值为NULL
     */
    private final Exception exception;

    /**
     * 创建DeletingGroupEventArgs实例
     *
     * @param source     源
     * @param objectType 对象组中对象的类型
     * @param objects    对象组中的对象
     */
    public EndDeletingGroupEventArgs(Object source, ObjectType objectType, Object[] objects, Exception exception) {
        super(source, objectType, objects);
        this.exception = exception;
    }

    /**
     * 获取删除操作发生的异常，如果删除成功则值为NULL。
     *
     * @return 删除操作发生的异常
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 获取一个值，该值指示删除操作是否发生了异常。
     *
     * @return 示删除操作是否发生了异常
     */
    public boolean getFailed() {
        return this.exception != null;
    }
}
