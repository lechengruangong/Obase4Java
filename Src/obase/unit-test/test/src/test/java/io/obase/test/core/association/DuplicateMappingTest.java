package io.obase.test.core.association;

import io.obase.core.TimeBasedIdGenerator;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.duplicateMapping.GoodsAttribute;
import io.obase.test.domain.association.duplicateMapping.SelectableValue;
import io.obase.test.domain.association.duplicateMapping.StandardValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 在关联型中有重复的映射测试
 */
@ExtendWith(ConfigSetUp.class)
public class DuplicateMappingTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(GoodsAttribute.class).delete(p -> p.getAttributeId() > 0 || p.getGoodsId() > 0, GoodsAttribute.class);
            context.createSet(SelectableValue.class).delete(p -> p.getAttributeId() > 0 || p.getCategoryId() > 0, SelectableValue.class);
            context.createSet(StandardValue.class).delete(p -> p.getAttributeId() > 0 || p.getCategoryId() > 0 || p.getGoodsId() > 0, StandardValue.class);

            context = ContextUtils.createContext(dataSource);

            //加入测试数据
            var gen = new TimeBasedIdGenerator();

            var goodsAttr = new GoodsAttribute(gen.next(), gen.next());
            goodsAttr.setInputValue("测试输入值");
            var selectableValue = new SelectableValue(gen.next(), goodsAttr.getAttributeId());
            selectableValue.setAlias("测试属性值");
            selectableValue.setSequence(10);

            context.attach(goodsAttr);
            context.attach(selectableValue);

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
            context.createSet(GoodsAttribute.class).delete(p -> p.getAttributeId() > 0 || p.getGoodsId() > 0, GoodsAttribute.class);
            context.createSet(SelectableValue.class).delete(p -> p.getAttributeId() > 0 || p.getCategoryId() > 0, SelectableValue.class);
            context.createSet(StandardValue.class).delete(p -> p.getAttributeId() > 0 || p.getCategoryId() > 0 || p.getGoodsId() > 0, StandardValue.class);
        }
    }

    /**
     * 测试关联型中有重复的映射
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询GoodsAttribute
        var goodsAttr = context.createSet(GoodsAttribute.class).findFirst().orElse(null);
        //检测值
        assertNotNull(goodsAttr);
        assertEquals("测试输入值", goodsAttr.getInputValue());

        //查询SelectableValue
        var selectableValues = context.createSet(SelectableValue.class).findFirst().orElse(null);
        //检测值
        assertNotNull(selectableValues);
        assertEquals("测试属性值", selectableValues.getAlias());
        assertEquals(10, selectableValues.getSequence());

        //新增StandardValue
        var standardValue = new StandardValue(goodsAttr, selectableValues);
        standardValue.setAlias("某某");
        standardValue.setPhoto("1.jpg");
        context.attach(standardValue);
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);

        //查询StandardValue
        var qStandardValue = context.createSet(StandardValue.class).include(p -> p.getGoodsAttribute()).include(p -> p.getSelectedValue()).findFirst().orElse(null);
        //检测值
        assertNotNull(qStandardValue);
        assertEquals("某某", qStandardValue.getAlias());
        assertEquals("1.jpg", qStandardValue.getPhoto());
        assertNotNull(qStandardValue.getGoodsAttribute());
        assertEquals("测试输入值", qStandardValue.getGoodsAttribute().getInputValue());
        assertNotNull(qStandardValue.getSelectedValue());
        assertEquals("测试属性值", qStandardValue.getSelectedValue().getAlias());
        assertEquals(10, qStandardValue.getSelectedValue().getSequence());
    }
}
