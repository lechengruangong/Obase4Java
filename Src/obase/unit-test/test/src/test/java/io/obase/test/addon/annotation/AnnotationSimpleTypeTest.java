package io.obase.test.addon.annotation;

import io.obase.addon.test.domain.annotation.AnnotationJavaBean;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 标注建模的简单类型测试
 */
@ExtendWith(ConfigSetUp.class)
public class AnnotationSimpleTypeTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);
            //删除冗余旧数据
            context.createSet(AnnotationJavaBean.class).delete(p -> true, AnnotationJavaBean.class);
            //新增对象
            for (int i = 1; i < 21; i++) {
                var javaBean = new AnnotationJavaBean();
                javaBean.setIntNumber(i);
                javaBean.setBool(i % 2 == 0);
                javaBean.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, i)));
                javaBean.setString(i + "号字符串");
                javaBean.setDateTime(LocalDateTime.now());

                context.attach(javaBean);
            }

            context.saveChanges();
        }
    }

    /**
     * 销毁方法
     */
    @AfterAll
    public static void afterAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);

            //删除冗余旧数据
            context.createSet(AnnotationJavaBean.class).delete(p -> true, AnnotationJavaBean.class);
        }
    }

    /**
     * 简单查询
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void queryTest(EDataSource dataSource) {
        var context = ContextUtils.createAddonContext(dataSource);

        //无条件查询
        var list = context.createSet(AnnotationJavaBean.class).toList();

        //有20个
        assertEquals(20, list.size());
    }
}
