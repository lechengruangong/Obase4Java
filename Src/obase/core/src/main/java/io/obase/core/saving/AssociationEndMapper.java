/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联端映射器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 12:01:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.*;

import java.util.function.Predicate;

/**
 * 关联端映射器，封装特定于关联端的映射方案
 */
public class AssociationEndMapper extends RealElementMapper {

    /**
     * 映射的关联端所属的关联型
     */
    private AssociationType associationType;

    /**
     * 获取映射的关联端所属的关联型
     *
     * @return 关联端所属的关联型
     */
    public AssociationType getAssociationType() {
        return this.associationType;
    }

    /**
     * 设置映射的关联端所属的关联型
     *
     * @param associationType 关联端所属的关联型
     */
    public void setAssociationType(AssociationType associationType) {
        this.associationType = associationType;
    }

    /**
     * 确定是否应当选取指定的元素参与映射
     *
     * @param element             要确定的元素
     * @param objectType          元素所属对象的类型
     * @param objectStatus        元素所属对象的状态
     * @param attributeHasChanged Predicate{String}委托，用于判定属性是否已修改
     * @return 是否应当选取指定的元素参与映射
     */
    @Override
    public boolean select(TypeElement element, ObjectType objectType, EObjectStatus objectStatus, Predicate<String> attributeHasChanged) {
        AssociationType associationType = (AssociationType) objectType;
        AssociationEnd end = (AssociationEnd) element;

        return (objectStatus.equals(EObjectStatus.Added) || objectStatus.equals(EObjectStatus.Deleted)) &&
                !associationType.isCompanionEnd(end);
    }

    /**
     * 将元素映射到字段，即生成字段设值器
     *
     * @param element         要映射的元素
     * @param obj             要映射的元素所属的对象
     * @param mappingWorkflow 实施持久化的工作流机制
     */
    @Override
    public void map(TypeElement element, Object obj, IMappingWorkflow mappingWorkflow) {
        if (element instanceof AssociationEnd) {
            AssociationEnd end = (AssociationEnd) element;

            for (AssociationEndMapping mapp : end.getMappings()) {
                String keyAttr = mapp.getKeyAttribute();
                String targetFiled = mapp.getTargetField();
                Object value = null;
                if (!this.getSetNull()) {
                    value = end.getKeyAttributeValue(obj, keyAttr);
                }
                mappingWorkflow.setField(targetFiled, value);
            }
        }
    }
}

