/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：环境事务单例.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-17 17:18:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

/**
 * 环境事务单例
 */
public class Transaction {

    /**
     * 单例
     */
    private static volatile Transaction instance = null;

    /**
     * 私有构造
     */
    private Transaction() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static Transaction getInstance() {
        return instance;
    }

    /**
     * 增加事务
     */
    public static void addInstance() {
        if (instance == null) {
            synchronized (Transaction.class) {
                if (instance == null) {
                    instance = new Transaction();
                }
            }
        }
    }

    /**
     * 执行事务
     */
    public void doTransaction() {
        //事务块没有实现 无法执行环境事务 此处仅为占位
    }
}
