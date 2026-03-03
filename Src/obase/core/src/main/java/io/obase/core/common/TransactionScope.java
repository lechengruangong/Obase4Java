/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：事务块.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-17 17:12:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

/**
 * 表示环境事务
 */
public class TransactionScope {
    /**
     * 初始化事务块
     */
    public TransactionScope() {
        Transaction.addInstance();
        throw new UnsupportedOperationException("Java版环境事务未实现,暂不支持异构事务,跨上下文事务");
    }

    /**
     * 完成事务
     */
    public void complete() {
        throw new UnsupportedOperationException("Java版环境事务未实现,暂不支持异构事务,跨上下文事务");
    }

    /**
     * 关闭事务
     */
    public void close() {
        throw new UnsupportedOperationException("Java版环境事务未实现,暂不支持异构事务,跨上下文事务");
    }
}
