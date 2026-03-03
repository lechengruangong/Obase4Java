package io.obase.test.addon.annotation;

import io.obase.addon.test.domain.annotation.AnnotationCity;
import io.obase.addon.test.domain.annotation.AnnotationDomesticAddress;
import io.obase.addon.test.domain.annotation.AnnotationProvince;
import io.obase.addon.test.domain.annotation.AnnotationRegion;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 标注建模复杂类型测试
 */
@ExtendWith(ConfigSetUp.class)
public class AnnotationComplexAttributeTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createAddonContext(dataSource);

            //销毁所有对象
            context.createSet(AnnotationDomesticAddress.class).delete(p -> p.getKey() != "", AnnotationDomesticAddress.class);

            var province = new AnnotationProvince();
            province.setCode(1750300);
            province.setName("某某省");

            var city = new AnnotationCity();
            city.setCode(1865220);
            city.setName("某某市");

            var region = new AnnotationRegion();
            region.setCode(475900);
            region.setName("某某区");

            var domesticAddress = new AnnotationDomesticAddress();
            domesticAddress.setCity(city);
            domesticAddress.setProvince(province);
            domesticAddress.setRegion(region);
            domesticAddress.setKey(UUID.randomUUID().toString());
            domesticAddress.setDetailAddress("某某小区某某栋某某某某");

            context.attach(domesticAddress);
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

            //销毁所有对象
            context.createSet(AnnotationDomesticAddress.class).delete(p -> p.getKey() != "", AnnotationDomesticAddress.class);
        }
    }

    /**
     * 简单的增删改查测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest(EDataSource dataSource) {
        var context = ContextUtils.createAddonContext(dataSource);

        //查询出来
        var address = context.createSet(AnnotationDomesticAddress.class).findFirst().orElse(null);
        assertNotNull(address);
        assertNotNull(address.getCity());
        assertEquals("某某市", address.getCity().getName());
        assertEquals(1865220, address.getCity().getCode());
        assertEquals("某某省", address.getProvince().getName());
        assertEquals(1750300, address.getProvince().getCode());
        assertEquals("某某区", address.getRegion().getName());
        assertEquals(475900, address.getRegion().getCode());
        //修改
        var newCity = new AnnotationCity();
        newCity.setName("某某市");
        newCity.setCode(1865230);

        address.setCity(newCity);
        context.saveChanges();

        //查询出来
        address = context.createSet(AnnotationDomesticAddress.class).findFirst().orElse(null);
        //验证
        assertNotNull(address);
        assertNotNull(address.getCity());
        assertEquals("某某市", address.getCity().getName());
        assertEquals(1865230, address.getCity().getCode());
        assertEquals("某某省", address.getProvince().getName());
        assertEquals(1750300, address.getProvince().getCode());
        assertEquals("某某区", address.getRegion().getName());
        assertEquals(475900, address.getRegion().getCode());
        //删除
        context.remove(address);
        context.saveChanges();

        //查询出来
        address = context.createSet(AnnotationDomesticAddress.class).findFirst().orElse(null);
        assertNull(address);
    }
}
