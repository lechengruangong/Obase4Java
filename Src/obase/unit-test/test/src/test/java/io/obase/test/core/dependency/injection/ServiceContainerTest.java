/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：依赖注入容器、作用域与作用域内单例的测试.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.test.core.dependency.injection;

import io.obase.core.dependency.injection.ServiceContainer;
import io.obase.core.dependency.injection.ServiceContainerInstance;
import io.obase.core.dependency.injection.ServiceScope;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
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

    //region 容器基本行为

    /**
     * 单例:同一实例,且同一服务类型多次注册时以最后一次注册为准
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void singleton_ReturnsSameInstanceAndLastRegistrationWins(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IService first = container.getService(ContainerTestServices.IService.class);
        ContainerTestServices.IService second = container.getService(ContainerTestServices.IService.class);

        assertNotNull(first);
        assertSame(first, second);
        assertEquals("B", first.getCode());
    }

    /**
     * 获取某服务类型的全部实现 顺序为注册顺序
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void getServices_ReturnsAllImplementationsInRegistrationOrder(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        List<ContainerTestServices.IService> services = container.getServices(ContainerTestServices.IService.class);

        assertNotNull(services);
        assertEquals(2, services.size());
        assertEquals("A,B", String.join(",", services.stream().map(ContainerTestServices.IService::getCode)
                .toArray(String[]::new)));
    }

    /**
     * 构造函数注入:构造函数参数从容器中获取
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void constructorInjection_ResolvesRegisteredDependency(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.NeedsDependency service = container.getService(ContainerTestServices.NeedsDependency.class);
        ContainerTestServices.Dependency dependency = container.getService(ContainerTestServices.Dependency.class);

        assertNotNull(service);
        assertSame(dependency, service.getDependency());
    }

    /**
     * 构造函数参数无法解析时抛出IllegalArgumentException
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void constructorInjection_UnresolvableParameterThrows(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        assertThrows(IllegalArgumentException.class,
                () -> container.getService(ContainerTestServices.NeedsUnregistered.class));
    }

    /**
     * 由委托创建服务实例
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void implementationFactory_CreatesInstance(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IFactoryOnly first = container.getService(ContainerTestServices.IFactoryOnly.class);
        ContainerTestServices.IFactoryOnly second = container.getService(ContainerTestServices.IFactoryOnly.class);

        assertNotNull(first);
        assertTrue(first instanceof ContainerTestServices.FactoryOnly);
        assertSame(first, second);
    }

    /**
     * 同一上下文的容器已建造时 再次建造会抛出RuntimeException
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void duplicateBuildThrows(EDataSource dataSource) {
        var builder = io.obase.core.dependency.injection.ObaseDependencyInjection.createBuilder(
                TestCaseSourceConfigurationManager.getDataSourceContextType(dataSource));
        builder.addSingleton(ContainerTestServices.IService.class, ContainerTestServices.ServiceA.class);

        assertThrows(RuntimeException.class, builder::build);
    }

    /**
     * 同一服务类型与实现类型重复注册时只保留一个定义
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void duplicateRegistration_KeepsSingleEntry(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        List<ContainerTestServices.Dependency> services = container.getServices(ContainerTestServices.Dependency.class);

        assertNotNull(services);
        assertEquals(1, services.size());
        assertSame(services.get(0), container.getService(ContainerTestServices.Dependency.class));
    }

    /**
     * 泛型服务按擦除后的类型获取
     * 对应.NET版的同名用例:Java的泛型在运行时被擦除,IRepository&lt;String&gt;与IRepository&lt;Integer&gt;是同一个类型
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void genericService_ResolvedByErasedType(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IRepository repository = container.getService(ContainerTestServices.IRepository.class);

        assertNotNull(repository);
        assertTrue(repository instanceof ContainerTestServices.Repository);
    }

    /**
     * 获取泛型服务的全部实现
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void listOfGenericService_Resolved(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        List<ContainerTestServices.IRepository> repositories =
                container.getServices(ContainerTestServices.IRepository.class);

        assertNotNull(repositories);
        assertEquals(1, repositories.size());
        assertTrue(repositories.get(0) instanceof ContainerTestServices.Repository);
    }

    //endregion

    //region 健壮性

    /**
     * 获取未注册的类型返回null
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void getServiceOfUnregisteredType_ReturnsNull(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        assertNull(container.getService(ContainerTestServices.INotRegistered.class));
        assertNotNull(container.getService(ContainerTestServices.IService.class));
    }

    /**
     * 以接口作为服务类型、用委托注册时可以正常获取
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void factoryRegistrationWithInterfaceServiceType_Succeeds(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IFactoryOnly service = container.getService(ContainerTestServices.IFactoryOnly.class);

        assertNotNull(service);
        assertTrue(service instanceof ContainerTestServices.FactoryOnly);
    }

    /**
     * 循环依赖抛出异常 且异常信息包含解析链
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void circularDependencyThrowsWithResolutionChain(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> container.getService(ContainerTestServices.CyclicA.class));

        assertTrue(exception.getMessage().contains("循环依赖"));
        assertTrue(exception.getMessage().contains("CyclicA"));
        assertTrue(exception.getMessage().contains("CyclicB"));
    }

    /**
     * 并发获取同一单例不会重复创建
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void concurrentSingletonResolution_CreatesInstanceOnce(EDataSource dataSource) throws InterruptedException {
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
     * 单例在容器与作用域之间共享同一实例
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_SingletonSharedWithContainer(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ContainerTestServices.IService fromContainer = container.getService(ContainerTestServices.IService.class);
        try (ServiceScope scope = container.createScope()) {
            assertSame(fromContainer, scope.getService(ContainerTestServices.IService.class));
        }

        //作用域关闭不影响单例
        assertSame(fromContainer, container.getService(ContainerTestServices.IService.class));
    }

    /**
     * 作用域内获取的多例归作用域 随作用域关闭
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_TransientOwnedAndDisposedByScope(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.Tracked.reset();

        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.Tracked first = scope.getService(ContainerTestServices.Tracked.class);
            ContainerTestServices.Tracked second = scope.getService(ContainerTestServices.Tracked.class);

            assertNotSame(first, second);
            assertEquals(2, ContainerTestServices.Tracked.getCreated());
            assertEquals(0, ContainerTestServices.Tracked.getDisposed());
        }

        assertEquals(2, ContainerTestServices.Tracked.getDisposed());
    }

    /**
     * 从容器获取的多例不受作用域关闭影响
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_RootTransientNotAffectedByScopeClose(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.Tracked.reset();

        ContainerTestServices.Tracked fromContainer = container.getService(ContainerTestServices.Tracked.class);
        try (ServiceScope scope = container.createScope()) {
            scope.getService(ContainerTestServices.Tracked.class);
        }

        //只有作用域里的那一个被释放 容器里的仍归容器
        assertNotNull(fromContainer);
        assertEquals(2, ContainerTestServices.Tracked.getCreated());
        assertEquals(1, ContainerTestServices.Tracked.getDisposed());
    }

    /**
     * 作用域内按创建顺序的逆序释放
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_DisposesInReverseCreationOrder(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.DisposalLog.reset();

        try (ServiceScope scope = container.createScope()) {
            //解析B时会先创建它的依赖A
            scope.getService(ContainerTestServices.LoggedB.class);
        }

        assertEquals("B,A", ContainerTestServices.DisposalLog.joined());
    }

    /**
     * 嵌套作用域:关闭父作用域时先关闭仍然存活的子作用域
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_NestedScopeDisposedFirst(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
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
    }

    /**
     * 多层嵌套:由深到浅依次关闭
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_GrandchildScopesDisposedNewestFirst(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.DisposalLog.reset();

        ServiceScope outer = container.createScope();
        outer.getService(ContainerTestServices.OuterItem.class);
        ServiceScope child = outer.createScope();
        child.getService(ContainerTestServices.InnerItem.class);
        ServiceScope grandchild = child.createScope();
        grandchild.getService(ContainerTestServices.GrandchildItem.class);

        outer.close();

        assertTrue(grandchild.isClosed());
        assertTrue(child.isClosed());
        //由深到浅 每一层先释放自己的实例
        assertEquals("grandchild,inner,outer", ContainerTestServices.DisposalLog.joined());
    }

    /**
     * 兄弟作用域互不影响:关闭其中一个 另一个仍然可用
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_SiblingScopesAreIndependent(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.Tracked.reset();

        ServiceScope first = container.createScope();
        ServiceScope second = container.createScope();
        first.getService(ContainerTestServices.Tracked.class);
        second.getService(ContainerTestServices.Tracked.class);

        first.close();

        assertTrue(first.isClosed());
        assertFalse(second.isClosed());
        //只释放第一个作用域里的实例
        assertEquals(1, ContainerTestServices.Tracked.getDisposed());

        //第二个作用域仍然可以获取 关闭它时释放自己的实例
        assertNotNull(second.getService(ContainerTestServices.Tracked.class));
        second.close();
        //第二个作用域里一共创建了两个实例(之前的一个与刚才的一个) 一并释放
        assertEquals(3, ContainerTestServices.Tracked.getDisposed());
    }

    /**
     * 关闭父作用域时 从容器直接创建的兄弟作用域不受影响
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_DisposingParentKeepsSiblingUsable(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
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
    }

    /**
     * 已关闭的作用域再获取抛异常 重复关闭不抛异常
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_ClosedScopeThrowsAndCloseIsIdempotent(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ServiceScope scope = container.createScope();
        scope.getService(ContainerTestServices.LoggedA.class);
        scope.close();
        scope.close();

        assertTrue(scope.isClosed());
        assertThrows(IllegalStateException.class, () -> scope.getService(ContainerTestServices.LoggedA.class));
    }

    /**
     * 父作用域关闭后 已关闭的子作用域不能再获取
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_ResolvingInClosedChildScopeThrows(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        ServiceScope parent = container.createScope();
        ServiceScope child = parent.createScope();
        child.getService(ContainerTestServices.LoggedA.class);

        parent.close();

        assertTrue(child.isClosed());
        assertThrows(IllegalStateException.class, () -> child.getService(ContainerTestServices.LoggedA.class));
    }

    /**
     * 作用域内大量获取多例不会在容器中累积
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scope_ResolvingManyTransientsDoesNotAccumulate(EDataSource dataSource) {
        final int count = 20000;
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.Tracked.reset();

        try (ServiceScope scope = container.createScope()) {
            for (int i = 0; i < count; i++) scope.getService(ContainerTestServices.Tracked.class);

            assertEquals(count, ContainerTestServices.Tracked.getCreated());
            assertEquals(0, ContainerTestServices.Tracked.getDisposed());
        }

        //作用域关闭后全部释放 容器不持有这些实例
        assertEquals(count, ContainerTestServices.Tracked.getDisposed());
    }

    //endregion

    //region 作用域内单例

    /**
     * 作用域内单例:同一作用域内是同一个实例且只创建一次
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_SameInstanceWithinScope(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.ScopedOnly.reset();

        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.ScopedOnly first = scope.getService(ContainerTestServices.ScopedOnly.class);
            ContainerTestServices.ScopedOnly second = scope.getService(ContainerTestServices.ScopedOnly.class);

            assertSame(first, second);
            assertEquals(1, ContainerTestServices.ScopedOnly.getCreated());
        }
    }

    /**
     * 作用域内单例:不同作用域各自持有自己的实例
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_DifferentScopesGetDifferentInstances(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.ScopedOnly.reset();

        try (ServiceScope first = container.createScope(); ServiceScope second = container.createScope()) {
            assertNotSame(first.getService(ContainerTestServices.ScopedOnly.class),
                    second.getService(ContainerTestServices.ScopedOnly.class));
            assertEquals(2, ContainerTestServices.ScopedOnly.getCreated());
        }
    }

    /**
     * 作用域内单例:从容器获取时归容器所有 作用域关闭不影响它
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_FromContainerOwnedByContainer(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
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
    }

    /**
     * 作用域内单例:单例依赖作用域内单例时获取失败 异常信息包含解析链
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_SingletonDependingOnScopedThrows(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> container.getService(ContainerTestServices.SingletonNeedsScoped.class));

        assertTrue(exception.getMessage().contains("单例服务不能依赖作用域内单例的服务"));
        assertTrue(exception.getMessage().contains("SingletonNeedsScoped"));
        assertTrue(exception.getMessage().contains("ScopedOnly"));
    }

    /**
     * 作用域内单例:随作用域释放 且先于它的多例依赖释放
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_DisposedAfterItsTransientDependency(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.DisposalLog.reset();

        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.ScopedWithTransient service =
                    scope.getService(ContainerTestServices.ScopedWithTransient.class);
            assertNotNull(service.getDependency());
        }

        //多例依赖先创建 后释放;作用域内单例后创建 先释放
        assertEquals("scoped,A", ContainerTestServices.DisposalLog.joined());
    }

    /**
     * 作用域内单例:获取全部实现时在同一作用域内复用同一批实例
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void scoped_GetServicesReusesInstancesWithinScope(EDataSource dataSource) {
        ServiceContainer container = getContainer(dataSource);
        ContainerTestServices.ScopedOnly.reset();

        try (ServiceScope scope = container.createScope()) {
            ContainerTestServices.ScopedOnly single = scope.getService(ContainerTestServices.ScopedOnly.class);
            List<ContainerTestServices.ScopedOnly> all = scope.getServices(ContainerTestServices.ScopedOnly.class);

            assertEquals(1, all.size());
            assertSame(single, all.get(0));
            assertSame(single, scope.getService(ContainerTestServices.ScopedOnly.class));
            assertEquals(1, ContainerTestServices.ScopedOnly.getCreated());
        }
    }

    //endregion
}
