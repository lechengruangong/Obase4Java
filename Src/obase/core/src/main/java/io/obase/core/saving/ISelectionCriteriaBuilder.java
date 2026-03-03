/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：筛选条件建造器接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:28:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;

/**
 * 筛选条件建造器接口，定义抽象的建造器。
 */
public interface ISelectionCriteriaBuilder {

    /**
     * 筛选条件构造
     *
     * @param targetObj       对象
     * @param objectType      对象类型
     * @param mappingWorkflow 实施持久化的工作流机制
     */
    void build(Object targetObj, Object objectType, IMappingWorkflow mappingWorkflow);
}
