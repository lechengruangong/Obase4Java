/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：版本合并上下文.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-25 11:00:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.saving.EConcurrentConflictType;

/**
 * 版本合并上下文
 */
public class VersionCombinationContext {

    /**
     * 执行版本合并的对象
     */
    private final Object object;

    /**
     * 执行版本合并的对象的模型类型
     */
    private final ObjectType objType;

    /**
     * 并发冲突类型
     */
    private final EConcurrentConflictType conflictType;

    /**
     * 获取属性原值的委托
     */
    private final IGetAttributeValue attributeOriginalValueGetter;

    /**
     * 当前正在执行合并处理的复杂属性的对象
     */
    private ComplexAttribute complexAttribute;

    /**
     * 当前正在执行合并处理的复杂属性的对象
     */
    private Object complexObject;

    /**
     * 当前正在执行合并处理的属性的父属性
     */
    private AttributePath parentAttribute;

    /**
     * 创建VersionCombinationContext实例
     *
     * @param obj                     执行版本合并的对象
     * @param objType                 执行版本合并的对象的模型类型
     * @param conflictType            并发冲突类型
     * @param attrOriginalValueGetter 获取属性原值的委托
     */
    public VersionCombinationContext(Object obj, ObjectType objType, EConcurrentConflictType conflictType,
                                     IGetAttributeValue attrOriginalValueGetter) {

        this.object = obj;
        this.objType = objType;
        this.conflictType = conflictType;
        this.attributeOriginalValueGetter = attrOriginalValueGetter;
    }

    /**
     * 获取执行版本合并的对象
     *
     * @return 获取执行版本合并的对象
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * 获取执行版本合并的对象的模型类型
     *
     * @return 执行版本合并的对象的模型类型
     */
    public ObjectType getObjType() {
        return this.objType;
    }

    /**
     * 获取当前正在执行合并处理的属性的父属性
     *
     * @return 当前正在执行合并处理的属性的父属性
     */
    public AttributePath getParentAttribute() {
        return this.parentAttribute;
    }

    /**
     * 设置当前正在执行合并处理的属性的父属性
     *
     * @param parentAttribute 当前正在执行合并处理的属性的父属性
     */
    public void setParentAttribute(AttributePath parentAttribute) {
        this.parentAttribute = parentAttribute;
    }

    /**
     * 获取获取属性原值的委托
     *
     * @return 获取属性原值的委托
     */
    public IGetAttributeValue getAttributeOriginalValueGetter() {
        return this.attributeOriginalValueGetter;
    }

    /**
     * 获取并发冲突类型
     *
     * @return 并发冲突类型
     */
    public EConcurrentConflictType getConflictType() {
        return this.conflictType;
    }

    /**
     * 当前正在执行合并处理的复杂属性的对象
     *
     * @return 当前正在执行合并处理的复杂属性的对象
     */
    public Object getComplexObject() {
        return this.complexObject;
    }

    /**
     * 当前正在执行合并处理的复杂属性的对象
     *
     * @param complexObject 当前正在执行合并处理的复杂属性的对象
     */
    public void setComplexObject(Object complexObject) {
        this.complexObject = complexObject;
    }

    /**
     * 当前正在执行合并处理的复杂属性
     *
     * @return 当前正在执行合并处理的复杂属性
     */
    public ComplexAttribute getComplexAttribute() {
        return this.complexAttribute;
    }

    /**
     * 当前正在执行合并处理的复杂属性
     *
     * @param complexAttribute 当前正在执行合并处理的复杂属性
     */
    public void setComplexAttribute(ComplexAttribute complexAttribute) {
        this.complexAttribute = complexAttribute;
    }
}
