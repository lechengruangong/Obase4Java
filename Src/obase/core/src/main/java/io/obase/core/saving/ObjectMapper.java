/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象映射器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:27:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.AssociationType;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.TypeElement;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 对象映射器，负责生成数据源、确定修改Sql语句的修改类型、生成筛选条件、生成字段设值器。
 */
public class ObjectMapper {

    /**
     * 在对象映射过程中实施持久化的工作流机制
     */
    private final IMappingWorkflow mappingWorkflow;

    /**
     * 筛选条件建造器
     */
    private SelectionCriteriaBuilder criteriaBuilder;

    /**
     * 元素映射器
     */
    private ElementMapper elementMapper;

    /**
     * 创建ObjectMapper实例
     *
     * @param mappingWorkflow 映射工作流机制
     */
    public ObjectMapper(IMappingWorkflow mappingWorkflow) {
        this.mappingWorkflow = mappingWorkflow;
    }

    /**
     * 生成作为映射目标的查询源
     *
     * @param objectType 要映射的对象的类型
     */
    public void generateSource(ObjectType objectType) {
        this.mappingWorkflow.setSource(objectType.getTargetTable());
    }

    /**
     * 根据对象状态确定修改SQL的修改类型
     *
     * @param objectStatus 对象状态
     * @param objectType   要映射的对象的类型
     */
    public void determineChangeType(EObjectStatus objectStatus, ObjectType objectType) {
        if (objectType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) objectType;
            if (!associationType.getIndependent()) {
                this.mappingWorkflow.forUpdating();
                return;
            }
        }

        switch (objectStatus) {

            case Unchanged:
                break;
            case Added:
                this.mappingWorkflow.forInserting();
                break;
            case Deleted:
                this.mappingWorkflow.forDeleting();
                break;
            case Modified:
                this.mappingWorkflow.forUpdating();
                break;
        }
    }

    /**
     * 生成用于从数据源筛选指定对象的筛选条件
     *
     * @param obj        要筛选的对象
     * @param objectType 要筛选的对象的类型
     */
    public void generateCriteria(Object obj, ObjectType objectType) {
        if (this.criteriaBuilder == null)
            this.criteriaBuilder = new SelectionCriteriaBuilder();
        this.mappingWorkflow.setSource(objectType.getTargetTable());
        this.criteriaBuilder.build(obj, objectType, this.mappingWorkflow);
    }

    /**
     * 生成用于从数据源筛选指定对象组的筛选条件
     *
     * @param objs       要筛选的对象组
     * @param objectType 对象组中对象的类型
     */
    public void generateCriteria(Object[] objs, ObjectType objectType) {
        for (Object obj : objs) {
            this.generateCriteria(obj, objectType);
        }
    }

    /**
     * 生成字段设值器，这些设值器用于将对象映射到表
     *
     * @param obj                 要映射的对象
     * @param objectType          对象的类型
     * @param objectStatus        对象的状态
     * @param attributeHasChanged 一个委托，用于确定属性是否已修改
     */
    public void generateFieldSetter(Object obj, ObjectType objectType,
                                    EObjectStatus objectStatus, Predicate<String> attributeHasChanged) {
        if (this.elementMapper == null)
            this.elementMapper = new ElementMapper();

        this.elementMapper.setObjectType(objectType);

        //是否设置空值
        this.elementMapper.setSetNull(!objectStatus.equals(EObjectStatus.Added) && !objectStatus.equals(EObjectStatus.Modified));

        List<TypeElement> element = objectType.getElements().stream().sorted(Comparator.comparingInt(o -> o.getElementType().getType())).collect(Collectors.toList());

        for (TypeElement e : element) {
            boolean selected = this.elementMapper.select(e, objectType, objectStatus, attributeHasChanged);
            if (selected)
                this.elementMapper.map(e, obj, this.mappingWorkflow);
        }
    }
}
