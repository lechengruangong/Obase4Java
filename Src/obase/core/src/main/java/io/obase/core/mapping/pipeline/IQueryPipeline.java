/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询管道接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:25:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.common.EventHandler;

/**
 * 查询管道接口
 */
public interface IQueryPipeline {

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    EventHandler<QueryEventArgs> getIQueryPipelinePreExecuteCommand();

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    EventHandler<QueryEventArgs> getIQueryPipelinePostExecuteCommand();

    /**
     * 为BeginQuery事件附加或移除事件处理程序
     *
     * @return BeginQuery事件
     */
    EventHandler<QueryEventArgs> getBeginQuery();

    /**
     * 为EndQuery事件附加或移除事件处理程序
     *
     * @return EndQuery事件
     */
    EventHandler<QueryEventArgs> getEndQuery();
}
