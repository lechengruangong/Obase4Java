package io.obase.test.addon.multiTenant;

import io.obase.addon.test.domain.annotation.ESchoolType;
import io.obase.addon.test.domain.multi.tenant.MultiTenantSchool;
import io.obase.addon.test.domain.multi.tenant.MultiTenantTeacher;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用代码配置的 定义了多租户字段的测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiTenantTest {

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
            context.createSet(MultiTenantSchool.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchool.class);
            context.createSet(MultiTenantTeacher.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacher.class);

            //加入新对象 保存的对象都是第0个用户的
            var school = new MultiTenantSchool();
            school.setCreateTime(LocalDateTime.now());
            school.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
            school.setIsPrime(false);
            school.setName("第X某某学校");
            school.setSchoolType(ESchoolType.Primary);
            //此处赋一个错误值 会被ITenantIdReader或者委托的返回值覆盖
            school.setMultiTenantId(new UUID(0, 0));
            var teacher = new MultiTenantTeacher();
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
            context.createSet(MultiTenantSchool.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchool.class);
            context.createSet(MultiTenantTeacher.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacher.class);
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
        var teacher = context.createSet(MultiTenantTeacher.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //校验取出的值
        assertNotNull(teacher);
        assertEquals(TenantIdCenter.TenantIds.get(0), teacher.getMultiTenantId());
        assertNotNull(teacher.getSchool());
        assertEquals(TenantIdCenter.TenantIds.get(0), teacher.getSchool().getMultiTenantId());

        //设置当前用户为第1个用户
        TenantIdCenter.setCurrentUserIndex(1);
        //此时查询的是第1个用户的值
        context = ContextUtils.createAddonContext(dataSource);
        //此处应无法查询出对象
        teacher = context.createSet(MultiTenantTeacher.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //无法查出
        assertNull(teacher);

        //设置当前租户ID为全局ID
        TenantIdCenter.setCurrentUserIndex(2);

        var gTeacher = new MultiTenantTeacher();
        gTeacher.setName("某老师G");
        context.attach(gTeacher);

        context.saveChanges();

        //设置当前用户为第0个用户
        TenantIdCenter.setCurrentUserIndex(0);
        //MultiTenantTeacher启用了全局ID 会查出刚才保存的新的教师
        var list = context.createSet(MultiTenantTeacher.class).toList();
        //校验取出的值
        assertNotNull(list);
        assertEquals(2, list.size());
    }
}
