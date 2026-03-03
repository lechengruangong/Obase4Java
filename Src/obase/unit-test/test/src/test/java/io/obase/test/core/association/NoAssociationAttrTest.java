package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.ClassTeacher;
import io.obase.test.domain.association.ESchoolType;
import io.obase.test.domain.association.noAssociationExtAttr.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 不定义关联对象冗余属性测试
 */
@ExtendWith(ConfigSetUp.class)
public class NoAssociationAttrTest {
    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(NoAssociationExtAttrClass.class).delete(p -> p.getClassId() > 0, NoAssociationExtAttrClass.class);
            context.createSet(NoAssociationExtAttrSchool.class).delete(p -> p.getSchoolId() > 0, NoAssociationExtAttrSchool.class);
            context.createSet(NoAssociationExtAttrStudent.class).delete(p -> p.getStudentId() > 0, NoAssociationExtAttrStudent.class);
            context.createSet(NoAssociationExtAttrTeacher.class).delete(p -> p.getTeacherId() > 0, NoAssociationExtAttrTeacher.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() > 0 || p.getTeacherId() > 0, ClassTeacher.class);

            //加入测试学校
            var newSchool = new NoAssociationExtAttrSchool();
            newSchool.setName("不定义关联对象冗余属性的第X某某学校");
            newSchool.setSchoolType(ESchoolType.High);
            newSchool.setCreateTime(LocalDateTime.now());
            newSchool.setIsPrime(false);
            newSchool.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));

            //学校的班级
            var newClass = new NoAssociationExtAttrClass();
            newClass.setName("不定义关联对象冗余属性的某某班");
            newClass.setSchool(newSchool);

            context.createSet(NoAssociationExtAttrSchool.class).attach(newSchool);
            context.createSet(NoAssociationExtAttrClass.class).attach(newClass);

            //加入学生
            for (int i = 1; i < 3; i++) {
                var student = new NoAssociationExtAttrStudent();
                student.setName("不定义关联对象冗余属性的小" + i);
                student.setClazz(newClass);

                context.createSet(NoAssociationExtAttrStudent.class).attach(student);
            }

            //加入教师和班级任课教师
            var teacher = new NoAssociationExtAttrTeacher();
            teacher.setName("不定义关联对象冗余属性的某老师");
            var classTeacher = new NoAssociationExtAttrClassTeacher();
            var subjects = new ArrayList<String>();
            subjects.add("语文");
            subjects.add("数学");
            subjects.add("化学");
            classTeacher.setSubject(subjects);
            classTeacher.setClazz(newClass);
            classTeacher.setTeacher(teacher);
            classTeacher.setIsManage(true);
            classTeacher.setIsSubstitute(false);
            //设置班级任课教师
            var teachers = new ArrayList<NoAssociationExtAttrClassTeacher>();
            teachers.add(classTeacher);
            newClass.setClassTeachers(teachers);
            context.createSet(NoAssociationExtAttrTeacher.class).attach(teacher);
            //保存
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
            context.createSet(NoAssociationExtAttrClass.class).delete(p -> p.getClassId() > 0, NoAssociationExtAttrClass.class);
            context.createSet(NoAssociationExtAttrSchool.class).delete(p -> p.getSchoolId() > 0, NoAssociationExtAttrSchool.class);
            context.createSet(NoAssociationExtAttrStudent.class).delete(p -> p.getStudentId() > 0, NoAssociationExtAttrStudent.class);
            context.createSet(NoAssociationExtAttrTeacher.class).delete(p -> p.getTeacherId() > 0, NoAssociationExtAttrTeacher.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() > 0 || p.getTeacherId() > 0, ClassTeacher.class);
        }
    }

    /**
     * 测试方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //查询学校
        var school = context.createSet(NoAssociationExtAttrSchool.class).findFirst(p -> p.getSchoolId() > 0).orElse(null);
        assertNotNull(school);
        //查询班级  延迟加载学校 学生 任课教师
        var clazz = context.createSet(NoAssociationExtAttrClass.class).findFirst(p -> p.getClassId() > 0).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getSchool());
        assertNotNull(clazz.getStudents());
        assertNotNull(clazz.getClassTeachers());

        //查询班级 Include学校 学生 任课教师
        context = ContextUtils.createContext(dataSource);
        clazz = context.createSet(NoAssociationExtAttrClass.class).include(NoAssociationExtAttrClass::getSchool).include(NoAssociationExtAttrClass::getStudents)
                .include(NoAssociationExtAttrClass::getClassTeachers).findFirst(p -> p.getClassId() > 0).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getSchool());
        assertNotNull(clazz.getStudents());
        assertNotNull(clazz.getClassTeachers());

        context = ContextUtils.createContext(dataSource);
        //查询班级 Include加载任课教师.教师
        clazz = context.createSet(NoAssociationExtAttrClass.class).include("ClassTeachers.Teacher").findFirst(p -> p.getClassId() > 0).orElse(null);
        assertNotNull(clazz);
        assertNotNull(clazz.getStudents());
        assertNotNull(clazz.getClassTeachers().get(0).getTeacher());

        //移除测试
        context = ContextUtils.createContext(dataSource);

        school = context.createSet(NoAssociationExtAttrSchool.class).findFirst(p -> p.getSchoolId() > 0).orElse(null);
        context.createSet(NoAssociationExtAttrSchool.class).remove(school);
        clazz = context.createSet(NoAssociationExtAttrClass.class).findFirst(p -> p.getClassId() > 0).orElse(null);
        context.createSet(NoAssociationExtAttrClass.class).remove(clazz);
    }
}
