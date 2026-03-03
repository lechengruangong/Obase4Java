/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性映射器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 14:46:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.*;

import java.util.function.Predicate;

/**
 * 属性映射器，封装特定于属性的映射方案
 */
public class AttributeMapper extends RealElementMapper {
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
            Attribute attr = (Attribute) element;
            //按照以下顺序判断是否参与映射：
            //1. 如果是自动生成的值，则不参与映射
            //2. 如果是新增的对象，则参与映射
            //3. 如果是主键字段，则不参与映射
            //4. 如果是删除的对象，且如果是独立的关联端，则参与映射，否则不参与映射
            //5. 如果属性未修改，则不参与映射
            if (attr.getDbGenerateValue()) return false;

            if (objectStatus == EObjectStatus.Added) return true;

            if (objectType.getKeyFields().contains(attr.getTargetField())) return false;

            if (objectType instanceof AssociationType) {
                AssociationType associationType = (AssociationType) objectType;
                if (objectStatus == EObjectStatus.Deleted) {
                    return !associationType.getIndependent();
                }
            }


            return attributeHasChanged == null || attributeHasChanged.test(element.getName());
        }

        throw new IllegalArgumentException("要选取的参与映射的元素必须为属性,且对象必须为关联型.");
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
            Attribute attr = (Attribute) element;
            if (!attr.getIsForeignKeyDefineMissing()) {
                Object value = attr.getValueGetter().getValue(obj);
                if (attr.getIsComplex()) {
                    ComplexAttribute complex = (ComplexAttribute) attr;
                    for (Attribute attribute : complex.getComplexType().getAttributes()) {
                        this.mapComplexAttribute(complex, attribute, value, mappingWorkflow);
                    }
                } else {
                    Object realValue = this.getSetNull() ? null : value;
                    mappingWorkflow.setField(attr.getTargetField(), realValue);
                }
            }
        }
    }

    /**
     * 映射复杂属性
     *
     * @param complex         所属的复杂属性
     * @param attribute       当前复杂属性的属性
     * @param obj             值
     * @param mappingWorkflow 工作流
     */
    private void mapComplexAttribute(ComplexAttribute complex, Attribute attribute, Object obj,
                                     IMappingWorkflow mappingWorkflow) {
        //转换值
        Object value = attribute.getValueGetter().getValue(obj);
        Object realValue = this.getSetNull() ? null : value;

        String connectionStr = "";
        //如果是minvalue 则表示未设置连接符 按照一般名称处理
        if (complex.getMappingConnectionChar() != (char) -1)
            connectionStr = complex.getTargetField() + complex.getMappingConnectionChar();
        //字段全名
        String filedName = connectionStr + attribute.getTargetField();

        mappingWorkflow.setField(filedName, realValue);
    }
}
