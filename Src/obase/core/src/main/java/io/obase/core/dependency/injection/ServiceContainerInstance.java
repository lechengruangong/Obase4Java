/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务容器实例,提供服务容器实例的单例.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 16:32:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务容器实例
 */
public class ServiceContainerInstance implements AutoCloseable {

    /**
     * 对象上下文类服务容器缓存
     */
    private final ConcurrentHashMap<Class<?>, ServiceContainer> serviceContainers = new ConcurrentHashMap<>();

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
        return InstanceHolder.INSTANCE;
    }

    /**
     * 设置上下文的服务容器
     *
     * @param contextType 上下文类型
     * @param container   服务容器
     */
    public void setServiceContainer(Class<?> contextType, ServiceContainer container) {
        //使用并发字典保证线程安全 已存在时不覆盖
        this.serviceContainers.putIfAbsent(contextType, container);
    }

    /**
     * 获取上下文的服务容器
     *
     * @param contextType 上下文类型
     * @return 服务容器
     */
    public ServiceContainer getServiceContainer(Class<?> contextType) {
        //使用并发字典保证线程安全 读取不到则返回null
        return this.serviceContainers.get(contextType);
    }

    /**
     * 销毁方法
     */
    @Override
    public void close() throws Exception {
        for (ServiceContainer container : this.serviceContainers.values()) {
            if (container != null) container.close();
        }
    }

    /**
     * 服务容器实例单例持有者
     */
    private static final class InstanceHolder {

        /**
         * 单例
         */
        private static final ServiceContainerInstance INSTANCE = new ServiceContainerInstance();
    }
}
