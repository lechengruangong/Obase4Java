/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：发生并发冲突时执行强制覆盖.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:19:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.FunctionWithTwoArgs;
import io.obase.core.IMappingWorkflow;
import io.obase.core.IStorageProvider;
import io.obase.core.odm.ObjectDataModel;

/**
 * 发生并发冲突时执行强制覆盖
 */
public class OverwriteConflictHandler extends ConcurrentConflictHandler implements IRepeatCreationHandler, IVersionConflictHandler {

    /**
     * 用于执行Sql语句的执行器
     */
    private final IStorageProvider storageProvider;

    /**
     * 用于探测属性值是否发生更改的委托
     */
    private FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged = (obj, attr) -> true;

    /**
     * 创建Overwrite-ConflictHandler实例
     *
     * @param model               对象数据模型
     * @param storageProvider     在冲突处理过程中实施持久化的存储提供程序
     * @param attributeHasChanged 用于探测属性是否发生更改的委托
     */
    public OverwriteConflictHandler(ObjectDataModel model, IStorageProvider storageProvider,
                                    FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged) {
        super(model);


        this.storageProvider = storageProvider;
        if (attributeHasChanged != null)
            this.attributeHasChanged = attributeHasChanged;
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
        mappingUnit.saveOld(mappingWorkflow, false, this.getModel(), this.attributeHasChanged, null, null, null);
    }

    /**
     * 处理重复创建冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processRepeatConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.RepeatCreation);
    }

    /**
     * 处理版本冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processVersionConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.VersionConflict);
    }
}
