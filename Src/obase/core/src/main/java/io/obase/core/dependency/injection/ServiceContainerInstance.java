/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务容器实例,提供服务容器实例的单例.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 16:32:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 服务容器实例
 */
public class ServiceContainerInstance implements AutoCloseable {


    /**
     * 单例
     */
    private static volatile ServiceContainerInstance instance;
    /**
     * 对象上下文类服务容器缓存
     */
    private final Map<Class<?>, ServiceContainer> serviceContainers = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建服务容器实例
     */
    private ServiceContainerInstance() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ServiceContainerInstance.getInstance().close();
            } catch (Exception e) {
                throw new RuntimeException("释放Obase依赖注入容器错误:" + e.getMessage(), e);
            }
        }));
    }

    /**
     * 获取单例
     *
     * @return 容器单例
     */
    public static ServiceContainerInstance getInstance() {
        if (instance == null) {
            synchronized (ServiceContainerInstance.class) {
                if (instance == null) {
                    instance = new ServiceContainerInstance();
                }
            }
        }
        return instance;
    }

    /**
     * 设置上下文的服务容器
     *
     * @param contextType 上下文类型
     * @param container   服务容器
     */
    public void setServiceContainer(Class<?> contextType, ServiceContainer container) {
        long stamp = this.stampedLock.writeLock();
        this.serviceContainers.put(contextType, container);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取上下文的服务容器
     *
     * @param contextType 上下文类型
     * @return 服务容器
     */
    public ServiceContainer getServiceContainer(Class<?> contextType) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.serviceContainers.getOrDefault(contextType, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }

    /**
     * 销毁方法
     */
    @Override
    public void close() throws Exception {
        for (ServiceContainer container : this.serviceContainers.values()) {
            container.close();
        }
    }
}
