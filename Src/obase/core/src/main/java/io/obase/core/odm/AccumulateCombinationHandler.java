/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：累加合并处理策略处理器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:03:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.IMappingWorkflow;
import io.obase.core.saving.EConcurrentConflictType;

import static io.obase.core.common.Utils.getAttributeFiledName;

/**
 * 执行“累加”合并处理策略。
 */
public class AccumulateCombinationHandler implements IAttributeCombinationHandler {

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
        Object newValue = getter.getValue(obj);
        Object originalValue = null;
        EConcurrentConflictType conflictType = context.getConflictType();

        //取出原始值
        if (conflictType == EConcurrentConflictType.VersionConflict) {
            IGetAttributeValue attributeGetter = context.getAttributeOriginalValueGetter();
            Object tempValue = context.getComplexAttribute() != null ? attributeGetter.getAttributeValue(context.getObject(), context.getComplexAttribute(), null) : attributeGetter.getAttributeValue(context.getObject(), attribute, null);
            originalValue = context.getComplexObject() != null ? getter.getValue(tempValue) : tempValue;
        }

        //判断累加值
        long increment = 0L;

        if (originalValue != null) {
            try {
                long newValueLong = Long.parseLong(newValue.toString());
                long originalValueLong = Long.parseLong(originalValue.toString());
                increment = newValueLong - originalValueLong;
            } catch (NumberFormatException ignored) {
                //不成功就保持0
            }
        }

        if (increment == 0) {
            try {
                long newValueLong = Long.parseLong(newValue.toString());
                if (newValueLong != 0)
                    increment += newValueLong;
            } catch (NumberFormatException ignored) {
                //不成功就保持0
            }
        }
        //获取属性的映射字段
        String filedName = getAttributeFiledName(attribute, context);

        workflow.increaseField(filedName, increment);
    }
}
