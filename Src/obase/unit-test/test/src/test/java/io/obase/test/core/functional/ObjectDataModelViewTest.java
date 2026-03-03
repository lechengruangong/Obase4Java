package io.obase.test.core.functional;

import io.obase.core.odm.ObjectDataModelViewer;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 对象数据模型视图测试
 */
@ExtendWith(ConfigSetUp.class)
public class ObjectDataModelViewTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        //无需设置 对象数据模型视图测试不需要预置数据
    }

    /**
     * 销毁方法
     */
    @AfterAll
    public static void afterAll() {
        //无需清理 对象数据模型视图测试不需要清理数据
    }

    /**
     * 测试对象数据模型视
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //创建对象数据模型视图
        var view = ObjectDataModelViewer.getFullObjectDataModelMappingView(context);

        //验证视图是否正确
        assertNotNull(view.toString());

        //获取当前连接池的信息
        var statistics = ObaseConnectionPool.getInstance().getStatistics();
        assertNotNull(statistics);

        //获取当前连接池的完整信息
        var statisticsFully = ObaseConnectionPool.getInstance().getFullStatistics();
        assertNotNull(statisticsFully);
    }
}
