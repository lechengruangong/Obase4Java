package io.obase.test.addon.multiTenant;

import io.obase.addon.test.domain.annotation.ESchoolType;
import io.obase.addon.test.domain.multi.tenant.MultiTenantSchoolNoDefAnnotation;
import io.obase.addon.test.domain.multi.tenant.MultiTenantTeacherNoDefAnnotation;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 用标注配置的 没有定义多租户字段的测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiTenantNoDefAnnotationTest {

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
            context.createSet(MultiTenantSchoolNoDefAnnotation.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchoolNoDefAnnotation.class);
            context.createSet(MultiTenantTeacherNoDefAnnotation.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacherNoDefAnnotation.class);

            //加入新对象 保存的对象都是第0个用户的
            var school = new MultiTenantSchoolNoDefAnnotation();
            school.setCreateTime(LocalDateTime.now());
            school.setEstablishmentTime(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
            school.setIsPrime(false);
            school.setName("第X某某学校");
            school.setSchoolType(ESchoolType.Primary);

            var teacher = new MultiTenantTeacherNoDefAnnotation();
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
            context.createSet(MultiTenantSchoolNoDefAnnotation.class).delete(p -> p.getSchoolId() > 0, MultiTenantSchoolNoDefAnnotation.class);
            context.createSet(MultiTenantTeacherNoDefAnnotation.class).delete(p -> p.getTeacherId() > 0, MultiTenantTeacherNoDefAnnotation.class);
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
        var teacher = context.createSet(MultiTenantTeacherNoDefAnnotation.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //校验取出的值
        assertNotNull(teacher);
        assertNotNull(teacher.getSchool());

        //设置当前用户为第1个用户
        TenantIdCenter.setCurrentUserIndex(1);
        //此时查询的是第1个用户的值
        context = ContextUtils.createAddonContext(dataSource);
        //此处应无法查询出对象
        teacher = context.createSet(MultiTenantTeacherNoDefAnnotation.class).include(p -> p.getSchool()).findFirst().orElse(null);
        //无法查出
        assertNull(teacher);
    }
}
