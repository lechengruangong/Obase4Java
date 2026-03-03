/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联右端标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:27:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration;

import java.util.List;

/**
 * 隐式关联标注属性
 */
public class ImplicitAssociationAttributeConfigurator extends MemberAttributeConfigurator {

    /**
     * 是否启用延迟加载
     */
    private final boolean enableLazyLoading;

    /**
     * 映射表名
     */
    private final String targetTableName;

    /**
     * 构造隐式关联标注属性
     *
     * @param enableLazyLoading 是否启用延迟加载
     * @param targetTableName   映射表名
     */
    public ImplicitAssociationAttributeConfigurator(boolean enableLazyLoading, String targetTableName) {

        if (targetTableName == null || targetTableName.isEmpty())
            throw new IllegalArgumentException("隐式关联标注必须指定映射表名");

        this.enableLazyLoading = enableLazyLoading;
        this.targetTableName = targetTableName;
    }

    /**
     * 获取映射表名
     *
     * @return 映射表名
     */
    public String getTargetTableName() {
        return this.targetTableName.replace(" ", "");
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
        throw new IllegalArgumentException("隐式关联标注不应调用此方法");
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

            ModelBuilder modelBuilder = structuralTypeConfiguration.getModelBuilder();

            //查询属性类型（集合则用元素类型）模型配置项
            StructuralTypeConfiguration<?> implicitEntityConfig = modelBuilder.findConfiguration(type.realValue);
            if (implicitEntityConfig instanceof EntityTypeConfiguration) {
                //查找隐式关联
                Class<?>[] endTypes = new Class<?>[]{memberInfo.getGetterMethod().getDeclaringClass(), type.realValue};
                AssociationConfiguratorBuilder builder = modelBuilder.findImplicitAssociationConfigurationBuilder(AssociationConfiguratorBuilder.generateEndsTag(endTypes, modelBuilder));

                //有隐式关联型配置
                if (builder != null) {

                    List<AssociationEndConfiguration> endConfigurations = builder.getEndConfigurations();

                    String leftEnd = endConfigurations.stream().filter(p -> p.getEntityType().equals(memberInfo.getGetterMethod().getDeclaringClass())).findFirst().map(TypeElementConfiguration::getName).orElse("");
                    if (!leftEnd.isEmpty())
                        configurator.hasLeftEndI(leftEnd);

                    String rightEnd = endConfigurations.stream().filter(p -> p.getEntityType().equals(type.realValue)).findFirst().map(TypeElementConfiguration::getName).orElse("");
                    if (!rightEnd.isEmpty())
                        configurator.hasRightEndI(rightEnd);
                }
            }

            //取值器设值器
            this.configureGetterAndSetter(memberInfo, configurator);

            //设置是否延迟加载
            configurator.hasEnableLazyLoadingI(this.enableLazyLoading);

            //追加触发器
            if (configurator instanceof TypeElementConfiguration) {
                TypeElementConfiguration typeElement = (TypeElementConfiguration) configurator;
                if (typeElement.getBehaviorTriggers().size() == 0 && memberInfo.getGetterMethod() != null) {
                    //启用了延迟加载才配置触发器
                    if (configurator.getEnableLazyLoadingI()) {
                        configurator.hasLoadingTriggerI(memberInfo.getGetterMethod());
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
        throw new IllegalArgumentException("隐式关联标注不应调用此方法");
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
