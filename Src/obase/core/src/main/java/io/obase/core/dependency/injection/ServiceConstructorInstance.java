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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务的构造函数缓存
 */
public class ServiceConstructorInstance {

    /**
     * 对象上下文类服务容器缓存
     */
    private final ConcurrentHashMap<Class<?>, Constructor<?>> serviceConstructors = new ConcurrentHashMap<>();

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
        return InstanceHolder.INSTANCE;
    }

    /**
     * 设置服务的构造函数
     *
     * @param serviceType 服务类型
     * @param constructor 构造函数
     */
    void setServiceConstructor(Class<?> serviceType, Constructor<?> constructor) {
        //使用并发字典保证线程安全 已存在时不覆盖
        this.serviceConstructors.putIfAbsent(serviceType, constructor);
    }

    /**
     * 获取服务的构造函数
     *
     * @param serviceType 服务类型
     * @return 构造函数
     */
    Constructor<?> getServiceConstructor(Class<?> serviceType) {
        //使用并发字典保证线程安全 如果不存在则返回null
        return this.serviceConstructors.get(serviceType);
    }

    /**
     * 查看服务的构造函数是否已缓存
     *
     * @param serviceType 服务类型
     * @return 是否已缓存
     */
    boolean exist(Class<?> serviceType) {
        //使用并发字典保证线程安全
        return this.serviceConstructors.containsKey(serviceType);
    }

    /**
     * 服务的构造函数缓存单例持有者
     */
    private static final class InstanceHolder {

        /**
         * 单例
         */
        private static final ServiceConstructorInstance INSTANCE = new ServiceConstructorInstance();
    }
}
