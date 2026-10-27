/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务容器建造器,提供建造服务容器方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:43:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import io.obase.core.ObjectContext;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 服务容器建造器
 */
public final class ServiceContainerBuilder implements Iterable<ServiceDefinition> {

    /**
     * 所属的上下文类型
     */
    private final Class<?> contextType;

    /**
     * 注入的服务定义集合
     */
    private final List<ServiceDefinition> services = new ArrayList<>();

    /**
     * 初始化服务容器建造器
     *
     * @param contextType 上下文类型
     */
    public ServiceContainerBuilder(Class<?> contextType) {
        if (!ObjectContext.class.isAssignableFrom(contextType))
            throw new IllegalArgumentException(contextType.getName() + "必须是ObjectContext的实现类.");

        this.contextType = contextType;
    }

    /**
     * 返回迭代器
     *
     * @return 服务定义的迭代器
     */
    @Override
    public Iterator<ServiceDefinition> iterator() {
        return this.services.iterator();
    }

    /**
     * 添加一个服务定义
     * 如果存在相同服务类型和实现类型的服务定义则返回原有定义
     *
     * @param item 服务定义
     * @return 自身
     */
    private ServiceContainerBuilder add(ServiceDefinition item) {
        //由委托创建的服务 实现类型由委托决定 不做接口/抽象类的检查
        if (item.getImplementationFactory() == null) {
            Class<?> implementType = item.getImplementType();
            if (Modifier.isInterface(implementType.getModifiers()) || Modifier.isAbstract(implementType.getModifiers()))
                throw new IllegalArgumentException("实现类型不能是接口或者抽象类,服务类型:" + item.getServiceType().getName() + ",实现类型:" + implementType.getName() + "." +
                        "请为" + item.getServiceType().getName() + "指定具体的实现类型,或改用委托注册.");
        }

        if (this.services.stream().anyMatch(s ->
                s.getServiceType().equals(item.getServiceType()) && s.getImplementType().equals(item.getImplementType())))
            return this;

        this.services.add(item);
        return this;
    }

    /**
     * 添加一个单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType 服务的类型
     * @param <TService>  服务的类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addSingleton(Class<TService> serviceType) {
        return this.add(ServiceDefinition.singletonService(serviceType));
    }

    /**
     * 添加一个单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType         服务的类型
     * @param implementType       实现类类型
     * @param <TService>          服务的类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addSingleton(Class<TService> serviceType, Class<TServiceImplement> implementType) {
        return this.add(ServiceDefinition.singletonService(serviceType, implementType));
    }

    /**
     * 添加一个单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType 服务类型
     * @param factory     构造委托
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addSingleton(Class<TService> serviceType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.singletonService(serviceType, factory));
    }

    /**
     * 添加一个单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param factory             构造委托
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addSingleton(Class<TService> serviceType, Class<TServiceImplement> implementType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.singletonService(serviceType, implementType, factory));
    }

    /**
     * 添加一个多例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType 服务的类型
     * @param <TService>  服务的类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addTransient(Class<TService> serviceType) {
        return this.add(ServiceDefinition.transientService(serviceType));
    }

    /**
     * 添加一个多例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType         服务的类型
     * @param implementType       实现类类型
     * @param <TService>          服务的类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addTransient(Class<TService> serviceType, Class<TServiceImplement> implementType) {
        return this.add(ServiceDefinition.transientService(serviceType, implementType));
    }

    /**
     * 添加一个多例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType 服务类型
     * @param factory     构造委托
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addTransient(Class<TService> serviceType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.transientService(serviceType, factory));
    }

    /**
     * 添加一个多例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param factory             构造委托
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addTransient(Class<TService> serviceType, Class<TServiceImplement> implementType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.transientService(serviceType, implementType, factory));
    }

    /**
     * 添加一个作用域内单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType 服务的类型
     * @param <TService>  服务的类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addScoped(Class<TService> serviceType) {
        return this.add(ServiceDefinition.scopedService(serviceType));
    }

    /**
     * 添加一个作用域内单例的服务定义
     * 此服务的创建方式为根据反射获取到的第一个公开或非公开构造函数创建
     *
     * @param serviceType         服务的类型
     * @param implementType       实现类类型
     * @param <TService>          服务的类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addScoped(Class<TService> serviceType, Class<TServiceImplement> implementType) {
        return this.add(ServiceDefinition.scopedService(serviceType, implementType));
    }

    /**
     * 添加一个作用域内单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType 服务类型
     * @param factory     构造委托
     * @param <TService>  服务类型
     * @return 服务定义
     */
    public <TService> ServiceContainerBuilder addScoped(Class<TService> serviceType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.scopedService(serviceType, factory));
    }

    /**
     * 添加一个作用域内单例的服务定义
     * 此服务的创建方式为方法委托创建
     *
     * @param serviceType         服务类型
     * @param implementType       实现类类型
     * @param factory             构造委托
     * @param <TService>          服务类型
     * @param <TServiceImplement> 实现类类型
     * @return 服务定义
     */
    public <TService, TServiceImplement extends TService> ServiceContainerBuilder addScoped(Class<TService> serviceType, Class<TServiceImplement> implementType, ServiceFactory<TService> factory) {
        return this.add(ServiceDefinition.scopedService(serviceType, implementType, factory));
    }

    /**
     * 建造服务容器
     * 会同时将服务容器放置于单例中
     *
     * @return 服务容器
     */
    public ServiceContainer build() {
        //先检查是否已经创建过 避免多余地构造容器
        if (ServiceContainerInstance.getInstance().getServiceContainer(this.contextType) != null)
            throw new RuntimeException("上下文" + this.contextType.getName() + "已创建服务容器,不能重复创建.");
        //建造时对服务定义做快照 建造之后再向建造器注册服务不会影响已建成的容器
        ServiceContainer container = new ServiceContainer(this.contextType, new ArrayList<>(this.services));
        ServiceContainerInstance.getInstance().setServiceContainer(this.contextType, container);
        return container;
    }
}
