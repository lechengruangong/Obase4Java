package io.obase.test.addon.annotation;

import io.obase.addon.test.domain.annotation.AnnotationJavaBeanWithCustomAttribute;
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
 * 标注建模简单类型但包含自定义属性测试
 */
@ExtendWith(ConfigSetUp.class)
public class AnnotationSimpleTypeWithCustomAttributesTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);

            //删除冗余旧数据
            context.createSet(AnnotationJavaBeanWithCustomAttribute.class).delete(p -> true, AnnotationJavaBeanWithCustomAttribute.class);
            //新增对象
            for (int i = 1; i < 21; i++) {
                String[] strings = new String[3];
                strings[0] = String.valueOf(i - 1);
                strings[1] = String.valueOf(i);
                strings[2] = String.valueOf(i + 1);

                var javaBean = new AnnotationJavaBeanWithCustomAttribute(i, BigDecimal.valueOf(Math.pow(Math.PI, i)), LocalDateTime.now(), i + "号字符串", i % 2 == 0, strings);

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
            context.createSet(AnnotationJavaBeanWithCustomAttribute.class).delete(p -> true, AnnotationJavaBeanWithCustomAttribute.class);
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
        var list = context.createSet(AnnotationJavaBeanWithCustomAttribute.class).toList();

        //有20个
        assertEquals(20, list.size());
    }
}
