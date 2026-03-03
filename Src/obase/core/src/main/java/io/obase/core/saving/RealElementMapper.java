/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元素的映射方案基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 11:59:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.IMappingWorkflow;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.TypeElement;

import java.util.function.Predicate;

/**
 * 为不同元素的映射方案提供基础实现
 */
public abstract class RealElementMapper implements IElementMapper {

    /**
     * 是否将元素涉及的映射目标字段置空
     */
    private boolean setNull;

    /**
     * 获取一个值，该值指示是否将元素涉及的映射目标字段置空
     *
     * @return 是否将元素涉及的映射目标字段置空
     */
    public boolean getSetNull() {
        return this.setNull;
    }

    /**
     * 设置一个值，该值指示是否将元素涉及的映射目标字段置空
     *
     * @param setNull 否将元素涉及的映射目标字段置空
     */
    public void setSetNull(boolean setNull) {
        this.setNull = setNull;
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
    public abstract boolean select(TypeElement element, ObjectType objectType, EObjectStatus objectStatus, Predicate<String> attributeHasChanged);

    /**
     * 将元素映射到字段，即生成字段设值器
     *
     * @param element         要映射的元素
     * @param obj             要映射的元素所属的对象
     * @param mappingWorkflow 实施持久化的工作流机制
     */
    @Override
    public abstract void map(TypeElement element, Object obj, IMappingWorkflow mappingWorkflow);
}
