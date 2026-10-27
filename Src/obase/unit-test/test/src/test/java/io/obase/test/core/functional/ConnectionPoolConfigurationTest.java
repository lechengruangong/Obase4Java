/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：连接池配置的测试.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.test.core.functional;

import com.zaxxer.hikari.HikariDataSource;
import io.obase.core.ObjectContext;
import io.obase.core.dependency.injection.ObaseDependencyInjection;
import io.obase.providers.sql.connectionpool.IObaseConnectionPoolConfiguration;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import io.obase.test.infrastructure.configuration.ObaseConnectionPoolConfiguration;
import io.obase.test.infrastructure.context.SqliteContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 连接池配置的测试
 * <p>
 * 连接池按驱动、连接字符串、用户名、口令缓存,本测试为每个用例生成唯一的连接字符串,
 * 保证拿到的是新建的连接池,从而能校验配置到连接池的映射结果.
 * 使用SQLite的内存库,不依赖测试配置中的数据源.
 * 与.NET版ConnectionPoolConfigurationTest的用例一一对应,两版用例数必须保持一致.
 */
public class ConnectionPoolConfigurationTest {

    /**
     * SQLite的驱动类名
     */
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    /**
     * 本测试创建的数据源 用例结束后逐一关闭
     */
    private final List<DataSource> createdSources = new ArrayList<>();

    /**
     * 生成唯一的连接字符串 保证每个用例拿到新建的连接池
     *
     * @return 连接字符串
     */
    private static String createConnectionString() {
        return "jdbc:sqlite:file:obase_pool_" + UUID.randomUUID().toString().replace("-", "") + "?mode=memory&cache=shared";
    }

    /**
     * 为指定的上下文类型注册连接池配置
     *
     * @param contextType   上下文类型
     * @param configuration 连接池配置
     */
    private static void registerConfiguration(Class<? extends ObjectContext> contextType,
                                              IObaseConnectionPoolConfiguration configuration) {
        ObaseDependencyInjection.createBuilder(contextType)
                .addSingleton(IObaseConnectionPoolConfiguration.class, container -> configuration)
                .build();
    }

    /**
     * 获取指定的上下文类型对应的连接池
     *
     * @param contextType 上下文类型
     * @return 连接池
     */
    private HikariDataSource createPool(Class<? extends ObjectContext> contextType) {
        DataSource source = ObaseConnectionPool.getInstance()
                .getPool(SQLITE_DRIVER, createConnectionString(), null, null, contextType);
        this.createdSources.add(source);

        assertNotNull(source);
        assertTrue(source instanceof HikariDataSource);

        return (HikariDataSource) source;
    }

    /**
     * 用例结束后关闭本用例创建的数据源
     */
    @AfterEach
    public void closePools() {
        for (DataSource source : this.createdSources) {
            if (source instanceof HikariDataSource)
                ((HikariDataSource) source).close();
        }

        this.createdSources.clear();
    }

    /**
     * 测试基础设施中的连接池配置实现:名称取构造参数,最大连接数固定为5,其余项沿用接口的默认值
     */
    @Test
    public void connectionPoolConfiguration_ExposesExpectedValues() {
        ObaseConnectionPoolConfiguration configuration = new ObaseConnectionPoolConfiguration("Test ConnectionPool");

        assertEquals("Test ConnectionPool", configuration.name());
        assertEquals(5, configuration.getMaximumPoolSize());

        //未覆写的项取接口的默认值 表示由连接池自行决定
        assertEquals(-1, configuration.getMinimumIdle());
        assertEquals(-1L, configuration.getConnectionTimeout());
        assertEquals(-1L, configuration.getIdleTimeout());
        assertEquals(-1L, configuration.getValidationTimeout());
        assertEquals(-1L, configuration.getKeepaliveTime());
        assertEquals(-1L, configuration.getMaxLifetime());
        assertEquals(-1L, configuration.getLeakDetectionThreshold());
        assertNull(configuration.getAutoCommit());
        assertNull(configuration.getReadOnly());
        assertNull(configuration.getConnectionTestQuery());
    }

