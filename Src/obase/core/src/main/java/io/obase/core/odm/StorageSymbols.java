/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储标记.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 15:08:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 存储标记集，包含当前系统使用的所有存储标记。
 * 说明
 * StorageSymbols预定义了一批存储标记，涵盖了常用的存储服务。如果应用程序需要更多的存储服务，可以扩展本类以定义更多的存储标记。
 */
public class StorageSymbols {

    /**
     * 单例
     */
    private static volatile StorageSymbols current;

    /**
     * 指代HBase数据库的存储标记
     */
    private final StorageSymbol hBase;

    /**
     * 指代系统使用的主数据库的存储标记
     */
    private final StorageSymbol major;

    /**
     * 指代MemoryCache数据库的存储标记
     */
    private final StorageSymbol memoryCache;

    /**
     * 指代MongoDB的存储标记
     */
    private final StorageSymbol mongoDB;

    /**
     * 指代关系数据库的存储标记
     */
    private final StorageSymbol rdb;

    /**
     * 指代Redis数据库的存储标记
     */
    private final StorageSymbol redis;

    /**
     * 指代系统使用的从数据库的存储标记
     */
    private final StorageSymbol sub;

    /**
     * 私有构造
     */
    private StorageSymbols() {
        this.hBase = new StorageSymbol();
        this.hBase.setDebugName("HBase");
        this.major = new StorageSymbol();
        this.major.setDebugName("Major");
        this.memoryCache = new StorageSymbol();
        this.memoryCache.setDebugName("MemoryCache");
        this.mongoDB = new StorageSymbol();
        this.mongoDB.setDebugName("MongoDB");
        this.rdb = new StorageSymbol();
        this.rdb.setDebugName("RDB");
        this.redis = new StorageSymbol();
        this.redis.setDebugName("Redis");
        this.sub = new StorageSymbol();
        this.sub.setDebugName("Sub");
    }

    /**
     * 获取当前应用程序域中唯一的存储标记集
     *
     * @return 单例
     */
    public static StorageSymbols getCurrent() {
        if (current == null) {
            synchronized (StorageSymbols.class) {
                if (current == null) {
                    current = new StorageSymbols();
                }
            }
        }
        return current;
    }

    /**
     * 获取表示默认数据库的存储标记
     *
     * @return 默认数据库的存储标记
     */
    public final StorageSymbol getDefault() {
        return this.getRDB();
    }

    /**
     * 指代HBase数据库的存储标记
     *
     * @return HBase数据库的存储标记
     */
    public final StorageSymbol getHBase() {
        return this.hBase;
    }

    /**
     * 指代系统使用的主数据库的存储标记
     *
     * @return 主数据库的存储标记
     */
    public final StorageSymbol getMajor() {
        return this.major;
    }

    /**
     * 指代MemoryCache数据库的存储标记
     *
     * @return MemoryCache数据库的存储标记
     */
    public final StorageSymbol getMemoryCache() {
        return this.memoryCache;
    }

    /**
     * 指代MongoDB的存储标记
     *
     * @return MongoDB的存储标记
     */
    public final StorageSymbol getMongoDB() {
        return this.mongoDB;
    }

    /**
     * 指代关系数据库的存储标记
     *
     * @return 关系数据库的存储标记
     */
    public final StorageSymbol getRDB() {
        return this.rdb;
    }

    /**
     * 指代Redis数据库的存储标记
     *
     * @return Redis数据库的存储标记
     */
    public final StorageSymbol getRedis() {
        return this.redis;
    }

    /**
     * 指代系统使用的从数据库的存储标记
     *
     * @return 从数据库的存储标记
     */
    public final StorageSymbol getSub() {
        return this.sub;
    }
}
