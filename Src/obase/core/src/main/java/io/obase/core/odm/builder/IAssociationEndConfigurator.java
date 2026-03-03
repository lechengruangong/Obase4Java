/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置关联端的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:43:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;

/**
 * 定义配置关联端的规范
 */
public interface IAssociationEndConfigurator extends IReferenceElementConfigurator {

    /**
     * 端的ClrType
     *
     * @return 端的ClrType
     */
    Class<?> getEntityTypeI();

    /**
     * 获取该关联端上基于当前关联定义的关联引用。
     *
     * @return 当前关联定义的关联引用
     */
    IAssociationReferenceConfigurator getReferenceConfiguratorI();

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化(覆盖现有配置)
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     */
    void hasDefaultAsNewI(boolean defaultAsNew);

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     * @param override     是否覆盖既有配置
     */
    void hasDefaultAsNewI(boolean defaultAsNew, boolean override);

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端(覆盖现有配置)
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     */
    void isAggregatedI(boolean isAggregated);

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     * @param override     是否覆盖既有配置
     */
    void isAggregatedI(boolean isAggregated, boolean override);

    /**
     * 配置关联端映射(覆盖现有配置)
     *
     * @param keyAttribute 关联端标识属性的名称
     * @param targetField  上述标识属性的映射字段
     */
    void hasMappingI(String keyAttribute, String targetField);

    /**
     * 配置关联端映射
     *
     * @param keyAttribute 关联端标识属性的名称
     * @param targetField  上述标识属性的映射字段
     * @param override     是否覆盖既有配置
     */
    void hasMappingI(String keyAttribute, String targetField, boolean override);

    /**
     * 指示是否将当前关联端作为伴随端
     * 设置当前端为伴随端会将之前设置的伴随端改设不作为伴随端。
     * 当override为false时，其它端只要任意一端已设置为伴随端，本方法就不再执行设置操作。
     *
     * @param value 是否伴随
     */
    void asCompanionI(boolean value);

    /**
     * 指示是否将当前关联端作为伴随端
     * 设置当前端为伴随端会将之前设置的伴随端改设不作为伴随端。
     * 当override为false时，其它端只要任意一端已设置为伴随端，本方法就不再执行设置操作。
     *
     * @param value    是否伴随
     * @param override 是否覆盖既有配置
     */
    void asCompanionI(boolean value, boolean override);

    /**
     * 生成基于当前关联定义的关联引用的配置器，如果配置器已存在返回该配置器。
     *
     * @param propInfo 返回关联引用的访问器，如果关联引用没有访问器返回null
     * @return 当前关联定义的关联引用的配置器
     */
    IAssociationReferenceConfigurator associationReferenceI(ObjectReferencePack<Property> propInfo);
}
