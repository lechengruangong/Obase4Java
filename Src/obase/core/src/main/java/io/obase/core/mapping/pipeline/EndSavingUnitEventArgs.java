/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结束保存单元事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:21:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.saving.EObjectStatus;
import io.obase.core.saving.MappingUnit;

/**
 * 结束保存单元事件数据类
 */
public class EndSavingUnitEventArgs extends MappingUnitEventArgs {

    /**
     * 保存过程中发生的异常，如果执行成功则值为NULL
     */
    private final Exception exception;

    /**
     * 创建MappingUnitEventArgs实例，并指定映射单元。
     *
     * @param source           源
     * @param mappingUnit      映射单元
     * @param hostObjectStatus 映射单元主对象状态
     */
    public EndSavingUnitEventArgs(Object source, MappingUnit mappingUnit, EObjectStatus hostObjectStatus, Exception exception) {
        super(source, mappingUnit, hostObjectStatus);
        this.exception = exception;
    }


    /**
     * 获取保存操作发生的异常，如果删除成功则值为NULL。
     *
     * @return 保存操作发生的异常
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 获取一个值，该值指示保存操作是否发生了异常。
     *
     * @return 指示保存除操作是否发生了异常
     */
    public boolean getFailed() {
        return this.exception != null;
    }
}
