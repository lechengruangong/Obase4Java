/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性标注配置器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:16:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.Annotation;

/**
 * 属性标注配置器工厂
 */
class MemberAttributeConfiguratorFactory {

    /**
     * 生成属性标注对应的配置器
     *
     * @param annotation 属性标注
     * @param <T>        属性标注类型 继承自Annotation
     * @return 属性标注配置器
     */
    public static <T extends Annotation> MemberAttributeConfigurator GenerateConfigurator(T annotation) {
        switch (annotation.annotationType().getSimpleName()) {
            case "AssociationEndAttribute":
                return new AssociationEndAttributeConfigurator();
            case "AssociationReferenceAttribute": {
                AssociationReferenceAttribute associationReferenceAttribute = (AssociationReferenceAttribute) annotation;
                return new AssociationReferenceAttributeConfigurator(associationReferenceAttribute.enabledLazyLoading());
            }
            case "EndMappingAttribute": {
                EndMappingAttribute endMappingAttribute = (EndMappingAttribute) annotation;
                return new EndMappingAttributeConfigurator(endMappingAttribute.keyAttribute(), endMappingAttribute.targetField());
            }
            case "ImplicitAssociationAttribute": {
                ImplicitAssociationAttribute implicitAssociationAttribute = (ImplicitAssociationAttribute) annotation;
                return new ImplicitAssociationAttributeConfigurator(implicitAssociationAttribute.enableLazyLoading(), implicitAssociationAttribute.targetTableName());
            }
            case "LazyLoadingTriggerAttribute":
                return new LazyLoadingTriggerAttributeConfigurator();
            case "LeftEndMappingAttribute": {
                LeftEndMappingAttribute leftEndMappingAttribute = (LeftEndMappingAttribute) annotation;
                return new LeftEndMappingAttributeConfigurator(leftEndMappingAttribute.keyAttribute(), leftEndMappingAttribute.targetField());
            }
            case "RightEndMappingAttribute": {
                RightEndMappingAttribute rightEndMappingAttribute = (RightEndMappingAttribute) annotation;
                return new RightEndMappingAttributeConfigurator(rightEndMappingAttribute.keyAttribute(), rightEndMappingAttribute.targetField());
            }
            case "TypeAttributeAttribute":
                return new TypeAttributeAttributeConfigurator();
            default:
                return null;
        }
    }
}
