package io.obase.test.core.simple;

import io.obase.core.odm.Attribute;
import io.obase.core.odm.StructuralType;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.JavaBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JavaBean 的 List&lt;Long&gt; 属性(numbers)与 List&lt;Long&gt;(nullableNumbers)的集成测试
 * 覆盖值类型元素集合的建模 序列化存储 读取 更新 以及在同一个对象上与其它属性共存
 * <p>
 * 注：Java没有long?类型 值类型元素集合统一由包装类型List&lt;Long&gt;表示 其中nullableNumbers允许包含null项；
 * 另因Java泛型擦除 序列化器以List.class为原始类型 反序列化得到的元素为Integer 比较时统一按数值(long)比较
 */
@ExtendWith(ConfigSetUp.class)
public class SimpleTypeValueCollectionIntegrationTest {

    /**
     * 写入的对象数量
     */
    private static final int count = 20;

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(JavaBean.class).delete(p -> p.getIntNumber() > 0, JavaBean.class);
            //添加一批新对象
            for (int i = 1; i <= count; i++) {
                var javaBean = new JavaBean();
                javaBean.setIntNumber(i);
                javaBean.setBool(i % 2 == 0);
                javaBean.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, i)));
                javaBean.setString(i + "号字符串");
                javaBean.setStrings(new String[]{String.valueOf(i - 1), String.valueOf(i), String.valueOf(i + 1)});
                javaBean.setDateTime(LocalDateTime.now());
                javaBean.setLongNumber(i * 100L);
                javaBean.setByteNumber((byte) i);
                javaBean.setCharNumber('\u006A');
                javaBean.setFloatNumber((float) Math.pow(Math.PI, i));
                javaBean.setDoubleNumber(Math.pow(Math.PI, i));
                javaBean.setDate(LocalDate.now());
                javaBean.setTime(LocalTime.now());
                javaBean.setUuid(UUID.randomUUID());
                //值类型元素集合
                javaBean.setNumbers(Arrays.asList((long) i, i * 10L, i * 100L));
                //可空值类型元素集合 中间插入一个null
                javaBean.setNullableNumbers(Arrays.asList((long) i, null, i * 10L));

                context.createSet(JavaBean.class).attach(javaBean);
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
            context.createSet(JavaBean.class).delete(p -> p.getIntNumber() > 0, JavaBean.class);
        }
    }

    /**
     * 测试从数据库读取 List&lt;Long&gt; 属性
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void readValueCollection(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var list = context.createSet(JavaBean.class).sorted(JavaBean::getIntNumber).toList();

        assertEquals(count, list.size());

        for (var bean : list) {
            assertNotNull(bean.getNumbers(), bean.getIntNumber() + "号对象的numbers不应为空");
            assertEquals(
                    Arrays.asList((long) bean.getIntNumber(), bean.getIntNumber() * 10L, bean.getIntNumber() * 100L),
                    asLongs(bean.getNumbers()),
                    bean.getIntNumber() + "号对象的numbers不正确");
        }

        //最小值与最大值都能正确往返
        assertEquals(Arrays.asList(1L, 10L, 100L), asLongs(list.get(0).getNumbers()));
        assertEquals(Arrays.asList((long) count, count * 10L, count * 100L), asLongs(list.get(count - 1).getNumbers()));
    }

    /**
     * 测试可空值类型元素集合的往返 集合中允许存在null项
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void readNullableValueCollection(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var list = context.createSet(JavaBean.class).sorted(JavaBean::getIntNumber).toList();

        assertEquals(count, list.size());

        for (var bean : list) {
            assertNotNull(bean.getNullableNumbers(), bean.getIntNumber() + "号对象的nullableNumbers不应为空");
            assertEquals(3, bean.getNullableNumbers().size());
            //null项必须原样保留
            assertNull(bean.getNullableNumbers().get(1), bean.getIntNumber() + "号对象的nullableNumbers第2项应为null");
            assertEquals(
                    Arrays.asList((long) bean.getIntNumber(), null, bean.getIntNumber() * 10L),
                    asLongs(bean.getNullableNumbers()),
                    bean.getIntNumber() + "号对象的nullableNumbers不正确");
        }

        assertEquals(Arrays.asList(1L, null, 10L), asLongs(list.get(0).getNullableNumbers()));
    }

    /**
     * 测试可空值类型元素集合的更新(含全部为null与全部非null)
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void updateNullableValueCollection(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 2).orElse(null);
        assertNotNull(bean);

        //全部为null
        bean.setNullableNumbers(Arrays.asList(null, null));
        context.saveChanges();

        var newContext = ContextUtils.createContext(dataSource);
        var reloaded = newContext.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 2).orElse(null);
        assertNotNull(reloaded);
        assertEquals(Arrays.asList(null, null), asLongs(reloaded.getNullableNumbers()));

        //全部非null
        reloaded.setNullableNumbers(Arrays.asList(20L, 21L));
        newContext.saveChanges();

        var finalContext = ContextUtils.createContext(dataSource);
        var finalReloaded = finalContext.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 2).orElse(null);
        assertNotNull(finalReloaded);
        assertEquals(Arrays.asList(20L, 21L), asLongs(finalReloaded.getNullableNumbers()));

        //还原 避免影响其它用例
        finalReloaded.setNullableNumbers(Arrays.asList(2L, null, 20L));
        finalContext.saveChanges();
    }

    /**
     * 测试值类型元素集合与引用类型元素集合在同一个对象上共存
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void valueCollectionCoexistsWithOthers(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 7).orElse(null);
        assertNotNull(bean);

        //引用类型元素集合仍然正常
        assertArrayEquals(new String[]{"6", "7", "8"}, bean.getStrings());
        //值类型元素集合正常
        assertEquals(Arrays.asList(7L, 70L, 700L), asLongs(bean.getNumbers()));
        //可空值类型元素集合正常
        assertEquals(Arrays.asList(7L, null, 70L), asLongs(bean.getNullableNumbers()));
        //同行的其它标量属性不受影响
        assertEquals("7号字符串", bean.getString());
        assertEquals(700L, bean.getLongNumber());
    }

    /**
     * 测试条件查询后读取 List&lt;Long&gt; 属性
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void readValueCollectionByCondition(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 5).orElse(null);
        assertNotNull(bean);
        assertEquals(Arrays.asList(5L, 50L, 500L), asLongs(bean.getNumbers()));

        //条件查询返回的多个对象
        var list = context.createSet(JavaBean.class).filter(p -> p.getIntNumber() > 15).sorted(JavaBean::getIntNumber).toList();
        assertEquals(5, list.size());
        assertTrue(list.stream().allMatch(p -> p.getNumbers() != null && p.getNumbers().size() == 3));
    }

    /**
     * 测试修改 List&lt;Long&gt; 属性后保存并重新读取
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void updateValueCollection(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 1).orElse(null);
        assertNotNull(bean);

        //整体替换
        bean.setNumbers(Arrays.asList(999L, 1000L));
        context.saveChanges();

        var newContext = ContextUtils.createContext(dataSource);
        var reloaded = newContext.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 1).orElse(null);
        assertNotNull(reloaded);
        assertEquals(Arrays.asList(999L, 1000L), asLongs(reloaded.getNumbers()));

        //清空
        reloaded.setNumbers(Arrays.asList());
        newContext.saveChanges();

        var emptyContext = ContextUtils.createContext(dataSource);
        var emptyReloaded = emptyContext.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 1).orElse(null);
        assertNotNull(emptyReloaded);
        assertNotNull(emptyReloaded.getNumbers());
        assertEquals(0, emptyReloaded.getNumbers().size());

        //还原 避免影响其它用例
        emptyReloaded.setNumbers(Arrays.asList(1L, 10L, 100L));
        emptyContext.saveChanges();

        var finalContext = ContextUtils.createContext(dataSource);
        var finalReloaded = finalContext.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 1).orElse(null);
        assertNotNull(finalReloaded);
        assertEquals(Arrays.asList(1L, 10L, 100L), asLongs(finalReloaded.getNumbers()));
    }

    /**
     * 测试 List&lt;Long&gt; 属性以Json文本形式参与序列化(取值器输出的即入库内容)
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void valueCollectionIsStoredAsJson(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        StructuralType structuralType = context.getModel().getStructuralType(JavaBean.class);
        Attribute attribute = structuralType.getAttribute("Numbers");
        Attribute nullableAttribute = structuralType.getAttribute("NullableNumbers");

        assertNotNull(attribute, "Numbers应当被建模为属性");
        assertNotNull(nullableAttribute, "NullableNumbers应当被建模为属性");
        //配置序列化器后 List<Long> 折叠为string存储
        assertEquals(String.class, attribute.getDataType());
        assertEquals(String.class, nullableAttribute.getDataType());

        var bean = context.createSet(JavaBean.class).findFirst(p -> p.getIntNumber() == 3).orElse(null);
        assertNotNull(bean);

        assertEquals("[3,30,300]", attribute.getValueGetter().getValue(bean));
        //可空值类型元素中的null在Json里表现为null字面量
        assertEquals("[3,null,30]", nullableAttribute.getValueGetter().getValue(bean));
    }

    /**
     * 将集合元素统一按long比较
     * 说明：Java泛型擦除后Json反序列化得到的元素为Integer 直接比较List会因元素类型不同而不相等
     *
     * @param list 集合
     * @return 按long表示的集合
     */
    private static List<Long> asLongs(List<?> list) {
        if (list == null) return null;
        return list.stream().map(p -> p == null ? null : ((Number) p).longValue()).collect(Collectors.toList());
    }
}
