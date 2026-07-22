package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.dataError.DataErrorStudent;
import io.obase.test.domain.functional.dataError.DataErrorStudentInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 数据错误(关联引用是一对一 但数据是一对多)的关联测试
 */
@ExtendWith(ConfigSetUp.class)
public class DataErrorTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(DataErrorStudent.class).delete(p -> p.getStudentId() >= 0, DataErrorStudent.class);
            context.createSet(DataErrorStudentInfo.class).delete(p -> p.getStudentInfoId() >= 0, DataErrorStudentInfo.class);

            context = ContextUtils.createContext(dataSource);

            //加入测试数据
            DataErrorStudent stu = new DataErrorStudent();
            stu.setStudentId(1);
            stu.setName("小1");


            for (int i = 1; i < 3; i++) {
                context.attach(stu);
                DataErrorStudentInfo studentInfo = new DataErrorStudentInfo();
                studentInfo.setBackground("普通" + i);
                studentInfo.setDescription("普普通通" + i);
                studentInfo.setStudentId(1);
                studentInfo.setStudentInfoId(i);
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
            context.createSet(DataErrorStudent.class).delete(p -> p.getStudentId() >= 0, DataErrorStudent.class);
            context.createSet(DataErrorStudentInfo.class).delete(p -> p.getStudentInfoId() >= 0, DataErrorStudentInfo.class);
        }
    }

    /**
     * 测试错误数据
     * 关联引用是一对一 但数据是一对多
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //加载一对一关联
        DataErrorStudent student = context.createSet(DataErrorStudent.class).include(DataErrorStudent::getStudentInfo).findFirst().orElse(null);
        //此时军不为空
        assertNotNull(student);
        assertNotNull(student.getStudentInfo());
        //随意修改一个属性
        student.setName("小X");
        //保存后
        context.saveChanges();

        //此时 DataErrorStudentInfo中StudentId为1的 仍然有多个 没有被关联解除
        var count = context.createSet(DataErrorStudentInfo.class).count(p -> p.getStudentId() == 1);
        //仍然有两个
        assertEquals(2, count);
    }
}
