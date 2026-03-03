/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标注成员解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 11:12:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * 标注成员解析器
 */
public class AnnotatedMemberAnalyzer implements ITypeMemberAnalyzer {

    /**
     * 下一节
     */
    private final ITypeMemberAnalyzer analyzer;

    /**
     * 初始化标注成员解析器
     *
     * @param analyzer 下一节解析器
     */
    public AnnotatedMemberAnalyzer(ITypeMemberAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * 获取类型成员解析管道中的下一个解析器
     *
     * @return 下一个成员解析器
     */
    @Override
    public ITypeMemberAnalyzer getNext() {
        return this.analyzer;
    }

    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo 类型成员
     * @param name       如果作为元素，返回元素名称 取元素[0]
     * @return 是否将作为类型元素
     */
    @Override
    public boolean asElement(Property memberInfo, ObjectReferencePack<String> name) {
        name.realValue = null;
        Annotation[] attrs = Utils.getAnnotations(memberInfo);
        if (attrs.length > 0) {
            boolean result = false;
            ObjectReferencePack<String> pack = new ObjectReferencePack<>();

            for (Annotation attribute : attrs) {
                //获取配置器
                MemberAttributeConfigurator configurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(attribute);
                if (configurator != null) {
                    result |= configurator.asElement(memberInfo, pack);
                }
            }

            return result;
        }

        return false;
    }

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, ITypeElementConfigurator configurator) {
        if (configurator instanceof IReferenceElementConfigurator) {
            IReferenceElementConfigurator referenceElementConfigurator = (IReferenceElementConfigurator) configurator;
            this.configure(memberInfo, referenceElementConfigurator);
        }


        if (configurator instanceof IAttributeConfigurator) {
            IAttributeConfigurator attributeConfigurator = (IAttributeConfigurator) configurator;
            this.configure(memberInfo, attributeConfigurator);
        }
    }

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAttributeConfigurator configurator) {
        //调用TypeAttributeAttribute配置
        TypeAttributeAttribute typeAttr = Utils.getAnnotation(memberInfo, TypeAttributeAttribute.class);
        if (typeAttr != null) {

            //获取配置器
            MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(typeAttr);
            if (memberAttributeConfigurator != null) {
                memberAttributeConfigurator.configureAttribute(memberInfo, configurator);
                if (typeAttr.field() != null && !typeAttr.field().isEmpty())
                    configurator.toFieldI(typeAttr.field());
                if (typeAttr.maxNumber() > 0)
                    configurator.hasMaxCharNumberI(typeAttr.maxNumber());
                if (typeAttr.precision() > 0)
                    configurator.hasPrecisionI(typeAttr.precision());
                configurator.hasNullableI(typeAttr.nullable());
            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IReferenceElementConfigurator configurator) {
        if (configurator instanceof IAssociationEndConfigurator) {
            IAssociationEndConfigurator associationEndConfigurator = (IAssociationEndConfigurator) configurator;
            this.configure(memberInfo, associationEndConfigurator);
        }


        if (configurator instanceof IAssociationReferenceConfigurator) {
            IAssociationReferenceConfigurator associationReferenceConfigurator = (IAssociationReferenceConfigurator) configurator;
            this.configure(memberInfo, associationReferenceConfigurator);
        }
    }

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAssociationReferenceConfigurator configurator) {
        //调用AssociationReferenceAttribute配置
        Annotation[] attrs = Utils.getAnnotations(memberInfo);
        if (attrs.length > 0) {
            if (Arrays.stream(attrs).filter(p -> p instanceof AssociationReferenceAttribute || p instanceof ImplicitAssociationAttribute).count() > 1)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:不能同时标注为显式关联引用AssociationReference和隐式关联ImplicitAssociation");

            if (Arrays.stream(attrs).filter(p -> p instanceof AssociationReferenceAttribute).count() == 1 && attrs.length > 1)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:不能将显式关联引用AssociationReference与其他标注一同使用");

            if (Arrays.stream(attrs).filter(p -> p instanceof ImplicitAssociationAttribute).count() == 1 &&
                    Arrays.stream(attrs).filter(p -> p instanceof LeftEndMappingAttribute || p instanceof RightEndMappingAttribute).count() != 2)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:隐式关联ImplicitAssociation标注需要和隐式关联左端LeftEndMapping,隐式关联右端标注RightEndMapping一起标注");

            if (Arrays.stream(attrs).filter(p -> p instanceof ImplicitAssociationAttribute).count() == 1 && attrs.length > 3)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:不能将隐式关联引用AssociationReference与除隐式关联左端LeftEndMapping,隐式关联右端标注RightEndMapping的其他标注一同使用");

            //配置引用
            AssociationReferenceAttribute associationReferenceAttribute = Utils.getAnnotation(memberInfo, AssociationReferenceAttribute.class);
            if (associationReferenceAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(associationReferenceAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationReference(memberInfo, configurator);
            }

            ImplicitAssociationAttribute implicitAssociationAttribute = Utils.getAnnotation(memberInfo, ImplicitAssociationAttribute.class);
            if (implicitAssociationAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(implicitAssociationAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationReference(memberInfo, configurator);
            }

            LeftEndMappingAttribute leftMapping = Utils.getAnnotation(memberInfo, LeftEndMappingAttribute.class);
            if (leftMapping != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(leftMapping);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationReference(memberInfo, configurator);
            }

            RightEndMappingAttribute rightMapping = Utils.getAnnotation(memberInfo, RightEndMappingAttribute.class);
            if (rightMapping != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(rightMapping);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationReference(memberInfo, configurator);
            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAssociationEndConfigurator configurator) {
        //调用AssociationEndAttribute配置
        Annotation[] attrs = Utils.getAnnotations(memberInfo);
        if (attrs.length > 0) {

            if (Arrays.stream(attrs).filter(p -> p instanceof ImplicitAssociationAttribute).count() == 1 && attrs.length > 3)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:不能将隐式关联引用AssociationReference与除隐式关联左端LeftEndMapping,隐式关联右端标注RightEndMapping的其他标注一同使用");

            if (Arrays.stream(attrs).filter(p -> p instanceof ImplicitAssociationAttribute).count() == 1 &&
                    Arrays.stream(attrs).filter(p -> p instanceof LeftEndMappingAttribute || p instanceof RightEndMappingAttribute).count() != 2)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:隐式关联ImplicitAssociation标注需要和隐式关联左端LeftEndMapping,隐式关联右端标注RightEndMapping一起标注");

            if (Arrays.stream(attrs).filter(p -> p instanceof AssociationEndAttribute || p instanceof EndMappingAttribute).count() != 2)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:显式关联端AssociationEnd标注需要和显式关联映射EndMapping标注一起标注");

            if (Arrays.stream(attrs).filter(p -> p instanceof AssociationEndAttribute || p instanceof EndMappingAttribute).count() == 2 && attrs.length > 2)
                throw new IllegalArgumentException(memberInfo.getGetterMethod().getDeclaringClass().getName() + "." + memberInfo.getName() + "标注错误:不能将显式关联端AssociationEnd标注和显式关联映射EndMapping与其他标注一同使用");

            AssociationEndAttribute associationEndAttribute = Utils.getAnnotation(memberInfo, AssociationEndAttribute.class);
            if (associationEndAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(associationEndAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationEnd(memberInfo, configurator);
            }

            EndMappingAttribute endMappingAttribute = Utils.getAnnotation(memberInfo, EndMappingAttribute.class);
            if (endMappingAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(endMappingAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationEnd(memberInfo, configurator);
            }

            LeftEndMappingAttribute leftEndMappingAttribute = Utils.getAnnotation(memberInfo, LeftEndMappingAttribute.class);
            if (leftEndMappingAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(leftEndMappingAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationEnd(memberInfo, configurator);
            }

            RightEndMappingAttribute rightEndMappingAttribute = Utils.getAnnotation(memberInfo, RightEndMappingAttribute.class);
            if (rightEndMappingAttribute != null) {
                //获取配置器
                MemberAttributeConfigurator memberAttributeConfigurator = MemberAttributeConfiguratorFactory.GenerateConfigurator(rightEndMappingAttribute);
                if (memberAttributeConfigurator != null)
                    memberAttributeConfigurator.configureAssociationEnd(memberInfo, configurator);
            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IStructuralTypeConfigurator configurator) {

    }
}
