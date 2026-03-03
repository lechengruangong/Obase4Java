package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.Class;
import io.obase.test.domain.association.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 关联的更新和删除测试
 */
@ExtendWith(ConfigSetUp.class)
public class AssociationUpdateAndDeleteTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(School.class).delete(p -> true, School.class);
            context.createSet(Class.class).delete(p -> true, Class.class);

            //添加新对象
            for (var i = 1; i < 5; i++) {
                var school = new School();
                school.setCreateTime(LocalDateTime.now());
                school.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
                school.setIsPrime(i % 2 == 0);
                school.setName("第" + i + "某某学校");
                school.setSchoolType(ESchoolType.Junior);
                context.attach(school);

                if (i == 1) {
                    //学校的班级 只有一个
                    var newClass = new Class();
                    newClass.setName("某某班");
                    newClass.setSchool(school);
                    context.attach(newClass);
                }
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
            //销毁所有旧对象
            context.createSet(School.class).delete(p -> true, School.class);
            context.createSet(Class.class).delete(p -> true, Class.class);
        }
    }

    /**
     * 测试修改和标记删除方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void updateAndRemove(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查找一组对象
        var list = context.createSet(Class.class).filter(p -> p.getClassId() > 0).include(Class::getSchool).toList();
        //1个班级 一个学校
        assertEquals(1, list.size());
        assertNotNull(list.get(0).getSchool());

        //测试修改学校的名称和班级的名称
        //班级是主查询的 学校是一并加载的
        list.get(0).setName("新某某班");
        list.get(0).getSchool().setName("新第X某某学校");
        //保存
        context.saveChanges();

        //查出来
        context = ContextUtils.createContext(dataSource);
        var classId = list.get(0).getClassId();
        //查询修改的学校和班级
        var cla = context.createSet(Class.class).include(Class::getSchool).findFirst(p -> p.getClassId() == classId).orElse(null);

        //是修改后的值
        assertNotNull(cla);
        assertEquals("新某某班", cla.getName());
        assertNotNull(cla.getSchool());
        assertEquals("新第X某某学校", cla.getSchool().getName());

        //修改这次查出来的学校的名称
        cla.getSchool().setName("新第X某某学校-1");
        //标记删除班级
        context.remove(cla);
        //保存
        context.saveChanges();
        //多次保存
        context.saveChanges();

        //查出来
        context = ContextUtils.createContext(dataSource);
        var schoolId = list.get(0).getSchoolId();
        //是修改后的值
        var school = context.createSet(School.class).findFirst(p -> p.getSchoolId() == schoolId).orElse(null);
        assertNotNull(school);
        assertEquals("新第X某某学校-1", school.getName());

        //不修改 直接保存
        context.saveChanges();

        var exist = context.createSet(Class.class).anyMatch(p -> p.getClassId() == classId);
        //不存在此对象
        assertFalse(exist);
    }

    /**
     * 测试直接移除和直接修改方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void deleteAndDirectChangeTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //添加一个对象
        var school = new School();
        school.setCreateTime(LocalDateTime.now());
        school.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
        school.setIsPrime(false);
        school.setName("第X某某学校");
        school.setSchoolType(ESchoolType.High);
        context.attach(school);

        context.attach(school);
        //保存
        context.saveChanges();

        //查出来
        var schoolId = school.getSchoolId();

        school = context.createSet(School.class).findFirst(p -> p.getSchoolId() == schoolId).orElse(null);
        //存在此对象
        assertNotNull(school);

        //就地修改
        var map = new HashMap<String, Object>();
        map.put("Name", "新第X某某学校");
        var result = context.createSet(School.class).setAttributes(map, p -> p.getSchoolId() == schoolId, School.class);

        //查出来
        context = ContextUtils.createContext(dataSource);
        school = context.createSet(School.class).findFirst(p -> p.getSchoolId() == schoolId).orElse(null);
        //是修改后的值
        assertNotNull(school);
        assertEquals(1, result);
        assertEquals("新第X某某学校", school.getName());

        //就地删除
        result = context.createSet(School.class).delete(p -> p.getSchoolId() == schoolId, School.class);
        //受影响的行数为1
        assertEquals(1, result);

        //查出来
        context = ContextUtils.createContext(dataSource);
        school = context.createSet(School.class).findFirst(p -> p.getSchoolId() == schoolId).orElse(null);
        //没有此对象
        assertNull(school);
    }

    /**
     * 测试显式关联修改
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void explicitAssociationModifyTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //新增一个学校的班级
        Class newclass = new Class();
        newclass.setName("显示关联班");
        context.attach(newclass);
        //新增一个教师和任课教师
        var teacher = new Teacher();
        teacher.setName("显示关联老师");
        var clasTeacher = new ClassTeacher(newclass, teacher);
        clasTeacher.setIsManage(true);
        clasTeacher.setIsSubstitute(false);
        List<String> subjects = new ArrayList<>();
        subjects.add("语文");
        subjects.add("数学");
        subjects.add("化学");
        clasTeacher.setSubject(subjects);

        List<ClassTeacher> teachers = new ArrayList<>();
        teachers.add(clasTeacher);
        newclass.setClassTeachers(teachers);
        context.attach(teacher);
        //保存数据
        context.saveChanges();

        //查出来 修改属性
        context = ContextUtils.createContext(dataSource);
        var classId = newclass.getClassId();
        var teacherId = teacher.getTeacherId();
        Class clazz = context.createSet(Class.class).include("ClassTeachers.Teacher")
                .findFirst(p -> p.getClassId() == classId).orElse(null);

        teacher = context.createSet(Teacher.class).findFirst(p -> p.getTeacherId() == teacherId).orElse(null);
        //都有值
        assertNotNull(clazz);
        assertNotNull(clazz.getClassTeachers());
        assertNotNull(teacher);
        //将任课教师的属性修改
        subjects = new ArrayList<>();
        subjects.add("显示关联语文");
        subjects.add("显示关联数学");
        subjects.add("显示关联化学");
        clazz.getClassTeachers().get(0).setSubject(subjects);
        clazz.getClassTeachers().get(0).setIsManage(false);
        clazz.getClassTeachers().get(0).setIsSubstitute(true);
        //保存
        context.saveChanges();
        //查出来
        context = ContextUtils.createContext(dataSource);
        clazz = context.createSet(Class.class).include("ClassTeachers.Teacher")
                .findFirst(p -> p.getClassId() == classId).orElse(null);

        teacher = context.createSet(Teacher.class).findFirst(p -> p.getTeacherId() == teacherId).orElse(null);
        //验证属性
        assertNotNull(clazz);
        assertNotNull(clazz.getClassTeachers());
        assertNotNull(teacher);

        var subA = new String[]{"显示关联语文", "显示关联数学", "显示关联化学"};
        //验证任课教师的属性
        assertArrayEquals(subA, clazz.getClassTeachers().get(0).getSubject().toArray(new String[0]));
        assertFalse(clazz.getClassTeachers().get(0).getIsManage());
        assertTrue(clazz.getClassTeachers().get(0).getIsSubstitute());
        //移除后重建相同关联端的对象并修改属性
        clazz.getClassTeachers().clear();

        clasTeacher = new ClassTeacher(clazz, teacher);
        clasTeacher.setIsManage(false);
        clasTeacher.setIsSubstitute(false);
        subjects = new ArrayList<>();
        subjects.add("显示关联语文1");
        subjects.add("显示关联数学2");
        subjects.add("显示关联化学3");
        clasTeacher.setSubject(subjects);

        //重新设置任课教师
        teachers = new ArrayList<>();
        teachers.add(clasTeacher);
        clazz.setClassTeachers(teachers);
        //保存
        context.saveChanges();
        //查出来
        context = ContextUtils.createContext(dataSource);
        clazz = context.createSet(Class.class).include("ClassTeachers.Teacher")
                .findFirst(p -> p.getClassId() == classId).orElse(null);
        //验证属性
        assertNotNull(clazz);
        assertNotNull(clazz.getClassTeachers());
        assertNotNull(clazz.getClassTeachers().get(0).getTeacher());
        assertEquals(teacherId, clazz.getClassTeachers().get(0).getTeacher().getTeacherId());

        subA = new String[]{"显示关联语文1", "显示关联数学2", "显示关联化学3"};
        //验证任课教师的属性
        assertArrayEquals(subA, clazz.getClassTeachers().get(0).getSubject().toArray(new String[0]));
        assertFalse(clazz.getClassTeachers().get(0).getIsManage());
        assertFalse(clazz.getClassTeachers().get(0).getIsSubstitute());
        //删除任课教师和班级
        context.createSet(Class.class).remove(newclass);
        context.createSet(Teacher.class).remove(teacher);
        context.saveChanges();
    }
}
