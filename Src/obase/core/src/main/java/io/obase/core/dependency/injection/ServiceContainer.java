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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务容器
 * <p>
 * 单例由容器持有,容器关闭时统一释放;多例每次获取都新建,从容器获取的多例归容器所有,
 * 从{@link ServiceScope}获取的多例归该作用域所有,作用域关闭时即释放.
 * 作用域内单例(EServiceLifetime.Scoped)在同一个作用域内只创建一次并归该作用域所有,
 * 从容器获取时归容器所有.
 */
public class ServiceContainer implements AutoCloseable {

    /**
     * 单次解析允许的最大嵌套层数
     * 仅用于兜底,正常情况下应由解析链的循环依赖检测先发现
     */
    private static final int MAX_RESOLUTION_DEPTH = 128;

    /**
     * 当前线程的解析嵌套层数
     */
    private static final ThreadLocal<Integer> RESOLUTION_DEPTH = ThreadLocal.withInitial(() -> 0);

    /**
     * 服务定义索引(服务类型 -> 该服务类型下的全部服务定义,按注册顺序排列)
     */
    private final Map<Class<?>, List<ServiceDefinition>> serviceIndex;

    /**
     * 单例集合
     */
    private final ConcurrentHashMap<ServiceKey, Object> singletonInstances = new ConcurrentHashMap<>();

    /**
     * 每个单例键对应的创建锁 保证同一个单例只被创建一次
     */
    private final ConcurrentHashMap<ServiceKey, Object> singletonLocks = new ConcurrentHashMap<>();

    /**
     * 从容器解析的作用域内单例的实例集合
     * 容器本身也是一个作用域,从容器解析的作用域内单例归容器所有
     */
    private final ConcurrentHashMap<ServiceKey, Object> rootScopedInstances = new ConcurrentHashMap<>();

    /**
     * 从容器解析的作用域内单例的创建锁
     */
    private final ConcurrentHashMap<ServiceKey, Object> rootScopedLocks = new ConcurrentHashMap<>();

    /**
     * 当前线程正在创建的单例层数
     * 不为零时说明正在创建单例,此时不允许依赖作用域内单例
     */
    private static final ThreadLocal<Integer> SINGLETON_CREATION_DEPTH = ThreadLocal.withInitial(() -> 0);

    /**
     * 归属于容器自身的待释放实例(按创建顺序记录)
     */
    private final Queue<Object> disposables = new ConcurrentLinkedQueue<>();

    /**
     * 当前仍然存活的作用域
     */
    private final Set<ServiceScope> scopes = ConcurrentHashMap.newKeySet();

    /**
     * 作用域的创建序号 用于保证关闭时后创建的先关闭
     */
    private final AtomicLong scopeSequence = new AtomicLong(0);

    /**
     * 是否已关闭
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 所属的上下文类型
     */
    private final Class<?> contextType;

    /**
     * 初始化服务容器
     *
     * @param contextType        所属的上下文类型
     * @param serviceDefinitions 服务定义集合
     */
    ServiceContainer(Class<?> contextType, List<ServiceDefinition> serviceDefinitions) {
        this.contextType = contextType;
        //对服务定义做快照 建造之后再向建造器注册服务不会影响已建成的容器
        List<ServiceDefinition> snapshot = serviceDefinitions == null
                ? new ArrayList<>()
                : new ArrayList<>(serviceDefinitions);
        this.serviceIndex = buildServiceIndex(snapshot);
    }

    /**
     * 建立服务定义索引
     *
     * @param serviceDefinitions 服务定义集合
     * @return 服务定义索引
     */
    private static Map<Class<?>, List<ServiceDefinition>> buildServiceIndex(List<ServiceDefinition> serviceDefinitions) {
        Map<Class<?>, List<ServiceDefinition>> index = new HashMap<>();
        for (ServiceDefinition serviceDefinition : serviceDefinitions) {
            List<ServiceDefinition> definitions = index.computeIfAbsent(serviceDefinition.getServiceType(), k -> new ArrayList<>());
            definitions.add(serviceDefinition);
        }
        return index;
    }

    /**
     * 创建一个服务作用域
     * 从作用域获取的多例服务归该作用域所有,作用域关闭时释放
     *
     * @return 服务作用域
     */
    public ServiceScope createScope() {
        return this.createScopeCore(null);
    }

    /**
     * 创建一个服务作用域
     *
     * @param parent 父作用域 为null表示是容器的直接子作用域
     * @return 服务作用域
     */
    ServiceScope createScopeCore(ServiceScope parent) {
        this.ensureOpen();
        ServiceScope scope = new ServiceScope(this, parent, this.scopeSequence.incrementAndGet());
        //只有容器的直接子作用域由容器保管 子作用域由父作用域保管
        if (parent == null)
            this.scopes.add(scope);
        else
            parent.registerChild(scope);
        return scope;
    }

