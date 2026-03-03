/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联右端标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:04:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;

/**
 * 隐式关联右端标注属性配置器
 */
public class RightEndMappingAttributeConfigurator extends MemberAttributeConfigurator {

    /**
     * 此端的键属性
     */
    private final String keyAttribute;

    /**
     * 此端的键属性映射字段
     */
    private final String targetField;

    /**
     * 隐式关联右端标注属性配置器
     *
     * @param keyAttribute 此端的键属性
     * @param targetField  此端的键属性映射字段
     */
    public RightEndMappingAttributeConfigurator(String keyAttribute, String targetField) {
        this.keyAttribute = keyAttribute;
        this.targetField = targetField;
    }

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
        return true;
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
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAttribute(Property memberInfo, IAttributeConfigurator configurator) {
        throw new IllegalArgumentException("隐式关联右端标注不应调用此方法");
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

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationReference(Property memberInfo, IAssociationReferenceConfigurator configurator) {

        ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
        //属性为集合类型
        Utils.getIsMultiple(memberInfo, type);

        if (configurator.upwardI() instanceof StructuralTypeConfiguration) {
            StructuralTypeConfiguration<?> structuralTypeConfiguration = (StructuralTypeConfiguration<?>) configurator.upwardI();
            //查找隐式关联
            Class<?>[] endTypes = new Class<?>[]{type.realValue, memberInfo.getGetterMethod().getDeclaringClass()};
            ModelBuilder modelBuilder = structuralTypeConfiguration.getModelBuilder();
            String endTags = AssociationConfiguratorBuilder.generateEndsTag(endTypes, modelBuilder);
            AssociationConfiguratorBuilder implicitAssociationConfig = modelBuilder.findImplicitAssociationConfigurationBuilder(endTags);

            //有隐式关联型配置
            if (implicitAssociationConfig != null) {
                java.util.List<io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration> ends = implicitAssociationConfig.getEndConfigurations();

                ends.stream().filter(p -> p.getEntityType().equals(type.realValue)).findFirst()
                        .ifPresent(rightEnd -> rightEnd.hasMappingI(this.keyAttribute, this.targetField));
            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationEnd(Property memberInfo, IAssociationEndConfigurator configurator) {
        throw new IllegalArgumentException("隐式关联右端标注不应调用此方法");
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
