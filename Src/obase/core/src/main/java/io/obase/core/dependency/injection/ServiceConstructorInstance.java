/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务的构造函数缓存,缓存注册的服务构造函数.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 16:34:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 服务的构造函数缓存
 */
public class ServiceConstructorInstance {

    /**
     * 单例
     */
    private static volatile ServiceConstructorInstance instance;
    /**
     * 对象上下文类服务容器缓存
     */
    private final Map<Class<?>, Constructor<?>> serviceConstructors = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建服务的构造函数缓存
     */
    private ServiceConstructorInstance() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static ServiceConstructorInstance getInstance() {
        if (instance == null) {
            synchronized (ServiceConstructorInstance.class) {
                if (instance == null) {
                    instance = new ServiceConstructorInstance();
                }
            }
        }
        return instance;
    }

    /**
     * 设置服务的构造函数
     *
     * @param serviceType 服务类型
     * @param constructor 构造函数
     */
    void setServiceConstructor(Class<?> serviceType, Constructor<?> constructor) {
        long stamp = this.stampedLock.writeLock();
        this.serviceConstructors.put(serviceType, constructor);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取服务的构造函数
     *
     * @param serviceType 服务类型
     * @return 构造函数
     */
    Constructor<?> getServiceConstructor(Class<?> serviceType) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.serviceConstructors.getOrDefault(serviceType, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }

    /**
     * 查看服务的构造函数是否已缓存
     *
     * @param serviceType 服务类型
     * @return 是否已缓存
     */
    boolean exist(Class<?> serviceType) {
        long stamp = this.stampedLock.readLock();
        boolean result = this.serviceConstructors.containsKey(serviceType);
        this.stampedLock.unlockRead(stamp);
        return result;
    }
}
