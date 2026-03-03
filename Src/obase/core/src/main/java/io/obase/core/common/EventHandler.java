/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：事件处理器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:18:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

/**
 * 事件处理器,处理事件
 *
 * @param <T> 要处理的事件数据
 */
public class EventHandler<T extends EventObject> {

    /**
     * 监听者集合
     */
    private final Set<EventListener<T>> listeners;

    /**
     * 构造处理器
     */
    public EventHandler() {
        this.listeners = new HashSet<>();
    }

    /**
     * 添加监听者
     *
     * @param listener 监听者
     */
    public void addListener(EventListener<T> listener) {
        this.listeners.add(listener);
    }

    /**
     * 移除监听者
     *
     * @param listener 监听者
     */
    public void removeListener(EventListener<T> listener) {
        this.listeners.remove(listener);
    }

    /**
     * 发布事件
     *
     * @param eventObject 事件数据
     */
    public void publishEvent(T eventObject) {
        if (this.listeners.size() == 0) {
            return;
        }
        for (EventListener<T> listener : this.listeners) {
            listener.onEvent(eventObject);
        }
    }
}

