/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结束就地修改事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:28:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.expression.Expression;

import java.util.Map;

/**
 * 结束就地修改事件数据类
 */
public class EndDirectlyChangingEventArgs extends DirectlyChangingEventArgs {

    /**
     * 影响的行数
     */
    private final int affectedCount;

    /**
     * 执行过程中发生的异常，如果执行成功则值为NULL
     */
    private final Exception exception;

    /**
     * 创建DirectlyChangingEventArgs实例，并指定条件表达式和属性新值字典
     *
     * @param source     源
     * @param expression 条件表达式
     * @param changeType 修改类型
     * @param objectType 修改的对象类型
     * @param newValues  属性新值字典
     */
    public EndDirectlyChangingEventArgs(Object source, Expression expression, EDirectlyChangeType changeType, Class<?> objectType, Map<String, Object> newValues, int affectedCount, Exception exception) {
        super(source, expression, changeType, objectType, newValues);
        this.affectedCount = affectedCount;
        this.exception = exception;
    }

    /**
     * 获取就地修改操作发生的异常，如果删除成功则值为NULL。
     *
     * @return 发生的异常
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 获取一个值，该值指示修改操作是否发生了异常。
     *
     * @return 修改操作是否发生了异常
     */
    public boolean getFailed() {
        return this.exception != null;
    }

    /**
     * 影响的行数
     *
     * @return 影响的行数
     */
    public int getAffectedCount() {
        return this.affectedCount;
    }
}
