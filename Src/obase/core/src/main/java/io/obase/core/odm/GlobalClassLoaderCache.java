/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：ClassLoader缓存.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:39:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.concurrent.locks.StampedLock;

/**
 * ClassLoader缓存
 */
public class GlobalClassLoaderCache {

    /**
     * 单例对象
     */
    private volatile static GlobalClassLoaderCache instance;
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();
    /**
     * 缓存的CLassLoader
     */
    private ClassLoader classLoader;

    /**
     * 私有构造
     */
    private GlobalClassLoaderCache() {
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static GlobalClassLoaderCache getInstance() {
        if (instance == null) {
            synchronized (GlobalClassLoaderCache.class) {
                if (instance == null) {
                    instance = new GlobalClassLoaderCache();
                }
            }
        }
        return instance;
    }

    /**
     * 获取CLassLoader
     *
     * @return CLassLoader
     */
    public ClassLoader getClassLoader() {
        long stamp = this.stampedLock.readLock();
        try {
            return this.classLoader;
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }

    /**
     * 设置CLassLoader
     *
     * @param classLoader CLassLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        long stamp = this.stampedLock.writeLock();
        this.classLoader = classLoader;
        this.stampedLock.unlockWrite(stamp);
    }
}
