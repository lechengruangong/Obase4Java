/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：依赖注入容器、作用域与作用域内单例的测试.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.test.core.functional;

import io.obase.core.dependency.injection.ObaseDependencyInjection;
import io.obase.core.dependency.injection.ServiceContainer;
import io.obase.core.dependency.injection.ServiceContainerInstance;
import io.obase.core.dependency.injection.ServiceScope;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.dependencyInjection.ContainerTestServices;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 依赖注入容器、作用域与作用域内单例的测试
 * <p>
 * 测试用的服务在ConfigSetUp中注册,本测试只从已建好的容器中获取,按数据源逐个验证.
 * 同一主题的场景合并在一个用例内断言,避免用例过于零碎.
 * 与.NET版ServiceContainerTest的用例一一对应,两版用例数必须保持一致.
 */
@ExtendWith(ConfigSetUp.class)
public class ServiceContainerTest {

    /**
     * 获取当前数据源对应的服务容器
     *
     * @param dataSource 数据源类型
     * @return 服务容器
     */
    private static ServiceContainer getContainer(EDataSource dataSource) {
        ServiceContainer container = ServiceContainerInstance.getInstance()
                .getServiceContainer(TestCaseSourceConfigurationManager.getDataSourceContextType(dataSource));
        assertNotNull(container);
        return container;
    }

    //region 容器解析

