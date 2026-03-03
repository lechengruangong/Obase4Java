/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：映射模块接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:16:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.ObjectContext;

/**
 * 映射模块接口
 */
public interface IMappingModule {

    /**
     * 初始化映射模块
     *
     * @param savingPipeline           "保存"管道
     * @param deletingPipeline         "删除"管道
     * @param queryPipeline            "查询"管道
     * @param directlyChangingPipeline "就地修改"管道
     * @param objectContext            对象上下文
     */
    void init(ISavingPipeline savingPipeline, IDeletingPipeline deletingPipeline, IQueryPipeline queryPipeline, IDirectlyChangingPipeline directlyChangingPipeline, ObjectContext objectContext);
}
