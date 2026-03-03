/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为更新幻影冲突的处理策略定义规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:08:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 为更新幻影冲突的处理策略定义规范
 */
public interface IUpdatingPhantomHandler {

    /**
     * 处理更新幻影冲突
     *
     * @param mappingUnit 映射执行器
     */
    void processUpdatingPhantomConflict(MappingUnit mappingUnit);
}
