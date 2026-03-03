/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置类型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:47:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.IInstanceConstructor;

import java.lang.reflect.Constructor;

/**
 * 定义配置类型的规范
 */
public interface IStructuralTypeConfigurator {

    /**
     * 继承自谁
     *
     * @return 继承的类型
     */
    Class<?> getDerivedFromI();

    /**
     * 启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个
     *
     * @param name     属性名称，它将作为配置项的键
     * @param dataType 属性的数据类型
     * @return 属性配置项
     */
    IAttributeConfigurator attributeI(String name, Class<?> dataType);

    /**
     * 指定当前类型的基类型
     *
     * @param type 基类型
     */
    void deriveFromI(Class<?> type);

    /**
     * 根据名称获取元素配置器
     *
     * @param name 元素名称
     * @return 元素配置器
     */
    ITypeElementConfigurator getElementI(String name);

    /**
     * 使用一个构造函数为类型创建实例构造器(覆盖现有配置)
     *
     * @param constructorInfo 构造函数
     * @return 实例构造器
     */
    IParameterConfigurator hasConstructorI(Constructor<?> constructorInfo);

    /**
     * 使用一个构造函数为类型创建实例构造器
     *
     * @param constructorInfo 构造函数
     * @param override        是否覆盖既有配置
     * @return 实例构造器
     */
    IParameterConfigurator hasConstructorI(Constructor<?> constructorInfo, boolean override);

    /**
     * 设置类型的实例构造器(覆盖现有配置)
     *
     * @param constructor 实例构造器
     */
    void hasConstructorI(IInstanceConstructor constructor);

    /**
     * 设置类型的实例构造器
     *
     * @param constructor 实例构造器
     * @param override    是否覆盖既有配置
     */
    void hasConstructorI(IInstanceConstructor constructor, boolean override);

    /**
     * 为类型配置项设置一个扩展配置器
     *
     * @param configType 扩展配置器的类型，须继承自TypeExtensionConfiguration
     * @return 扩展配置器
     */
    TypeExtensionConfiguration hasExtensionI(Class<? extends TypeExtensionConfiguration> configType);

    /**
     * 设置类型的命名空间(覆盖现有配置)
     *
     * @param nameSpace 命名空间
     */
    void hasNamespaceI(String nameSpace);

    /**
     * 设置类型的命名空间
     *
     * @param nameSpace 命名空间
     * @param override  是否覆盖既有配置
     */
    void hasNamespaceI(String nameSpace, boolean override);
}
