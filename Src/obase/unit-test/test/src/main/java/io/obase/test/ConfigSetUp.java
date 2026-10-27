package io.obase.test;

import io.obase.core.dependency.injection.ObaseDependencyInjection;
import io.obase.core.mapping.pipeline.IChangeNoticeSender;
import io.obase.multi.tenant.ITenantIdReader;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.connectionpool.IObaseConnectionPoolConfiguration;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.dependencyInjection.*;
import io.obase.test.infrastructure.ObasePreHeater;
import io.obase.test.infrastructure.configuration.ObaseConnectionPoolConfiguration;
import io.obase.test.service.MessageSender;
import io.obase.test.service.TenantIdReader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.LocalDateTime;

/**
 * 配置的设置类
 */
public class ConfigSetUp implements BeforeAllCallback {

    /**
     * 静态标志位：确保初始化只执行一次
     */
    private static boolean isInitialized = false;

    /**
     * 此方法仅在所有测试运行前执行一次 在此方法中可以进行一些全局的配置
     * 首先TestCaseSourceConfigurationManager触发RelationshipDataBaseConfigurationManager的构造函数读取测试配置文件
     * 之后对Obase进行依赖注入并且调用Obase的预热器
     */
    public static void globalSetUp() {

        //根据测试配置输出当前的数据源
        for (EDataSource dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            //注入单例 用于单例测试
            var builder = ObaseDependencyInjection.createBuilder(TestCaseSourceConfigurationManager.getDataSourceContextType(dataSource));
            builder.addSingleton(ServiceSA.class).addSingleton(ServiceSB.class, ServiceSB.class)
                    .addSingleton(IServiceS.class, ServiceSB.class).addSingleton(ServiceSC.class)
                    .addSingleton(IServiceS.class, ServiceSD.class).addSingleton(ServiceSD.class)
                    .addSingleton(ServiceSE.class).addSingleton(ServiceSF.class)
                    .addSingleton(ServiceSG.class, container -> new ServiceSG(LocalDateTime.of(2000, 1, 1, 0, 0, 0)))
                    .addSingleton(IServiceSO.class, ServiceSH.class, container -> new ServiceSH(LocalDateTime.of(1999, 1, 1, 0, 0, 0)));
            //注入多例 用于多例测试
            builder.addTransient(ServiceTA.class).addTransient(ServiceTB.class, ServiceTB.class)
                    .addTransient(IServiceT.class, ServiceTB.class).addTransient(ServiceTC.class)
                    .addTransient(IServiceT.class, ServiceTD.class).addTransient(ServiceTD.class)
                    .addTransient(ServiceTE.class).addTransient(ServiceTF.class)
                    .addTransient(ServiceTG.class, container -> new ServiceTG(LocalDateTime.of(2000, 1, 1, 0, 0, 0)))
                    .addTransient(IServiceTO.class, ServiceTH.class, container -> new ServiceTH(LocalDateTime.of(1999, 1, 1, 0, 0, 0)));

            //注入消息发送器 用于对象变更通知
            builder.addSingleton(IChangeNoticeSender.class, MessageSender.class);
            //注入日志 用于预热器输出 Java不需要 只需要引用即可
            //依赖注入连接池配置 可以在日志中看到相关的更改
            builder.addSingleton(IObaseConnectionPoolConfiguration.class, ObaseConnectionPoolConfiguration.class, o ->
                    new ObaseConnectionPoolConfiguration(dataSource + " ConnectionPool"));

            //依赖注入容器与作用域的测试用服务 供ServiceContainerTest使用
            builder.addSingleton(ContainerTestServices.IService.class, ContainerTestServices.ServiceA.class)
                    .addSingleton(ContainerTestServices.IService.class, ContainerTestServices.ServiceB.class)//同一服务类型的多个实现
                    .addSingleton(ContainerTestServices.Dependency.class)
                    .addSingleton(ContainerTestServices.Dependency.class)//重复注册 只保留一条
                    .addSingleton(ContainerTestServices.NeedsDependency.class)
                    .addSingleton(ContainerTestServices.NeedsUnregistered.class)//构造函数参数未注册 获取时才会失败
                    .addSingleton(ContainerTestServices.CyclicA.class)
                    .addSingleton(ContainerTestServices.CyclicB.class)//循环依赖 获取时才会失败
                    .addSingleton(ContainerTestServices.SlowSingleton.class)
                    .addSingleton(ContainerTestServices.IFactoryOnly.class, container -> new ContainerTestServices.FactoryOnly())
                    .addTransient(ContainerTestServices.IRepository.class, ContainerTestServices.Repository.class)
                    .addTransient(ContainerTestServices.Tracked.class)
                    .addTransient(ContainerTestServices.LoggedA.class)
                    .addTransient(ContainerTestServices.LoggedB.class)
                    .addTransient(ContainerTestServices.OuterItem.class)
                    .addTransient(ContainerTestServices.InnerItem.class)
                    .addTransient(ContainerTestServices.GrandchildItem.class)
                    .addScoped(ContainerTestServices.ScopedOnly.class)
                    .addScoped(ContainerTestServices.ScopedDisposable.class)
                    .addScoped(ContainerTestServices.ScopedWithTransient.class)
                    .addSingleton(ContainerTestServices.SingletonNeedsScoped.class);//获取时应当报错

            //建造依赖注入容器 结束依赖注入的配置
            builder.build();

            //创建插件的依赖注入容器
            var addonBuilder =
                    ObaseDependencyInjection.createBuilder(
                            TestCaseSourceConfigurationManager.getDataSourceAddonContextType(dataSource));
            //注入插件的服务 多租户ID读取器
            addonBuilder.addSingleton(ITenantIdReader.class, TenantIdReader.class);
            //建造依赖注入容器 结束依赖注入的配置
            addonBuilder.build();

            //预热器
            var preHeater = new ObasePreHeater();

            //此处为普通的对象上下文
            var context = ContextUtils.createContext(dataSource);
            //预热普通上下文 会在日志中输出预热的结果
            preHeater.preHeat(context);

            //此处为普通的对象上下文
            var addonContext = ContextUtils.createAddonContext(dataSource);
            //预热插件上下文 会在日志中输出预热的结果
            preHeater.preHeat(addonContext);
        }
    }

    /**
     * 实现beforeAll方法
     *
     * @param extensionContext 上下文
     */
    @Override
    public synchronized void beforeAll(ExtensionContext extensionContext) {
        // 仅当未初始化过时执行
        if (!isInitialized) {
            // 调用初始化方法
            globalSetUp();
            isInitialized = true;
        }
    }
}
