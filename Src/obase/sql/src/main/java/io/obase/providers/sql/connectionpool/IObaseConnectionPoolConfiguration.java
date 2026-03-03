/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase连接池的配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-4 17:09:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.connectionpool;

/**
 * Obase连接池的配置
 */
public interface IObaseConnectionPoolConfiguration {

    /**
     * 连接池的名称 如果为空或空字符串 则使用默认值Obase ConnectionPool
     *
     * @return 连接池的名称
     */
    String name();

    /**
     * 连接池的最大大小 如果小于等于0 则使用默认值-1
     *
     * @return 连接池的最大大小
     */
    int getMaximumPoolSize();
}
