package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.SqlParameterizedView;
import io.obase.providers.sql.sqlobject.ChangeSql;
import io.obase.providers.sql.sqlobject.QuerySql;
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
 * QuerySqlParameterView查看执行的SQL语句测试
 */
@ExtendWith(ConfigSetUp.class)
public class SqlParameterViewTest {

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

        //注册一个映射模块
        context.registerModule((savingPipeline, deletingPipeline, queryPipeline, directlyChangingPipeline, objectContext) -> {
            //查询sql执行前
            queryPipeline.getIQueryPipelinePreExecuteCommand().addListener(eventObject -> {
                var obj = eventObject.getContext().getCommand();
                assertNotNull(obj);
                var view = SqlParameterizedView.getSqlParameterizedView((QuerySql) obj, dataSource);
                assertNotNull(view);
                assertNotNull(view.getSimpleSqlString());
            });
            //保存(Update/Insert)Sql执行前
            savingPipeline.getSavingPreExecuteCommand().addListener(eventObject -> {
                var obj = eventObject.getCommand();
                assertNotNull(obj);
                var view = SqlParameterizedView.getSqlParameterizedView((ChangeSql) obj, dataSource);
                assertNotNull(view);
                assertNotNull(view.getSimpleSqlString());
            });
            //标记删除Sql执行前
            deletingPipeline.getDeletingPreExecuteCommand().addListener(eventObject -> {
                var obj = eventObject.getCommand();
                assertNotNull(obj);
                var view = SqlParameterizedView.getSqlParameterizedView((ChangeSql) obj, dataSource);
                assertNotNull(view);
                assertNotNull(view.getSimpleSqlString());
            });
            //直接修改Sql执行前
            directlyChangingPipeline.getDirectlyChangingPreExecuteCommand().addListener(eventObject -> {
                var obj = eventObject.getCommand();
                assertNotNull(obj);
                var view = SqlParameterizedView.getSqlParameterizedView((ChangeSql) obj, dataSource);
                assertNotNull(view);
                assertNotNull(view.getSimpleSqlString());
            });

        });

        //查看查询管道SQL
        var date = LocalDateTime.now();
        //测试时间条件
        var list = context.createSet(JavaBean.class)
                .filter(p -> p.getDateTime().isAfter(date)).toList();

        //有0个
        assertEquals(0, list.size());

        double local = 987D;

        //复杂条件
        list = context.createSet(JavaBean.class)
                .filter(p -> p.getIntNumber() > 10 && p.getDoubleNumber() > local).toList();

        //有10个
        assertEquals(10, list.size());

        //几种布尔值的查询
        list = context.createSet(JavaBean.class).filter(p -> !p.getBool()).toList();

        assertEquals(10, list.size());

        list = context.createSet(JavaBean.class)
                .filter(p -> !"".equals(p.getString())).toList();

        //有20个
        assertEquals(20, list.size());

        String empty = "";

        list = context.createSet(JavaBean.class)
                .filter(p -> !p.getString().equals(empty)).toList();

        //有20个
        assertEquals(20, list.size());

        //查看修改管道Sql
        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 1).orElse(null);
        assertNotNull(bean);
        bean.setString("123");
        context.saveChanges();

        //删除管道Sql
        context.remove(bean);
        context.saveChanges();

        //就地删除管道Sql
        context.createSet(JavaBean.class).delete(p -> true, JavaBean.class);
    }
}
