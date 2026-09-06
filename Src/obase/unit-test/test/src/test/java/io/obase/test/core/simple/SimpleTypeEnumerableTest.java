package io.obase.test.core.simple;

import io.obase.core.expression.EPredicateType;
import io.obase.core.expression.IGroupingBy;
import io.obase.core.expression.PredicateCombiner;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.JavaBean;
import io.obase.test.domain.simpleType.SimpleJavaBeanSelect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试简单类型的Enumerable扩展方法
 */
@ExtendWith(ConfigSetUp.class)
public class SimpleTypeEnumerableTest {

    /**
     * 用于测试的字段
     */
    private static final int con = 10;

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(JavaBean.class).delete(p -> true, JavaBean.class);
            //添加一批新对象
            for (int i = 1; i < 21; i++) {
                var javaBean = new JavaBean();
                javaBean.setIntNumber(i);
                javaBean.setBool(i % 2 == 0);
                javaBean.setDecimalNumber(BigDecimal.valueOf(Math.pow(Math.PI, i)));
                javaBean.setString(i + "号字符串");
                String[] strings = new String[3];
                strings[0] = String.valueOf(i - 1);
                strings[1] = String.valueOf(i);
                strings[2] = String.valueOf(i + 1);
                javaBean.setStrings(strings);
                javaBean.setDateTime(LocalDateTime.now());
                javaBean.setLongNumber(i);
                javaBean.setByteNumber((byte) i);
                javaBean.setCharNumber('\u006A');
                javaBean.setFloatNumber((float) Math.pow(Math.PI, i));
                javaBean.setDoubleNumber(Math.pow(Math.PI, i));
                javaBean.setDate(LocalDate.now());
                javaBean.setTime(LocalTime.now());
                javaBean.setUuid(UUID.randomUUID());

                if (i == 20) {
                    javaBean.setDateTime(LocalDateTime.of(1752, 1, 1, 0, 0, 0));
                }

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
            context.createSet(JavaBean.class).delete(p -> true, JavaBean.class);
        }
    }

    /**
     * 测试All方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void allTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //是否都满足
        var allResult = context.createSet(JavaBean.class).allMatch(JavaBean::getBool);
        //有一半的对象Bool为true 所以不满足
        assertFalse(allResult);

        var local = 0;
        //是否都满足
        allResult = context.createSet(JavaBean.class).allMatch(p -> p.getDoubleNumber() > local);
        //所有的对象DecimalNumber都大于0 所以满足
        assertTrue(allResult);
    }

    /**
     * 测试Any方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void anyTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //是否任意满足
        var anyResult = context.createSet(JavaBean.class).anyMatch(JavaBean::getBool);
        //有一半的对象Bool为true 所以满足
        assertTrue(anyResult);

        anyResult = context.createSet(JavaBean.class).anyMatch();
        //有20个对象 所以满足
        assertTrue(anyResult);
    }

    /**
     * 测试Avg方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void avgTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //求平均数
        var avgResult = context.createSet(JavaBean.class).avgInt(JavaBean::getIntNumber);

        //本地算一遍
        var localResult = 0d;
        for (int i = 1; i < 21; i++) {
            localResult += i;
        }
        localResult /= 20;

        assertEquals(localResult, avgResult);
    }

    /**
     * 测试Contains方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void containsTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //定义一个本地的int列表
        var ids = new ArrayList<Integer>();
        ids.add(1);
        ids.add(2);
        ids.add(3);

        List<JavaBean> list = context.createSet(JavaBean.class).filter(p -> ids.contains(p.getIntNumber())).toList();

        //有3个
        assertEquals(3, list.size());
        //本地变量
        var local1 = "2";
        var local2 = "3";
        list = context.createSet(JavaBean.class).filter(p -> p.getString().contains(local1) || p.getString().contains(local2)).toList();

        //有5个
        assertEquals(5, list.size());
    }

    /**
     * 测试StartsWith 和EndsWith 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void startWithAndEndsWithTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        String local1 = "2号字";

        List<JavaBean> containsResult = context.createSet(JavaBean.class).filter(p -> p.getString().startsWith(local1)).toList();

        //有1个
        assertEquals(1, containsResult.size());

        containsResult = context.createSet(JavaBean.class).filter(p -> p.getString().endsWith("号字符串")).toList();

        //有20个
        assertEquals(20, containsResult.size());

        containsResult = context.createSet(JavaBean.class).filter(p -> "1号字符串啊".startsWith(p.getString())).toList();

        //有1个
        if (dataSource == EDataSource.Sqlite) {
            //Sqlite会解析类名和%所以是0个
            assertEquals(0, containsResult.size());
        } else {
            assertEquals(1, containsResult.size());
        }

    }

    /**
     * 测试Count 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void countTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var local = 50D;

        //查找个数
        var countResult = context.createSet(JavaBean.class).count(p -> p.getDoubleNumber() > local && p.getIntNumber() > 0);

        assertEquals(17, countResult);

        countResult = context.createSet(JavaBean.class).count();

        assertEquals(20, countResult);
    }

    /**
     * 测试Distinct 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void distinctTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //去重结果
        var distinctResult = context.createSet(JavaBean.class).distinct().toList();

        assertEquals(20, distinctResult.size());
    }

    /**
     * 测试ElementAt 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void elementAtTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        int local = 1;
        //获取第2个元素
        var elementAtResult = context.createSet(JavaBean.class).elementAt(local);

        assertTrue(elementAtResult.isPresent());
        //第2个元素的IntNumber为2
        assertEquals(2, elementAtResult.get().getIntNumber());
    }

    /**
     * 测试First 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void firstTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //找出第一个
        var firstResult = context.createSet(JavaBean.class).findFirst(p -> p.getDoubleNumber() > 90);

        assertTrue(firstResult.isPresent());
        //第一个DecimalNumber大于90的对象的IntNumber为4
        assertEquals(4, firstResult.get().getIntNumber());
        //找出第一个
        firstResult = context.createSet(JavaBean.class).findFirst();

        assertTrue(firstResult.isPresent());
        //第一个对象的IntNumber为1
        assertEquals(1, firstResult.get().getIntNumber());
        //找出不存在的第一个
        firstResult = context.createSet(JavaBean.class).findFirst(p -> p.getDoubleNumber() == 0);
        //不存在的第一个对象为null
        assertFalse(firstResult.isPresent());
    }

    /**
     * 测试GroupingBy 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void groupingByTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //测试toHashMap方法
        var map = context.createSet(JavaBean.class).toHashMap(JavaBean::getIntNumber, JavaBean::getDoubleNumber);

        assertEquals(20, map.size());

        //测试toHashMapWithIterable方法
        var mapWithIterableResult = context.createSet(JavaBean.class).toHashMapWithIterableResult(JavaBean::getBool, p -> p);

        assertEquals(2, mapWithIterableResult.size());
        assertEquals(10, ((List<JavaBean>) mapWithIterableResult.get(true)).size());
        assertEquals(10, ((List<JavaBean>) mapWithIterableResult.get(false)).size());

        //根据某个键分组
        var javaBeanHashMap = context.createSet(JavaBean.class).filter(JavaBean::getBool).groupBy(JavaBean::getIntNumber).toHashMap(IGroupingBy::getKey, IGroupingBy::getElement);

        assertEquals(10, javaBeanHashMap.size());

        //根据某个键和结果选择器分组
        var integerDateHashMap = context.createSet(JavaBean.class).filter(JavaBean::getBool).groupBy(JavaBean::getString, JavaBean::getUuid).toHashMap(IGroupingBy::getKey, IGroupingBy::getElement);

        assertEquals(10, integerDateHashMap.size());

        //分组中使用函数
        var subStringDateHashMap = context.createSet(JavaBean.class).filter(JavaBean::getBool).groupBy(p -> p.getString().substring(1, 1), JavaBean::getUuid).toHashMapWithIterableResult(IGroupingBy::getKey, IGroupingBy::getElement);

        assertEquals(5, subStringDateHashMap.size());
    }

    /**
     * 测试Last 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void lastTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var local = 0;

        //有条件的找出最后一个
        Optional<JavaBean> lastResult = context.createSet(JavaBean.class).findLast(p -> p.getDoubleNumber() > local || p.getDoubleNumber() > 0);

        assertTrue(lastResult.isPresent());
        //为20
        assertEquals(20, lastResult.get().getIntNumber());
        //找出最后一个
        lastResult = context.createSet(JavaBean.class).findLast();

        assertTrue(lastResult.isPresent());
        //最后一个对象的IntNumber为20
        assertEquals(20, lastResult.get().getIntNumber());
        //找出不存在的最后一个
        lastResult = context.createSet(JavaBean.class).findLast(p -> p.getIntNumber() == 0);
        //为空
        assertFalse(lastResult.isPresent());
    }

    /**
     * 测试Max 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void max(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //求最大
        var distinctResult = context.createSet(JavaBean.class).max(JavaBean::getIntNumber);
        //最大值为20
        assertEquals(20, distinctResult);
    }

    /**
     * 测试Min方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void min(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //求最小
        var distinctResult = context.createSet(JavaBean.class).min(JavaBean::getIntNumber);
        //最小值为1
        assertEquals(1, distinctResult);
    }

    /**
     * 测试无条件查询
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void nullQueryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //无条件查询
        var list = context.createSet(JavaBean.class).toList();
        //有20个
        assertEquals(20, list.size());

        //无条件查询
        var array = context.createSet(JavaBean.class).toArray();

        //有20个
        assertEquals(20, array.length);
    }

    /**
     * 测试Order 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void orderTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //覆盖式排序 实际生效的是后一个
        List<JavaBean> orderResult = context.createSet(JavaBean.class).sorted(JavaBean::getDateTime).sorted(JavaBean::getIntNumber).toList();

        //有20个
        assertEquals(20, orderResult.size());
        assertEquals(1, orderResult.get(0).getIntNumber());

        //非覆盖式排序 两个生效
        orderResult = context.createSet(JavaBean.class).sorted(JavaBean::getDate).thenSortedDesc(JavaBean::getIntNumber).toList();

        //有20个
        assertEquals(20, orderResult.size());
        assertEquals(20, orderResult.get(0).getIntNumber());
    }

    /**
     * 测试Reverse 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void reverseTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //排序后倒置
        List<JavaBean> orderResult = context.createSet(JavaBean.class).sortedDesc(JavaBean::getIntNumber).reverse().toList();

        //有20个
        assertEquals(20, orderResult.size());
        assertEquals(1, orderResult.get(0).getIntNumber());
    }

    /**
     * 测试Select方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void selectTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //投影
        List<Boolean> selectResult = context.createSet(JavaBean.class).map(JavaBean::getBool, boolean.class).toList();

        //有20个
        assertEquals(20, selectResult.size());
        assertTrue(selectResult.get(1));
        assertFalse(selectResult.get(0));

        List<SimpleJavaBeanSelect> selectToEntity =
                context.createSet(JavaBean.class).map(p -> new SimpleJavaBeanSelect(p.getBool(), p.getIntNumber()), SimpleJavaBeanSelect.class).toList();

        assertEquals(20, selectToEntity.size());
    }

    /**
     * 测试Single方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void singleTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var local = 1;

        //单值
        var singleResult = context.createSet(JavaBean.class).single(p -> p.getIntNumber() == local);

        assertTrue(singleResult.isPresent());
        assertNotNull(singleResult.get());
        assertEquals(1, singleResult.get().getIntNumber());

        var ex = assertThrowsExactly(IllegalArgumentException.class, () -> {
            context.createSet(JavaBean.class).single();
        });

        assertTrue(ex.getMessage().contains("Sequence contains more than one matching element"));
    }

    /**
     * 测试Skip方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void skipTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var take = 10;

        ///跳过10个取10个
        var skipResult = context.createSet(JavaBean.class).filter(p -> p.getIntNumber() > 0).skip(10).limit(take).toList();

        //有10个
        assertEquals(10, skipResult.size());
        assertEquals(11, skipResult.get(0).getIntNumber());
    }

    /**
     * 测试Sum方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void sumTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        ///求和
        var sumInt = context.createSet(JavaBean.class).sumInt(JavaBean::getIntNumber);
        //本地算一遍
        var localResult = 0;
        for (int i = 1; i < 21; i++) localResult += i;
        //结果和本地的相等
        assertEquals(localResult, sumInt);
    }

    /**
     * 测试Filter 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void whereTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        LocalDateTime date = LocalDateTime.now().plusMinutes(1);
        //测试时间条件
        List<JavaBean> list = context.createSet(JavaBean.class).filter(p -> p.getDateTime().isAfter(date)).toList();

        //有0个
        assertEquals(0, list.size());

        //测试较小的LocalDateTime
        var minDate = LocalDateTime.of(1752, 1, 1, 0, 0, 0);
        list = context.createSet(JavaBean.class).filter(p -> p.getDateTime().equals(minDate)).toList();

        //有1个
        assertEquals(1, list.size());

        var local = 987D;

        //复杂条件
        list = context.createSet(JavaBean.class)
                .filter(p -> p.getIntNumber() > con && p.getDoubleNumber() > local).toList();

        //有10个
        assertEquals(10, list.size());

        //几种布尔值的查询
        list = context.createSet(JavaBean.class).filter(p -> !p.getBool()).toList();

        assertEquals(10, list.size());

        list = context.createSet(JavaBean.class).filter(JavaBean::getBool).toList();

        assertEquals(10, list.size());

        //布尔值和其他的组合
        list = context.createSet(JavaBean.class).filter(p -> p.getBool() && p.getIntNumber() > 0).toList();

        assertEquals(10, list.size());

        list = context.createSet(JavaBean.class).filter(p -> !p.getBool() && p.getIntNumber() > 0).toList();

        assertEquals(10, list.size());

        //常数表达式置于比较前方
        list = context.createSet(JavaBean.class).filter(p -> 10 < p.getIntNumber() && local < p.getDoubleNumber()).toList();

        //有10个
        assertEquals(10, list.size());

        list = context.createSet(JavaBean.class).filter(p -> null != p.getString()).toList();

        //有10个
        assertEquals(20, list.size());

        //字符串条件
        list = context.createSet(JavaBean.class).filter(p -> p.getString() != null).toList();

        //有20个
        assertEquals(20, list.size());

        list = context.createSet(JavaBean.class).filter(p -> !"".equals(p.getString())).toList();

        //有20个
        assertEquals(20, list.size());

        String empty = "";

        list = context.createSet(JavaBean.class).filter(p -> !p.getString().equals(empty)).toList();

        //有20个
        assertEquals(20, list.size());

        list = context.createSet(JavaBean.class).filter(p -> p.getString().equals(empty)).toList();


        //有0个
        assertEquals(0, list.size());

        JavaBean[] beans = context.createSet(JavaBean.class).filter(p -> p.getString().equals(empty)).toArray();

        //有0个
        assertEquals(0, beans.length);

        String n = null;

        beans = context.createSet(JavaBean.class).filter(p -> p.getString().equals(n)).toArray();

        //有0个
        assertEquals(0, beans.length);

        beans = context.createSet(JavaBean.class).filter(p -> p.getString() == n).toArray();

        //有0个
        assertEquals(0, beans.length);
    }

    /**
     * 测试FilterIf 方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void whereIfTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //传入的参数肯定不是OLE DB 所以拼接了p => p.Bool
        List<JavaBean> result = context.createSet(JavaBean.class).filter(p -> p.getDoubleNumber() > 0).filter(p -> p.getIntNumber() > 0).filterIf(dataSource != EDataSource.Oledb, p -> p.getBool()).toList();

        assertEquals(10, result.size());
    }

    /**
     * 测试静态变量
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void consTest(EDataSource dataSource) {

        var context = ContextUtils.createContext(dataSource);
        //只有一个true 表示全查询
        var list = context.createSet(JavaBean.class).filter(p -> true).toList();
        assertEquals(20, list.size());
        //只有一个false 表示不查询
        list = context.createSet(JavaBean.class).filter(p -> false).toList();
        assertEquals(0, list.size());
        //1==1 等同于全查询
        var combiner = new PredicateCombiner<JavaBean>(p -> 1 == 1);
        list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        assertEquals(20, list.size());
        //1!=1 等同不查询
        combiner = new PredicateCombiner<>(p -> 1 != 1);
        list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        assertEquals(0, list.size());
        //几种常见的静态变量拼接
        list = context.createSet(JavaBean.class).filter(p -> !p.getBool()).toList();
        assertEquals(10, list.size());

        combiner = new PredicateCombiner<>(p -> 1 == 1);
        combiner.and(JavaBean::getBool);
        list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        assertEquals(10, list.size());

        list = context.createSet(JavaBean.class).filter(p -> !p.getBool() || p.getIntNumber() > 0).toList();
        assertEquals(20, list.size());

        combiner = new PredicateCombiner<>(p -> true);
        combiner.and(JavaBean::getBool);
        list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        assertEquals(10, list.size());

        combiner = new PredicateCombiner<>(p -> true);
        combiner.or(JavaBean::getBool);
        list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        assertEquals(20, list.size());
    }

    /**
     * 测试基础的表达式拼接
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void combinerTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //拼接一个全查询 然后再拼接 DoubleNumber > 987 && IntNumber > con
        var combiner = new PredicateCombiner<JavaBean>(p -> 1 == 1);
        combiner.and(p -> p.getDoubleNumber() > 987D);
        combiner.and(p -> p.getIntNumber() > con);
        var list = context.createSet(JavaBean.class).filter(combiner.getLambdaExpression()).toList();
        //有10个
        assertEquals(10, list.size());

        //拼接DoubleNumber > 50 && IntNumber > 0
        combiner = new PredicateCombiner<>(p -> p.getDoubleNumber() > 50D);
        combiner.and(p -> p.getIntNumber() > 0);
        //查找个数
        var countResult = context.createSet(JavaBean.class).count(combiner.getLambdaExpression());

        assertEquals(17, countResult);

        var local = 0;
        LocalDateTime now = LocalDateTime.now();
        //拼接 DoubleNumber > local || DateTime > now
        combiner = new PredicateCombiner<>(p -> p.getDoubleNumber() > local);
        combiner.or(p -> p.getDateTime().isAfter(now));
        var allResult = context.createSet(JavaBean.class).allMatch(combiner.getLambdaExpression());

        assertTrue(allResult);
        //拼接空条件
        combiner = new PredicateCombiner<>(null);
        var anyResult = context.createSet(JavaBean.class).anyMatch(combiner.getLambdaExpression());

        assertTrue(anyResult);

        combiner = new PredicateCombiner<>(p -> p.getDoubleNumber() > 0);
        //找出第一个
        var firstResult = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression());

        assertTrue(firstResult.isPresent());

        assertEquals(1, firstResult.get().getIntNumber());

        //拼接 DoubleNumber > local || DoubleNumber > 0
        combiner = new PredicateCombiner<>(p -> p.getDoubleNumber() > local);
        combiner.or(p -> p.getDoubleNumber() > 0);
        //找出第一个
        var lastResult = context.createSet(JavaBean.class).findLast(combiner.getLambdaExpression());

        assertTrue(lastResult.isPresent());

        assertEquals(20, lastResult.get().getIntNumber());

        combiner = new PredicateCombiner<>(p -> p.getIntNumber() == 1);
        //单值
        var singleResult = context.createSet(JavaBean.class).single(combiner.getLambdaExpression());

        assertTrue(singleResult.isPresent());
        assertNotNull(singleResult.get());
        assertEquals(1, singleResult.get().getIntNumber());

        //拼接DoubleNumber > local || DoubleNumber == local
        combiner = new PredicateCombiner<>(p -> true);
        combiner.and(p -> p.getDoubleNumber() > local || p.getDoubleNumber() == local);
        //找出第一个
        lastResult = context.createSet(JavaBean.class).findLast(combiner.getLambdaExpression());

        assertTrue(lastResult.isPresent());

        assertEquals(20, lastResult.get().getIntNumber());
    }

    /**
     * 测试进阶的表达式拼接
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void combinerPlusTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        var combiner = new PredicateCombiner<JavaBean>();
        //拼接IntNumber == 1 && String == "1号字符串" && DoubleNumber, > 1 && String.Contains("1号")
        combiner.and(combiner.getWrapper().eq(JavaBean::getIntNumber, 1)).and(JavaBean::getString, EPredicateType.Equal, "1号字符串");
        combiner.and(combiner.getWrapper().gt(JavaBean::getDoubleNumber, 1D)).and(JavaBean::getString, EPredicateType.Contains, "1号");

        var bean = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接Bool == true && DateTime <= Now && UUID != Random
        combiner = new PredicateCombiner<>();
        combiner.and(combiner.getWrapper().eq(JavaBean::getBool, true)).and(JavaBean::getDateTime, EPredicateType.LessThanOrEqual, LocalDateTime.now().plusMinutes(1));
        combiner.and(combiner.getWrapper().ne(JavaBean::getUuid, UUID.randomUUID()));

        bean = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接DecimalNumber > 0 || DoubleNumber > 1 || String.StartWith("字符串") && FloatNumber != 0
        combiner = new PredicateCombiner<>();
        combiner.and(combiner.getWrapper().gt(JavaBean::getDecimalNumber, new BigDecimal(0))).or(JavaBean::getDoubleNumber, EPredicateType.GreaterThan, 1D);
        combiner.or(combiner.getWrapper().sw(JavaBean::getString, "字符串")).and(JavaBean::getFloatNumber, EPredicateType.NotEqual, 0F);

        bean = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接String != null && IntNumber == 1 && DoubleNumber > 1 && IntNumber >= 0 && Strings != null
        combiner = new PredicateCombiner<>();
        combiner.notEqual(JavaBean::getString, null).equal(JavaBean::getIntNumber, 1);
        combiner.greaterThan(JavaBean::getDoubleNumber, 1D).greaterThanOrEqual(JavaBean::getIntNumber, 0)
                .and(p -> p.getStrings() != null);

        bean = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //拼接String.Contains("号") && String.StartWith("1") && String.EndWith("字符串") && String != null
        combiner = new PredicateCombiner<>();
        combiner.contains(JavaBean::getString, "号").startsWith(JavaBean::getString, "1").endsWith(JavaBean::getString, "字符串").and(p -> p.getStrings() != null);

        bean = context.createSet(JavaBean.class).findFirst(combiner.getLambdaExpression()).orElse(null);

        assertNotNull(bean);

        //复杂条件拼接 ((IntNumber == 1 || IntNumber == 20) && (String == "1号字符串" || String == "19号字符串"))
        var sub1 = new PredicateCombiner<JavaBean>();
        sub1.or(combiner.getWrapper().eq(JavaBean::getIntNumber, 1)).or(combiner.getWrapper().eq(JavaBean::getIntNumber, 20));
        var sub2 = new PredicateCombiner<JavaBean>();
        sub2.or(combiner.getWrapper().eq(JavaBean::getString, "1号字符串")).or(combiner.getWrapper().eq(JavaBean::getString, "19号字符串"));

        var list = context.createSet(JavaBean.class).filter(PredicateCombiner.and(sub1.getLambdaExpression(), sub2.getLambdaExpression())).toList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getIntNumber());
        assertEquals("1号字符串", list.get(0).getString());

        //复杂条件拼接 ((IntNumber == 1 && IntNumber < 20) && (String == "1号字符串" || String == "19号字符串") && (1=1))
        sub1 = new PredicateCombiner<>();
        sub1.or(combiner.getWrapper().eq(JavaBean::getIntNumber, 1)).and(combiner.getWrapper().lt(JavaBean::getIntNumber, 20));
        sub2 = new PredicateCombiner<>();
        sub2.or(combiner.getWrapper().eq(JavaBean::getString, "1号字符串")).or(combiner.getWrapper().eq(JavaBean::getString, "19号字符串"));
        var sub3 = new PredicateCombiner<JavaBean>(p -> true);

        list = context.createSet(JavaBean.class).filter(PredicateCombiner.and(PredicateCombiner.and(sub1.getLambdaExpression(), sub2.getLambdaExpression()), sub3.getLambdaExpression())).toList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getIntNumber());
        assertEquals("1号字符串", list.get(0).getString());
    }
}
