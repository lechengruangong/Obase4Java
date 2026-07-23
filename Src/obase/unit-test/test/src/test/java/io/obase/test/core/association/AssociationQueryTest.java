package io.obase.test.core.association;

import io.obase.core.expression.IGroupingBy;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.Class;
import io.obase.test.domain.association.*;
import io.obase.test.domain.association.selectResult.SimpleStu;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 关联查询测试
 */
@ExtendWith(ConfigSetUp.class)
public class AssociationQueryTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //清理可能的冗余数据
            context.createSet(Student.class).delete(p -> p.getStudentId() > 0, Student.class);
            context.createSet(School.class).delete(p -> p.getSchoolId() > 0, School.class);
            context.createSet(Class.class).delete(p -> p.getClassId() > 0, Class.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() > 0 || p.getTeacherId() > 0, ClassTeacher.class);
            context.createSet(StudentInfo.class).delete(p -> p.getStudentId() > 0, StudentInfo.class);
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() > 0, Teacher.class);

            //加入测试学校
            School newschool = new School();
            newschool.setName("第X某某学校");
            newschool.setSchoolType(ESchoolType.High);
            newschool.setCreateTime(LocalDateTime.now());
            newschool.setIsPrime(false);
            newschool.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));

            //学校的班级
            Class newclass = new Class();
            newclass.setName("某某班");
            newclass.setSchool(newschool);

            context.attach(newschool);
            context.attach(newclass);

            //加入学生
            for (int i = 1; i < 6; i++) {
                Student student = new Student();
                student.setName("小" + i);
                student.setClazz(newclass);
                student.setSchool(newschool);

                context.attach(student);
            }

            //加入教师
            //一对多

            Teacher teacher = new Teacher();
            teacher.setName("某老师");
            teacher.setSchoolId(newschool.getSchoolId());
            ClassTeacher classTeacher = new ClassTeacher(newclass, teacher);
            List<String> subjects = new ArrayList<>();
            subjects.add("语文");
            subjects.add("数学");
            subjects.add("化学");
            classTeacher.setSubject(subjects);
            classTeacher.setIsManage(true);
            classTeacher.setIsSubstitute(false);

            List<ClassTeacher> teachers = new ArrayList<>();
            teachers.add(classTeacher);
            newclass.setClassTeachers(teachers);
            context.attach(teacher);
            context.attach(classTeacher);

            //保存
            context.saveChanges();

            //为学生加入学生信息 学生信息没有引用学生 只能靠StudentId关联 所以此处需要先保存学生获取ID
            var studentList = context.createSet(Student.class).toList();
            for (Student student : studentList) {
                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setBackground("普通");
                studentInfo.setDescription("普普通通");
                studentInfo.setStudentId(student.getStudentId());

                context.attach(studentInfo);
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
            context.createSet(Student.class).delete(p -> p.getStudentId() > 0, Student.class);
            context.createSet(School.class).delete(p -> p.getSchoolId() > 0, School.class);
            context.createSet(Class.class).delete(p -> p.getClassId() > 0, Class.class);
            context.createSet(ClassTeacher.class).delete(p -> p.getClassId() > 0 || p.getTeacherId() > 0, ClassTeacher.class);
            context.createSet(StudentInfo.class).delete(p -> p.getStudentId() > 0, StudentInfo.class);
            context.createSet(Teacher.class).delete(p -> p.getTeacherId() > 0, Teacher.class);
        }
    }

    /**
     * 测试关系分组
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void groupByTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //按照属性分组 选择某个其他属性 结果是多个的
        var result2 = context.createSet(Student.class).groupBy(Student::getClassId, p -> p.getStudentId()).toHashMapWithIterableResult(IGroupingBy::getKey, IGroupingBy::getElement);

        assertNotNull(result2);
        assertEquals(1, result2.size());

        var cla = context.createSet(Class.class).findFirst();
        assertTrue(cla.isPresent());
        assertTrue(result2.containsKey(cla.get().getClassId()));
    }

    /**
     * 测试强制包含方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void includeTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //测试字符串的Include 隐式关联型
        var cla09 = context.createSet(Class.class).filter(p -> p.getName() != "").include("Students.School").toList();
        var te1 = cla09.get(0).getStudents().get(0).getSchool();
        //可以获取到学校
        assertNotNull(te1);
        //显式关联型
        var cla9 = context.createSet(Class.class).filter(p -> p.getName() != "").include("ClassTeachers.Teacher").toList();
        var te9 = cla9.get(0).getClassTeachers().get(0).getTeacher();
        //可以获取到教师
        assertNotNull(te9);

        //测试没有条件直接包含关联引用
        context = ContextUtils.createContext(dataSource);

        var cla1 = context.createSet(Class.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //可以获取到学校
        assertNotNull(cla1);
        assertNotNull(cla1.getSchool());

        //测试有条件包含关联引用
        cla1 = context.createSet(Class.class).include(p -> p.getSchool()).findFirst(p -> p.getName() != "").orElse(null);
        //可以获取到学校
        assertNotNull(cla1);
        assertNotNull(cla1.getSchool());

        context = ContextUtils.createContext(dataSource);
        //多重性元素阻断关联路径表达的问题 使用字符串表示
        var cla0 = context.createSet(Class.class).filter(p -> p.getName() != "").include("Students.School").toList();
        var te = cla0.get(0).getStudents().get(0).getSchool();
        //可以获取到学校
        assertNotNull(te);

        context = ContextUtils.createContext(dataSource);
        //分别包含任课教师和学生
        var cla = context.createSet(Class.class).filter(p -> p.getName() != "").include(p -> p.getStudents()).include(p -> p.getClassTeachers())
                .findFirst().orElse(null);
        //可以获取到任课教师和学生
        assertNotNull(cla);
        assertNotNull(cla.getTeachers());
        assertEquals(1, cla.getTeachers().size());
        assertNotNull(cla.getStudents());
        assertEquals(5, cla.getStudents().size());

        context = ContextUtils.createContext(dataSource);
        //执行强制包含 将非延迟加载的关联引用放入对象中
        var stu = context.createSet(Student.class).filter(p -> p.getName() != "").include(p -> p.getClazz().getSchool()).include(p -> p.getStudentInfo()).findFirst().orElse(null);
        //可以获取到班级 学校 学生信息
        assertNotNull(stu);
        assertNotNull(stu.getClazz());
        assertNotNull(stu.getStudentInfo());
        //延迟加载的学校
        assertNotNull(stu.getSchool());

        context = ContextUtils.createContext(dataSource);
        //测试空查询 只有一个包含操作
        var classes = context.createSet(Class.class).include(p -> p.getClassTeachers()).toList();
        //可以获取到任课教师
        assertNotNull(classes.get(0));
        assertNotNull(classes.get(0).getClassTeachers());

        context = ContextUtils.createContext(dataSource);
        //测试连续Include后无其他Op
        classes = context.createSet(Class.class).include(p -> p.getClassTeachers()).include(p -> p.getStudents()).include(p -> p.getSchool()).toList();
        //可以获取到任课教师 学生 学校
        assertNotNull(classes.get(0));
        assertNotNull(classes.get(0).getStudents());
        assertNotNull(classes.get(0).getSchool());
        assertNotNull(classes.get(0).getClassTeachers());


        //测试错误的Include
        //Name不是引用元素
        var ex = assertThrowsExactly(IllegalArgumentException.class, () -> ContextUtils.createContext(dataSource).createSet(Class.class).include(p -> p.getName()).toList());
        //校验
        assertNotNull(ex);
        assertEquals("包含路径错误,找不到为Name的引用元素.", ex.getMessage());

        //测试School是引用元素但CreateTime不是
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> ContextUtils.createContext(dataSource).createSet(Class.class).include(p -> p.getSchool().getCreateTime()).toList());
        //校验
        assertNotNull(ex);
        assertEquals("包含路径错误,找不到为CreateTime的引用元素.", ex.getMessage());

        //Name不是引用元素
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> ContextUtils.createContext(dataSource).createSet(Class.class).include("Name").toList());
        //校验
        assertNotNull(ex);
        assertEquals("包含路径错误,找不到为Name的引用元素.", ex.getMessage());

        //测试根本没有的元素
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> ContextUtils.createContext(dataSource).createSet(Class.class).include("123").toList());
        //校验
        assertNotNull(ex);
        assertEquals("无法从io.obase.test.domain.association.Class中获取属性123,请检查Include的参数.", ex.getMessage());

        //测试School是引用元素但根本没有456元素
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> ContextUtils.createContext(dataSource).createSet(Class.class).include("School.456").toList());
        //校验
        assertNotNull(ex);
        assertEquals("无法从io.obase.test.domain.association.School中获取属性456,请检查Include的参数.", ex.getMessage());
    }

    /**
     * 测试排序
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void orderTest(EDataSource dataSource) {
        //测试用关联属性排序
        var context = ContextUtils.createContext(dataSource);
        //使用班级名称 和 班级关联的学校创建时间排序
        var classes = context.createSet(Class.class)
                .sorted(p -> p.getName()).thenSorted(p -> p.getSchool().getCreateTime()).toList();

        assertEquals(1, classes.size());

        //根据显式关联型引用的关联端的属性排序
        var classTeachers = context.createSet(ClassTeacher.class).include(p -> p.getClazz()).include(p -> p.getTeacher())
                .filter(p -> p.getClazz().getName() != "123")
                .sorted(p -> p.getClazz().getName()).skip(0).limit(1).toList();

        assertEquals(1, classTeachers.size());

        //投影之后 使用学生名称 和 学生关联的班级关联的学校创建时间排序
        var oStud = context.createSet(Class.class).flatMap(p -> p.getStudents(), Student.class)
                .sorted(p -> p.getName()).thenSorted(p -> p.getSchool().getCreateTime()).toList();

        assertEquals(5, oStud.size());
    }

    /**
     * 测试投影
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void selectTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //一对多 Select投影到关联引用
        //结果是List<List<关联引用对象>>
        var stu1 = context.createSet(Class.class).map(Class::getStudents, List.class).toList();
        assertNotNull(stu1);
        assertEquals(1, stu1.size());
        assertEquals(5, stu1.get(0).size());

        var stu2 = context.createSet(Student.class).include(Student::getSchool).map(p -> new SimpleStu(p.getStudentId(), p.getName()), SimpleStu.class).toList();

        assertEquals(5, stu2.size());

        //测试SelectMany
        context = ContextUtils.createContext(dataSource);
        //一对多 SelectMany至关联引用 后 投影成新对象
        var stu4 = context.createSet(Class.class).flatMap(Class::getStudents, Student.class).map(p -> new SimpleStu(p.getStudentId(), p.getName()), SimpleStu.class).toList();

        assertEquals(5, stu4.size());

        //投影到多个对象
        var stu5 = context.createSet(Class.class).flatMap(Class::getStudents, Student.class).toList();
        assertEquals(5, stu5.size());

        context = ContextUtils.createContext(dataSource);
        //投影到单个属性
        var stuNameList = context.createSet(Student.class).include(Student::getSchool).map(p -> p.getName(), String.class).distinct().toList();
        assertEquals(5, stuNameList.size());

        //投影到一对一关联的属性
        var studentBackgroundList = context.createSet(Student.class).include(Student::getSchool).map(p -> p.getStudentInfo().getBackground(), String.class).toList();
        assertEquals(5, studentBackgroundList.size());

        //投影到枚举
        var schoolType = context.createSet(School.class).filter(p -> p.getName() != "").map(p -> p.getSchoolType(), List.class).distinct().toList();
        assertEquals(1, schoolType.size());

        //投影到显式关联
        var classTeachers = context.createSet(Class.class).flatMap(p -> p.getClassTeachers(), ClassTeacher.class).toList();
        assertEquals(1, classTeachers.size());

        //从显式关联投影到某端的简单属性
        var teacherNames = context.createSet(ClassTeacher.class).include(p -> p.getClazz()).include(p -> p.getTeacher()).map(p -> p.getTeacher().getName(), String.class).toList();

        assertEquals(1, teacherNames.size());

        context = ContextUtils.createContext(dataSource);

        //连续的一对一 如A.B.C A.Include(A.B.C).Select(B) <=> B.Include(C)
        var classIncludeSelect = context.createSet(Student.class).filter(p -> p.getStudentId() > 0).skip(0).limit(1).include(p -> p.getClazz().getSchool()).map(p -> p.getClazz(), List.class).toList();

        assertEquals(1, classIncludeSelect.size());
        assertNotNull(classIncludeSelect.get(0).getSchool());

        //classIncludeSelect也可以改写为A.Select(B).Include(B.C)
        classIncludeSelect = context.createSet(Student.class).filter(p -> p.getStudentId() > 0).skip(0).limit(1).map(p -> p.getClazz(), List.class).include(p -> p.getSchool()).toList();

        assertEquals(1, classIncludeSelect.size());
        assertNotNull(classIncludeSelect.get(0).getSchool());

        //一对多也可以投影
        var studentsIncludeSelect = context.createSet(Class.class).filter(p -> p.getClassId() > 0).skip(0).limit(1).include("Students.School").flatMap(p -> p.getStudents(), Student.class).toList();

        assertEquals(5, studentsIncludeSelect.size());
        assertTrue(studentsIncludeSelect.stream().allMatch(p -> p.getSchool() != null));

        //从显式关联型投影
        var ExClass = context.createSet(ClassTeacher.class).filter(p -> p.getClassId() > 0).skip(0).limit(1).include(p -> p.getClazz().getSchool()).include(p -> p.getTeacher()).map(p -> p.getClazz(), List.class).toList();

        assertEquals(1, ExClass.size());
        assertNotNull(ExClass.get(0).getSchool());

        //测试投影到显式关联型且Include显式关联型
        var exClassTeacher1 = context.createSet(Class.class).include(Class::getClassTeachers).map(Class::getClassTeachers, List.class).toList();

        assertEquals(1, exClassTeacher1.size());

        var exClassTeacher2 = context.createSet(Class.class).include(Class::getClassTeachers).flatMap(Class::getClassTeachers, ClassTeacher.class).toList();

        assertEquals(1, exClassTeacher2.size());

        //测试投影到显式关联型且Include显式关联型.对端
        var exClassTeacher3 = context.createSet(Class.class).include("ClassTeachers.Teacher").map(Class::getClassTeachers, List.class).toList();

        assertEquals(1, exClassTeacher3.size());

        var exClassTeacher4 = context.createSet(Class.class).include("ClassTeachers.Teacher").flatMap(Class::getClassTeachers, ClassTeacher.class).toList();

        assertEquals(1, exClassTeacher4.size());
        assertNotNull(exClassTeacher4.get(0).getTeacher());

        //测试投影到多端之后再次筛选
        List<Student> mStu = context.createSet(Class.class).filter(p -> p.getName() == "某某班").flatMap(p -> p.getStudents(), Student.class).filter(p -> p.getName() == "小2").toList();

        assertNotNull(mStu);
        assertEquals(1, mStu.size());
        assertEquals("小2", mStu.get(0).getName());

        var sStuInfo = context.createSet(Student.class).filter(p -> p.getName() == "小3").map(p -> p.getStudentInfo(), StudentInfo.class).filter(p -> p.getBackground() == "普通").toList();

        assertNotNull(sStuInfo);
        assertEquals(1, sStuInfo.size());
        assertEquals("普普通通", sStuInfo.get(0).getDescription());

        //从班级投影到学生再投影回来
        var cla = context.createSet(Class.class).flatMap(p -> p.getStudents(), Student.class).filter(p -> p.getName() != "123").toList().stream()
                .map(p -> p.getClazz()).filter(p -> p.getName() != "123").toList();
        //有5个满足条件的学生 再投影后是5个班级 且这5个班级都是一样的
        assertNotNull(cla);
        assertEquals(5, cla.size());
    }

    /**
     * 测试根据关联对象属性筛选
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void whereTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询学生信息
        var queryStudent = context.createSet(StudentInfo.class).findFirst().orElse(null);
        assertNotNull(queryStudent);
        //修改成不普通
        queryStudent.setBackground("不普通");
        context.saveChanges();

        long studentId = queryStudent.getStudentId();

        //用学生信息的背景筛选学生
        var students = context.createSet(Student.class).filter(p -> p.getStudentInfo().getBackground() == "不普通").toList();
        //有一个学生
        assertNotNull(students);
        assertEquals(1, students.size());

        //用学生信息的背景包含学生
        students = context.createSet(Student.class).filter(p -> p.getStudentInfo().getBackground().contains("不普通")).toList();
        //有一个学生
        assertNotNull(students);
        assertEquals(1, students.size());

        //用之前查询的ID查询学生
        students = context.createSet(Student.class).filter(p -> p.getStudentInfo().getStudentId() == studentId).toList();
        //有一个学生
        assertNotNull(students);
        assertEquals(1, students.size());
        //使用之前查询的ID查询学生
        var student = context.createSet(Student.class).filter(p -> p.getStudentInfo().getBackground() == "不普通").findFirst().orElse(null);
        //与之前查询的ID相同
        assertNotNull(student);
        //使用之前查询的ID查询学生
        student = context.createSet(Student.class).filter(p -> p.getStudentInfo().getStudentId() == studentId).findFirst().orElse(null);
        //与之前查询的ID相同
        assertNotNull(student);

        //使用列表包含
        var list = new ArrayList<Long>();
        list.add(-1L);
        list.add(-2L);
        list.add(-3L);
        list.add(student.getStudentId());
        //使用列表包含查询学生
        student = context.createSet(Student.class).filter(p -> list.contains(p.getStudentInfo().getStudentId())).findLast().orElse(null);
        //与之前查询的ID相同
        assertNotNull(student);
        assertEquals(queryStudent.getStudentId(), student.getStudentId());

        //使用枚举类型查询
        context = ContextUtils.createContext(dataSource);
        var schools = context.createSet(School.class).filter(p -> p.getSchoolType().equals(ESchoolType.High)).toList();
        assertNotNull(schools);
        assertEquals(1, schools.size());

        //查询计数
        var count = context.createSet(Student.class).count(p -> p.getStudentInfo().getStudentId() == studentId);
        assertEquals(1, count);

        //增加使用显式关联型的关联属性测试
        List<ClassTeacher> classTeacher = context.createSet(ClassTeacher.class).include(p -> p.getClazz()).filter(p -> p.getTeacher().getName() == "某老师1")
                .toList();

        assertNotNull(classTeacher);
        assertEquals(0, classTeacher.size());
    }
}
