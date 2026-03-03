/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托取值器缓存.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:27:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 委托取值器缓存
 */
public class GlobalDelegateValueGetterCache {
    /**
     * 单例
     */
    private static volatile GlobalDelegateValueGetterCache instance;
    /**
     * 对象上下文类Getter缓存
     */
    private final Map<String, IValueGetter> getters = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建全局模型缓存实例
     */
    private GlobalDelegateValueGetterCache() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static GlobalDelegateValueGetterCache getInstance() {
        if (instance == null) {
            synchronized (GlobalDelegateValueGetterCache.class) {
                if (instance == null) {
                    instance = new GlobalDelegateValueGetterCache();
                }

            }
        }
        return instance;
    }

    /**
     * 设置要缓存的Getter
     *
     * @param name   名称
     * @param getter Getter
     */
    public void setGetter(String name, IValueGetter getter) {
        long stamp = this.stampedLock.writeLock();
        this.getters.put(name, getter);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取Getter
     *
     * @param name 名称
     * @return Getter
     */
    public IValueGetter getGetter(String name) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.getters.getOrDefault(name, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }
}
