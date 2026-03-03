/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：筛选条件建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:29:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.AssociationType;
import io.obase.core.odm.EntityType;

/**
 * 用于建造筛选条件
 */
public class SelectionCriteriaBuilder implements ISelectionCriteriaBuilder {

    /**
     * 伴随关联对象条件建造器
     */
    private CompanionAssociationSelectionCriteriaBuilder companionBuilder;

    /**
     * 对象筛选条件建造器
     */
    private EntitySelectionCriteriaBuilder entityBuilder;

    /**
     * 独立关联对象条件建造器
     */
    private IndependentAssociationSelectionCriteriaBuilder independentBuilder;

    /**
     * 筛选条件构造
     *
     * @param targetObj       对象
     * @param objectType      对象类型
     * @param mappingWorkflow 实施持久化的工作流机制
     */
    @Override
    public void build(Object targetObj, Object objectType, IMappingWorkflow mappingWorkflow) {
        if (objectType instanceof EntityType) {
            EntityType entity = (EntityType) objectType;
            if (this.entityBuilder == null)
                this.entityBuilder = new EntitySelectionCriteriaBuilder();
            this.entityBuilder.build(targetObj, entity, mappingWorkflow);
        } else if (objectType instanceof AssociationType) {
            AssociationType assoc = (AssociationType) objectType;

            if (assoc.getIndependent()) //独立映射
            {
                if (this.independentBuilder == null)
                    this.independentBuilder = new IndependentAssociationSelectionCriteriaBuilder();
                this.independentBuilder.build(targetObj, assoc, mappingWorkflow);
            } else //伴随映射
            {
                if (this.companionBuilder == null)
                    this.companionBuilder = new CompanionAssociationSelectionCriteriaBuilder();
                this.companionBuilder.build(targetObj, objectType, mappingWorkflow);
            }
        }
    }
}

