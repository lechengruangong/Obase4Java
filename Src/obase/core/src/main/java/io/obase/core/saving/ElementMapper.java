/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元素映射器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:48:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.*;

import java.util.function.Predicate;

/**
 * 元素映射器，用于将属性或关联端映射到字段。该映射器不执行实际的映射任务，而是将任务交由具体的元素映射器完成。
 */
public class ElementMapper implements IElementMapper {

    /**
     * 关联端映射器
     */
    private final AssociationEndMapper associationEndMapper;

    /**
     * 属性映射器
     */
    private final AttributeMapper attributeMapper;

    /**
     * 元素所属类型
     */
    private ObjectType objectType;

    /**
     * 构造函数
     */
    public ElementMapper() {
        this.associationEndMapper = new AssociationEndMapper();
        this.attributeMapper = new AttributeMapper();
    }

    /**
     * 获取映射的元素所属的类型
     *
     * @return 元素所属的类型
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * 设置映射的元素所属的类型
     *
     * @param objectType 元素所属的类型
     */
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
        if (this.objectType instanceof AssociationType)
            this.associationEndMapper.setAssociationType((AssociationType) objectType);
    }

    /**
     * 获取一个值，该值指示是否将元素涉及的映射目标字段置空
     *
     * @return 是否将元素涉及的映射目标字段置空
     */
    public boolean getSetNull() {
        return this.associationEndMapper.getSetNull();
    }

    /**
     * 设置一个值，该值指示是否将元素涉及的映射目标字段置空
     *
     * @param setNull 是否将元素涉及的映射目标字段置空
     */
    public void setSetNull(boolean setNull) {
        this.associationEndMapper.setSetNull(setNull);
        this.attributeMapper.setSetNull(setNull);
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
        if (element instanceof Attribute) {
            return this.attributeMapper.select(element, objectType, objectStatus, attributeHasChanged);
        }

        if (element instanceof AssociationEnd) {
            return this.associationEndMapper.select(element, objectType, objectStatus, attributeHasChanged);
        }

        return false;
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
        if (element instanceof Attribute) {
            this.attributeMapper.map(element, obj, mappingWorkflow);
        } else {
            this.associationEndMapper.map(element, obj, mappingWorkflow);
        }
    }
}
