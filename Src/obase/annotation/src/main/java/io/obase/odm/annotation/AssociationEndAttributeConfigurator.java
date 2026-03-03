/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联端标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:59:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.core.common.Property;
import io.obase.core.odm.PropertyGetTrigger;
import io.obase.core.odm.builder.*;

/**
 * 显式关联端标注属性配置器
 */
public class AssociationEndAttributeConfigurator extends TypeReferenceElementAttributeConfigurator {
    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAttribute(Property memberInfo, IAttributeConfigurator configurator) {
        throw new IllegalArgumentException("关联端标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationReference(Property memberInfo, IAssociationReferenceConfigurator configurator) {
        throw new IllegalArgumentException("关联端标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationEnd(Property memberInfo, IAssociationEndConfigurator configurator) {
        //配置设值器和取值器
        this.configureGetterAndSetter(memberInfo, configurator);

        //追加触发器
        if (configurator instanceof TypeElementConfiguration) {
            TypeElementConfiguration typeElement = (TypeElementConfiguration) configurator;
            if (typeElement.getBehaviorTriggers().stream().anyMatch(p -> !p.getUniqueId().equals(memberInfo.getName())) &&
                    memberInfo.getGetterMethod() != null) {
                //启用了延迟加载才配置触发器
                if (configurator.getEnableLazyLoadingI()) {
                    //默认属性触发器（用以延时加载，访问属性的get的访问器时触发）
                    configurator.hasLoadingTriggerI(new PropertyGetTrigger<>(memberInfo));
                }
            }
        }
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
