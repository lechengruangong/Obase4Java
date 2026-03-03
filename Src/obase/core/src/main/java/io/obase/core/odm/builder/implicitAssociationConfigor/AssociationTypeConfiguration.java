/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联型的配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 18:00:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.builder.TypeExtensionConfiguration;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 隐式关联型的配置器
 *
 * @param <TAssociation> 关联型类型
 */
public class AssociationTypeConfiguration<TAssociation> extends io.obase.core.odm.builder.AssociationTypeConfiguration<TAssociation> {

    /**
     * 获取关联端标签
     */
    private final String endsTag;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param modelBuilder    指定类型配置项所属的建模器
     * @param associationType 关联型类型
     */
    protected AssociationTypeConfiguration(AssociationEndConfiguration[] endsConfigs, TypeExtensionConfiguration[] extensionConfigs, String endsTag, ModelBuilder modelBuilder, Class<TAssociation> associationType) {
        super(associationType, modelBuilder);
        //初始化容器
        this.typeElementConfigurations = new HashMap<>();
        //赋值
        this.endsTag = endsTag;
        for (AssociationEndConfiguration endConfiguration : endsConfigs) {
            this.typeElementConfigurations.put(endConfiguration.getName(), endConfiguration);
        }
        this.extensionConfigs.addAll(Arrays.asList(extensionConfigs));
    }

    /**
     * 获取关联端标签
     *
     * @return 关联端标签
     */
    public String getEndsTag() {
        return this.endsTag;
    }
}
