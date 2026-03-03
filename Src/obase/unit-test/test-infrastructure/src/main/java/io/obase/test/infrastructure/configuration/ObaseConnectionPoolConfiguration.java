package io.obase.test.infrastructure.configuration;

import io.obase.providers.sql.connectionpool.IObaseConnectionPoolConfiguration;

/**
 * Obase连接池配置
 *
 * @param name 连接池的名称
 */
public record ObaseConnectionPoolConfiguration(String name) implements IObaseConnectionPoolConfiguration {

    /**
     * 初始化Obase连接池配置
     *
     * @param name 连接池的名称
     */
    public ObaseConnectionPoolConfiguration {
    }

    /**
     * 连接池的名称 如果为空或空字符串 则使用默认值Obase ConnectionPool
     *
     * @return 连接池的名称
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * 连接池的最大大小 如果小于等于0 则使用默认值-1
     *
     * @return 连接池的最大大小
     */
    @Override
    public int getMaximumPoolSize() {
        return 5;
    }
}
