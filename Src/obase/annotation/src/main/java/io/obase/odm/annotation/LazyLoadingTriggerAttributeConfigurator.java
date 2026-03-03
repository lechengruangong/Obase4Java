/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：延迟加载标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:22:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.odm.builder.*;

/**
 * 延迟加载标注属性配置器
 */
public class LazyLoadingTriggerAttributeConfigurator extends MemberAttributeConfigurator {
    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo  属性
     * @param elementName 成员名册
     * @return 是否将作为类型元素
     */
    @Override
    protected boolean asElement(Property memberInfo, ObjectReferencePack<String> elementName) {
        return false;
    }

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureElement(Property memberInfo, ITypeElementConfigurator configurator) {

    }

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAttribute(Property memberInfo, IAttributeConfigurator configurator) {

    }

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureReferenceElement(Property memberInfo, IReferenceElementConfigurator configurator) {

    }

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationReference(Property memberInfo, IAssociationReferenceConfigurator configurator) {

    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationEnd(Property memberInfo, IAssociationEndConfigurator configurator) {

    }

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo       属性
     * @param typeConfigurator 配置器
     */
    @Override
    protected void configureType(Property memberInfo, IStructuralTypeConfigurator typeConfigurator) {

    }
}
