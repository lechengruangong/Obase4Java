/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：“保存”管道接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:17:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.common.EventHandler;

import java.util.EventObject;

/**
 * “保存”管道接口
 */
public interface ISavingPipeline {

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    EventHandler<PreExecuteCommandEventArgs> getSavingPreExecuteCommand();

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    EventHandler<PostExecuteCommandEventArgs> getSavingPostExecuteCommand();

    /**
     * 为BeginSaving事件附加或移除事件处理程序
     *
     * @return BeginSaving事件
     */
    EventHandler<EventObject> getBeginSaving();

    /**
     * 为PostGenerateQueue事件附加或移除事件处理程序
     *
     * @return PostGenerateQueue事件
     */
    EventHandler<EventObject> getPostGenerateQueue();

    /**
     * 为BeginSavingUnit事件附加或移除事件处理程序
     *
     * @return BeginSavingUnit事件
     */
    EventHandler<BeginSavingUnitEventArgs> getBeginSavingUnit();

    /**
     * 为EndSavingUnit事件附加或移除事件处理程序
     *
     * @return EndSavingUnit事件
     */
    EventHandler<EndSavingUnitEventArgs> getEndSavingUnit();

    /**
     * 为EndSaving事件附加或移除事件处理程序
     *
     * @return EndSaving事件
     */
    EventHandler<EventObject> getEndSaving();
}
