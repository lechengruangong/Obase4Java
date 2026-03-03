/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置实体型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:49:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 定义配置实体型的规范
 */
public interface IEntityTypeConfigurator extends IObjectTypeConfigurator {

    /**
     * 设置标识属性(覆盖现有配置)
     *
     * @param attrName 属性名称
     */
    void hasKeyAttributeI(String attrName);

    /**
     * 设置标识属性
     *
     * @param attrName 属性名称
     * @param override 是否覆盖既有配置
     */
    void hasKeyAttributeI(String attrName, boolean override);

    /**
     * 设置一个值，该值指示标识属性是否为自增(覆盖现有配置)
     *
     * @param keyIsSelfIncreased 是否为自增
     */
    void hasKeyIsSelfIncreasedI(boolean keyIsSelfIncreased);

    /**
     * 设置一个值，该值指示标识属性是否为自增
     *
     * @param keyIsSelfIncreased 是否为自增
     * @param override           是否覆盖既有配置
     */
    void hasKeyIsSelfIncreasedI(boolean keyIsSelfIncreased, boolean override);

    /**
     * 获取标识属性集合
     *
     * @return 标识属性集合
     */
    String[] getKeyAttributesFiledI();
}
