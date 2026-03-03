/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：发生并发冲突时执行版本合并.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:06:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.FunctionWithTwoArgs;
import io.obase.core.IMappingWorkflow;
import io.obase.core.IStorageProvider;
import io.obase.core.odm.*;

import java.util.List;

/**
 * 发生并发冲突时执行版本合并
 */
public class CombineConflictHandler extends ConcurrentConflictHandler implements IRepeatCreationHandler, IVersionConflictHandler {

    /**
     * 用于获取属性原值的委托
     */
    private final IGetAttributeValue attributeOriginalValueGetter;
    /**
     * 用于执行Sql语句的执行器
     */
    private final IStorageProvider storageProvider;
    /**
     * 用于探测属性值是否发生更改的委托
     */
    private FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged = (o, s) -> true;

    /**
     * 创建Combine-ConflictHandler实例
     *
     * @param model                   对象数据模型
     * @param storageProvider         在冲突处理过程中实施持久化的存储提供程序
     * @param attrOriginalValueGetter 用于获取属性原值的委托
     * @param attrHasChanged          用于探测属性是否已更改的委托
     */
    public CombineConflictHandler(ObjectDataModel model, IStorageProvider storageProvider,
                                  IGetAttributeValue attrOriginalValueGetter,
                                  FunctionWithTwoArgs<Object, String, Boolean> attrHasChanged) {
        super(model);
        this.storageProvider = storageProvider;
        this.attributeOriginalValueGetter = attrOriginalValueGetter;
        if (attrHasChanged != null) this.attributeHasChanged = attrHasChanged;
    }

    /**
     * 处理并发冲突
     *
     * @param mappingUnit  映射执行器
     * @param conflictType 并发冲突类型
     */
    @Override
    public void processConflict(MappingUnit mappingUnit, EConcurrentConflictType conflictType) {
        IMappingWorkflow workFlow = this.storageProvider.createMappingWorkflow();
        ObjectType hostType = this.getModel().getObjectType(mappingUnit.getHostObject().getClass());

        workFlow.begin();

        workFlow.forUpdating();
        ObjectMapper objectMapper = new ObjectMapper(workFlow);
        objectMapper.generateSource(hostType);
        objectMapper.generateCriteria(mappingUnit.getHostObject(), hostType);

        List<Object> objItems = mappingUnit.getMappingObjects();

        if (objItems != null && objItems.size() > 0) {
            for (Object objItem : objItems) {

                ObjectType itemType = this.getModel().getObjectType(objItem.getClass());

                if (itemType == null) continue;

                VersionCombinationContext context =
                        new VersionCombinationContext(objItem, itemType, conflictType, this.attributeOriginalValueGetter);

                if (itemType.getAttributes() != null && itemType.getAttributes().size() > 0) {
                    for (int j = 0; j < itemType.getAttributes().size(); j++) {
                        Attribute attr = itemType.getAttributes().get(j);
                        this.attributeHasChanged.invoke(objItem, attr.getName());
                        this.combineAttribute(attr, workFlow, context);
                    }
                }

                workFlow.commit(null, null);
            }
        }
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

    /**
     * 在对象执行版本合并期间，处理指定的属性
     *
     * @param attribute 目标属性
     * @param workflow  对象修改并实施持久化的工作流机制
     * @param context   版本合并上下文
     */
    private void combineAttribute(Attribute attribute, IMappingWorkflow workflow, VersionCombinationContext context) {
        if (attribute.getIsComplex()) {
            ComplexType comType = ((ComplexAttribute) attribute).getComplexType();
            AttributePath parent = context.getParentAttribute() == null ? new AttributePath(comType) : context.getParentAttribute();
            parent.goDown(attribute);
            context.setParentAttribute(parent);

            if (comType != null && comType.getAttributes() != null) {
                for (int i = 0; i < comType.getAttributes().size(); i++) {
                    Attribute subAttr = comType.getAttributes().get(i);
                    context.setComplexObject(attribute.getValue(context.getObject()));
                    context.setComplexAttribute((ComplexAttribute) attribute);
                    this.combineAttribute(subAttr, workflow, context);
                    context.setComplexObject(null);
                    context.setComplexAttribute(null);
                }
            }

            context.setParentAttribute(null);
        } else {
            IAttributeCombinationHandler combiner = attribute.getCombinationHandler();
            if (combiner != null)
                combiner.process(attribute, workflow, context);
        }
    }
}
