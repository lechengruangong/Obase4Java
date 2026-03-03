/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：事件监听器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:19:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.util.EventObject;

/**
 * 事件监听器
 *
 * @param <T> 事件数据
 */
public interface EventListener<T extends EventObject> {

    /**
     * 触发事件后的处理方法
     *
     * @param eventObject 事件数据
     */
    void onEvent(T eventObject);
}