    /**
     * 获取某个类型最后注册的服务
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 某个类型最后注册的服务 未注册时返回null
     */
    @SuppressWarnings("unchecked")
    public <TService> TService getService(Class<TService> serviceType) {
        this.ensureOpen();
        return (TService) this.resolve(serviceType, null, new ArrayDeque<>());
    }

    /**
     * 获取某个类型所有的服务
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 某个类型所有的服务
     */
    public <TService> List<TService> getServices(Class<TService> serviceType) {
        this.ensureOpen();
        return this.getServicesInScope(serviceType, null);
    }

    /**
     * 在指定的作用域内获取服务
     * 供{@link ServiceScope}调用
     *
     * @param serviceType 服务类型
     * @param scope       所属的作用域
     * @return 服务实例 未注册时返回null
     */
    Object getServiceInScope(Class<?> serviceType, ServiceScope scope) {
        this.ensureOpen();
        return this.resolve(serviceType, scope, new ArrayDeque<>());
    }

    /**
     * 在指定的作用域内获取某个类型所有的服务
     * 供{@link ServiceScope}调用
     *
     * @param serviceType 服务类型
     * @param scope       所属的作用域
     * @param <TService>  服务类型
     * @return 某个类型所有的服务
     */
    @SuppressWarnings("unchecked")
    <TService> List<TService> getServicesInScope(Class<?> serviceType, ServiceScope scope) {
        this.ensureOpen();
        List<ServiceDefinition> definitions = this.serviceIndex.get(serviceType);
        List<TService> list = new ArrayList<>();
        if (definitions == null) return list;

        for (ServiceDefinition definition : definitions) {
            Object service = this.resolveSingle(serviceType, definition, scope, new ArrayDeque<>());
            if (service != null) list.add((TService) service);
        }
        return list;
    }

    /**
     * 注销作用域
     * 供{@link ServiceScope}关闭时调用
     *
     * @param scope 作用域
     */
    void unregisterScope(ServiceScope scope) {
        this.scopes.remove(scope);
    }

    /**
     * 解析服务
     *
     * @param serviceType 服务类型
     * @param scope       所属的作用域 为null表示从容器解析
     * @param chain       本次解析的链 用于检测循环依赖
     * @return 服务实例 未注册时返回null
     */
    private Object resolve(Class<?> serviceType, ServiceScope scope, Deque<Class<?>> chain) {
        this.ensureOpen();
        if (scope != null && scope.isClosed())
            throw new IllegalStateException("服务作用域已被释放,不能再获取服务");

        //同一解析链上重复出现同一服务类型 说明存在循环依赖
        if (chain.contains(serviceType))
            throw new IllegalStateException("服务" + serviceType.getName() + "存在循环依赖,解析链:" + describeChain(chain, serviceType) + ".");

        List<ServiceDefinition> definitions = this.serviceIndex.get(serviceType);
        if (definitions == null || definitions.isEmpty()) return null;

        chain.addLast(serviceType);
        try {
            //取最后注册的那个定义
            return this.resolveSingle(serviceType, definitions.get(definitions.size() - 1), scope, chain);
        } finally {
            chain.removeLast();
        }
    }

    /**
     * 按一个服务定义解析服务实例
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @param scope             所属的作用域 为null表示从容器解析
     * @param chain             本次解析的链
     * @return 服务实例
     */
    private Object resolveSingle(Class<?> serviceType, ServiceDefinition serviceDefinition, ServiceScope scope, Deque<Class<?>> chain) {
        switch (serviceDefinition.getServiceLifetime()) {
            case Singleton:
                return this.resolveSingleton(serviceType, serviceDefinition, chain);
            case Scoped:
                return this.resolveScoped(serviceType, serviceDefinition, scope, chain);
            default:
                //多例 每次创建 归解析方(容器或作用域)所有
                Object transientInstance = this.createInstance(serviceType, serviceDefinition, scope, chain);
                this.track(scope, transientInstance);
                return transientInstance;
        }
    }

