/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：开始就地修改事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:04:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.expression.Expression;

import java.util.Map;

/**
 * 开始就地修改事件数据类
 */
public class BeginDirectlyChangingEventArgs extends DirectlyChangingEventArgs {

    /**
     * 创建DirectlyChangingEventArgs实例，并指定条件表达式和属性新值字典
     *
     * @param source     源
     * @param expression 条件表达式
     * @param changeType 修改类型
     * @param objectType 修改的对象类型
     * @param newValues  属性新值字典
     */
    public BeginDirectlyChangingEventArgs(Object source, Expression expression, EDirectlyChangeType changeType, Class<?> objectType, Map<String, Object> newValues) {
        super(source, expression, changeType, objectType, newValues);
    }
}
