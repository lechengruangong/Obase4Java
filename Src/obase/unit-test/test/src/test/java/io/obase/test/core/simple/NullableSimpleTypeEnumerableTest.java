package io.obase.test.core.simple;

import io.obase.core.expression.EPredicateType;
import io.obase.core.expression.PredicateCombiner;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.NullableJavaBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试简单类型的Enumerable扩展方法
 */
@ExtendWith(ConfigSetUp.class)
public class NullableSimpleTypeEnumerableTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(NullableJavaBean.class).delete(p -> true, NullableJavaBean.class);
            //添加新对象
            for (int i = 1; i < 21; i++) {
                var javaBean = new NullableJavaBean();
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
                javaBean.setLongNumber(Long.parseLong(String.valueOf(i)));
                javaBean.setByteNumber((byte) i);
                javaBean.setCharNumber('\u006A');
                javaBean.setFloatNumber((float) Math.pow(Math.PI, i));
                javaBean.setDoubleNumber(Math.pow(Math.PI, i));
                javaBean.setDate(LocalDate.now());
                javaBean.setTime(LocalTime.now());
                javaBean.setUuid(UUID.randomUUID());

                context.attach(javaBean);
            }

            //添加一个空对象
            var javaBean = new NullableJavaBean();
            javaBean.setIntNumber(21);
            context.attach(javaBean);

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
            context.createSet(NullableJavaBean.class).delete(p -> true, NullableJavaBean.class);
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
        var context = ContextUtils.createContext(dataSource);

        //无条件查询
        List<NullableJavaBean> list = context.createSet(NullableJavaBean.class).toList();
        //有21个
        assertEquals(21, list.size());

        //第一个有值
        assertNotNull(list.get(0).getBool());
        assertNotNull(list.get(0).getUuid());

        //最后一个没值
        assertNull(list.get(20).getDateTime());
        assertNull(list.get(20).getLongNumber());
    }

    /**
     * 测试可空对象的条件拼接
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void combinerPlusTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //拼接IntNumber == 1 && String == "1号字符串" && DoubleNumber > 1 && String.Contains("1号")
        var combiner = new PredicateCombiner<NullableJavaBean>();
        combiner.and(combiner.getWrapper().eq(NullableJavaBean::getIntNumber, 1)).and(NullableJavaBean::getString, EPredicateType.Equal, "1号字符串");
        combiner.and(combiner.getWrapper().gt(NullableJavaBean::getDoubleNumber, 1D)).and(NullableJavaBean::getString, EPredicateType.Contains, "1号");

        var bean = context.createSet(NullableJavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接Bool == true && DateTime <= Now && UUID != Random
        combiner = new PredicateCombiner<>();
        combiner.and(combiner.getWrapper().eq(NullableJavaBean::getBool, true)).and(NullableJavaBean::getDateTime, EPredicateType.LessThanOrEqual, LocalDateTime.now());
        combiner.and(combiner.getWrapper().ne(NullableJavaBean::getUuid, UUID.randomUUID()));

        bean = context.createSet(NullableJavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接DecimalNumber > 0 || DoubleNumber > 1 || String.StartWith("字符串") && FloatNumber != 0
        combiner = new PredicateCombiner<>();
        combiner.and(combiner.getWrapper().gt(NullableJavaBean::getDecimalNumber, new BigDecimal(0))).or(NullableJavaBean::getDoubleNumber, EPredicateType.GreaterThan, 1D);
        combiner.or(combiner.getWrapper().sw(NullableJavaBean::getString, "字符串")).and(NullableJavaBean::getFloatNumber, EPredicateType.NotEqual, 0F);

        bean = context.createSet(NullableJavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);
    }
}
