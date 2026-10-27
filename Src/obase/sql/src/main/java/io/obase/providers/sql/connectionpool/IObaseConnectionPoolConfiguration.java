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
 * <p>
 * 除{@link #name()}与{@link #getMaximumPoolSize()}外,其余配置项都提供了默认实现,
 * 已有的实现类无需改动即可继续编译和运行;需要调整某项配置时,重写对应方法即可.
 * <p>
 * 取值约定(所有配置项一致):
 * <ul>
 *     <li>字符串为null或空字符串:使用默认值.</li>
 *     <li>数值小于等于0:使用默认值,表示不做调整.</li>
 *     <li>包装类型{@link Boolean}为null:使用默认值,表示不设置该连接属性.</li>
 * </ul>
 * 连接池由HikariCP提供,时间类配置项的单位均为毫秒,可用范围与HikariCP一致:
 * 小于HikariCP要求下限的值会被HikariCP按它自己的规则调整并输出警告日志.
 */
public interface IObaseConnectionPoolConfiguration {

    /**
     * 连接池的名称 如果为空或空字符串 则使用默认值Obase ConnectionPool
     *
     * @return 连接池的名称
     */
    String name();

    /**
     * 连接池的最大大小 如果小于等于0 则使用默认值10
     *
     * @return 连接池的最大大小
     */
    int getMaximumPoolSize();

    /**
     * 连接池的最小空闲连接数 如果小于等于0 则使用默认值(HikariCP的默认值,与最大大小相同)
     * <p>
     * 该值不应大于{@link #getMaximumPoolSize()},否则HikariCP会把它调整为最大大小.
     *
     * @return 连接池的最小空闲连接数
     */
    default int getMinimumIdle() {
        return -1;
    }

    /**
     * 获取连接的超时时间(毫秒) 如果小于等于0 则使用默认值30000
     * <p>
     * 从连接池获取连接时等待的最长时间,超时后抛出异常.该值不应小于250.
     *
     * @return 获取连接的超时时间(毫秒)
     */
    default long getConnectionTimeout() {
        return -1L;
    }

    /**
     * 空闲连接的超时时间(毫秒) 如果小于等于0 则使用默认值600000
     * <p>
     * 连接空闲超过该时间且空闲连接数大于最小空闲连接数时,连接会被回收.该值不应小于10000.
     *
     * @return 空闲连接的超时时间(毫秒)
     */
    default long getIdleTimeout() {
        return -1L;
    }

    /**
     * 连接有效性校验的超时时间(毫秒) 如果小于等于0 则使用默认值5000
     * <p>
     * 该值不应小于250,并且应小于{@link #getConnectionTimeout()}.
     *
     * @return 连接有效性校验的超时时间(毫秒)
     */
    default long getValidationTimeout() {
        return -1L;
    }

    /**
     * 连接保活的时间间隔(毫秒) 如果小于等于0 则不启用保活
     * <p>
     * 启用后连接池会按该间隔检测空闲连接,避免连接被数据库或中间网络设备断开.该值不应小于30000.
     *
     * @return 连接保活的时间间隔(毫秒)
     */
    default long getKeepaliveTime() {
        return -1L;
    }

    /**
     * 连接的最大存活时间(毫秒) 如果小于等于0 则使用默认值1800000
     * <p>
     * 连接从创建到被回收的最长时间,用于避免连接长期存活.该值不应小于30000.
     * 该配置项由HikariCP提供,.NET版的自有连接池没有对应能力,配置后不生效.
     *
     * @return 连接的最大存活时间(毫秒)
     */
    default long getMaxLifetime() {
        return -1L;
    }

    /**
     * 连接泄漏检测的阈值(毫秒) 如果小于等于0 则不启用泄漏检测
     * <p>
     * 启用后连接被借出超过该时间仍未归还时会输出警告日志,用于排查忘记归还连接的问题.
     * 该值不应小于2000.该配置项由HikariCP提供,.NET版的自有连接池没有对应能力,配置后不生效.
     *
     * @return 连接泄漏检测的阈值(毫秒)
     */
    default long getLeakDetectionThreshold() {
        return -1L;
    }

    /**
     * 连接是否自动提交事务 为null则不设置,沿用数据库驱动或连接字符串的默认值
     * <p>
     * 该配置项由JDBC的{@link java.sql.Connection#setAutoCommit(boolean)}提供,
     * .NET版的DbConnection没有对应能力,配置后不生效.
     *
     * @return 连接是否自动提交事务
     */
    default Boolean getAutoCommit() {
        return null;
    }

    /**
     * 连接是否只读 为null则不设置,沿用数据库驱动或连接字符串的默认值
     * <p>
     * 该配置项由JDBC的{@link java.sql.Connection#setReadOnly(boolean)}提供,
     * .NET版的DbConnection没有对应能力,配置后不生效.
     *
     * @return 连接是否只读
     */
    default Boolean getReadOnly() {
        return null;
    }

    /**
     * 连接有效性校验使用的语句 如果为null或空字符串则使用驱动提供的校验方式
     * <p>
     * 通常不需要设置,只有在驱动不支持JDBC4的{@link java.sql.Connection#isValid(int)}时才需要指定,
     * 例如Oracle的{@code SELECT 1 FROM DUAL}.
     *
     * @return 连接有效性校验使用的语句
     */
    default String getConnectionTestQuery() {
        return null;
    }
}
