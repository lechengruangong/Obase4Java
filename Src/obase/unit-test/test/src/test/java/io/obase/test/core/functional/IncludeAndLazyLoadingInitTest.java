package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.Class;
import io.obase.test.domain.association.Student;
import io.obase.test.domain.association.Teacher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 包含和延迟加载初始化容器测试
 */
@ExtendWith(ConfigSetUp.class)
public class IncludeAndLazyLoadingInitTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(Class.class).delete(p -> p.getClassId() >= 0, Class.class);
            context.createSet(Student.class).delete(p -> p.getStudentId() >= 0, Student.class);

            //添加一个教师
            var teacher = new Teacher();
            teacher.setName("无通行证教师");

            context.attach(teacher);
            context.saveChanges();

            //添加班级和学生
            //学校的班级
            Class newClass = new Class();
            newClass.setName("初始化容器某某班");

            context.attach(newClass);

            context.saveChanges();

            //加入学生
            for (int i = 1; i < 6; i++) {
                var student = new Student();
                student.setClazz(newClass);
                student.setName("小" + i);
                context.attach(student);
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
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() >= 0, Teacher.class);
            context.createSet(Class.class).delete(p -> p.getClassId() >= 0, Class.class);
            context.createSet(Student.class).delete(p -> p.getStudentId() >= 0, Student.class);
        }
    }

    /**
     * 测试包含和延迟加载初始化容器
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //此教师无通行证
        Teacher queryTeacher = context.createSet(Teacher.class).findFirst(p -> p.getName() == "无通行证教师").orElse(null);
        //使用延迟加载进行加载 获得一个空容器
        assertNotNull(queryTeacher);
        assertNotNull(queryTeacher.getPassPaperList());
        assertEquals(0, queryTeacher.getPassPaperList().size());

        context = ContextUtils.createContext(dataSource);
        //使用Include进行加载 获得一个空容器
        queryTeacher = context.createSet(Teacher.class).include(p -> p.getPassPaperList()).findFirst(p -> p.getName() == "无通行证教师").orElse(null);

        assertNotNull(queryTeacher);
        assertNotNull(queryTeacher.getPassPaperList());
        assertEquals(0, queryTeacher.getPassPaperList().size());

        //删除
        context.remove(queryTeacher);
        context.saveChanges();

        //测试无延迟加载的关联
        context = ContextUtils.createContext(dataSource);

        var clazz = context.createSet(Class.class).findFirst().orElse(null);
        //没有包含Student 是空值
        assertNotNull(clazz);
        assertNull(clazz.getStudents());
        //没有包含ClassTeachers 是空值
        assertNotNull(clazz);
        assertNull(clazz.getClassTeachers());

        clazz = context.createSet(Class.class).include(p -> p.getStudents()).include(p -> p.getClassTeachers()).findFirst(p -> p.getName() == "初始化容器某某班").orElse(null);
        assertNotNull(clazz);
        //加载了Student 有值
        assertNotNull(clazz.getStudents());
        assertEquals(5, clazz.getStudents().size());
        //加载了ClassTeacher 没值
        assertNotNull(clazz.getClassTeachers());
        assertEquals(0, clazz.getClassTeachers().size());
    }
}
