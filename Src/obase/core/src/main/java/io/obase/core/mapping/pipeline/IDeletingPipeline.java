/*
┌──────────────────────────────────────────────────────────────┐
│　描   述："删除"管道接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:23:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.common.EventHandler;

import java.util.EventObject;

/**
 * "删除"管道接口
 */
public interface IDeletingPipeline {

    /**
     * 返回为PreExecuteSql事件附加或移除事件处理程序的EventHandler
     *
     * @return PreExecuteSql事件
     */
    EventHandler<PreExecuteCommandEventArgs> getDeletingPreExecuteCommand();

    /**
     * 返回为PostExecuteSql事件附加或移除事件处理程序的EventHandler
     *
     * @return 为PostExecuteSql事件
     */
    EventHandler<PostExecuteCommandEventArgs> getDeletingPostExecuteCommand();

    /**
     * 为BeginDeleting事件附加或移除事件处理程序
     *
     * @return BeginDeleting事件
     */
    EventHandler<EventObject> getBeginDeleting();

    /**
     * 为PostGenerateGroup事件附加或移除事件处理程序
     *
     * @return PostGenerateGroup事件
     */
    EventHandler<EventObject> getPostGenerateGroup();

    /**
     * 为BeginDeletingGroup事件附加或移除事件处理程序
     *
     * @return BeginDeletingGroup事件
     */
    EventHandler<BeginDeletingGroupEventArgs> getBeginDeletingGroup();

    /**
     * 为EndDeletingGroup事件附加或移除事件处理程序
     *
     * @return EndDeletingGroup事件
     */
    EventHandler<EndDeletingGroupEventArgs> getEndDeletingGroup();

    /**
     * 为EndDeleting事件附加或移除事件处理程序
     *
     * @return EndDeleting事件
     */
    EventHandler<EventObject> getEndDeleting();
}