    /**
     * 解析单例服务
     * 单例归容器所有
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @param chain             本次解析的链
     * @return 服务实例
     */
    private Object resolveSingleton(Class<?> serviceType, ServiceDefinition serviceDefinition, Deque<Class<?>> chain) {
        ServiceKey key = new ServiceKey(serviceType, serviceDefinition);
        Object instance = this.singletonInstances.get(key);
        if (instance != null) return instance;

        //按单例键加锁 保证同一个单例只被创建一次 也避免并发时创建出多个被丢弃的实例
        Object lock = this.singletonLocks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            instance = this.singletonInstances.get(key);
            if (instance == null) {
                int depth = SINGLETON_CREATION_DEPTH.get();
                SINGLETON_CREATION_DEPTH.set(depth + 1);
                try {
                    instance = this.createInstance(serviceType, serviceDefinition, null, chain);
                } finally {
                    SINGLETON_CREATION_DEPTH.set(depth);
                }
                this.singletonInstances.put(key, instance);
                this.track(null, instance);
            }
            return instance;
        }
    }

    /**
     * 解析作用域内单例的服务
     * 同一个作用域内只创建一次,归该作用域所有;从容器解析时归容器所有
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @param scope             所属的作用域 为null表示归容器所有
     * @param chain             本次解析的链
     * @return 服务实例
     */
    private Object resolveScoped(Class<?> serviceType, ServiceDefinition serviceDefinition, ServiceScope scope, Deque<Class<?>> chain) {
        //单例的生命周期比作用域长 一旦依赖作用域内单例 就会把某个作用域的实例长期持有
        if (SINGLETON_CREATION_DEPTH.get() > 0)
            throw new IllegalStateException("单例服务不能依赖作用域内单例的服务,当前正在解析:" + serviceType.getName() + ".解析链:" + describeChain(chain) + ".");

        Map<ServiceKey, Object> instances = scope != null ? scope.getScopedInstances() : this.rootScopedInstances;
        ServiceKey key = new ServiceKey(serviceType, serviceDefinition);
        Object instance = instances.get(key);
        if (instance != null) return instance;

        Map<ServiceKey, Object> locks = scope != null ? scope.getScopedLocks() : this.rootScopedLocks;
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            instance = instances.get(key);
            if (instance == null) {
                //实例的依赖同样归这个作用域所有
                instance = this.createInstance(serviceType, serviceDefinition, scope, chain);
                instances.put(key, instance);
                this.track(scope, instance);
            }
            return instance;
        }
    }

    /**
     * 创建服务实例
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @param scope             所属的作用域
     * @param chain             本次解析的链
     * @return 服务实例
     */
    private Object createInstance(Class<?> serviceType, ServiceDefinition serviceDefinition, ServiceScope scope, Deque<Class<?>> chain) {
        //解析嵌套过深 大概率是委托创建的服务之间存在循环调用 用深度兜底
        int depth = RESOLUTION_DEPTH.get() + 1;
        RESOLUTION_DEPTH.set(depth);
        try {
            if (depth > MAX_RESOLUTION_DEPTH)
                throw new IllegalStateException("服务" + serviceType.getName() + "的解析嵌套已超过" + MAX_RESOLUTION_DEPTH + "层," +
                        "可能存在循环依赖(例如在委托中互相调用getService).解析链:" + describeChain(chain, serviceType) + ".");

            //定义了自己的方法 由定义方处理
            if (serviceDefinition.getImplementationFactory() != null) return serviceDefinition.getImplementationFactory().getServiceInstance(this);

            Class<?> implementType = this.resolveImplementType(serviceType, serviceDefinition);
            if (Modifier.isInterface(implementType.getModifiers()) || Modifier.isAbstract(implementType.getModifiers()))
                throw new IllegalArgumentException("实现类型不能是接口或者抽象类,服务类型:" + serviceType.getName() + ",实现类型:" + implementType.getName() + "." +
                        "请为" + serviceType.getName() + "指定具体的实现类型.");

            Constructor<?> constructor = this.getConstructor(implementType);
            if (constructor == null)
                throw new IllegalArgumentException("无法获取到服务" + implementType.getName() + "可用的构造函数.");

            Parameter[] parameters = constructor.getParameters();
            //没有参数
            if (parameters.length == 0) return newInstance(constructor, new Object[0]);

            Object[] parameterValues = new Object[parameters.length];
            //有参数 从当前容器(或作用域)里取符合的参数
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Object parameterValue = this.resolve(parameter.getType(), scope, chain);
                if (parameterValue == null)
                    throw new IllegalArgumentException("处理服务" + implementType.getName() + "的构造函数参数" + parameter.getName() + "出错," +
                            "无法从已注册的服务中获取类型为" + parameter.getType().getName() + "的对象.解析链:" + describeChain(chain, implementType) + ".");
                parameterValues[i] = parameterValue;
            }

            return newInstance(constructor, parameterValues);
        } finally {
            RESOLUTION_DEPTH.set(depth - 1);
        }
    }

    /**
     * 调用构造函数创建实例 统一处理反射异常
     *
     * @param constructor 构造函数
     * @param arguments   构造参数
     * @return 服务实例
     */
    private static Object newInstance(Constructor<?> constructor, Object[] arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法构造服务" + constructor.getDeclaringClass().getName(), e);
        }
    }

    /**
     * 确定服务的实现类型
     *
     * @param serviceType       服务类型
     * @param serviceDefinition 服务定义
     * @return 实现类型
     */
    private Class<?> resolveImplementType(Class<?> serviceType, ServiceDefinition serviceDefinition) {
        Class<?> implementType = serviceDefinition.getImplementType();
        return implementType == null ? serviceDefinition.getServiceType() : implementType;
    }

    /**
     * 获取服务类型的构造函数(公开的优先 其次非公开的)
     *
     * @param implementType 实现类型
     * @return 构造函数
     */
    private Constructor<?> getConstructor(Class<?> implementType) {
        //从缓存里面取
        Constructor<?> constructor = ServiceConstructorInstance.getInstance().getServiceConstructor(implementType);
        if (constructor != null) return constructor;

        //获取所有公开的
        Constructor<?>[] constructors = implementType.getConstructors();
        if (constructors.length == 0)
            //没有就获取非公开的
            constructors = implementType.getDeclaredConstructors();

        constructor = constructors.length == 0 ? null : constructors[0];
        //非公开的构造函数需要显式允许访问 与.NET版可直接调用非公开构造函数的行为保持一致
        if (constructor != null)
            try {
                constructor.setAccessible(true);
            } catch (RuntimeException ignored) {
                //受限制的运行时可能不允许 此处在使用时报错
            }

        //不存在的放入缓存
        if (constructor != null)
            ServiceConstructorInstance.getInstance().setServiceConstructor(implementType, constructor);

        return constructor;
    }

    /**
     * 登记需要释放的实例
     *
     * @param scope    所属的作用域 为null表示归容器自身所有
     * @param instance 服务实例
     */
    private void track(ServiceScope scope, Object instance) {
        if (!(instance instanceof AutoCloseable)) return;

        if (scope != null)
            scope.track(instance);
        else
            this.disposables.add(instance);
    }

    /**
     * 关闭容器 释放所有作用域与容器自身的实例
     * 重复关闭不会产生任何影响
     */
    @Override
    public void close() {
        //已经关闭过 直接返回
        if (!this.closed.compareAndSet(false, true)) return;

        //先释放仍然存活的作用域 后创建的先释放
        List<ServiceScope> liveScopes = new ArrayList<>(this.scopes);
        liveScopes.sort((x, y) -> Long.compare(y.getSequence(), x.getSequence()));
        for (ServiceScope scope : liveScopes) {
            try {
                scope.close();
            } catch (Exception ignored) {
                //单个作用域释放失败 不影响其余作用域的释放
            }
        }
        this.scopes.clear();

        //再逆序释放容器自身的实例
        List<Object> instances = new ArrayList<>(this.disposables);
        Collections.reverse(instances);
        for (Object instance : instances) {
            try {
                ((AutoCloseable) instance).close();
            } catch (Exception ignored) {
                //单个实例释放失败 不影响其余实例的释放
            }
        }

        this.disposables.clear();
        this.singletonInstances.clear();
        this.singletonLocks.clear();
        //容器本身的作用域内单例同样已经释放
        this.rootScopedInstances.clear();
        this.rootScopedLocks.clear();

        //从全局登记中移除自己 关闭之后可以重新建造同一上下文的容器
        ServiceContainerInstance.getInstance().remove(this);
    }

    /**
     * 已关闭时不再提供服务 避免被关闭的容器又创建出无人释放的新实例
     */
    private void ensureOpen() {
        if (this.closed.get())
            throw new IllegalStateException("上下文" + (this.contextType == null ? "" : this.contextType.getName()) + "的服务容器已被释放,不能再获取服务.");
    }

    /**
     * 描述解析链 用于异常信息
     *
     * @param chain 解析链
     * @return 解析链的描述
     */
    private static String describeChain(Deque<Class<?>> chain) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> type : chain) {
            if (builder.length() > 0) builder.append(" -> ");
            builder.append(type.getSimpleName());
        }
        return builder.toString();
    }

    /**
     * 描述解析链 用于异常信息
     *
     * @param chain       解析链
     * @param serviceType 出现循环的服务类型
     * @return 解析链的描述
     */
    private static String describeChain(Deque<Class<?>> chain, Class<?> serviceType) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> type : chain) {
            if (builder.length() > 0) builder.append(" -> ");
            builder.append(type.getSimpleName());
        }
        if (builder.length() > 0) builder.append(" -> ");
        builder.append(serviceType == null ? "" : serviceType.getSimpleName());
        return builder.toString();
    }

    /**
     * 服务类型键
     * 表示某个服务类型的具体定义
     */
    static class ServiceKey {

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
            this.implementType = definition.getImplementType();
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
            return Objects.equals(this.serviceType, that.serviceType) && Objects.equals(this.implementType, that.implementType);
        }

        /**
         * 重写哈希码
         * 不再拼接字符串 避免每次查找都产生一次字符串分配
         *
         * @return 哈希码
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.serviceType, this.implementType);
        }
    }
}
