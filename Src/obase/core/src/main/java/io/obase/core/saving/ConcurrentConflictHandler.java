/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为并发冲突处理器提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:05:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectDataModel;

/**
 * 为并发冲突处理器提供基础实现
 */
public abstract class ConcurrentConflictHandler {

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 创建ConcurrentConflictHandler实例
     *
     * @param model 对象数据模型
     */
    protected ConcurrentConflictHandler(ObjectDataModel model) {
        this.model = model;
    }

    /**
     * 获取对象数据模型
     *
     * @return 获取对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 处理并发冲突
     *
     * @param mappingUnit  映射执行器
     * @param conflictType 并发冲突类型
     */
    public abstract void processConflict(MappingUnit mappingUnit, EConcurrentConflictType conflictType);
}
