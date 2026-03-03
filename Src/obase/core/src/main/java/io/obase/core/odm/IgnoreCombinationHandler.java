/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：“忽略”合并处理策略.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 11:54:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.IMappingWorkflow;

import static io.obase.core.common.Utils.getAttributeFiledName;

/**
 * 执行“忽略”合并处理策略。
 */
public class IgnoreCombinationHandler implements IAttributeCombinationHandler {

    /**
     * 对指定属性执行合并处理
     *
     * @param attribute 要合并其值的属性
     * @param workflow  对象修改并实施持久化的工作流机制
     * @param context   合并上下文
     */
    @Override
    public void process(Attribute attribute, IMappingWorkflow workflow, VersionCombinationContext context) {
        //获取属性的映射字段
        String filedName = getAttributeFiledName(attribute, context);
        workflow.ignoreField(filedName);
    }
}

