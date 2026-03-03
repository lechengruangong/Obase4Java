package io.obase.test.core.simple;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.JavaBeanWithConstructorArgs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试只有带参构造的简单类型
 */
@ExtendWith(ConfigSetUp.class)
public class SimpleTypeWithConstructorArgsEnumerableTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(JavaBeanWithConstructorArgs.class).delete(p -> p.getIntNumber() > 0, JavaBeanWithConstructorArgs.class);
            //添加新对象
            for (int i = 1; i < 21; i++) {
                String[] strings = new String[3];
                strings[0] = String.valueOf(i - 1);
                strings[1] = String.valueOf(i);
                strings[2] = String.valueOf(i + 1);
                JavaBeanWithConstructorArgs javaBean = new JavaBeanWithConstructorArgs(BigDecimal.valueOf(Math.pow(Math.PI, i)), LocalDateTime.now(), i + "号字符串", i % 2 == 0,
                        i, i, (byte) i, '\u006A', (float) Math.pow(Math.PI, i), Math.pow(Math.PI, i), LocalTime.now(), LocalDate.now());
                javaBean.setStrings(strings);

                context.createSet(JavaBeanWithConstructorArgs.class).attach(javaBean);
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
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(JavaBeanWithConstructorArgs.class).delete(p -> p.getIntNumber() > 0, JavaBeanWithConstructorArgs.class);
        }
    }

    /**
     * 测试简单查询方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void QueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //无条件查询
        var list = context.createSet(JavaBeanWithConstructorArgs.class).toList();
        //有20个
        assertEquals(20, list.size());
    }
}
