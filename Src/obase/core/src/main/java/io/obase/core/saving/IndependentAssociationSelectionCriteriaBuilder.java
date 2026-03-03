/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：独立关联对象筛选条件建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:46:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.FilterSegment;
import io.obase.core.IMappingWorkflow;
import io.obase.core.MappingFilter;
import io.obase.core.odm.AssociationEnd;
import io.obase.core.odm.AssociationEndMapping;
import io.obase.core.odm.AssociationType;

/**
 * 独立关联对象筛选条件建造器
 */
public class IndependentAssociationSelectionCriteriaBuilder implements ISelectionCriteriaBuilder {

    /**
     * 筛选条件构造
     *
     * @param targetObj       对象
     * @param objectType      对象类型
     * @param mappingWorkflow 实施持久化的工作流机制
     */
    @Override
    public void build(Object targetObj, Object objectType, IMappingWorkflow mappingWorkflow) {
        //过滤器
        MappingFilter filter = mappingWorkflow.or();
        if (objectType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) objectType;
            for (AssociationEnd end : associationType.getAssociationEnds()) {
                for (AssociationEndMapping mapp : end.getMappings()) {
                    Object value = ObjectSystemVisitor.getValue(targetObj, associationType, end, mapp.getKeyAttribute());
                    //片段
                    FilterSegment segment = filter.addSegment();
                    segment.setField(mapp.getTargetField());
                    segment.setReferenceValue(value);
                }
            }
        }

        filter.end();
    }
}
