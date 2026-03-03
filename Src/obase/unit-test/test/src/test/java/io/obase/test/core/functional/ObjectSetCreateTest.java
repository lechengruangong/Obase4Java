package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.Class;
import io.obase.test.domain.association.ClassTeacher;
import io.obase.test.domain.association.Teacher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 使用ObjectSet创建对象测试
 */
@ExtendWith(ConfigSetUp.class)
public class ObjectSetCreateTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Class.class).delete(p -> p.getClassId() >= 0, Class.class);
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() >= 0, ClassTeacher.class);
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
            context.createSet(Class.class).delete(p -> p.getClassId() >= 0, Class.class);
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() >= 0, ClassTeacher.class);
        }
    }

    /**
     * 测试使用ObjectSet创建对象
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void createTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //创建一个班级和一个教师
        var clazz = new Class();
        clazz.setName("C班级");

        var teacher = new Teacher();
        teacher.setName("C教师");

        context.attach(clazz);
        context.attach(teacher);
        context.saveChanges();

        //创建一个显式关联对象 此种方式New的对象是域类对象 无法触发延迟加载
        var stringList = new ArrayList<String>();
        stringList.add("C课程");
        var classTeacher =
                new ClassTeacher(clazz.getClassId(), teacher.getTeacherId(), true, true, stringList);
        //关联端对象均为null
        assertNull(classTeacher.getClazz());
        assertNull(classTeacher.getTeacher());

        //使用Create方法创建 会根据定义的端冗余主键进行加载 并将创建的对象附加到上下文
        classTeacher = context.Create(ClassTeacher.class, clazz.getClassId(), teacher.getTeacherId(), true, true, stringList);
        //关联端对象均不为null
        assertNotNull(classTeacher.getClazz());
        assertNotNull(classTeacher.getTeacher());

        //此处已经将classTeacher附加 直接保存即可
        context.saveChanges();

        context.remove(clazz);
        context.remove(teacher);
        context.remove(classTeacher);

        context.saveChanges();
    }
}
