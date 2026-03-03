/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型元素标注配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:51:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.odm.builder.IAttributeConfigurator;
import io.obase.core.odm.builder.IReferenceElementConfigurator;
import io.obase.core.odm.builder.ITypeElementConfigurator;

/**
 * 类型元素标注配置器
 */
public abstract class TypeElementAttributeConfigurator extends MemberAttributeConfigurator {

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
        return this instanceof TypeAttributeAttributeConfigurator;
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
}
