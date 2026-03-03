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

/**
 * 测试旧对象主动附加
 */
@ExtendWith(ConfigSetUp.class)
public class OldObjectAttachTest {

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
     * 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //一个新的对象
        var newObj = new JavaBean();
        newObj.setIntNumber(21);
        newObj.setBool(false);
        newObj.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, -2)));
        newObj.setString(21 + "号字符串");
        String[] strings = new String[3];
        strings[0] = String.valueOf(20);
        strings[1] = String.valueOf(21);
        strings[2] = String.valueOf(22);
        newObj.setStrings(strings);
        newObj.setDateTime(LocalDateTime.now());
        newObj.setLongNumber(21);
        newObj.setByteNumber((byte) 21);
        newObj.setCharNumber('\u006A');
        newObj.setFloatNumber((float) Math.pow(Math.PI, 2));
        newObj.setDoubleNumber(Math.pow(Math.PI, 2));
        newObj.setDate(LocalDate.now());
        newObj.setTime(LocalTime.now());
        //一个旧的对象
        var oldObj = context.createSet(JavaBean.class).findLast().orElse(null);
        //都附加
        context.createSet(JavaBean.class).attach(newObj);
        context.createSet(JavaBean.class).attach(oldObj);
        //保存
        context.saveChanges();
        //因为旧对象已经存在，所以不会插入新的对象
        var count = Math.toIntExact(context.createSet(JavaBean.class).count());
        //只新增了一个
        assertEquals(21, count);
    }
}