    /**
     * 配置的项全部生效
     */
    @Test
    public void pool_AppliesConfiguredValues() {
        registerConfiguration(FullConfigurationContext.class, new FullPoolConfiguration());

        HikariDataSource pool = createPool(FullConfigurationContext.class);

        assertEquals("Custom ConnectionPool", pool.getPoolName());
        assertEquals(7, pool.getMaximumPoolSize());
        assertEquals(2, pool.getMinimumIdle());
        assertEquals(15000L, pool.getConnectionTimeout());
        assertEquals(120000L, pool.getIdleTimeout());
        assertEquals(3000L, pool.getValidationTimeout());
        assertEquals(60000L, pool.getKeepaliveTime());
        assertEquals(1800000L, pool.getMaxLifetime());
        assertEquals(10000L, pool.getLeakDetectionThreshold());
        assertEquals("SELECT 1", pool.getConnectionTestQuery());
        assertFalse(pool.isAutoCommit());
        assertFalse(pool.isReadOnly());
    }

    /**
     * 配置项取值无效时(名称空、数值不大于0、可空项为null)回落到默认值
     */
    @Test
    public void pool_FallsBackToDefaultsWhenConfiguredValuesAreInvalid() {
        registerConfiguration(InvalidConfigurationContext.class, new InvalidPoolConfiguration());

        HikariDataSource pool = createPool(InvalidConfigurationContext.class);

        //名称空 最大连接数不大于0 回落到默认值
        assertEquals("Obase ConnectionPool", pool.getPoolName());
        assertEquals(10, pool.getMaximumPoolSize());

        //其余项不设置 使用HikariCP的默认值
        assertEquals(10, pool.getMinimumIdle());
        assertEquals(30000L, pool.getConnectionTimeout());
        assertEquals(600000L, pool.getIdleTimeout());
        assertEquals(5000L, pool.getValidationTimeout());
        assertEquals(0L, pool.getKeepaliveTime());
        assertEquals(1800000L, pool.getMaxLifetime());
        assertEquals(0L, pool.getLeakDetectionThreshold());
        assertNull(pool.getConnectionTestQuery());
        assertTrue(pool.isAutoCommit());
        assertFalse(pool.isReadOnly());
    }

    /**
     * 上下文中没有注册连接池配置时 全部使用默认值
     */
    @Test
    public void pool_UsesDefaultsWhenNoConfigurationRegistered() {
        HikariDataSource pool = createPool(NoConfigurationContext.class);

        assertEquals("Obase ConnectionPool", pool.getPoolName());
        assertEquals(10, pool.getMaximumPoolSize());
        assertEquals(30000L, pool.getConnectionTimeout());
        assertEquals(600000L, pool.getIdleTimeout());
        assertEquals(5000L, pool.getValidationTimeout());
        assertEquals(1800000L, pool.getMaxLifetime());
        assertNull(pool.getConnectionTestQuery());
        assertTrue(pool.isAutoCommit());
    }

    /**
     * 只配置了部分项时 只有配置的项生效 其余项仍使用默认值
     */
    @Test
    public void pool_AppliesPartialConfiguration() {
        registerConfiguration(PartialConfigurationContext.class, new PartialPoolConfiguration());

        HikariDataSource pool = createPool(PartialConfigurationContext.class);

        //配置的项生效
        assertEquals("Partial ConnectionPool", pool.getPoolName());
        assertEquals(4, pool.getMaximumPoolSize());

        //未配置的项保持默认值
        assertEquals(4, pool.getMinimumIdle());
        assertEquals(30000L, pool.getConnectionTimeout());
        assertEquals(600000L, pool.getIdleTimeout());
        assertEquals(1800000L, pool.getMaxLifetime());
        assertEquals(0L, pool.getLeakDetectionThreshold());
        assertNull(pool.getConnectionTestQuery());
        assertTrue(pool.isAutoCommit());
    }

