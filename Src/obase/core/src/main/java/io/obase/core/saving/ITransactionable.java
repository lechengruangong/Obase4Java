/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：本地事务.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:32:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.common.EIsolationLevel;

/**
 * 提供对本地事务的支持
 * 本地事务包含的操作局限于单一数据资源(如数据库或消息队列),由该数据资源负责管理.
 */
public interface ITransactionable {

    /**
     * 开始本地事务
     *
     * @param isolationLevel 事务隔离级别
     */
    void beginTransaction(EIsolationLevel isolationLevel);

    /**
     * 提交本地事务
     */
    void commitTransaction();

    /**
     * 回滚本地事务
     */
    void rollbackTransaction();
}
