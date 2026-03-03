package io.obase.test.addon.annotation;

import io.obase.addon.test.domain.annotation.*;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 标注建模基础关联测试
 */
@ExtendWith(ConfigSetUp.class)
public class AnnotationAssociationTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);
            //清理可能的冗余数据
            context.createSet(AnnotationStudent.class).delete(p -> true, AnnotationStudent.class);
            context.createSet(AnnotationSchool.class).delete(p -> true, AnnotationSchool.class);
            context.createSet(AnnotationClass.class).delete(p -> true, AnnotationClass.class);
            context.createSet(AnnotationClassTeacher.class).delete(p -> true, AnnotationClassTeacher.class);
            context.createSet(AnnotationTeacher.class).delete(p -> true, AnnotationTeacher.class);

            //加入测试学校
            var newSchool = new AnnotationSchool();
            newSchool.setName("第X某某学校");
            newSchool.setSchoolType(ESchoolType.High);
            newSchool.setCreateTime(LocalDateTime.now());
            newSchool.setIsPrime(false);
            newSchool.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));

            //学校的班级
            var newClass = new AnnotationClass();
            newClass.setName("某某班");
            newClass.setSchool(newSchool);

            context.attach(newSchool);
            context.attach(newClass);
            //加入学生
            for (int i = 1; i < 6; i++) {
                var student = new AnnotationStudent();
                student.setName("小" + i);
                student.setClazz(newClass);
                student.setSchool(newSchool);

                context.attach(student);
            }

            context.saveChanges();

            //加入教师
            //一对多
            var teacher = new AnnotationTeacher();
            teacher.setName("某老师");
            teacher.setSchoolId(newSchool.getSchoolId());
            var classTeacher = new AnnotationClassTeacher();
            classTeacher.setClazz(newClass);
            classTeacher.setTeacher(teacher);
            List<String> subjects = new ArrayList<>();
            subjects.add("语文");
            subjects.add("数学");
            subjects.add("化学");
            classTeacher.setSubject(subjects);
            classTeacher.setIsManage(true);
            classTeacher.setIsSubstitute(false);

            var teachers = new ArrayList<AnnotationClassTeacher>();
            teachers.add(classTeacher);
            newClass.setClassTeachers(teachers);
            context.attach(teacher);
            context.attach(classTeacher);

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

            //清理可能的冗余数据
            context.createSet(AnnotationStudent.class).delete(p -> true, AnnotationStudent.class);
            context.createSet(AnnotationSchool.class).delete(p -> true, AnnotationSchool.class);
            context.createSet(AnnotationClass.class).delete(p -> true, AnnotationClass.class);
            context.createSet(AnnotationClassTeacher.class).delete(p -> true, AnnotationClassTeacher.class);
            context.createSet(AnnotationTeacher.class).delete(p -> true, AnnotationTeacher.class);
        }
    }

    /**
     * 简单的增删改查测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleCurdTest(EDataSource dataSource) {
        var context = ContextUtils.createAddonContext(dataSource);

        //查询所有的学生
        var students = context.createSet(AnnotationStudent.class).include(p -> p.getClazz()).include(p -> p.getSchool()).toList();
        //有5个
        assertNotNull(students);
        assertEquals(5, students.size());
        //每一个都不是空 且有班级和学校
        for (var student : students) {
            assertNotNull(student.getClazz());
            assertNotNull(student.getSchool());
        }

        //查询班级
        var classes = context.createSet(AnnotationClass.class).include(p -> p.getSchool()).include("ClassTeachers.Teacher.School").toList();

        //有1个
        assertNotNull(classes);
        assertEquals(1, classes.size());

        //班级的学生是延迟加载的 可以在访问后获取到
        var classStudents = classes.get(0).getStudents();
        //有5个学生
        assertNotNull(classStudents);
        assertEquals(5, classStudents.size());

        //班级的学校是Include的 可以直接获取
        var school = classes.get(0).getSchool();
        //学校不为空
        assertNotNull(school);

        //班级的任课教师是Include的 且加载到了教师->学校
        var classTeachers = classes.get(0).getClassTeachers();
        //不为空
        assertNotNull(classTeachers);
        assertEquals(1, classTeachers.size());
        //任课教师有教师和学校
        assertNotNull(classTeachers.get(0).getTeacher());
        assertNotNull(classTeachers.get(0).getTeacher().getSchool());

    }
}
