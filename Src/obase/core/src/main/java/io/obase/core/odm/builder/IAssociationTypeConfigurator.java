/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义关联型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-23 17:20:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 定义关联型的规范
 */
public interface IAssociationTypeConfigurator extends IObjectTypeConfigurator {

    /**
     * 关联型
     *
     * @return 关联型
     */
    Class<?> getAssociationTypeI();

    /**
     * 关联端集合
     *
     * @return 关联端集合
     */
    IAssociationEndConfigurator[] getAssociationEndsI();

    /**
     * 设置是否为显式关联型
     *
     * @param value 是否为显式关联型
     */
    void setIsVisibleI(boolean value);

    /**
     * 设置是否为显式关联型
     *
     * @param value    是否为显式关联型
     * @param override 是否覆盖
     */
    void setIsVisibleI(boolean value, boolean override);

    /**
     * 启动一个关联端配置项，如果要启动的配置项未创建则新建一个
     *
     * @param name       关联端的名称
     * @param entityType 作为关联端的实体类型
     * @return 关联端配置项
     */
    IAssociationEndConfigurator associationEndI(String name, Class<?> entityType);
}
