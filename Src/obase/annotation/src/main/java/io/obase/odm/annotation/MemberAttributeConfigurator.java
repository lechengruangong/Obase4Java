/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员标注属性配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 09:48:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.odm.EValueSettingMode;
import io.obase.core.odm.MethodValueSetter;
import io.obase.core.odm.ValueSetter;
import io.obase.core.odm.builder.*;

import java.lang.reflect.Method;

/**
 * 成员标注属性配置器
 */
public abstract class MemberAttributeConfigurator {

    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo  属性
     * @param elementName 成员名册
     * @return 是否将作为类型元素
     */
    protected abstract boolean asElement(Property memberInfo, ObjectReferencePack<String> elementName);

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected abstract void configureElement(Property memberInfo, ITypeElementConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected abstract void configureAttribute(Property memberInfo, IAttributeConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected abstract void configureReferenceElement(Property memberInfo, IReferenceElementConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected abstract void configureAssociationReference(Property memberInfo, IAssociationReferenceConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected abstract void configureAssociationEnd(Property memberInfo, IAssociationEndConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo       属性
     * @param typeConfigurator 配置器
     */
    protected abstract void configureType(Property memberInfo, IStructuralTypeConfigurator typeConfigurator);

    /**
     * 配置取值器和设值器
     *
     * @param memberInfo   属性
     * @param configurator 配置器
     */
    protected void configureGetterAndSetter(Property memberInfo, IReferenceElementConfigurator configurator) {
        //取值器
        //取值方法是可读还是公开的
        if (memberInfo.getGetterMethod() != null) {
            configurator.hasValueGetterI(memberInfo.getGetterMethod());
        }

        //设值器
        //设值方法是可读还是公开的
        if (memberInfo.getSetterMethod() == null) {
            try {
                //找set+属性名
                Method method = memberInfo.getGetterMethod().getDeclaringClass().getDeclaredMethod("set" + memberInfo.getName(), memberInfo.getPropertyType());
                configurator.hasValueSetterI(new MethodValueSetter(method), false);
            } catch (NoSuchMethodException ignored) {
                //没有 忽略掉
            }
        } else {
            //有公开的设值方法
            Class<?> parType = memberInfo.getPropertyType();

            EValueSettingMode model = EValueSettingMode.Assignment;
            if (parType != String.class && Iterable.class.isAssignableFrom(parType))
                model = EValueSettingMode.Appending;

            configurator.hasValueSetterI(ValueSetter.create(memberInfo.getSetterMethod(), model), false);
        }
    }
}
