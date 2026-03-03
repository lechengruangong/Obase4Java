/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：PostExecuteCommand事件数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:40:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.util.EventObject;

/**
 * PostExecuteSql事件数据类
 */
public class PostExecuteCommandEventArgs extends EventObject {

    /**
     * 受影响的行数
     */
    private final int affectedCount;

    /**
     * 要执行的存储指令（如Sql语句）
     */
    private final Object command;

    /**
     * 执行Sql语句的过程中发生的异常，未发生异常则为Null
     */
    private final Exception exception;

    /**
     * 执行Sql语句所消耗的时间，以毫秒为单位
     */
    private final int timeConsumed;

    /**
     * 创建PostExecuteSqlEventArgs实例，并指定要执行的存储指令（如Sql语句）和执行消耗的时间
     *
     * @param source        源
     * @param command       要执行的存储指令（如Sql语句
     * @param timeConsumed  执行指令所消耗的时间，以毫秒为单位
     * @param affectedCount 受影响的行数
     */
    public PostExecuteCommandEventArgs(Object source, Object command, int timeConsumed, int affectedCount) {
        super(source);
        this.affectedCount = affectedCount;
        this.command = command;
        this.timeConsumed = timeConsumed;
        this.exception = null;
    }

    /**
     * 创建PostExecuteSqlEventArgs实例，并指定要执行的存储指令（如Sql语句）、执行消耗的时间、以及执行过程中发生的异常。
     *
     * @param source       源
     * @param command      要执行的存储指令（如Sql语句
     * @param timeConsumed 执行指令所消耗的时间，以毫秒为单位
     * @param exception    异常
     */
    public PostExecuteCommandEventArgs(Object source, Object command, int timeConsumed, Exception exception) {
        super(source);
        this.affectedCount = 0;
        this.command = command;
        this.timeConsumed = timeConsumed;
        this.exception = exception;
    }

    /**
     * 获取执行Sql语句的过程中发生的异常，未发生异常则为Null。
     *
     * @return 发生的异常
     */
    public Exception getException() {
        return this.exception;
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
     * 获取要执行的存储指令（如Sql语句）
     *
     * @return 执行的存储指令（如Sql语句）
     */
    public Object getCommand() {
        return this.command;
    }

    /**
     * 获取执行Sql语句所消耗的时间，以毫秒为单位。
     *
     * @return 执行Sql语句所消耗的时间，以毫秒为单位。
     */
    public int getTimeConsumed() {
        return this.timeConsumed;
    }
}
