/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：与就地修改相关的事件的数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-25 17:45:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.expression.Expression;

import java.util.EventObject;
import java.util.Map;

/**
 * 与就地修改相关的事件的数据类
 */
public class DirectlyChangingEventArgs extends EventObject {

    /**
     * 条件表达式
     */
    private final Expression expression;

    /**
     * 修改类型
     */
    private final EDirectlyChangeType changeType;

    /**
     * 修改的对象类型
     */
    private final Class<?> objectType;

    /**
     * 存储属性新值的字典，键为属性名称，值为属性的新值。
     */
    private final Map<String, Object> newValues;

    /**
     * 创建DirectlyChangingEventArgs实例，并指定条件表达式和属性新值字典
     *
     * @param source     源
     * @param expression 条件表达式
     * @param changeType 修改类型
     * @param objectType 修改的对象类型
     * @param newValues  属性新值字典
     */
    protected DirectlyChangingEventArgs(Object source, Expression expression, EDirectlyChangeType changeType, Class<?> objectType,
                                        Map<String, Object> newValues) {
        super(source);
        this.expression = expression;
        this.changeType = changeType;
        this.objectType = objectType;
        this.newValues = newValues;
    }

    /**
     * 获取条件表达式
     *
     * @return 条件表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 修改的对象类型
     *
     * @return 对象类型
     */
    public Class<?> getObjectType() {
        return this.objectType;
    }

    /**
     * 获取修改类型
     *
     * @return 修改类型
     */
    public EDirectlyChangeType getChangeType() {
        return this.changeType;
    }

    /**
     * 获取存储属性新值的字典，键为属性名称，值为属性的新值。
     *
     * @return 存储属性新值的字典
     */
    public Map<String, Object> getNewValues() {
        return this.newValues;
    }
}
