/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：数据库连接池.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-4 17:11:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.obase.core.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 连接池
 */
public class ObaseConnectionPool implements AutoCloseable {

    /**
     * 单例
     */
    private static volatile ObaseConnectionPool instance;
    /**
     * 连接字符串对应的数据源
     */
    private final Map<String, DataSource> sources = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 私有构造
     */
    private ObaseConnectionPool() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ObaseConnectionPool.getInstance().close()));
    }

    /**
     * 获取连接池单例
     *
     * @return 连接池单例
     */
    public static ObaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ObaseConnectionPool.class) {
                if (instance == null) {
                    instance = new ObaseConnectionPool();
                }
            }
        }
        return instance;
    }

    /**
     * 获取简要分析
     *
     * @return 简要分析
     */
    public String getStatistics() {
        StringBuilder result = new StringBuilder();
        long stamp = this.stampedLock.readLock();
        for (DataSource dataSource : this.sources.values()) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                //取出Pool状态JMX对象
                HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
                result.append(hikariDataSource.getPoolName()).append(" / ");
                result.append(String.format("totalConnections:%s, activeConnections:%s, idleConnections:%s, awaitingConnections:%s",
                        hikariPoolMXBean.getTotalConnections(), hikariPoolMXBean.getActiveConnections(),
                        hikariPoolMXBean.getIdleConnections(), hikariPoolMXBean.getThreadsAwaitingConnection()));
                result.append(System.lineSeparator());
            }
        }

        this.stampedLock.unlock(stamp);
        return result.toString();
    }

    /**
     * 获取完整分析
     *
     * @return 完整分析
     */
    public String getFullStatistics() {
        StringBuilder result = new StringBuilder();
        long stamp = this.stampedLock.readLock();
        for (DataSource dataSource : this.sources.values()) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                //取出Pool状态JMX对象
                HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
                //取出Pool配置JMX对象
                HikariConfigMXBean hikariConfigMXBean = hikariDataSource.getHikariConfigMXBean();
                result.append(hikariDataSource.getPoolName()).append(" / ");
                result.append(String.format("maxPoolSize:%s, connectionTimeout:%s ms, idleTimeout:%s ms, maxLifetime:%s ms",
                        hikariConfigMXBean.getMaximumPoolSize(), hikariConfigMXBean.getConnectionTimeout(),
                        hikariConfigMXBean.getIdleTimeout(), hikariConfigMXBean.getMaxLifetime()));
                result.append(" / ");
                result.append(String.format("totalConnections:%s, activeConnections:%s, idleConnections:%s, awaitingConnections:%s",
                        hikariPoolMXBean.getTotalConnections(), hikariPoolMXBean.getActiveConnections(),
                        hikariPoolMXBean.getIdleConnections(), hikariPoolMXBean.getThreadsAwaitingConnection()));
                result.append(System.lineSeparator());


            }
        }
        this.stampedLock.unlock(stamp);
        return result.toString();
    }

    /**
     * 获取数据源(连接池)
     * 返回空则表示未设置 需初始化数据源
     *
     * @return 数据源(连接池)
     */
    public DataSource getPool(String driverName, String connectString, String userName, String passWord, Class<?> contextType) {

        long stamp = this.stampedLock.readLock();
        try {
            String key = String.format("[%s][%s][%s][%s]", driverName, connectString, userName, passWord);
            while (!this.sources.containsKey(key)) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    HikariConfig config = new HikariConfig();
                    //必需的配置
                    config.setDriverClassName(driverName);
                    config.setJdbcUrl(connectString);
                    config.setUsername(userName);
                    config.setPassword(passWord);
                    //可选的配置
                    this.initPool(config, contextType);

                    DataSource source = new HikariDataSource(config);
                    this.sources.put(key, source);
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.sources.get(key);
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }

    /**
     * 初始化连接池
     *
     * @param config      连接池的配置
     * @param contextType 上下文类型
     */
    private void initPool(HikariConfig config, Class<?> contextType) {
        //默认值
        String name = "Obase ConnectionPool";
        int poolSize = 10;
        //从依赖注入中获取值
        IObaseConnectionPoolConfiguration configuration = Utils.getDependencyInjectionServiceOrNull(contextType, IObaseConnectionPoolConfiguration.class);
        if (configuration != null) {
            if (!Utils.getStringIsEmpty(configuration.name()))
                name = configuration.name();
            if (configuration.getMaximumPoolSize() > 0)
                poolSize = configuration.getMaximumPoolSize();
        }

        config.setPoolName(name);
        config.setMaximumPoolSize(poolSize);
    }

    /**
     * 销毁方法
     */
    @Override
    public void close() {
        for (DataSource source : this.sources.values()) {
            if (source instanceof HikariDataSource) {
                HikariDataSource basicDataSource = (HikariDataSource) source;
                basicDataSource.close();
            }
        }
        //搞一些输出
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Obase ConnectionPool Has Destroyed!");
    }
}

