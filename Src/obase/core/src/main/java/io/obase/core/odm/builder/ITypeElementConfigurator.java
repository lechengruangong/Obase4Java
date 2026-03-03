/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置类型元素的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:46:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.odm.EValueSettingMode;
import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.IValueSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 定义配置类型元素的规范
 */
public interface ITypeElementConfigurator {

    /**
     * 为元素配置项设置一个扩展配置器
     *
     * @param configType 扩展配置器的类型，须继承自ElementExtensionConfiguration
     * @return 扩展配置器
     */
    <TExtensionConfiguration extends ElementExtensionConfiguration> ElementExtensionConfiguration hasExtensionI(Class<TExtensionConfiguration> configType);

    /**
     * 为类型元素设置取值器(覆盖现有配置)
     *
     * @param valueGetter 取值器
     */
    void hasValueGetterI(IValueGetter valueGetter);

    /**
     * 为类型元素设置取值器
     *
     * @param valueGetter 取值器
     * @param override    是否覆盖既有配置
     */
    void hasValueGetterI(IValueGetter valueGetter, boolean override);

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器(覆盖现有配置)
     *
     * @param method 获取元素值的方法
     */
    void hasValueGetterI(Method method);

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器
     *
     * @param method   获取元素值的方法
     * @param override 是否覆盖既有配置
     */
    void hasValueGetterI(Method method, boolean override);

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器(覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    void hasValueGetterI(Property property);

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    void hasValueGetterI(Property property, boolean override);

    /**
     * 使用表示类型元素的字段为类型元素创建取值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    void hasValueGetterI(Field field);

    /**
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    void hasValueGetterI(Field field, boolean override);

    /**
     * 使用指定的类成员为类型元素创建取值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    void hasValueGetterI(String memberName);

    /**
     * 使用指定的类成员为类型元素创建取值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    void hasValueGetterI(String memberName, boolean override);

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器(覆盖现有配置)
     */
    void hasValueGetterI();

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器
     *
     * @param override 是否覆盖既有配置
     */
    void hasValueGetterI(boolean override);

    /**
     * 为类型元素设置设值器(覆盖现有配置)
     *
     * @param valueSetter 设值器
     */
    void hasValueSetterI(IValueSetter valueSetter);

    /**
     * 为类型元素设置设值器
     *
     * @param valueSetter 设值器
     * @param override    是否覆盖既有配置
     */
    void hasValueSetterI(IValueSetter valueSetter, boolean override);

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器(覆盖现有配置)
     *
     * @param method 为类型元素设值的方法
     * @param mode   设值模式
     */
    void hasValueSetterI(Method method, EValueSettingMode mode);

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器
     *
     * @param method   为类型元素设值的方法
     * @param mode     设值模式
     * @param override 是否覆盖既有配置
     */
    void hasValueSetterI(Method method, EValueSettingMode mode, boolean override);

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    void hasValueSetterI(Property property);

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    void hasValueSetterI(Property property, boolean override);

    /**
     * 使用表示类型元素的字段为类型元素创建设值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    void hasValueSetterI(Field field);

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    void hasValueSetterI(Field field, boolean override);

    /**
     * 使用指定的类成员为类型元素创建设值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    void hasValueSetterI(String memberName);

    /**
     * 使用指定的类成员为类型元素创建设值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    void hasValueSetterI(String memberName, boolean override);

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器(覆盖现有配置)
     */
    void hasValueSetterI();

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器
     *
     * @param override 是否覆盖既有配置
     */
    void hasValueSetterI(boolean override);

    /**
     * 进入当前元素所属类型的配置项
     *
     * @return 元素所属类型的配置项
     */
    IStructuralTypeConfigurator upwardI();
}
