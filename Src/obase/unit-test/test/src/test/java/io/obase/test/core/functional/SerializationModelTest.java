package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.serialization.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 有序列化模型的序列划测试
 */
@ExtendWith(ConfigSetUp.class)
public class SerializationModelTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(Service.class).delete(p -> true, Service.class);

            //添加一个服务 Code为Simple
            var serviceSimple = new Service();
            serviceSimple.setCode("Simple");
            var route = new Route("*/Get", EAction.Pass);
            route.setInner(BigDecimal.valueOf(123456));
            route.setSort(0);
            route.setWeight(123.456);
            route.setEnabled(true);
            serviceSimple.setRoute(route);
            var route1 = new Route("*/Delete", EAction.Reject);
            route1.setInner(BigDecimal.valueOf(123));
            route1.setSort(1);
            route1.setWeight(123);
            route1.setEnabled(false);
            var route2 = new Route("*/Patch", EAction.Drop);
            route2.setInner(BigDecimal.valueOf(456));
            route2.setSort(2);
            route2.setWeight(456);
            route2.setEnabled(false);
            serviceSimple.setSubRoute(List.of(new Route[]{route1, route2}));
            var identity = new Identity(UUID.randomUUID(), LocalDateTime.now(), "Admin");
            identity.setVersion(1);
            identity.setSubVersion(2);
            serviceSimple.setIdentity(identity);
            var analyser = new AnalyserA(new AnalyserB(new AnalyserC(null)));
            analyser.setSubAnalysers(List.of(new AnalyserC(null)));
            serviceSimple.setAnalyser(analyser);

            //构造一组互相引用的组件
            var component1 = new Component();
            component1.setName("Component1");
            var component2 = new Component();
            component2.setName("Component2");
            var component3 = new Component();
            component3.setName("Component3");

            component1.setComponents(new Component[]{component2, component3});
            component2.setComponents(new Component[]{component1, component3});
            component3.setComponents(new Component[]{component1, component2});
            //将组件添加到服务中
            serviceSimple.setComponents(List.of(new Component[]{component1, component2, component3}));

            //附加
            context.attach(serviceSimple);

            //添加一个服务 Code为Nan
            var serviceNull = new Service();
            serviceNull.setCode("Nan");
            //附加
            context.attach(serviceNull);

            //保存
            context.saveChanges();
        }
    }

    /**
     * 销毁方法
     */
    @AfterAll
    public static void afterAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(Service.class).delete(p -> true, Service.class);
        }
    }

    /**
     * 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void queryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //查找对象Simple
        var service = context.createSet(Service.class).findFirst(p -> p.getCode().equals("Simple")).orElse(null);
        //可以查询到
        assertNotNull(service);
        //检查Route
        assertNotNull(service.getRoute());
        assertEquals(service.getRoute().getAction(), EAction.Pass);
        assertEquals(service.getRoute().getRule(), "*/Get");
        assertNull(service.getRoute().getPalaceHolder());
        assertTrue(service.getRoute().getEnabled());
        assertEquals(service.getRoute().getSort(), 0);
        assertEquals(service.getRoute().getWeight(), 123.456);
        assertEquals(service.getRoute().getInner(), BigDecimal.valueOf(123456));
        //检查SubRoute
        assertNotNull(service.getSubRoute());
        assertEquals(service.getSubRoute().size(), 2);
        assertEquals(service.getSubRoute().get(0).getAction(), EAction.Reject);
        assertEquals(service.getSubRoute().get(0).getRule(), "*/Delete");
        assertNull(service.getSubRoute().get(0).getPalaceHolder());
        assertFalse(service.getSubRoute().get(0).getEnabled());
        assertEquals(service.getSubRoute().get(0).getSort(), 1);
        assertEquals(service.getSubRoute().get(0).getWeight(), 123);
        assertEquals(service.getSubRoute().get(0).getInner(), BigDecimal.valueOf(123));
        assertEquals(service.getSubRoute().get(1).getAction(), EAction.Drop);
        assertEquals(service.getSubRoute().get(1).getRule(), "*/Patch");
        assertNull(service.getSubRoute().get(1).getPalaceHolder());
        assertEquals(service.getSubRoute().get(1).getSort(), 2);
        assertEquals(service.getSubRoute().get(1).getWeight(), 456);
        assertEquals(service.getSubRoute().get(1).getInner(), BigDecimal.valueOf(456));
        //检查Identity
        assertNotNull(service.getIdentity());
        assertNotEquals(service.getIdentity().getId(), new UUID(0, 0));
        assertNotEquals(service.getIdentity().getCreateTime(), LocalDateTime.now());
        assertEquals(service.getIdentity().getRole(), "Admin");
        assertEquals(service.getIdentity().getQueryTime(), service.getIdentity().getCreateTime());
        assertEquals(service.getIdentity().getVersion(), 0);
        assertEquals(service.getIdentity().getSubVersion(), 0);
        //检查Analyser
        assertNotNull(service.getAnalyser());
        assertEquals(service.getAnalyser().getName(), "AnalyserA");
        assertNotNull(service.getAnalyser().getSubAnalysers());
        assertEquals(service.getAnalyser().getSubAnalysers().size(), 1);
        assertEquals(service.getAnalyser().getSubAnalysers().get(0).getName(), "AnalyserC");
        assertNotNull(service.getAnalyser().getNext());
        assertEquals(service.getAnalyser().getNext().getName(), "AnalyserB");
        assertNotNull(service.getAnalyser().getNext().getNext());
        assertEquals(service.getAnalyser().getNext().getNext().getName(), "AnalyserC");
        assertNull(service.getAnalyser().getNext().getNext().getNext());
        //检查Component
        assertNotNull(service.getComponents());
        assertEquals(service.getComponents().size(), 3);
        assertEquals(service.getComponents().get(0).getName(), "Component1");
        assertEquals(service.getComponents().get(1).getName(), "Component2");
        assertEquals(service.getComponents().get(2).getName(), "Component3");
        assertNotNull(service.getComponents().get(0).getComponents());
        assertEquals(service.getComponents().get(0).getComponents().length, 2);
        assertEquals(service.getComponents().get(0).getComponents()[0].getName(), "Component2");
        assertEquals(service.getComponents().get(0).getComponents()[1].getName(), "Component3");
        assertNotNull(service.getComponents().get(1).getComponents());
        assertEquals(service.getComponents().get(1).getComponents().length, 2);
        assertEquals(service.getComponents().get(1).getComponents()[0].getName(), "Component1");
        assertEquals(service.getComponents().get(1).getComponents()[1].getName(), "Component3");
        assertNotNull(service.getComponents().get(2).getComponents());
        assertEquals(service.getComponents().get(2).getComponents().length, 2);
        assertEquals(service.getComponents().get(2).getComponents()[0].getName(), "Component1");
        assertEquals(service.getComponents().get(2).getComponents()[1].getName(), "Component2");


        //查找对象Nan
        service = context.createSet(Service.class).findFirst(p -> p.getCode().equals("Nan")).orElse(null);
        //可以查询到
        assertNotNull(service);
        //检查Route
        assertNull(service.getRoute());
        //检查SubRoute
        assertNull(service.getSubRoute());
        //检查Identity
        assertNull(service.getIdentity());
        //检查Analyser
        assertNull(service.getAnalyser());
        //检查Component
        assertNull(service.getComponents());
    }
}
