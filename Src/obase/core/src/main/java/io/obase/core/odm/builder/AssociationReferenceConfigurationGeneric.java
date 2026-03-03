/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联引用配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 17:20:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 关联引用配置项
 *
 * @param <TEntity>            关联所在的实体型类型
 * @param <TTypeConfiguration> 实体型配置
 */
public class AssociationReferenceConfigurationGeneric<TEntity, TTypeConfiguration extends EntityTypeConfiguration<TEntity>> extends AssociationReferenceConfiguration<TEntity> {
    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param dataType          关联引用的关联类型
     * @param isMultiple        指示元素是否具有多重性，即其值是否为集合
     * @param entityType        实体类型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    public AssociationReferenceConfigurationGeneric(String name, Class<?> dataType, Boolean isMultiple, Class<TEntity> entityType, TTypeConfiguration typeConfiguration) {
        super(name, dataType, isMultiple, entityType, typeConfiguration);
    }

    /**
     * 进入当前关联引用所属实体型的配置项
     *
     * @return 所属实体型的配置项
     */
    public TTypeConfiguration upward() {
        return (TTypeConfiguration) this.typeConfiguration;
    }
}
