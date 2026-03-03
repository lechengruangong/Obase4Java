/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户的补充配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:36:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.odm.Attribute;
import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.builder.IComplementConfigurator;
import io.obase.core.odm.builder.StructuralTypeConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * 补充配置器
 */
public class ComplementConfigurator implements IComplementConfigurator {

    /**
     * 补充配置管道中的下一个配置器
     */
    private final IComplementConfigurator next;

    /**
     * 初始化补充配置器
     *
     * @param next 下一个配置器
     */
    public ComplementConfigurator(IComplementConfigurator next) {
        this.next = next;
    }

    /**
     * 补充配置管道中的下一个配置器
     *
     * @return 下一个补充配置器
     */
    @Override
    public IComplementConfigurator getNext() {
        return this.next;
    }

    /**
     * 根据类型配置项中的元数据配置指定的类型
     *
     * @param targetType    要配置的类型
     * @param configuration 包含配置元数据的类型配置项
     */
    @Override
    public void configure(StructuralType targetType, StructuralTypeConfiguration<?> configuration) {
        MultiTenantExtension ext = (MultiTenantExtension) targetType.getExtension(MultiTenantExtension.class);
        if (ext != null && (ext.getTenantIdMark() == null || ext.getTenantIdMark().isEmpty())) {
            Attribute attribute = new Attribute(boolean.class, "Obase_gen_tenantIdMark");
            //目标字段 若果未设置DeletionField就和DeletionMark相同
            attribute.setTargetField((ext.getTenantIdField() == null || ext.getTenantIdField().isEmpty()) ? ext.getTenantIdMark() : ext.getTenantIdField());
            Field field;
            try {
                field = targetType.getRebuildingType().getDeclaredField(StringUtils.uncapitalize(attribute.getName()));
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("无法获取多租户字段", e);
            }
            //构造FieldValueGetter
            IValueGetter valueGetter;
            if (ext.getTenantIdType() == String.class || ext.getTenantIdType() == UUID.class) {
                valueGetter = new MultiTenantStringFieldValueGetter(field, targetType.getRebuildingType(), configuration.getModelBuilder().getContextType());
            } else if (ext.getTenantIdType() == int.class || ext.getTenantIdType() == Integer.class) {
                valueGetter = new MultiTenantIntFieldValueGetter(field, targetType.getRebuildingType(), configuration.getModelBuilder().getContextType());
            } else if (ext.getTenantIdType() == long.class || ext.getTenantIdType() == Long.class) {
                valueGetter = new MultiTenantLongFieldValueGetter(field, targetType.getRebuildingType(), configuration.getModelBuilder().getContextType());
            } else {
                throw new IllegalArgumentException("多租户主键属性必须为string,int,long,Guid类型中的一种");
            }
            attribute.setValueGetter(valueGetter);
            //构造FieldValueSetter
            MultiTenantFieldValueSetter setter = new MultiTenantFieldValueSetter(field, targetType);
            attribute.setValueSetter(setter);
            targetType.addAttribute(attribute);
        }
    }
}
