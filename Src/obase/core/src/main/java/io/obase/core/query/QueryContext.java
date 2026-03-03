/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询上下文.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-26 10:24:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.Expression;

/**
 * 查询上下文
 */
public class QueryContext {

    /**
     * 表示查询表达式
     */
    private final Expression expression;
    /**
     * 受影响的行数
     */
    private int affectedCount;
    /**
     * 要执行的存储指令（如Sql语句）
     */
    private Object command;
    /**
     * 执行命令的过程中发生的异常，未发生异常则为Null
     */
    private Exception exception;
    /**
     * 指示查询操作是否已被取消
     */
    private boolean hasCanceled;

    /**
     * 要执行的查询
     */
    private QueryOp query;

    /**
     * 查询结果
     */
    private Object result;

    /**
     * 执行Sql语句所消耗的时间，以毫秒为单位
     */
    private int timeConsumed;

    /**
     * 在查询过程中由用户自定义的状态信息
     */
    private String userState;

    /**
     * 实例化QueryContext的新实例
     *
     * @param query      要执行的查询
     * @param expression 查询表达式
     */
    public QueryContext(QueryOp query, Expression expression) {
        this.query = query;
        this.expression = expression;
    }

    /**
     * 获取执行命令的过程中发生的异常，未发生异常则为Null
     *
     * @return 命令的过程中发生的异常
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 设置执行命令的过程中发生的异常，未发生异常则为Null
     *
     * @param exception 命令的过程中发生的异常
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * 获取表示查询表达式
     *
     * @return 查询表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 获取受影响的行数
     *
     * @return 受影响的行数
     */
    public int getAffectedCount() {
        return this.affectedCount;
    }

    /**
     * 设置受影响的行数
     *
     * @param affectedCount 受影响的行数
     */
    public void setAffectedCount(int affectedCount) {
        this.affectedCount = affectedCount;
    }

    /**
     * 获取执行Sql语句所消耗的时间，以毫秒为单位
     *
     * @return 执行Sql语句所消耗的时间，以毫秒为单位
     */
    public int getTimeConsumed() {
        return this.timeConsumed;
    }

    /**
     * 设置执行Sql语句所消耗的时间，以毫秒为单位
     *
     * @param timeConsumed 执行Sql语句所消耗的时间，以毫秒为单位
     */
    public void setTimeConsumed(int timeConsumed) {
        this.timeConsumed = timeConsumed;
    }

    /**
     * 获取要执行的存储指令
     *
     * @return 要执行的存储指令
     */
    public Object getCommand() {
        return this.command;
    }

    /**
     * 设置要执行的存储指令
     *
     * @param command 要执行的存储指令
     */
    public void setCommand(Object command) {
        this.command = command;
    }

    /**
     * 获取执行的结果
     *
     * @return 执行的结果
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * 设置执行的结果
     *
     * @param result 执行的结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取查询操作
     *
     * @return 查询操作
     */
    public QueryOp getQuery() {
        return this.query;
    }

    /**
     * 设置查询操作
     *
     * @param query 查询操作
     */
    public void setQuery(QueryOp query) {
        this.query = query;
    }

    /**
     * 获取在查询过程中由用户自定义的状态信息
     *
     * @return 查询过程中由用户自定义的状态信息
     */
    public String getUserState() {
        return this.userState;
    }

    /**
     * 设置在查询过程中由用户自定义的状态信息
     *
     * @param userState 查询过程中由用户自定义的状态信息
     */
    public void setUserState(String userState) {
        this.userState = userState;
    }

    /**
     * 获取指示查询操作是否已被取消
     *
     * @return 查询操作是否已被取消
     */
    public boolean getHasCanceled() {
        return this.hasCanceled;
    }

    /**
     * 设置指示查询操作是否已被取消
     *
     * @param hasCanceled 查询操作是否已被取消
     */
    public void setHasCanceled(boolean hasCanceled) {
        this.hasCanceled = hasCanceled;
    }


}

