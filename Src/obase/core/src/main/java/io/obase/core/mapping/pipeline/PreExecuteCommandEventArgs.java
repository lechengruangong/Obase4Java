/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：PreExecuteCommandEventArgs事件的数据类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:37:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.util.EventObject;

/**
 * PreExecuteCommandEventArgs事件的数据类
 */
public class PreExecuteCommandEventArgs extends EventObject {

    /**
     * 要执行的存储指令（如Sql语句）
     */
    private final Object command;

    /**
     * 创建PreExecuteCommandEventArgs实例
     *
     * @param command 要执行的存储指令（如Sql语句）
     * @param source  源
     */
    public PreExecuteCommandEventArgs(Object command, Object source) {
        super(source);
        this.command = command;
    }

    /**
     * 获取要执行的Sql语句
     *
     * @return 获取要执行的Sql语句
     */
    public Object getCommand() {
        return this.command;
    }
}
