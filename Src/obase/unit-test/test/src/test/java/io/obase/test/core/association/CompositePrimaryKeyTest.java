package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.EPassPaperType;
import io.obase.test.domain.association.PassPaper;
import io.obase.test.domain.association.Teacher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 联合主键关联测试
 */
@ExtendWith(ConfigSetUp.class)
public class CompositePrimaryKeyTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(PassPaper.class).delete(p -> p.getTeacherId() >= 0, PassPaper.class);
            //加入测试的联合主键老师
            var teacher = new Teacher();
            teacher.setName("联合主键老师");
            context.attach(teacher);
            context.saveChanges();
            //加入教师的通行证
            var passPaper1 = new PassPaper(teacher.getTeacherId(), EPassPaperType.A);
            var passPaper2 = new PassPaper(teacher.getTeacherId(), EPassPaperType.B);

            passPaper1.setMemo("备注1");
            passPaper2.setMemo("备注2");

            context.attach(passPaper1);
            context.attach(passPaper2);

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
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(PassPaper.class).delete(p -> p.getTeacherId() >= 0, PassPaper.class);
        }
    }

    /**
     * 联合主键测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询教师
        var qTeacher = context.createSet(Teacher.class).findFirst(p -> p.getTeacherId() > 0).orElse(null);
        //验证教师和通行证 此处通行证是延迟加载的
        assertNotNull(qTeacher);
        assertNotNull(qTeacher.getPassPaperList());
        assertEquals(2, qTeacher.getPassPaperList().length);


        //修改通行证
        qTeacher.getPassPaperList()[0].setMemo("修改后的备注1");
        qTeacher.getPassPaperList()[1].setMemo("修改后的备注2");
        //保存
        context.saveChanges();

        //查出来 验证修改和包含加载
        context = ContextUtils.createContext(dataSource);
        qTeacher = context.createSet(Teacher.class).include(p -> p.getPassPaperList()).findFirst(p -> p.getTeacherId() > 0).orElse(null);
        //验证
        assertNotNull(qTeacher);
        assertNotNull(qTeacher.getPassPaperList());
        assertEquals(2, qTeacher.getPassPaperList().length);
        assertEquals("修改后的备注1", qTeacher.getPassPaperList()[0].getMemo());
        assertEquals("修改后的备注2", qTeacher.getPassPaperList()[1].getMemo());

        //查询通行证
        context = ContextUtils.createContext(dataSource);
        var qPassPaper = context.createSet(PassPaper.class).toList();
        //验证
        assertNotNull(qPassPaper);
        assertEquals(2, qPassPaper.size());
        assertEquals(qPassPaper.get(0).getTeacher().getTeacherId(), qPassPaper.get(1).getTeacher().getTeacherId());

        //查询通行证并且使用Include加载教师
        context = ContextUtils.createContext(dataSource);
        qTeacher = context.createSet(Teacher.class).include(p -> p.getPassPaperList()).findFirst(p -> p.getTeacherId() > 0).orElse(null);
        //验证
        assertNotNull(qTeacher);
        assertNotNull(qTeacher.getPassPaperList());
        assertEquals(2, qTeacher.getPassPaperList().length);

        //删除通行证和教师
        context.remove(qTeacher);
        context.remove(qTeacher.getPassPaperList()[0]);
        context.remove(qTeacher.getPassPaperList()[0]);

        context.saveChanges();
    }
}
