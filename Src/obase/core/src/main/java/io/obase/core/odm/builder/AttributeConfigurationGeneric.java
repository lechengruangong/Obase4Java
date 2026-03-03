/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 16:38:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 属性配置项
 */
public class AttributeConfigurationGeneric<TStructural, TTypeConfiguration extends StructuralTypeConfiguration<TStructural>> extends AttributeConfiguration<TStructural> {
    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param dataType          数据类型
     * @param structuralType    属性所属的类型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    public AttributeConfigurationGeneric(String name, Class<?> dataType, Class<TStructural> structuralType, StructuralTypeConfiguration<TStructural> typeConfiguration) {
        super(name, dataType, structuralType, typeConfiguration);
    }

    /**
     * 进入当前属性所属类型的配置项
     *
     * @return 属性所属类型的配置项
     */
    public TTypeConfiguration upward() {
        return (TTypeConfiguration) this.typeConfiguration;
    }
}
