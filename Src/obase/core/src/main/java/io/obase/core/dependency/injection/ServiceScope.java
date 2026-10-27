/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务作用域,表示一批服务实例的归属与释放边界.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-10-27 11:03:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务作用域
 * <p>
 * 作用域只是释放边界,不是可见性边界:作用域内能解析到的服务与容器完全一致.
 * 从作用域解析出的多例服务与作用域内单例(EServiceLifetime.Scoped)归该作用域所有,
 * 作用域关闭时按创建顺序的逆序释放;单例始终归容器所有,不受作用域影响.
 * 作用域可以再创建子作用域,关闭父作用域时先关闭仍然存活的子作用域.
 * <p>
 * 作用域不是线程安全的,应当只在一个逻辑执行流内使用,使用方法为:
 * <pre>
 * try (ServiceScope scope = container.createScope()) {
 *     IRepository repository = scope.getService(IRepository.class);
 * }
 * </pre>
 */
public final class ServiceScope implements AutoCloseable {

    /**
     * 子作用域
     */
    private final Map<ServiceScope, Boolean> children = new ConcurrentHashMap<>();

    /**
     * 所属的服务容器
     */
    private final ServiceContainer container;

    /**
     * 归属于本作用域的实例 按创建顺序记录
     */
    private final Queue<Object> disposables = new ConcurrentLinkedQueue<>();

    /**
     * 父作用域 为null表示是容器的直接子作用域
     */
    private final ServiceScope parent;

    /**
     * 归属于本作用域的作用域内单例的实例
     */
    private final Map<ServiceContainer.ServiceKey, Object> scopedInstances = new ConcurrentHashMap<>();

    /**
     * 归属于本作用域的作用域内单例的创建锁
     */
    private final Map<ServiceContainer.ServiceKey, Object> scopedLocks = new ConcurrentHashMap<>();

    /**
     * 创建序号 用于关闭时保证后创建的先关闭
     */
    private final long sequence;

    /**
     * 是否已关闭
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 初始化服务作用域
     * 由{@link ServiceContainer#createScope()}创建,不对外公开构造
     *
     * @param container 所属的服务容器
     * @param parent    父作用域 为null表示是容器的直接子作用域
     * @param sequence  创建序号
     */
    ServiceScope(ServiceContainer container, ServiceScope parent, long sequence) {
        this.container = container;
        this.parent = parent;
        this.sequence = sequence;
    }

    /**
     * 创建一个子作用域
     * 子作用域内解析的多例服务与作用域内单例归子作用域所有,关闭本作用域时子作用域会先被关闭
     *
     * @return 子作用域
     */
    public ServiceScope createScope() {
        this.ensureOpen();
        return this.container.createScopeCore(this);
    }

    /**
     * 获取服务
     * 多例服务与作用域内单例归本作用域所有,单例仍是容器中的同一个实例
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 服务实例 未注册时返回null
     */
    @SuppressWarnings("unchecked")
    public <TService> TService getService(Class<TService> serviceType) {
        this.ensureOpen();
        return (TService) this.container.getServiceInScope(serviceType, this);
    }

    /**
     * 获取某个类型所有的服务
     * 多例服务与作用域内单例归本作用域所有,单例仍是容器中的同一个实例
     *
     * @param serviceType 服务类型
     * @param <TService>  服务类型
     * @return 某个类型所有的服务
     */
    public <TService> List<TService> getServices(Class<TService> serviceType) {
        this.ensureOpen();
        return this.container.getServicesInScope(serviceType, this);
    }

    /**
     * 是否已关闭
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    /**
     * 关闭作用域 释放归属于本作用域的实例
     * 先关闭仍然存活的子作用域(后创建的先关闭) 再释放本作用域自己的实例
     * 重复关闭不会产生任何影响
     */
    @Override
    public void close() {
        //已经关闭过 直接返回
        if (!this.closed.compareAndSet(false, true)) return;

        //先关闭子作用域 后创建的先关闭
        if (!this.children.isEmpty()) {
            List<ServiceScope> childScopes = new ArrayList<>(this.children.keySet());
            childScopes.sort((x, y) -> Long.compare(y.getSequence(), x.getSequence()));
            for (ServiceScope child : childScopes) {
                try {
                    child.close();
                } catch (Exception ignored) {
                    //单个子作用域关闭失败 不影响其余子作用域的关闭
                }
            }

            this.children.clear();
        }

        //逆序释放 先创建的后释放 尽量让依赖方先于被依赖方释放
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
        this.scopedInstances.clear();
        this.scopedLocks.clear();

        //从父作用域或容器中注销 关闭之后不再负责这个作用域
        if (this.parent != null)
            this.parent.unregisterChild(this);
        else
            this.container.unregisterScope(this);
    }

    /**
     * 获取创建序号
     *
     * @return 创建序号
     */
    long getSequence() {
        return this.sequence;
    }

    /**
     * 登记子作用域
     * 供容器创建子作用域时调用
     *
     * @param child 子作用域
     */
    void registerChild(ServiceScope child) {
        if (child == null) return;

        this.children.put(child, Boolean.TRUE);
    }

    /**
     * 注销子作用域
     * 供子作用域关闭时调用
     *
     * @param child 子作用域
     */
    void unregisterChild(ServiceScope child) {
        if (child == null) return;

        this.children.remove(child);
    }

    /**
     * 获取归属于本作用域的作用域内单例的实例
     *
     * @return 作用域内单例的实例集合
     */
    Map<ServiceContainer.ServiceKey, Object> getScopedInstances() {
        return this.scopedInstances;
    }

    /**
     * 获取归属于本作用域的作用域内单例的创建锁
     *
     * @return 作用域内单例的创建锁集合
     */
    Map<ServiceContainer.ServiceKey, Object> getScopedLocks() {
        return this.scopedLocks;
    }

    /**
     * 登记一个归属于本作用域的实例
     *
     * @param instance 服务实例
     */
    void track(Object instance) {
        if (instance instanceof AutoCloseable) this.disposables.add(instance);
    }

    /**
     * 确认作用域未关闭
     */
    private void ensureOpen() {
        if (this.closed.get()) throw new IllegalStateException("服务作用域已被释放,不能再获取服务.");
    }
}
