package io.obase.test.addon.logicDeletion;

import io.obase.addon.test.domain.logical.deletion.LogicDeletionAnnotation;
import io.obase.logical.deletion.LogicDeletionExtensions;
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
 * 标注配置的逻辑删除(有定义的字段)测试
 */
@ExtendWith(ConfigSetUp.class)
public class LogicDeletionAnnotationTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);

            //销毁可能的冗余数据
            context.createSet(LogicDeletionAnnotation.class).delete(p -> p.getIntNumber() > 0, LogicDeletionAnnotation.class);

            //添加新对象 一半删除一半没删除
            for (var i = 1; i < 21; i++) {
                var logicDeletion = new LogicDeletionAnnotation();
                logicDeletion.setBool(i % 2 == 0);
                logicDeletion.setDateTime(LocalDateTime.now());
                logicDeletion.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, i)));
                logicDeletion.setIntNumber(i);
                logicDeletion.setString(i + "号字符串");
                context.attach(logicDeletion);
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

            //销毁可能的冗余数据
            context.createSet(LogicDeletionAnnotation.class).delete(p -> p.getIntNumber() > 0, LogicDeletionAnnotation.class);
        }
    }

    /**
     * 简单的增删改查测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void queryTest(EDataSource dataSource) {
        var context = ContextUtils.createAddonContext(dataSource);

        //新对象直接删除
        var newObject = new LogicDeletionAnnotation();
        newObject.setBool(false);
        newObject.setDateTime(LocalDateTime.now());
        newObject.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, 0)));
        newObject.setIntNumber(0);
        newObject.setString("0号字符串");
        LogicDeletionExtensions.removeLogically(context.createSet(LogicDeletionAnnotation.class), newObject);

        context = ContextUtils.createAddonContext(dataSource);
        //无条件查询
        var list = context.createSet(LogicDeletionAnnotation.class).toList();

        //有10个
        assertEquals(10, list.size());

        //逻辑删除其中部分
        for (var logicDeletion : list) {
            //逻辑删除所有带1的字符串值 包含1,10,11,13,15,17,19
            if (logicDeletion.getString().contains("1")) {
                LogicDeletionExtensions.removeLogically(context.createSet(LogicDeletionAnnotation.class), logicDeletion);
            }
        }
        context.saveChanges();

        //查询
        list = context.createSet(LogicDeletionAnnotation.class).toList();

        //有4个
        assertEquals(4, list.size());

        //测试直接逻辑删除
        LogicDeletionExtensions.deleteLogically(context.createSet(LogicDeletionAnnotation.class), p -> p.getIntNumber() <= 5, LogicDeletionAnnotation.class);

        //查询
        list = context.createSet(LogicDeletionAnnotation.class).toList();

        //有2个
        assertEquals(2, list.size());

        //测试恢复
        LogicDeletionExtensions.recoveryLogically(context.createSet(LogicDeletionAnnotation.class), p -> p.getIntNumber() >= 0, LogicDeletionAnnotation.class);

        //查询
        list = context.createSet(LogicDeletionAnnotation.class).toList();

        //有20个
        assertEquals(20, list.size());
    }
}
