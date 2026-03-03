/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的存储标记判定器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 17:13:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.*;

/**
 * 默认的存储标记判定器，基于“异构存储扩展”判定对象的存储标识。
 */
public class StorageSymbolJudge implements IStorageSymbolJudge {

    /**
     * 判定指定对象的存储标记
     *
     * @param obj     要判定其存储标记的对象
     * @param objType 对象的类型
     * @return 指定对象的存储标记
     */
    @Override
    public StorageSymbol judge(Object obj, ObjectType objType) {
        TypeExtension typeExtension = objType.getExtension(HeterogStorageExtension.class);
        if (typeExtension == null)
            return StorageSymbols.getCurrent().getDefault();
        HeterogStorageExtension heterogStorageExtension = (HeterogStorageExtension) typeExtension;
        return heterogStorageExtension.getStorageSymbol();
    }

    /**
     * 判定指定类型的对象的存储标记
     * 在特定情形（如分区存储）下，同一类型的对象可能分散存储于多个存储服务，因而有多个存储标记。
     *
     * @param objType 对象类型
     * @return 存储标记集
     */
    @Override
    public StorageSymbol[] judge(ObjectType objType) {
        TypeExtension typeExtension = objType.getExtension(HeterogStorageExtension.class);
        if (typeExtension == null)
            return new StorageSymbol[]{StorageSymbols.getCurrent().getDefault()};
        HeterogStorageExtension heterogStorageExtension = (HeterogStorageExtension) typeExtension;
        return new StorageSymbol[]{heterogStorageExtension.getStorageSymbol()};
    }
}
