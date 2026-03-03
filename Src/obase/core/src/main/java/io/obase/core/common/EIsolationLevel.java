/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举事务隔离级别.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:09:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.sql.Connection;

/**
 * 数据库事务隔离级别的枚举
 * 实际值取自java.sql.Connection中同名字段
 */
public enum EIsolationLevel {

    /**
     * 无隔离
     */
    TRANSACTION_NONE(Connection.TRANSACTION_NONE),

    /**
     * 读未提交数据
     */
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * 读已提交数据
     */
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * 可重复读
     */
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * 串行化
     */
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    /**
     * 据库事务隔离级别
     */
    private final int level;

    /**
     * 数据库事务隔离级别的枚举
     *
     * @param level 具体值
     */
    EIsolationLevel(int level) {
        this.level = level;
    }

    /**
     * 获取数据库事务隔离级别
     *
     * @return 数据库事务隔离级别
     */
    public int getLevel() {
        return this.level;
    }
}
