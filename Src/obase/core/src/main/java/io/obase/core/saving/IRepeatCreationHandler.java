/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为重复创建冲突的处理策略定义规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 14:51:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 为重复创建冲突的处理策略定义规范
 */
public interface IRepeatCreationHandler {

    /**
     * 处理重复创建冲突
     *
     * @param mappingUnit 映射执行器
     */
    void processRepeatConflict(MappingUnit mappingUnit);
}
