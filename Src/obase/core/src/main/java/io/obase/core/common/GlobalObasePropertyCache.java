/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase的内省属性的缓存.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-16 17:05:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * Obase内省属性缓存
 */
public class GlobalObasePropertyCache {

    /**
     * 单例
     */
    private static volatile GlobalObasePropertyCache instance;
    /**
     * Obase内省属性缓存
     */
    private final Map<PropertyKey, List<Property>> properties = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建全局模型缓存实例
     */
    private GlobalObasePropertyCache() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    static GlobalObasePropertyCache getInstance() {
        if (instance == null) {
            synchronized (GlobalObasePropertyCache.class) {
                if (instance == null) {
                    instance = new GlobalObasePropertyCache();
                }
            }
        }
        return instance;
    }

    /**
     * 设置内省属性缓存
     *
     * @param key    键
     * @param values 内省属性
     */
    public void setProperties(PropertyKey key, List<Property> values) {
        long stamp = this.stampedLock.writeLock();
        this.properties.put(key, values);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取内省属性
     *
     * @param key 键
     * @return 内省属性
     */
    public List<Property> getProperties(PropertyKey key) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.properties.getOrDefault(key, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }

    /**
     * 清除缓存
     *
     * @param key 键
     */
    public void remove(PropertyKey key) {
        long stamp = this.stampedLock.readLock();
        try {
            this.properties.remove(key);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }
}
