package io.obase.test.addon.multiTenant;

import io.obase.addon.test.domain.annotation.ESchoolType;
import io.obase.addon.test.domain.multi.tenant.MultiTenantSchoolNoDef;
import io.obase.addon.test.domain.multi.tenant.MultiTenantTeacherNoDef;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.service.TenantIdCenter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用代码配置的 没有定义多租户字段的测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiTenantNoDefTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        //设置当前用户为第0个用户
        TenantIdCenter.setCurrentUserIndex(0);

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);
            //销毁所有可能的冗余对象
            context.createSet(MultiTenantSchoolNoDef.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchoolNoDef.class);
            context.createSet(MultiTenantTeacherNoDef.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacherNoDef.class);

            //加入新对象 保存的对象都是第0个用户的
            var school = new MultiTenantSchoolNoDef();
            school.setCreateTime(LocalDateTime.now());
            school.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
            school.setIsPrime(false);
            school.setName("第X某某学校");
            school.setSchoolType(ESchoolType.Primary);

            var teacher = new MultiTenantTeacherNoDef();
            teacher.setName("某老师");
            teacher.setSchool(school);
            context.attach(school);
            context.attach(teacher);

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
            //销毁所有可能的冗余对象
            context.createSet(MultiTenantSchoolNoDef.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchoolNoDef.class);
            context.createSet(MultiTenantTeacherNoDef.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacherNoDef.class);
        }
    }

    /**
     * 简单的增删改查测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void queryTest(EDataSource dataSource) {
        //设置当前用户为第0个用户
        TenantIdCenter.setCurrentUserIndex(0);
        var context = ContextUtils.createAddonContext(dataSource);
        //查询时会将读取器的返回值作为附加条件 即此时查询的是第0个用户的
        var teacher = context.createSet(MultiTenantTeacherNoDef.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //校验取出的值 这里没有定义此属性
        assertNotNull(teacher);
        assertNotNull(teacher.getSchool());

        //设置当前用户为第1个用户
        TenantIdCenter.setCurrentUserIndex(1);
        context = ContextUtils.createAddonContext(dataSource);

        //查询时会将读取器的返回值作为附加条件 此处应无法查询出对象
        teacher = context.createSet(MultiTenantTeacherNoDef.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //校验取出的值
        assertNull(teacher);

        //设置当前租户ID为全局ID
        TenantIdCenter.setCurrentUserIndex(2);

        var gTeacher = new MultiTenantTeacherNoDef();
        gTeacher.setName("某老师G");
        context.attach(gTeacher);

        context.saveChanges();

        //设置当前用户为第0个用户
        TenantIdCenter.setCurrentUserIndex(0);
        //MultiTenantTeacher启用了全局ID 会查出刚才保存的新的教师
        var list = context.createSet(MultiTenantTeacherNoDef.class).toList();
        //校验取出的值
        assertNotNull(list);
        assertEquals(2, list.size());
    }
}
