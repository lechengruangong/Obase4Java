package io.obase.test.core.functional;

import io.obase.core.dependency.injection.ObaseDependencyInjection;
import io.obase.core.dependency.injection.ServiceContainerInstance;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.dependencyInjection.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 依赖注入测试
 * 本测试主要测试Obase的依赖注入功能 向容器注册的代码位于ConfigSetUp
 */
@ExtendWith(ConfigSetUp.class)
public class DependencyInjectionTest {

    /**
     * 初始化方法
     * ConfigSetUp依赖于此方法的调用后回调 测试类必须保留此方法 无初始化则不需要内容
     */
    @BeforeAll
    public static void beforeAll() {
        //无需初始化 一次性的初始化内容位于ConfigSetUp
    }

    /**
     * 测试单例的依赖注入
     *
     * @param dataSource 数据源类型
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testSingleton(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var builder = ObaseDependencyInjection.createBuilder(context.getClass());
        builder.addSingleton(ServiceSA.class);
        //重复创建会报不能重复创建的InvalidOperationException
        assertThrows(RuntimeException.class, builder::build);

        //获取容器
        var container = ServiceContainerInstance.getInstance().getServiceContainer(context.getClass());

        var sA = container.getService(ServiceSA.class);
        //sA 可以取出
        assertEquals(ServiceSA.class, sA.getClass());

        var sB = container.getService(ServiceSB.class);
        //记录一下sB的时间
        var dateTime = sB.getCreateTime();
        //是一样的
        sB = container.getService(ServiceSB.class);
        assertEquals(dateTime, sB.getCreateTime());

        var sC = container.getService(ServiceSC.class);
        //创建时间是固定值
        assertEquals(LocalDateTime.of(1999, 12, 31, 0, 0, 0), sC.getCreateTime());

        //IService同时注册了B和D 此时获取到的只有D
        var sD = container.getService(IServiceS.class);

        assertEquals(ServiceSD.class, sD.getClass());

        //可以获取List装载所有的IService
        var iS = container.getServices(IServiceS.class);

        //2个 按照注册顺序 第一个是ServiceSB 第二个是ServiceSD
        assertEquals(2, iS.size());
        assertEquals(ServiceSB.class, iS.get(0).getClass());
        assertEquals(ServiceSD.class, iS.get(1).getClass());

        //E创建需要依赖D
        var sE = container.getService(ServiceSE.class);
        //D已经注册了 可以创建出来
        assertNotNull(sE);

        //F 使用了DateTime作为参数 会报ArgumentException错误
        assertThrows(IllegalArgumentException.class, () -> container.getService(ServiceSF.class));

        //G 自定义的创建方法
        var sG = container.getService(ServiceSG.class);
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0, 0), sG.getCreateTime());

        //H 自定义的创建方法 且注册在接口下
        var sOH = container.getService(IServiceSO.class);
        assertTrue(sOH instanceof ServiceSH);
        assertEquals(LocalDateTime.of(1999, 1, 1, 0, 0, 0), sOH.createTime());
    }

    /**
     * 测试多例的依赖注入
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTransient(EDataSource dataSource) throws InterruptedException {
        var context = ContextUtils.createContext(dataSource);

        var builder = ObaseDependencyInjection.createBuilder(context.getClass());
        builder.addTransients(ServiceSA.class);
        //重复创建会报不能重复创建的InvalidOperationException
        assertThrows(RuntimeException.class, builder::build);

        //获取容器
        var container = ServiceContainerInstance.getInstance().getServiceContainer(context.getClass());

        var tA = container.getService(ServiceTA.class);
        //tA 可以取出
        assertEquals(ServiceTA.class, tA.getClass());

        var tB = container.getService(ServiceTB.class);
        //记录一下sB的时间
        var dateTime = tB.getCreateTime();
        //Local默认精度较低 此处休眠1毫秒
        Thread.sleep(1);
        //是不一样的
        tB = container.getService(ServiceTB.class);
        assertNotEquals(dateTime, tB.getCreateTime());

        var tC = container.getService(ServiceTC.class);
        //创建时间是固定值
        assertEquals(LocalDateTime.of(1999, 12, 31, 0, 0, 0), tC.getCreateTime());

        //IService同时注册了B和D 此时获取到的只有D
        var tD = container.getService(IServiceT.class);

        assertEquals(ServiceTD.class, tD.getClass());

        //可以获取List装载所有的IService
        var iT = container.getServices(IServiceT.class);

        //2个 按照注册顺序 第一个是ServiceSB 第二个是ServiceSD
        assertEquals(2, iT.size());
        assertEquals(ServiceTB.class, iT.get(0).getClass());
        assertEquals(ServiceTD.class, iT.get(1).getClass());

        //E创建需要依赖D
        var tE = container.getService(ServiceTE.class);
        //D已经注册了 可以创建出来
        assertNotNull(tE);

        //F 使用了DateTime作为参数 会报ArgumentException错误
        assertThrows(IllegalArgumentException.class, () -> container.getService(ServiceTF.class));

        //G 自定义的创建方法
        var tG = container.getService(ServiceTG.class);
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0, 0), tG.getCreateTime());

        //H 自定义的创建方法 且注册在接口下
        var tOH = container.getService(IServiceTO.class);
        assertTrue(tOH instanceof ServiceTH);
        assertEquals(LocalDateTime.of(1999, 1, 1, 0, 0, 0), tOH.createTime());
    }
}
