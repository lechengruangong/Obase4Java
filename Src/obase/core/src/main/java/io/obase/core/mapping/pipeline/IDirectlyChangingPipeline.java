/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：“就地修改”管道接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:28:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.common.EventHandler;

/**
 * “就地修改”管道接口。
 * “就地修改”是指直接在数据库中修改符合条件的对象，而不是先将对象载入缓存、修改后再写回数据库。包含更改对象属性和删除对象。
 */
public interface IDirectlyChangingPipeline {

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    EventHandler<PreExecuteCommandEventArgs> getDirectlyChangingPreExecuteCommand();

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    EventHandler<PostExecuteCommandEventArgs> getDirectlyChangingPostExecuteCommand();

    /**
     * 为BeginDirectlyChanging事件附加或移除事件处理程序
     *
     * @return BeginDirectlyChanging事件
     */
    EventHandler<BeginDirectlyChangingEventArgs> getBeginDirectlyChanging();

    /**
     * 为EndDirectlyChanging事件附加或移除事件处理程序
     *
     * @return EndDirectlyChanging事件
     */
    EventHandler<EndDirectlyChangingEventArgs> getEndDirectlyChanging();
}
