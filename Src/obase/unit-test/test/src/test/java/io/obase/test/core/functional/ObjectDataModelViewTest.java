package io.obase.test.core.functional;

import io.obase.core.odm.IntegrityCheckFailException;
import io.obase.core.odm.ObjectDataModelViewer;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.infrastructure.context.InvalidOdmValidator;
import io.obase.test.infrastructure.context.InvalidSodmValidator;
import io.obase.test.infrastructure.context.SimpleOdmValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /**
     * 测试ODM验证器 模型合法时返回模型的完整视图
     */
    @Test
    public void validatorTest() {
        //使用简单的ODM验证器验证核心模型
        var result = new SimpleOdmValidator().validate();

        //验证通过
        assertTrue(result.getIsValid());
        //返回的是对象数据模型的完整视图
        assertNotNull(result.getMessage());
        assertFalse(result.getMessage().isEmpty());
        assertTrue(result.getMessage().contains("本模型共包含"));
        //没有任何完整性检查错误信息
        assertFalse(result.getMessage().contains(IntegrityCheckFailException.ODM_MESSAGE_PREFIX));
        assertFalse(result.getMessage().contains(IntegrityCheckFailException.SODM_MESSAGE_PREFIX));
    }

    /**
     * 测试ODM验证器 ODM完整性检查未通过时生成带ODM前缀的错误信息
     */
    @Test
    public void odmErrorMessageTest() {
        //使用一个未配置主键的模型进行验证
        var result = new InvalidOdmValidator().validate();

        //验证未通过
        assertFalse(result.getIsValid());
        //错误信息带ODM前缀
        assertTrue(result.getMessage().contains(IntegrityCheckFailException.ODM_MESSAGE_PREFIX));
        //不含有SODM的错误信息
        assertFalse(result.getMessage().contains(IntegrityCheckFailException.SODM_MESSAGE_PREFIX));
        //按照类型分组输出 包含类型名称 错误个数和具体的错误信息
        assertTrue(result.getMessage().contains("NullableJavaBean"));
        assertTrue(result.getMessage().contains("未配置主键"));
        assertTrue(result.getMessage().contains("1. "));
    }

    /**
     * 测试ODM验证器 SODM完整性检查未通过时生成带SODM前缀的错误信息
     */
    @Test
    public void sodmErrorMessageTest() {
        //使用一个序列化模型构造器参数配置不全的模型进行验证
        var result = new InvalidSodmValidator().validate();

        //验证未通过
        assertFalse(result.getIsValid());
        //错误信息带SODM前缀
        assertTrue(result.getMessage().contains(IntegrityCheckFailException.SODM_MESSAGE_PREFIX));
        //不含有ODM的错误信息
        assertFalse(result.getMessage().contains(IntegrityCheckFailException.ODM_MESSAGE_PREFIX));
        //包含具体的错误信息
        assertTrue(result.getMessage().contains("构造器应有"));
    }
}
