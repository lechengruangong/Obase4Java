package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.JavaBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 复用上下文测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiplexContextTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(JavaBean.class).delete(p -> p.getIntNumber() >= 0, JavaBean.class);

            for (int i = 1; i < 21; i++) {
                var javaBean = new JavaBean();
                javaBean.setIntNumber(i);
                javaBean.setBool(i % 2 == 0);
                javaBean.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, i)));
                javaBean.setString(i + "号字符串");
                String[] strings = new String[3];
                strings[0] = String.valueOf(i - 1);
                strings[1] = String.valueOf(i);
                strings[2] = String.valueOf(i + 1);
                javaBean.setStrings(strings);
                javaBean.setDateTime(LocalDateTime.now());
                javaBean.setLongNumber(i);
                javaBean.setByteNumber((byte) i);
                javaBean.setCharNumber('\u006A');
                javaBean.setFloatNumber((float) Math.pow(Math.PI, i));
                javaBean.setDoubleNumber(Math.pow(Math.PI, i));
                javaBean.setDate(LocalDate.now());
                javaBean.setTime(LocalTime.now());
                javaBean.setUuid(UUID.randomUUID());

                context.createSet(JavaBean.class).attach(javaBean);
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

            //清理可能的冗余数据
            context.createSet(JavaBean.class).delete(p -> p.getIntNumber() >= 0, JavaBean.class);
        }
    }

    /**
     * 测试复用上下文
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void multiplexTest(EDataSource dataSource) {
        var dbContext = ContextUtils.createContext(dataSource);
        //随意查询一部分
        var queryBeans = dbContext.createSet(JavaBean.class).filter(p -> !p.getBool()).toList();

        assertNotNull(queryBeans);
        assertEquals(10, queryBeans.size());

        //修改部分数据
        for (int i = 0; i < queryBeans.size(); i++) {
            if (i % 2 == 0) {
                queryBeans.get(i).setString(queryBeans.get(i).getString() + (i++));
            }
        }

        dbContext.saveChanges();

        //查出另外一部分数据 修改
        queryBeans = dbContext.createSet(JavaBean.class).filter(p -> !p.getBool() && p.getString().endsWith("2")).toList();
        assertNotNull(queryBeans);
        assertEquals(1, queryBeans.size());

        for (var bean : queryBeans) {
            bean.setDoubleNumber(bean.getDoubleNumber() + 1);
        }

        dbContext.saveChanges();
        //查询所有数据
        queryBeans = dbContext.createSet(JavaBean.class).toList();
        assertNotNull(queryBeans);
        assertEquals(20, queryBeans.size());
    }
}
