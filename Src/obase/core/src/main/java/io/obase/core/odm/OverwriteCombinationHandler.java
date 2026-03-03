/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：覆盖合并处理策略.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:13:34
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm;

import io.obase.core.IMappingWorkflow;
import io.obase.core.common.Utils;


/**
 * 执行“覆盖”合并处理策略
 */
public class OverwriteCombinationHandler implements IAttributeCombinationHandler {

    /**
     * 对指定属性执行合并处理
     *
     * @param attribute 要合并其值的属性
     * @param workflow  对象修改并实施持久化的工作流机制
     * @param context   合并上下文
     */
    @Override
    public void process(Attribute attribute, IMappingWorkflow workflow, VersionCombinationContext context) {
        IValueGetter getter = attribute.getValueGetter();
        Object obj = context.getComplexObject();
        if (obj == null) {
            obj = context.getObject();
        }
        Object value = getter.getValue(obj);

        //字段全名
        String filedName = Utils.getAttributeFiledName(attribute, context);

        workflow.setField(filedName, value);
    }
}
