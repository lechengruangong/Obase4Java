/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储标记判定器规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:31:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectType;
import io.obase.core.odm.StorageSymbol;

/**
 * 存储标记判定器规范，提供判定指定对象的存储标记的方法
 */
public interface IStorageSymbolJudge {

    /**
     * 判定指定对象的存储标记
     *
     * @param obj     要判定其存储标记的对象
     * @param objType 对象的类型
     * @return 存储标记
     */
    StorageSymbol judge(Object obj, ObjectType objType);

    /**
     * 判定指定类型的对象的存储标记
     * 在特定情形（如分区存储）下，同一类型的对象可能分散存储于多个存储服务，因而有多个存储标记。
     *
     * @param objType 对象类型
     * @return 存储标记集
     */
    StorageSymbol[] judge(ObjectType objType);
}
