/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:55:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.core.common.Property;
import io.obase.core.odm.EValueSettingMode;
import io.obase.core.odm.MethodValueSetter;
import io.obase.core.odm.ValueSetter;
import io.obase.core.odm.builder.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 属性标注属性配置器
 */
public class TypeAttributeAttributeConfigurator extends TypeElementAttributeConfigurator {
    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAttribute(Property memberInfo, IAttributeConfigurator configurator) {
        //没有配置取值器并且可读还是公开的
        if (memberInfo.getGetterMethod() != null &&
                (Modifier.isPublic(memberInfo.getGetterMethod().getModifiers()))) {
            configurator.hasValueGetterI(memberInfo.getGetterMethod());
        }

        if (memberInfo.getSetterMethod() != null) {

            //有公开的设值方法
            Class<?> parType = memberInfo.getPropertyType();

            EValueSettingMode model = EValueSettingMode.Assignment;
            if (parType != String.class && Iterable.class.isAssignableFrom(parType))
                model = EValueSettingMode.Appending;

            configurator.hasValueSetterI(ValueSetter.create(memberInfo.getSetterMethod(), model));
        } else {

            try {
                //找set+属性名
                Method method = memberInfo.getGetterMethod().getDeclaringClass().getDeclaredMethod("set" + memberInfo.getName(), memberInfo.getPropertyType());
                configurator.hasValueSetterI(new MethodValueSetter(method), false);
            } catch (NoSuchMethodException ignored) {
                //没有 忽略掉
            }
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
        throw new IllegalArgumentException("属性标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationReference(Property memberInfo, IAssociationReferenceConfigurator configurator) {
        throw new IllegalArgumentException("属性标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    @Override
    protected void configureAssociationEnd(Property memberInfo, IAssociationEndConfigurator configurator) {
        throw new IllegalArgumentException("属性标注不应调用此方法");
    }

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo       属性
     * @param typeConfigurator 配置器
     */
    @Override
    protected void configureType(Property memberInfo, IStructuralTypeConfigurator typeConfigurator) {
        throw new IllegalArgumentException("属性标注不应调用此方法");
    }
}