    /**
     * 解析已注册的服务:单例同一实例、末次注册胜出、全部实现按注册顺序返回、重复注册去重、
     * 构造函数注入、未注册类型返回null
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void container_ResolvesRegisteredServices(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        //单例:同一实例,且同一服务类型多次注册时以最后一次注册为准
        ContainerTestServices.IService first = container.getService(ContainerTestServices.IService.class);
        ContainerTestServices.IService second = container.getService(ContainerTestServices.IService.class);

        assertNotNull(first);
        assertSame(first, second);
        assertEquals("B", first.getCode());

        //获取某服务类型的全部实现 顺序为注册顺序
        List<ContainerTestServices.IService> services = container.getServices(ContainerTestServices.IService.class);

        assertNotNull(services);
        assertEquals(2, services.size());
        assertEquals("A,B", String.join(",", services.stream().map(ContainerTestServices.IService::getCode)
                .toArray(String[]::new)));

        //重复注册只保留一个定义
        List<ContainerTestServices.Dependency> dependencies =
                container.getServices(ContainerTestServices.Dependency.class);

        assertNotNull(dependencies);
        assertEquals(1, dependencies.size());
        assertSame(dependencies.get(0), container.getService(ContainerTestServices.Dependency.class));

        //构造函数注入:构造函数参数从容器中获取
        ContainerTestServices.NeedsDependency service =
                container.getService(ContainerTestServices.NeedsDependency.class);

        assertNotNull(service);
        assertSame(container.getService(ContainerTestServices.Dependency.class), service.getDependency());

        //未注册的类型返回null
        assertNull(container.getService(ContainerTestServices.INotRegistered.class));
    }

    /**
     * 由委托创建服务实例:以接口作为服务类型注册时也能正常获取,同一单例取到同一实例
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void container_CreatesInstancesFromFactories(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IFactoryOnly first = container.getService(ContainerTestServices.IFactoryOnly.class);
        ContainerTestServices.IFactoryOnly second = container.getService(ContainerTestServices.IFactoryOnly.class);

        assertNotNull(first);
        assertTrue(first instanceof ContainerTestServices.FactoryOnly);
        assertSame(first, second);
    }

    /**
     * 泛型服务按擦除后的类型解析
     * Java的泛型在运行时被擦除,不同的类型实参落到同一个服务定义上
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void container_ResolvesGenericServices(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IRepository repository = container.getService(ContainerTestServices.IRepository.class);

        assertNotNull(repository);
        assertTrue(repository instanceof ContainerTestServices.Repository);

        //泛型服务的全部实现
        List<ContainerTestServices.IRepository> repositories =
                container.getServices(ContainerTestServices.IRepository.class);

        assertNotNull(repositories);
        assertEquals(1, repositories.size());
        assertTrue(repositories.get(0) instanceof ContainerTestServices.Repository);
    }

    /**
     * 解析失败的各种情形:构造函数参数未注册、循环依赖、单例依赖作用域内单例、重复建造容器
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void container_ReportsResolutionErrors(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        //构造函数参数未注册 抛出IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> container.getService(ContainerTestServices.NeedsUnregistered.class));

        //循环依赖 异常信息包含解析链
        IllegalStateException cyclic = assertThrows(IllegalStateException.class,
                () -> container.getService(ContainerTestServices.CyclicA.class));

        assertTrue(cyclic.getMessage().contains("循环依赖"));
        assertTrue(cyclic.getMessage().contains("CyclicA"));
        assertTrue(cyclic.getMessage().contains("CyclicB"));

        //单例依赖作用域内单例 异常信息包含解析链
        IllegalStateException scoped = assertThrows(IllegalStateException.class,
                () -> container.getService(ContainerTestServices.SingletonNeedsScoped.class));

        assertTrue(scoped.getMessage().contains("单例服务不能依赖作用域内单例的服务"));
        assertTrue(scoped.getMessage().contains("SingletonNeedsScoped"));
        assertTrue(scoped.getMessage().contains("ScopedOnly"));

        //同一上下文的容器已建造时 再次建造抛出RuntimeException
        var builder = ObaseDependencyInjection.createBuilder(
                TestCaseSourceConfigurationManager.getDataSourceContextType(dataSource));
        builder.addSingleton(ContainerTestServices.IService.class, ContainerTestServices.ServiceA.class);

        assertThrows(RuntimeException.class, builder::build);
    }

    /**
     * 并发获取同一单例不会重复创建
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void container_ResolvesSingletonConcurrently(EDataSource dataSource) throws InterruptedException {
        ServiceContainer container = getContainer(dataSource);
        //单例可能在别的用例里已经创建过 这里只验证并发获取不会重复创建
        container.getService(ContainerTestServices.SlowSingleton.class);
        int createdBefore = ContainerTestServices.SlowSingleton.getCreated();

        Map<Integer, ContainerTestServices.SlowSingleton> instances = new ConcurrentHashMap<>();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(16);
        for (int i = 0; i < 16; i++) {
            int index = i;
            new Thread(() -> {
                try {
                    start.await();
                    instances.put(index, container.getService(ContainerTestServices.SlowSingleton.class));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await();

        assertEquals(16, instances.size());
        assertEquals(1, instances.values().stream().distinct().count());
        assertEquals(createdBefore, ContainerTestServices.SlowSingleton.getCreated());
    }

    //endregion

    //region 作用域

    /**
     * 作用域对多例的所有权:作用域与容器共享单例、多例随作用域关闭、容器中的多例不受影响、
     * 兄弟作用域互不影响、关闭父作用域不影响容器直接创建的作用域、大量多例不累积
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_OwnsTransientsAndIsolatesSiblingScopes(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        //单例在容器与作用域之间共享同一实例
        ContainerTestServices.IService singleton = container.getService(ContainerTestServices.IService.class);
        try (ServiceScope scope = container.createScope()) {
            assertSame(singleton, scope.getService(ContainerTestServices.IService.class));
        }

        //作用域关闭不影响单例
        assertSame(singleton, container.getService(ContainerTestServices.IService.class));

        //作用域内获取的多例归作用域 随作用域关闭
        ContainerTestServices.Tracked.reset();
        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.Tracked first = scope.getService(ContainerTestServices.Tracked.class);
            ContainerTestServices.Tracked second = scope.getService(ContainerTestServices.Tracked.class);

            assertNotSame(first, second);
            assertEquals(2, ContainerTestServices.Tracked.getCreated());
            assertEquals(0, ContainerTestServices.Tracked.getDisposed());
        }

        assertEquals(2, ContainerTestServices.Tracked.getDisposed());

        //从容器获取的多例不受作用域关闭影响
        ContainerTestServices.Tracked.reset();
        ContainerTestServices.Tracked fromContainer = container.getService(ContainerTestServices.Tracked.class);
        try (ServiceScope scope = container.createScope()) {
            scope.getService(ContainerTestServices.Tracked.class);
        }

        //只有作用域里的那一个被释放 容器里的仍归容器
        assertNotNull(fromContainer);
        assertEquals(2, ContainerTestServices.Tracked.getCreated());
        assertEquals(1, ContainerTestServices.Tracked.getDisposed());

        //兄弟作用域互不影响:关闭其中一个 另一个仍然可用
        ContainerTestServices.Tracked.reset();
        ServiceScope firstScope = container.createScope();
        ServiceScope secondScope = container.createScope();
        firstScope.getService(ContainerTestServices.Tracked.class);
        secondScope.getService(ContainerTestServices.Tracked.class);

        firstScope.close();

        assertTrue(firstScope.isClosed());
        assertFalse(secondScope.isClosed());
        //只释放第一个作用域里的实例
        assertEquals(1, ContainerTestServices.Tracked.getDisposed());

        //第二个作用域仍然可以获取 关闭它时释放自己的实例
        assertNotNull(secondScope.getService(ContainerTestServices.Tracked.class));
        secondScope.close();
        //第二个作用域里一共创建了两个实例(之前的一个与刚才的一个) 一并释放
        assertEquals(3, ContainerTestServices.Tracked.getDisposed());

        //关闭父作用域时 从容器直接创建的兄弟作用域不受影响
        ContainerTestServices.Tracked.reset();
        ServiceScope parent = container.createScope();
        ServiceScope child = parent.createScope();
        ServiceScope sibling = container.createScope();
        child.getService(ContainerTestServices.Tracked.class);
        sibling.getService(ContainerTestServices.Tracked.class);

        parent.close();

        assertTrue(child.isClosed());
        assertFalse(sibling.isClosed());
        //只有子作用域里的实例被释放
        assertEquals(1, ContainerTestServices.Tracked.getDisposed());

        sibling.close();
        assertEquals(2, ContainerTestServices.Tracked.getDisposed());

        //作用域内大量获取多例不会在容器中累积
        final int count = 20000;
        ContainerTestServices.Tracked.reset();
        try (ServiceScope scope = container.createScope()) {
            for (int i = 0; i < count; i++) scope.getService(ContainerTestServices.Tracked.class);

            assertEquals(count, ContainerTestServices.Tracked.getCreated());
            assertEquals(0, ContainerTestServices.Tracked.getDisposed());
        }

        //作用域关闭后全部释放 容器不持有这些实例
        assertEquals(count, ContainerTestServices.Tracked.getDisposed());
    }

    /**
     * 作用域的释放顺序与关闭后的行为:逆创建顺序释放、嵌套作用域由深到浅释放、
     * 重复关闭幂等、已关闭的作用域再获取抛异常
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_DisposesInNestingOrderAndRejectsClosedScope(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        //作用域内按创建顺序的逆序释放
        ContainerTestServices.DisposalLog.reset();
        try (ServiceScope scope = container.createScope()) {
            //解析B时会先创建它的依赖A
            scope.getService(ContainerTestServices.LoggedB.class);
        }

        assertEquals("B,A", ContainerTestServices.DisposalLog.joined());

        //嵌套作用域:关闭父作用域时先关闭仍然存活的子作用域
        ContainerTestServices.DisposalLog.reset();
        ServiceScope outer = container.createScope();
        outer.getService(ContainerTestServices.OuterItem.class);
        ServiceScope inner = outer.createScope();
        inner.getService(ContainerTestServices.InnerItem.class);

        //只关闭父作用域 子作用域应当先被关闭
        outer.close();

        assertTrue(inner.isClosed());
        assertTrue(outer.isClosed());
        assertEquals("inner,outer", ContainerTestServices.DisposalLog.joined());

        //多层嵌套:由深到浅依次关闭
        ContainerTestServices.DisposalLog.reset();
        ServiceScope outermost = container.createScope();
        outermost.getService(ContainerTestServices.OuterItem.class);
        ServiceScope middle = outermost.createScope();
        middle.getService(ContainerTestServices.InnerItem.class);
        ServiceScope deepest = middle.createScope();
        deepest.getService(ContainerTestServices.GrandchildItem.class);

        outermost.close();

        assertTrue(deepest.isClosed());
        assertTrue(middle.isClosed());
        //由深到浅 每一层先释放自己的实例
        assertEquals("grandchild,inner,outer", ContainerTestServices.DisposalLog.joined());

        //已关闭的作用域再获取抛异常 重复关闭不抛异常
        ServiceScope closed = container.createScope();
        closed.getService(ContainerTestServices.LoggedA.class);
        closed.close();
        closed.close();

        assertTrue(closed.isClosed());
        assertThrows(IllegalStateException.class, () -> closed.getService(ContainerTestServices.LoggedA.class));

        //父作用域关闭后 已关闭的子作用域不能再获取
        ServiceScope closedParent = container.createScope();
        ServiceScope closedChild = closedParent.createScope();
        closedChild.getService(ContainerTestServices.LoggedA.class);

        closedParent.close();

        assertTrue(closedChild.isClosed());
        assertThrows(IllegalStateException.class,
                () -> closedChild.getService(ContainerTestServices.LoggedA.class));
    }

    //endregion

    //region 作用域内单例

    /**
     * 作用域内单例的生命周期:同一作用域内是同一实例且全部实现复用同一批实例、不同作用域各自持有、
     * 从容器获取时归容器所有、随作用域释放且先于它的多例依赖释放
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_SingletonLifecycle(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        //同一作用域内是同一个实例且只创建一次 获取全部实现时复用同一批实例
        ContainerTestServices.ScopedOnly.reset();
        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.ScopedOnly single = scope.getService(ContainerTestServices.ScopedOnly.class);
            ContainerTestServices.ScopedOnly again = scope.getService(ContainerTestServices.ScopedOnly.class);
            List<ContainerTestServices.ScopedOnly> all = scope.getServices(ContainerTestServices.ScopedOnly.class);

            assertSame(single, again);
            assertEquals(1, all.size());
            assertSame(single, all.get(0));
            assertEquals(1, ContainerTestServices.ScopedOnly.getCreated());
        }

        //不同作用域各自持有自己的实例
        ContainerTestServices.ScopedOnly.reset();
        try (ServiceScope first = container.createScope(); ServiceScope second = container.createScope()) {
            assertNotSame(first.getService(ContainerTestServices.ScopedOnly.class),
                    second.getService(ContainerTestServices.ScopedOnly.class));
            assertEquals(2, ContainerTestServices.ScopedOnly.getCreated());
        }

        //从容器获取时归容器所有 作用域关闭不影响它
        ContainerTestServices.ScopedDisposable.reset();
        ContainerTestServices.ScopedDisposable fromContainer =
                container.getService(ContainerTestServices.ScopedDisposable.class);
        assertSame(fromContainer, container.getService(ContainerTestServices.ScopedDisposable.class));

        try (ServiceScope scope = container.createScope()) {
            assertNotSame(fromContainer, scope.getService(ContainerTestServices.ScopedDisposable.class));
        }

        //只释放了作用域里的那一个 容器的那个实例仍然存活
        assertEquals(1, ContainerTestServices.ScopedDisposable.getDisposed());
        assertSame(fromContainer, container.getService(ContainerTestServices.ScopedDisposable.class));

        //随作用域释放 且先于它的多例依赖释放
        ContainerTestServices.DisposalLog.reset();
        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.ScopedWithTransient service =
                    scope.getService(ContainerTestServices.ScopedWithTransient.class);
            assertNotNull(service.getDependency());
        }

        //多例依赖先创建 后释放;作用域内单例后创建 先释放
        assertEquals("scoped,A", ContainerTestServices.DisposalLog.joined());
    }

    //endregion
}
