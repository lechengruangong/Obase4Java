package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.Student;
import io.obase.test.domain.association.StudentInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聚合端测试
 */
@ExtendWith(ConfigSetUp.class)
public class AggregatedEndTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Student.class).delete(p -> p.getStudentId() > 0, Student.class);
            context.createSet(StudentInfo.class).delete(p -> p.getStudentInfoId() > 0, StudentInfo.class);

            context = ContextUtils.createContext(dataSource);

            for (int i = 1; i < 6; i++) {
                Student student = new Student();
                student.setName("小" + i);

                context.attach(student);
            }

            context.saveChanges();

            //为学生加入学生信息
            //一对一
            context = ContextUtils.createContext(dataSource);
            List<Student> studentList = context.createSet(Student.class).toList();
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
            context.createSet(StudentInfo.class).delete(p -> p.getStudentInfoId() > 0, StudentInfo.class);
        }
    }

    /**
     * 测试关联端聚合
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询学生
        var student = context.createSet(Student.class).include(p -> p.getStudentInfo()).findFirst().orElse(null);
        //学生和学生信息都不是空
        assertNotNull(student);
        assertNotNull(student.getStudentInfo());
        //移除了学生
        context.remove(student);
        context.saveChanges();

        var studentId = student.getStudentId();
        //学生和学生信息都是被删除
        student = context.createSet(Student.class).findFirst(p -> p.getStudentId() == studentId).orElse(null);
        assertNull(student);
        var studentInfo = context.createSet(StudentInfo.class).findFirst(p -> p.getStudentId() == studentId).orElse(null);
        assertNull(studentInfo);

        context = ContextUtils.createContext(dataSource);
        //另外一个学生
        student = context.createSet(Student.class).include(p -> p.getStudentInfo()).findFirst().orElse(null);

        //学生和学生信息都不是空
        assertNotNull(student);
        assertNotNull(student.getStudentInfo());

        //新建
        var newStudentInfo = new StudentInfo();
        newStudentInfo.setBackground("新普通");
        newStudentInfo.setDescription("新普普通通");
        newStudentInfo.setStudentId(student.getStudentId());
        context.attach(newStudentInfo);

        //替换学生信息
        student.setStudentInfo(newStudentInfo);
        context.saveChanges();

        student = context.createSet(Student.class).include(p -> p.getStudentInfo()).findFirst().orElse(null);

        //学生和学生信息都不是空
        assertNotNull(student);
        assertNotNull(student.getStudentInfo());
        assertEquals("新普通", student.getStudentInfo().getBackground());
        assertEquals("新普普通通", student.getStudentInfo().getDescription());
        //查询此时的学生信息
        var newStudentId = student.getStudentId();
        List<StudentInfo> infos = context.createSet(StudentInfo.class).toList();
        //应只有一条
        var count = infos.stream().filter(p -> p.getStudentId() == newStudentId).count();
        assertEquals(1, count);
        //并且没有其他被解除关系的
        count = infos.stream().filter(p -> p.getStudentId() == 0).count();

        assertEquals(0, count);
    }
}
