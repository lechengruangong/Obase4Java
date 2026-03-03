/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务定义,存储服务的类型等信息.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:47:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import io.obase.core.common.Utils;

import java.lang.reflect.Field;

/**
 * 服务定义
 */
public class ServiceDefinition {

    /**
     * 生命周期
     */
    private final EServiceLifetime serviceLifetime;

    /**
     * 实现类型
     */
    private final Class<?> implementType;

    /**
     * 服务类型
     */
    private final Class<?> serviceType;

    /**
     * 构造方法委托
     */
    private final ServiceFactory<?> factory;

    /**
     * 初始化服务定义
     *
     * @param serviceType     服务类型
     * @param implementType   实现类型
     * @param factory         构造方法委托
     * @param serviceLifetime 服务的生命周期
     */
    ServiceDefinition(Class<?> serviceType, Class<?> implementType,
                      ServiceFactory<?> factory,
                      EServiceLifetime serviceLifetime) {
        this.serviceType = serviceType;
        if (implementType == null)
            this.implementType = serviceType;
        else
            this.implementType = implementType;
        this.factory = factory;
        this.serviceLifetime = serviceLifetime;
    }

    /**
     * 构造一个单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public static <TService, TServiceImplement extends TService> ServiceDefinition singleton(Class<TService> serviceType, Class<TServiceImplement> implementType) {
        return new ServiceDefinition(serviceType, implementType, null, EServiceLifetime.Singleton);
    }

    /**
     * 构造一个单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public static <TService> ServiceDefinition singleton(Class<TService> serviceType) {
        return new ServiceDefinition(serviceType, null, null, EServiceLifetime.Singleton);
    }

    /**
     * 构造一个单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType 服务类型
     * @param factory     构造服务的工厂
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public static <TService> ServiceDefinition singleton(Class<TService> serviceType, ServiceFactory<TService> factory) {
        return new ServiceDefinition(serviceType, null, factory, EServiceLifetime.Singleton);
    }

    /**
     * 构造一个单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param factory             构造服务的工厂
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public static <TService, TServiceImplement extends TService> ServiceDefinition singleton(Class<TService> serviceType, Class<TServiceImplement> implementType, ServiceFactory<TService> factory) {
        return new ServiceDefinition(serviceType, implementType, factory, EServiceLifetime.Singleton);
    }

    /**
     * 构造一个多例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public static <TService, TServiceImplement extends TService> ServiceDefinition transients(Class<TService> serviceType, Class<TServiceImplement> implementType) {
        return new ServiceDefinition(serviceType, implementType, null, EServiceLifetime.Transients);
    }

    /**
     * 构造一个多例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public static <TService> ServiceDefinition transients(Class<TService> serviceType) {
        return new ServiceDefinition(serviceType, null, null, EServiceLifetime.Transients);
    }

    /**
     * 构造一个多例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType 服务类型
     * @param factory     构造服务的工厂
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public static <TService> ServiceDefinition transients(Class<TService> serviceType, ServiceFactory<TService> factory) {
        return new ServiceDefinition(serviceType, null, factory, EServiceLifetime.Transients);
    }

    /**
     * 构造一个多例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param factory             构造服务的工厂
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public static <TService, TServiceImplement extends TService> ServiceDefinition transients(Class<TService> serviceType, Class<TServiceImplement> implementType, ServiceFactory<TService> factory) {
        return new ServiceDefinition(serviceType, implementType, factory, EServiceLifetime.Transients);
    }

    /**
     * 获取服务的生命周期
     *
     * @return 服务的生命周期
     */
    public EServiceLifetime getServiceLifetime() {
        return this.serviceLifetime;
    }

    /**
     * 获取实现类型
     *
     * @return 实现类型
     */
    public Class<?> getImplementType() {
        return this.implementType;
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
     * 获取构造方法委托
     *
     * @return 构造方法委托
     */
    public ServiceFactory<?> getFactory() {
        return this.factory;
    }

    /**
     * 获取真实实现类型
     *
     * @return 真实实现类型
     */
    public Class<?> GetImplementType() {
        if (this.getImplementType() != null)
            return this.getImplementType();

        if (this.getFactory() != null) {
            Field field;
            try {
                field = this.getClass().getDeclaredField("factory");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("无法获取当前的构造方法委托类型", e);
            }
            //factory的第二个类型参数就是结果
            return Utils.getFieldGenericTypeArguments(field)[1];
        }

        return this.getServiceType();
    }
}
