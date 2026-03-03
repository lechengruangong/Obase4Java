/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为属性的合并处理策略定义规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:32:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.IMappingWorkflow;

/**
 * 为属性的合并处理策略定义规范。
 * 合并处理是指，在对象执行版本合并期间，根据一定的策略对对象的各个属性进行处理。预定义的策略有：
 * 忽略——忽略当前属性，即承认冲突对方版本的值；
 * 覆盖——强制覆盖对方版本的值
 * 累加——将当前版本中属性值的增量累加到对方版本。
 */
public interface IAttributeCombinationHandler {

    /**
     * 对指定属性执行合并处理
     *
     * @param attribute 要合并其值的属性
     * @param workflow  对象修改并实施持久化的工作流机制
     * @param context   合并上下文
     */
    void process(Attribute attribute, IMappingWorkflow workflow, VersionCombinationContext context);
}
