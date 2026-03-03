/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构存储扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 17:14:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 异构存储扩展，用于对模型中对象类型的配置进行扩展。
 */
public class HeterogStorageExtension extends TypeExtension {

    /**
     * 存储标记
     */
    private StorageSymbol storageSymbol;

    /**
     * 获取存储标记
     *
     * @return 默认存储标记
     */
    public StorageSymbol getStorageSymbol() {
        StorageSymbol result = this.storageSymbol;
        if (this.getExtendedType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this.getExtendedType();
            if (associationType.getCompanionEnd() != null) {
                TypeExtension extension =
                        associationType.getCompanionEnd().getEntityType().getExtension(HeterogStorageExtension.class);
                if (extension instanceof HeterogStorageExtension) {
                    HeterogStorageExtension heterogStorageExtension = (HeterogStorageExtension) extension;
                    result = heterogStorageExtension.getStorageSymbol();
                }
            }
        }
        return result;
    }

    /**
     * 设置存储标记
     *
     * @param storageSymbol 存储标记
     * @throws IllegalArgumentException 只能设置一次默认存储标记，对已设置的默认存储标记进行修改将引发异常。
     */
    public void setStorageSymbol(StorageSymbol storageSymbol) {
        if (this.storageSymbol != null)
            throw new IllegalArgumentException("只能设置一次默认存储标记");
        this.storageSymbol = storageSymbol;
    }
}

