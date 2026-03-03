/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：开始保存单元事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:11:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.saving.EObjectStatus;
import io.obase.core.saving.MappingUnit;

/**
 * 开始保存单元事件数据类
 */
public class BeginSavingUnitEventArgs extends MappingUnitEventArgs {
    /**
     * 创建MappingUnitEventArgs实例，并指定映射单元。
     *
     * @param source           源
     * @param mappingUnit      映射单元
     * @param hostObjectStatus 映射单元主对象状态
     */
    public BeginSavingUnitEventArgs(Object source, MappingUnit mappingUnit, EObjectStatus hostObjectStatus) {
        super(source, mappingUnit, hostObjectStatus);
    }
}
