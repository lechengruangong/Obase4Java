/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务容器,提供注册服务的容器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:49:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 服务容器
 */
public class ServiceContainer implements AutoCloseable {

    /**
     * 服务定义集合
     */
    private final List<ServiceDefinition> services;

    /**
     * 单例集合
     */
    private final ConcurrentHashMap<ServiceKey, Object> singletonInstances = new ConcurrentHashMap<>();

    /**
     * 多例集合
     */
    private final CopyOnWriteArrayList<Object> transientDisposables = new CopyOnWriteArrayList<>();

    /**
     * 初始化服务容器
     *
     * @param serviceDefinitions 服务定义集合
     */
    ServiceContainer(List<ServiceDefinition> serviceDefinitions) {
        this.services = Collections.unmodifiableList(serviceDefinitions);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.close();
            } catch (Exception e) {
                throw new RuntimeException("释放依赖注入容器错误:" + e.getMessage(), e);
            }
        }));
    }

    /**
     * 获取某个类型最后注册的服务
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 某个类型最后注册的服务
     */
    public <TService> TService getService(Class<TService> serviceType) {
        //在服务定义中查找
        ServiceDefinition serviceDefinition = this.services.stream().filter(s -> s.getServiceType().equals(serviceType)).reduce((f, s) -> s).orElse(null);
        if (serviceDefinition == null) {
            return null;
        }

        Object svc;
        //单例的 放入单例集合
        if (serviceDefinition.getServiceLifetime() == EServiceLifetime.Singleton) {
            ServiceKey key = new ServiceKey(serviceType, serviceDefinition);
            if (this.singletonInstances.containsKey(key)) {
                svc = this.singletonInstances.get(key);
            } else {
                svc = this.getServiceInstance(key.getServiceType(), serviceDefinition);
                this.singletonInstances.put(key, svc);
            }
            return (TService) svc;
        }
        //多例的 直接创建
        svc = this.getServiceInstance(serviceType, serviceDefinition);
        //实现了IDisposable的放入释放集合
        if (svc instanceof AutoCloseable)
            this.transientDisposables.add(svc);
        return (TService) svc;
    }

    /**
     * 获取某个类型所有的服务
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 某个类型所有的服务
     */
    public <TService> List<TService> getServices(Class<TService> serviceType) {
        //组装结果
        List<TService> list = new ArrayList<>();
        for (ServiceDefinition def : this.services.stream().filter(s -> s.getServiceType().equals(serviceType)).collect(Collectors.toList())) {
            Object svc;
            //单例 存放于字典
            if (def.getServiceLifetime() == EServiceLifetime.Singleton) {
                ServiceKey key = new ServiceKey(serviceType, def);
                if (this.singletonInstances.containsKey(key)) {
                    svc = this.singletonInstances.get(key);
                } else {
                    svc = this.getServiceInstance(key.getServiceType(), def);
                    this.singletonInstances.put(key, svc);
                }
            } else {
                //多例的 直接创建
                svc = this.getServiceInstance(serviceType, def);
                //实现了IDisposable的放入释放集合
                if (svc instanceof AutoCloseable)
                    this.transientDisposables.add(svc);
            }

            if (svc != null) list.add((TService) svc);
        }
        return list;
    }

    /**
     * 获取服务对象方法
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @return 服务对象方法
     */
    private Object getServiceInstance(Class<?> serviceType, ServiceDefinition serviceDefinition) {

        //定义了自己的方法 由定义方处理
        if (serviceDefinition.getFactory() != null) {
            return serviceDefinition.getFactory().getServiceInstance(this);
        }
        //没有定义 反射处理
        Class<?> implementType = serviceDefinition.getImplementType();
        if (implementType == null)
            implementType = serviceDefinition.getServiceType();

        if (Modifier.isInterface(implementType.getModifiers()) || Modifier.isAbstract(implementType.getModifiers())) {
            throw new IllegalArgumentException("实现类型不能是接口或者抽象类,服务类型:" + serviceType.getName() + ",实现类型:" + implementType.getName() + ".");
        }

        //从缓存里面取
        Constructor<?> constructor = ServiceConstructorInstance.getInstance().getServiceConstructor(implementType);
        //没有 反射取
        if (constructor == null) {
            //获取所有公开的
            Constructor<?>[] constructors = implementType.getConstructors();
            if (constructors.length == 0) {
                //没有就获取非公开的
                constructors = implementType.getDeclaredConstructors();
            }

            constructor = Arrays.stream(constructors).findFirst().orElse(null);
        }

        if (constructor == null)
            throw new IllegalArgumentException("无法获取到服务" + implementType.getName() + "可用的构造函数.");

        //放入缓存
        if (!ServiceConstructorInstance.getInstance().exist(implementType))
            ServiceConstructorInstance.getInstance().setServiceConstructor(implementType, constructor);

        Parameter[] parameters = constructor.getParameters();

        //没有参数
        if (parameters.length == 0) {
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("无法构造服务" + implementType.getName(), e);
            }
        }

        List<Object> parameterList = new ArrayList<>();
        //有参数 从当前容器里取符合的参数
        for (Parameter parameter : parameters) {
            Class<?> parameterType = parameter.getType();
            Object parameterValue = this.getService(parameterType);
            if (parameterValue == null)
                throw new IllegalArgumentException(
                        "处理服务" + implementType.getName() + "的构造函数参数" + parameter.getName() + "出错,无法从已注册的服务中获取类型为" + parameterType.getName() + "的对象.");
            parameterList.add(parameterValue);
        }

        try {
            return constructor.newInstance(parameterList.toArray(new Object[0]));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法构造服务" + implementType.getName(), e);
        }
    }

    /**
     * 关闭方法
     */
    @Override
    public void close() throws Exception {
        synchronized (this.singletonInstances) {
            //单例的释放掉
            for (Object instance : this.singletonInstances.values()) {
                if (instance instanceof AutoCloseable) {
                    ((AutoCloseable) instance).close();
                }
            }
            //多例的释放掉
            for (Object instance : this.transientDisposables) {
                if (instance instanceof AutoCloseable) {
                    ((AutoCloseable) instance).close();
                }
            }
            this.singletonInstances.clear();
        }
    }

    /**
     * 服务类型键
     * 表示某个服务类型的具体定义
     */
    private static class ServiceKey {

        /**
         * 服务类型
         */
        private final Class<?> serviceType;

        /**
         * 实现类类型
         */
        private final Class<?> implementType;

        /**
         * 初始化服务类型键
         *
         * @param serviceType 服务类型
         * @param definition  服务定义
         */
        public ServiceKey(Class<?> serviceType, ServiceDefinition definition) {
            this.serviceType = serviceType;
            this.implementType = definition.GetImplementType();
        }

        /**
         * 获取服务类型
         *
         * @return 服务类型
         */
        public Class<?> getServiceType() {
            return this.serviceType;
        }

        /**
         * 获取实现类类型
         *
         * @return 实现类类型
         */
        public Class<?> getImplementType() {
            return this.implementType;
        }

        /**
         * 重写Equal方法
         *
         * @param o 另外一个ServiceKey
         * @return 是否相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            ServiceKey that = (ServiceKey) o;
            return this.serviceType.equals(that.serviceType) && this.implementType.equals(that.implementType);
        }

        /**
         * 重写哈希码
         *
         * @return 哈希码
         */
        @Override
        public int hashCode() {
            String key = this.getServiceType().getName() + "_" + this.getImplementType().getName();
            return key.hashCode();
        }
    }
}
