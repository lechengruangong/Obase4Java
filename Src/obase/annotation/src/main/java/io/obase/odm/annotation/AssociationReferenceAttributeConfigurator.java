/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联引用标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:57:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;

import java.util.List;

/**
 * 显式关联引用标注属性配置器
 */
public class AssociationReferenceAttributeConfigurator extends TypeReferenceElementAttributeConfigurator {

    /**
     * 是否启用延迟加载
     */
    private final boolean enableLazyLoading;

    /**
     * 初始化显式关联引用标注属性配置器
     *
     * @param enabledLazyLoading 是否启用延迟加载
     */
    public AssociationReferenceAttributeConfigurator(boolean enabledLazyLoading) {
        this.enableLazyLoading = enabledLazyLoading;
    }

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAttribute(Property memberInfo, IAttributeConfigurator configurator) {
        throw new IllegalArgumentException("关联引用标注不应调用此方法");
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

            ModelBuilder modelBuilder = structuralTypeConfiguration.getModelBuilder();
            //尝试按照显式进行查询
            StructuralTypeConfiguration<?> obvious = modelBuilder.findConfiguration(type.realValue);

            if (obvious instanceof AssociationTypeConfiguration) {

                //查找显式关联型的各个属性
                List<Property> obviousProps = ObaseIntrospector.getObaseBeanProperties(obvious.getClrType());

                //左端为空 配置左端
                String leftEnd = obviousProps.stream().filter(p -> p.getPropertyType().equals(memberInfo.getGetterMethod().getDeclaringClass())).findFirst().map(Property::getName).orElse("");
                if (!leftEnd.isEmpty()) configurator.hasLeftEndI(leftEnd);
            }

            //取值器设值器
            this.configureGetterAndSetter(memberInfo, configurator);

            //设置是否延迟加载
            configurator.hasEnableLazyLoadingI(this.enableLazyLoading);

            //追加触发器
            if (configurator instanceof TypeElementConfiguration) {

                if (memberInfo.getGetterMethod() != null) {
                    TypeElementConfiguration typeElement = (TypeElementConfiguration) configurator;
                    if (typeElement.getBehaviorTriggers().size() == 0) {
                        //启用了延迟加载才配置触发器
                        if (configurator.getEnableLazyLoadingI()) {
                            configurator.hasLoadingTriggerI(memberInfo.getGetterMethod());
                        }
                    }
                }
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
        throw new IllegalArgumentException("关联引用标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo       属性
     * @param typeConfigurator 配置器
     */
    @Override
    protected void configureType(Property memberInfo, IStructuralTypeConfigurator typeConfigurator) {
        throw new IllegalArgumentException("关联引用标注不应调用此方法");
    }
}
