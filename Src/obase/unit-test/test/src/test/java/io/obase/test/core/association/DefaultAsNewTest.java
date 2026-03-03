package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.defaultAsNew.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试关联端是否默认附加
 */
@ExtendWith(ConfigSetUp.class)
public class DefaultAsNewTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(DefaultNewClass.class).delete(p -> p.getClassId() > 0, DefaultNewClass.class);
            context.createSet(DefaultClass.class).delete(p -> p.getClassId() > 0, DefaultClass.class);
            context.createSet(DefaultStudent.class).delete(p -> p.getStudentId() > 0, DefaultStudent.class);
            context.createSet(DefaultSchool.class).delete(p -> p.getSchoolId() > 0, DefaultSchool.class);
            context.createSet(DefaultClassTeacher.class).delete(p -> p.getTeacherId() > 0 || p.getClassId() > 0, DefaultClassTeacher.class);
            context.createSet(DefaultTeacher.class).delete(p -> p.getTeacherId() > 0, DefaultTeacher.class);
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
            context.createSet(DefaultNewClass.class).delete(p -> p.getClassId() > 0, DefaultNewClass.class);
            context.createSet(DefaultClass.class).delete(p -> p.getClassId() > 0, DefaultClass.class);
            context.createSet(DefaultStudent.class).delete(p -> p.getStudentId() > 0, DefaultStudent.class);
            context.createSet(DefaultSchool.class).delete(p -> p.getSchoolId() > 0, DefaultSchool.class);
            context.createSet(DefaultClassTeacher.class).delete(p -> p.getTeacherId() > 0 || p.getClassId() > 0, DefaultClassTeacher.class);
            context.createSet(DefaultTeacher.class).delete(p -> p.getTeacherId() > 0, DefaultTeacher.class);
        }
    }

    /**
     * 新建关联端 且 关联表在左端 测试方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultAsNewImpLeftCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultNewClass = new DefaultNewClass();
        defaultNewClass.setName("默认创建新关联端班级");
        //保存
        context.attach(defaultNewClass);
        context.saveChanges();
        //新建学校
        var school = new DefaultSchool();
        school.setName("新学校1");
        defaultNewClass.setSchool(school);
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        var id = defaultNewClass.getClassId();
        //会作为新对象被保存
        var clazz = context.createSet(DefaultNewClass.class).include(DefaultNewClass::getSchool).findFirst(p -> p.getClassId() == id).orElse(null);

        assertNotNull(clazz);
        assertNotNull(clazz.getSchool());
        assertEquals("新学校1", clazz.getSchool().getName());
    }

    /**
     * 新建关联端 且 关联表在右端 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultAsNewImpRightCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultNewClass = new DefaultNewClass();
        defaultNewClass.setName("默认创建新关联端班级");
        //保存
        context.attach(defaultNewClass);
        context.saveChanges();
        //新建学生
        var student = new DefaultStudent();
        student.setName("新学生1");
        defaultNewClass.setStudents(new ArrayList<>(Collections.singleton(student)));
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        var id = defaultNewClass.getClassId();
        //会作为新对象被保存
        var clazz = context.createSet(DefaultNewClass.class).include(DefaultNewClass::getStudents).findFirst(p -> p.getClassId() == id).orElse(null);

        assertNotNull(clazz);
        assertNotNull(clazz.getStudents());
        assertEquals("新学生1", clazz.getStudents().get(0).getName());
    }

    /**
     * 新建关联端 且 独立关联表 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultAsNewIndependentCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultNewClass = new DefaultNewClass();
        defaultNewClass.setName("默认创建新关联端班级");
        //保存
        context.attach(defaultNewClass);
        context.saveChanges();
        //新建任课教师和教师
        var classTeacher = new DefaultNewClassTeacher();
        var teacher = new DefaultTeacher();
        teacher.setName("新教师1");
        classTeacher.setTeacher(teacher);
        classTeacher.setClassId(defaultNewClass.getClassId());
        classTeacher.setClazz(defaultNewClass);
        classTeacher.setIsManage(true);
        defaultNewClass.setClassTeachers(new ArrayList<>(Collections.singleton(classTeacher)));
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        var id = defaultNewClass.getClassId();
        //会作为新对象被保存
        var clazz = context.createSet(DefaultNewClass.class).include("ClassTeachers.Teacher").findFirst(p -> p.getClassId() == id).orElse(null);

        assertNotNull(clazz);
        assertNotNull(clazz.getClassTeachers());
        assertEquals("新教师1", clazz.getClassTeachers().get(0).getTeacher().getName());
    }

    /**
     * 不新建关联端 且 关联表在左端 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultImpLeftCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultClass = new DefaultClass();
        defaultClass.setName("默认不创建新关联端班级");
        context.attach(defaultClass);

        var school = new DefaultSchool();
        school.setName("新学校1");
        context.attach(school);
        context.saveChanges();

        //换一个上下文
        context = ContextUtils.createContext(dataSource);
        var id = defaultClass.getClassId();
        var clazz = context.createSet(DefaultClass.class).include(DefaultClass::getSchool).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        //再新建学校 复制之前的值
        var defaultSchool = new DefaultSchool();
        defaultSchool.setName(school.getName());
        defaultSchool.setSchoolId(school.getSchoolId());
        clazz.setSchool(defaultSchool);
        context.saveChanges();

        //会建立关联
        clazz = context.createSet(DefaultClass.class).include(DefaultClass::getSchool).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getSchool());
        assertEquals("新学校1", clazz.getSchool().getName());
        assertEquals(defaultSchool.getSchoolId(), clazz.getSchool().getSchoolId());
    }

    /**
     * 不新建关联端 且 关联表在右端 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultImpRightCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultClass = new DefaultClass();
        defaultClass.setName("默认不创建新关联端班级");
        context.attach(defaultClass);
        //新建一个学生
        var student = new DefaultStudent();
        student.setName("新学生1");
        context.createSet(DefaultStudent.class).attach(student);
        context.saveChanges();

        //换一个上下文
        context = ContextUtils.createContext(dataSource);
        var id = defaultClass.getClassId();
        var clazz = context.createSet(DefaultClass.class).include(DefaultClass::getStudents).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        //再新建学校 复制之前的值
        var defaultStudent = new DefaultStudent();
        defaultStudent.setName(student.getName());
        defaultStudent.setStudentId(student.getStudentId());
        clazz.setStudents(new ArrayList<>(Collections.singleton(defaultStudent)));
        context.saveChanges();

        //会建立关联
        clazz = context.createSet(DefaultClass.class).include(DefaultClass::getStudents).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getStudents());
        assertEquals("新学生1", clazz.getStudents().get(0).getName());
        assertEquals(defaultStudent.getStudentId(), clazz.getStudents().get(0).getStudentId());
    }

    /**
     * 不新建关联端 且 独立关联表 测试方法
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void defaultIndependentCreateAndQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //新建班级
        var defaultClass = new DefaultClass();
        defaultClass.setName("默认不创建新关联端班级");
        context.attach(defaultClass);
        //新建教师
        var teacher = new DefaultTeacher();
        teacher.setName("新教师1");
        context.createSet(DefaultTeacher.class).attach(teacher);
        context.saveChanges();

        //换一个上下文
        context = ContextUtils.createContext(dataSource);
        var id = defaultClass.getClassId();
        var clazz = context.createSet(DefaultClass.class).include(DefaultClass::getClassTeachers).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        //再新建教师 复制之前的值
        var defaultTeacher = new DefaultTeacher();
        defaultTeacher.setName(teacher.getName());
        defaultTeacher.setTeacherId(teacher.getTeacherId());
        var defaultClassTeacher = new DefaultClassTeacher();
        defaultClassTeacher.setClazz(clazz);
        defaultClassTeacher.setClassId(clazz.getClassId());
        defaultClassTeacher.setTeacherId(defaultTeacher.getTeacherId());
        defaultClassTeacher.setTeacher(defaultTeacher);
        defaultClassTeacher.setIsManage(true);

        clazz.setClassTeachers(new ArrayList<>(Collections.singleton(defaultClassTeacher)));
        context.saveChanges();

        //会建立关联
        clazz = context.createSet(DefaultClass.class).include(DefaultClass::getClassTeachers).findFirst(p -> p.getClassId() == id).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getClassTeachers());
        assertEquals(teacher.getTeacherId(), clazz.getClassTeachers().get(0).getTeacherId());
    }
}
