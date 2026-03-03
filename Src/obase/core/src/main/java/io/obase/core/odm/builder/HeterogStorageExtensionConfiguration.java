/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构存储扩展对应的配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 17:07:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.HeterogStorageExtension;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.TypeExtension;

/**
 * HeterogStorageExtension（异构存储扩展）对应的配置器。
 */
public class HeterogStorageExtensionConfiguration extends TypeExtensionConfiguration {

    /**
     * 类型的存储标记
     */
    private StorageSymbol storageSymbol;

    /**
     * 获取类型扩展的类型
     *
     * @return 类型扩展的类型
     */
    @Override
    public Class<? extends TypeExtension> getExtensionType() {
        return HeterogStorageExtension.class;
    }

    /**
     * 根据配置元数据生成类型扩展实例
     *
     * @return 类型扩展实例
     */
    @Override
    public TypeExtension makeExtension() {
        HeterogStorageExtension extension = new HeterogStorageExtension();
        extension.setStorageSymbol(this.storageSymbol);
        return extension;
    }

    /**
     * 配置类型的存储标记
     *
     * @param symbol 存储标记
     * @return 自身
     */
    public HeterogStorageExtensionConfiguration hasStorageSymbol(StorageSymbol symbol) {
        this.storageSymbol = symbol;
        return this;
    }
}
