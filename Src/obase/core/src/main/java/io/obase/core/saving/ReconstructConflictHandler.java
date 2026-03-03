/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：发生并发冲突时重建对象.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:24:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.IStorageProvider;
import io.obase.core.odm.ObjectDataModel;

/**
 * 发生并发冲突时重建对象
 */
public class ReconstructConflictHandler extends ConcurrentConflictHandler implements IUpdatingPhantomHandler {

    /**
     * 用于执行Sql语句的执行器
     */
    private final IStorageProvider storageProvider;

    /**
     * 创建Reconstruct-ConflictHandler实例
     *
     * @param model           对象数据模型
     * @param storageProvider 在冲突处理过程中实施持久化的存储提供程序
     */
    public ReconstructConflictHandler(ObjectDataModel model, IStorageProvider storageProvider) {
        super(model);

        this.storageProvider = storageProvider;
    }

    /**
     * 处理并发冲突
     *
     * @param mappingUnit  映射执行器
     * @param conflictType 并发冲突类型
     */
    @Override
    public void processConflict(MappingUnit mappingUnit, EConcurrentConflictType conflictType) {
        IMappingWorkflow mappingWorkflow = this.storageProvider.createMappingWorkflow();
        mappingUnit.saveNew(mappingWorkflow, this.getModel(), null, null);
    }

    /**
     * 处理更新幻影冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processUpdatingPhantomConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.UpdatingPhantom);
    }
}