    /**
     * 不同上下文的连接池配置互不影响 各自使用自己的配置
     */
    @Test
    public void pool_AppliesPerContextConfiguration() {
        registerConfiguration(FirstContext.class, new NamedPoolConfiguration("First ConnectionPool", 3));
        registerConfiguration(SecondContext.class, new NamedPoolConfiguration("Second ConnectionPool", 6));

        HikariDataSource first = createPool(FirstContext.class);
        HikariDataSource second = createPool(SecondContext.class);

        //两个上下文各自建池 名称与容量都取自自己的配置
        assertNotSame(first, second);
        assertNotEquals(first.getPoolName(), second.getPoolName());
        assertEquals("First ConnectionPool", first.getPoolName());
        assertEquals(3, first.getMaximumPoolSize());
        assertEquals("Second ConnectionPool", second.getPoolName());
        assertEquals(6, second.getMaximumPoolSize());
    }

    /**
     * 全部配置项都给出自定义值的连接池配置
     */
    private static final class FullPoolConfiguration implements IObaseConnectionPoolConfiguration {

        @Override
        public String name() {
            return "Custom ConnectionPool";
        }

        @Override
        public int getMaximumPoolSize() {
            return 7;
        }

        @Override
        public int getMinimumIdle() {
            return 2;
        }

        @Override
        public long getConnectionTimeout() {
            return 15000L;
        }

        @Override
        public long getIdleTimeout() {
            return 120000L;
        }

        @Override
        public long getValidationTimeout() {
            return 3000L;
        }

        @Override
        public long getKeepaliveTime() {
            return 60000L;
        }

        @Override
        public long getMaxLifetime() {
            return 1800000L;
        }

        @Override
        public long getLeakDetectionThreshold() {
            return 10000L;
        }

        @Override
        public Boolean getAutoCommit() {
            return false;
        }

        @Override
        public Boolean getReadOnly() {
            return false;
        }

        @Override
        public String getConnectionTestQuery() {
            return "SELECT 1";
        }
    }

    /**
     * 全部配置项都取无效值的连接池配置
     */
    private static final class InvalidPoolConfiguration implements IObaseConnectionPoolConfiguration {

        @Override
        public String name() {
            return "";
        }

        @Override
        public int getMaximumPoolSize() {
            return 0;
        }

        @Override
        public int getMinimumIdle() {
            return -1;
        }

        @Override
        public long getConnectionTimeout() {
            return -1L;
        }

        @Override
        public long getIdleTimeout() {
            return -1L;
        }

        @Override
        public long getValidationTimeout() {
            return -1L;
        }

        @Override
        public long getKeepaliveTime() {
            return -1L;
        }

        @Override
        public long getMaxLifetime() {
            return -1L;
        }

        @Override
        public long getLeakDetectionThreshold() {
            return -1L;
        }

        @Override
        public Boolean getAutoCommit() {
            return null;
        }

        @Override
        public Boolean getReadOnly() {
            return null;
        }

        @Override
        public String getConnectionTestQuery() {
            return null;
        }
    }

    /**
     * 只给出部分配置项的连接池配置
     */
    private static final class PartialPoolConfiguration implements IObaseConnectionPoolConfiguration {

        @Override
        public String name() {
            return "Partial ConnectionPool";
        }

        @Override
        public int getMaximumPoolSize() {
            return 4;
        }
    }

    /**
     * 指定名称与最大连接数的连接池配置
     */
    private record NamedPoolConfiguration(String poolName, int maxPoolSize) implements IObaseConnectionPoolConfiguration {

        @Override
        public String name() {
            return this.poolName;
        }

        @Override
        public int getMaximumPoolSize() {
            return this.maxPoolSize;
        }
    }

    /**
     * 配置了全部项时使用的上下文
     */
    private static final class FullConfigurationContext extends SqliteContext {
    }

    /**
     * 配置了无效值时使用的上下文
     */
    private static final class InvalidConfigurationContext extends SqliteContext {
    }

    /**
     * 未注册连接池配置时使用的上下文
     */
    private static final class NoConfigurationContext extends SqliteContext {
    }

    /**
     * 只配置了部分项时使用的上下文
     */
    private static final class PartialConfigurationContext extends SqliteContext {
    }

    /**
     * 第一个上下文
     */
    private static final class FirstContext extends SqliteContext {
    }

    /**
     * 第二个上下文
     */
    private static final class SecondContext extends SqliteContext {
    }
}
