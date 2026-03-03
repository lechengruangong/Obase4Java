/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型引用元素标注配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:54:43
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.odm.builder.*;

/**
 * 类型引用元素标注配置器
 */
public abstract class TypeReferenceElementAttributeConfigurator extends MemberAttributeConfigurator {

    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo  属性
     * @param elementName 成员名册
     * @return 是否将作为类型元素
     */
    @Override
    protected boolean asElement(Property memberInfo, ObjectReferencePack<String> elementName) {
        elementName.realValue = null;
        return this instanceof AssociationReferenceAttributeConfigurator || this instanceof AssociationEndAttributeConfigurator;
    }

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureElement(Property memberInfo, ITypeElementConfigurator configurator) {
        if (configurator instanceof IReferenceElementConfigurator) {
            IReferenceElementConfigurator referenceElementConfigurator = (IReferenceElementConfigurator) configurator;
            this.configureReferenceElement(memberInfo, referenceElementConfigurator);
        }

        if (configurator instanceof IAttributeConfigurator) {
            IAttributeConfigurator attributeConfigurator = (IAttributeConfigurator) configurator;
            this.configureAttribute(memberInfo, attributeConfigurator);
        }
    }

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureReferenceElement(Property memberInfo, IReferenceElementConfigurator configurator) {
        if (configurator instanceof IAssociationReferenceConfigurator) {
            IAssociationReferenceConfigurator referenceConfigurator = (IAssociationReferenceConfigurator) configurator;
            this.configureAssociationReference(memberInfo, referenceConfigurator);
        }

        if (configurator instanceof IAssociationEndConfigurator) {
            IAssociationEndConfigurator endConfigurator = (IAssociationEndConfigurator) configurator;
            this.configureAssociationEnd(memberInfo, endConfigurator);
        }
    }
}
