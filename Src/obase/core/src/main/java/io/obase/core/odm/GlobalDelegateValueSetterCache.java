/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托设值器缓存.
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
 * 委托设值器缓存
 */
public class GlobalDelegateValueSetterCache {

    /**
     * 单例
     */
    private static volatile GlobalDelegateValueSetterCache instance;
    /**
     * 对象上下文类Setter缓存
     */
    private final Map<String, ValueSetter> setters = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();


    /**
     * 创建全局模型缓存实例
     */
    private GlobalDelegateValueSetterCache() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static GlobalDelegateValueSetterCache getInstance() {
        if (instance == null) {
            synchronized (GlobalDelegateValueSetterCache.class) {
                if (instance == null) {
                    instance = new GlobalDelegateValueSetterCache();
                }
            }
        }
        return instance;
    }

    /**
     * 设置要缓存的Setter
     *
     * @param name   名称
     * @param setter Setter
     */
    public void setSetter(String name, ValueSetter setter) {
        long stamp = this.stampedLock.writeLock();
        this.setters.put(name, setter);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取Setter
     *
     * @param name 名称
     * @return Setter
     */
    public ValueSetter getSetter(String name) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.setters.getOrDefault(name, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }
}
