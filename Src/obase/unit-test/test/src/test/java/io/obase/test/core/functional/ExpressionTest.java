package io.obase.test.core.functional;

import io.obase.core.expression.*;
import io.obase.test.ConfigSetUp;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.expression.Box;
import io.obase.test.domain.functional.expression.Can;
import io.obase.test.domain.functional.expression.WaterTank;
import io.obase.test.domain.simpleType.JavaBean;
import io.obase.test.domain.simpleType.NullableJavaBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表达式测试
 * 测试仿照C#的表达式定义的表达式部分
 */
@ExtendWith(ConfigSetUp.class)
public class ExpressionTest {

    /**
     * 初始化方法
     * ConfigSetUp依赖于此方法的调用后回调 测试类必须保留此方法 无初始化则不需要内容
     */
    @BeforeAll
    public static void beforeAll() {
        //初始化使用的实体类的方法检查器
        MethodChecker.registerClassMethod(JavaBean.class);
        MethodChecker.registerClassMethod(NullableJavaBean.class);
        MethodChecker.registerClassMethod(JavaBeanSelectResult.class);
        MethodChecker.registerClassMethod(Box.class);
        MethodChecker.registerClassMethod(Can.class);
        MethodChecker.registerClassMethod(WaterTank.class);
    }

    /**
     * 测试表达式翻译
     * 测试成员访问表达式
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate0() {
        //构造表达式翻译器
        var translator = new LambdaTranslator();
        //访问IntNumber字段
        SerializedFunction<JavaBean, Integer> member = JavaBean::getIntNumber;

        //进行翻译
        var lambdaExpression = translator.getLambdaExpression(member);

        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());

        //成员访问表达式 访问的是IntNumber
        assertEquals(MemberExpression.class, lambdaExpression.getBody().getClass());
        var memberExpression = (MemberExpression) lambdaExpression.getBody();
        assertEquals("IntNumber", memberExpression.getMemberName());
    }

    /**
     * 测试表达式翻译
     * 测试简单的翻译
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate1() {

        //构造表达式翻译器
        var translator = new LambdaTranslator();

        //测试简单的翻译
        //表达式含义为 bool为true 并且 byteNumber 大于 10
        //经JINQ解析后 转换为 bool为true 并且 byteNumber 大于 10
        SerializedPredicate<JavaBean> predicate = javaBean -> javaBean.getBool() && javaBean.getByteNumber() > 10;
        //翻译结果应该是一个Lambda表达式 以下称之为Lambda表达式
        //Lambda表达式的表达式体是一个二元表达式 运算符是并且 以下称之为二元表达式1 表示 "bool为true 并且 byteNumber 大于 10"
        //二元表达式1的左端是一个成员访问表达式 访问的是bool 以下称之为成员访问表达式1 表示 "bool字段为true"
        //二元表达式1的右端是一个二元表达式 运算符是大于 以下称之为二元表达式2 表示 "byteNumber 大于 10"
        //二元表达式2的左端是一个成员访问访问表达式 访问的是bool 以下称之为成员访问表达式2 表示 "byteNumber"
        //二元表达式2的右端是一个常量表达式 值是10  以下称之为常量表达式 表示 "10"
        //进行翻译
        var lambdaExpression = translator.getLambdaExpression(predicate);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //二元表达式1 运算符是并且
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        var binaryExpression1 = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.AndAlso, binaryExpression1.getExpressionType());

        //成员访问表达式1 访问的是bool
        assertEquals(MemberExpression.class, binaryExpression1.getLeft().getClass());
        var memberExpression1 = ((MemberExpression) (binaryExpression1).getLeft());
        assertEquals("Bool", memberExpression1.getMemberName());

        //二元表达式2 运算符是大于
        assertEquals(BinaryExpression.class, binaryExpression1.getRight().getClass());
        var binaryExpression2 = (BinaryExpression) binaryExpression1.getRight();
        assertEquals(EExpressionType.GreaterThan, binaryExpression2.getExpressionType());

        //成员访问表达式2 访问的是byteNumber
        assertEquals(MemberExpression.class, binaryExpression2.getLeft().getClass());
        var memberExpression2 = (MemberExpression) binaryExpression2.getLeft();
        assertEquals("ByteNumber", memberExpression2.getMemberName());
        //常量表达式 值是10
        assertEquals(ConstantExpression.class, binaryExpression2.getRight().getClass());
        var constantExpression = (ConstantExpression) binaryExpression2.getRight();
        assertEquals(10, constantExpression.getValue());
    }

    /**
     * 测试表达式翻译
     * 测试测试时间条件
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate2() {
        //构造表达式翻译器
        var translator = new LambdaTranslator();

        var now = LocalDateTime.now();
        var yesterday = now.minusDays(1);
        var tomorrow = now.plusDays(1);

        //测试时间条件
        //表达式含义为 (dateTime小于今天 并且 dateTime大于昨天) 或者 大于明天的
        //经JINQ解析后 转换为 ((dateTime大于等于今天 并且 dateTime大于明天) 或者 (dateTime小于今天 并且 dateTime大于昨天)) 或者 ((dateTime小于今天 并且 dateTime小于等于昨天) 并且 dateTime大于明天)
        //化简后 取或者的 后半部分 "((dateTime小于今天 并且 dateTime小于等于昨天) 并且 dateTime大于明天)" 这一部分不可能满足 故仅有前半部分有效
        //前半部分中 "(dateTime大于等于今天 并且 dateTime大于明天)" 仅有dateTime大于明天生效 故可化简为 dateTime大于明天
        //即 dateTime大于明天的 或者 (dateTime小于今天 并且 dateTime大于昨天) 与原意相同
        SerializedPredicate<JavaBean> predicate = javaBean -> (javaBean.getDateTime().isBefore(now) && javaBean.getDateTime().isAfter(yesterday)) || javaBean.getDateTime().isAfter(tomorrow);

        //翻译结果应该是一个Lambda表达式 以下称之为Lambda表达式
        //Lambda表达式的表达式体是一个二元表达式 运算符是或者 以下称之为二元表达式1 表示 "((dateTime大于等于今天 并且 dateTime大于明天) 或者 (dateTime小于今天 并且 dateTime大于昨天)) 或者 ((dateTime小于今天 并且 dateTime小于等于昨天) 并且 dateTime大于明天)"
        //二元表达式1的左端是一个二元表达式 运算符是或者 以下称之为二元表达式2 表示 "((dateTime大于等于今天 并且 dateTime大于明天) 或者 (dateTime小于今天 并且 dateTime大于昨天))"
        //二元表达式1的右端是一个二元表达式 运算符是并且 以下称之为二元表达式3 表示 "((dateTime小于今天 或者 dateTime小于昨天) 并且 dateTime大于明天)"
        //二元表达式2的左端是一个二元表达式 运算符是并且 以下称之为二元表达式4 表示 "(dateTime大于等于今天 并且 dateTime大于明天)"
        //二元表达式2的右端是一个二元表达式 运算符是并且 以下称之为二元表达式5 表示 "(dateTime小于今天 并且 dateTime大于昨天)"
        //二元表达式4的左端是一个二元表达式 运算符是大于等于 以下称之为二元表达式6 表示 "dateTime大于等于今天"
        //二元表达式4的右端是一个二元表达式 运算符是大于 以下称之为二元表达式7 表示 "dateTime大于明天"
        //二元表达式6的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式1 表示 "dateTime"
        //二元表达式6的右端是一个常量表达式 值是yesterday 以下称之为常量表达式1 表示"今天"
        //二元表达式7的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式2
        //二元表达式7的右端是一个常量表达式 值是tomorrow 以下称之为常量表达式2 表示"明天"
        //二元表达式5的左端是一个二元表达式 运算符是小于 以下称之为二元表达式8 表示 "dateTime小于今天"
        //二元表达式5的右端是一个二元表达式 运算符是大于 以下称之为二元表达式9 表示 "dateTime大于昨天"
        //二元表达式8的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式3 表示 "dateTime"
        //二元表达式8的右端是一个常量表达式 值是yesterday 以下称之为常量表达式3 表示"今天"
        //二元表达式9的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式4
        //二元表达式9的右端是一个常量表达式 值是tomorrow 以下称之为常量表达式4 表示"昨天"
        //二元表达式3的左端是一个二元表达式 运算符是并且 以下称之为二元表达式10 表示 "(dateTime小于今天 并且 dateTime小于等于昨天)"
        //二元表达式3的右端是一个二元表达式 运算符是大于 以下称之为二元表达式11 表示 "dateTime大于明天"
        //二元表达式10的左端是一个二元表达式 运算符是小于 以下称之为二元表达式12 表示 "dateTime小于今天"
        //二元表达式10的右端是一个二元表达式 运算符是小于等于 以下称之为二元表达式13 表示 "dateTime小于等于昨天"
        //二元表达式12的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式5 表示 "dateTime"
        //二元表达式12的右端是一个常量表达式 值是yesterday 以下称之为常量表达式5 表示"今天"
        //二元表达式13的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式6
        //二元表达式13的右端是一个常量表达式 值是tomorrow 以下称之为常量表达式6 表示"昨天"
        //二元表达式11的左端是一个成员访问表达式 访问的是dateTime 以下称之为成员访问表达式7 表示 "dateTime"
        //二元表达式11的右端是一个常量表达式 值是tomorrow 以下称之为常量表达式7 表示"明天"

        //进行翻译
        var lambdaExpression = translator.getLambdaExpression(predicate);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());

        //二元表达式1 运算符是或者
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        var binaryExpression1 = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.OrElse, binaryExpression1.getExpressionType());

        //二元表达式2 运算符是或者
        assertEquals(BinaryExpression.class, binaryExpression1.getLeft().getClass());
        var binaryExpression2 = (BinaryExpression) binaryExpression1.getLeft();
        assertEquals(EExpressionType.OrElse, binaryExpression2.getExpressionType());

        //二元表达式3 运算符是或者
        assertEquals(BinaryExpression.class, binaryExpression1.getRight().getClass());
        var binaryExpression3 = (BinaryExpression) binaryExpression1.getRight();
        assertEquals(EExpressionType.AndAlso, binaryExpression3.getExpressionType());

        //二元表达式4 运算符是并且
        assertEquals(BinaryExpression.class, binaryExpression2.getLeft().getClass());
        var binaryExpression4 = (BinaryExpression) binaryExpression2.getLeft();
        assertEquals(EExpressionType.AndAlso, binaryExpression4.getExpressionType());

        //二元表达式5 运算符是并且
        assertEquals(BinaryExpression.class, binaryExpression2.getRight().getClass());
        var binaryExpression5 = (BinaryExpression) binaryExpression2.getRight();
        assertEquals(EExpressionType.AndAlso, binaryExpression5.getExpressionType());

        //二元表达式6 运算符是大于等于
        assertEquals(BinaryExpression.class, binaryExpression4.getLeft().getClass());
        var binaryExpression6 = (BinaryExpression) binaryExpression4.getLeft();
        assertEquals(EExpressionType.GreaterThanOrEqual, binaryExpression6.getExpressionType());

        //二元表达式7 运算符是大于
        assertEquals(BinaryExpression.class, binaryExpression4.getRight().getClass());
        var binaryExpression7 = (BinaryExpression) binaryExpression4.getRight();
        assertEquals(EExpressionType.GreaterThan, binaryExpression7.getExpressionType());

        //成员访问表达式1 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression6.getLeft().getClass());
        var memberExpression1 = (MemberExpression) binaryExpression6.getLeft();
        assertEquals("DateTime", memberExpression1.getMemberName());
        //常量表达式1 值是now
        assertEquals(ConstantExpression.class, binaryExpression6.getRight().getClass());
        var constantExpression1 = (ConstantExpression) binaryExpression6.getRight();
        assertEquals(now, constantExpression1.getValue());

        //成员访问表达式2 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression7.getLeft().getClass());
        var memberExpression2 = (MemberExpression) binaryExpression7.getLeft();
        assertEquals("DateTime", memberExpression2.getMemberName());
        //常量表达式2 值是tomorrow
        assertEquals(ConstantExpression.class, binaryExpression7.getRight().getClass());
        var constantExpression2 = (ConstantExpression) binaryExpression7.getRight();
        assertEquals(tomorrow, constantExpression2.getValue());

        //二元表达式8 运算符是小于
        assertEquals(BinaryExpression.class, binaryExpression5.getLeft().getClass());
        var binaryExpression8 = (BinaryExpression) binaryExpression5.getLeft();
        assertEquals(EExpressionType.LessThan, binaryExpression8.getExpressionType());

        //二元表达式9 运算符是大于
        assertEquals(BinaryExpression.class, binaryExpression5.getRight().getClass());
        var binaryExpression9 = (BinaryExpression) binaryExpression5.getRight();
        assertEquals(EExpressionType.GreaterThan, binaryExpression9.getExpressionType());

        //成员访问表达式3 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression8.getLeft().getClass());
        var memberExpression3 = (MemberExpression) binaryExpression8.getLeft();
        assertEquals("DateTime", memberExpression3.getMemberName());
        //常量表达式3 值是now
        assertEquals(ConstantExpression.class, binaryExpression8.getRight().getClass());
        var constantExpression3 = (ConstantExpression) binaryExpression8.getRight();
        assertEquals(now, constantExpression3.getValue());

        //成员访问表达式4 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression9.getLeft().getClass());
        var memberExpression4 = (MemberExpression) binaryExpression9.getLeft();
        assertEquals("DateTime", memberExpression4.getMemberName());
        //常量表达式4 值是yesterday
        assertEquals(ConstantExpression.class, binaryExpression9.getRight().getClass());
        var constantExpression4 = (ConstantExpression) binaryExpression9.getRight();
        assertEquals(yesterday, constantExpression4.getValue());

        //二元表达式10 运算符是并且
        assertEquals(BinaryExpression.class, binaryExpression3.getLeft().getClass());
        var binaryExpression10 = (BinaryExpression) binaryExpression3.getLeft();
        assertEquals(EExpressionType.AndAlso, binaryExpression10.getExpressionType());

        //二元表达式11 运算符是并且
        assertEquals(BinaryExpression.class, binaryExpression3.getRight().getClass());
        var binaryExpression11 = (BinaryExpression) binaryExpression3.getRight();
        assertEquals(EExpressionType.GreaterThan, binaryExpression11.getExpressionType());

        //二元表达式12 运算符是小于
        assertEquals(BinaryExpression.class, binaryExpression10.getLeft().getClass());
        var binaryExpression12 = (BinaryExpression) binaryExpression10.getLeft();
        assertEquals(EExpressionType.LessThan, binaryExpression12.getExpressionType());

        //二元表达式13 运算符是小于等于
        assertEquals(BinaryExpression.class, binaryExpression10.getRight().getClass());
        var binaryExpression13 = (BinaryExpression) binaryExpression10.getRight();
        assertEquals(EExpressionType.LessThanOrEqual, binaryExpression13.getExpressionType());

        //成员访问表达式5 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression12.getLeft().getClass());
        var memberExpression5 = (MemberExpression) binaryExpression12.getLeft();
        assertEquals("DateTime", memberExpression5.getMemberName());
        //常量表达式5 值是now
        assertEquals(ConstantExpression.class, binaryExpression12.getRight().getClass());
        var constantExpression5 = (ConstantExpression) binaryExpression12.getRight();
        assertEquals(now, constantExpression5.getValue());

        //成员访问表达式6 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression13.getLeft().getClass());
        var memberExpression6 = (MemberExpression) binaryExpression13.getLeft();
        assertEquals("DateTime", memberExpression6.getMemberName());
        //常量表达式6 值是yesterday
        assertEquals(ConstantExpression.class, binaryExpression13.getRight().getClass());
        var constantExpression6 = (ConstantExpression) binaryExpression13.getRight();
        assertEquals(yesterday, constantExpression6.getValue());

        //成员访问表达式7 访问的是dateTime
        assertEquals(MemberExpression.class, binaryExpression11.getLeft().getClass());
        var memberExpression7 = (MemberExpression) binaryExpression11.getLeft();
        assertEquals("DateTime", memberExpression7.getMemberName());
        //常量表达式7 值是tomorrow
        assertEquals(ConstantExpression.class, binaryExpression11.getRight().getClass());
        var constantExpression7 = (ConstantExpression) binaryExpression11.getRight();
        assertEquals(tomorrow, constantExpression7.getValue());
    }

    /**
     * 测试表达式翻译
     * 测试表达式中含有包装类的翻译
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate3() {
        //构造表达式翻译器
        var translator = new LambdaTranslator();

        //测试表达式中含有包装类的翻译
        //boolean byte short int long float double char这些基本类型的包装类 应当直接翻译为二元表达式 左端是成员表达式 右端是静态表达式
        //Boolean
        var value = true;
        SerializedPredicate<NullableJavaBean> predicateN = javaBean -> javaBean.getBool() == value;

        //进行翻译
        var lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        var binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        var memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("Bool", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        var constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(true, constantExpressionN.getValue());

        //Byte
        predicateN = javaBean -> javaBean.getByteNumber() == 70;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("ByteNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(70, constantExpressionN.getValue());

        //Byte
        predicateN = javaBean -> javaBean.getByteNumber() == 70;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("ByteNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(70, constantExpressionN.getValue());

        //Short
        predicateN = javaBean -> javaBean.getShortNumber() == 170;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("ShortNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(170, constantExpressionN.getValue());

        //Int
        predicateN = javaBean -> javaBean.getIntNumber() == 40;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("IntNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(40, constantExpressionN.getValue());

        //Long
        predicateN = javaBean -> javaBean.getLongNumber() == 110;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("LongNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(110L, constantExpressionN.getValue());

        //Float
        predicateN = javaBean -> javaBean.getFloatNumber() == 110.2;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("FloatNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(110.2, constantExpressionN.getValue());

        //Double
        predicateN = javaBean -> javaBean.getDoubleNumber() == 110.13;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("DoubleNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(110.13, constantExpressionN.getValue());

        //Char
        predicateN = javaBean -> javaBean.getCharNumber() == 'F';

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateN);
        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("CharNumber", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals((int) 'F', constantExpressionN.getValue());
    }

    /**
     * 测试表达式翻译
     * 测试表达式中含有转换的翻译
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate4() {
        //构造表达式翻译器
        var translator = new LambdaTranslator();

        //测试表达式中含有转换的翻译
        //有两种转换 一种是基础类型转换 一种是关联对象的类型转换
        //此处将long类型转为int 在表达式层面与普通的表达式相同
        SerializedPredicate<Box> predicateB = box -> (int) box.getId() == 1;

        //进行翻译
        var lambdaExpression = translator.getLambdaExpression(predicateB);

        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());

        //表达式体是二元表达式 左端是成员访问表达式 右端是常量表达式
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        var binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());
        //左端 成员访问表达式
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        var memberExpressionB = (MemberExpression) binaryExpression.getLeft();
        assertEquals("Id", memberExpressionB.getMemberName());
        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        var constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(1, constantExpressionN.getValue());

        //此处将关联的Can转换为子类WaterTank 并使用WaterTank的vol属性查询
        predicateB = box -> ((WaterTank) box.getCan()).getVol() == 10L;

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(predicateB);

        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());

        //表达式体是二元表达式 左端是成员访问表达式1 右端是常量表达式
        //成员访问表达式1的Host是一元表达式 访问的是vol
        //一元表达式的操作数是 成员访问表达式2 类型是Convert
        //成员表达式2访问的是can
        assertEquals(BinaryExpression.class, lambdaExpression.getBody().getClass());
        binaryExpression = (BinaryExpression) lambdaExpression.getBody();
        assertEquals(EExpressionType.Equal, binaryExpression.getExpressionType());

        //左端 成员访问表达式1 访问vol
        assertEquals(MemberExpression.class, binaryExpression.getLeft().getClass());
        var memberExpression1 = (MemberExpression) binaryExpression.getLeft();
        assertEquals("Vol", memberExpression1.getMemberName());
        //成员访问表达式1的Host是一元表达式 操作是Convert
        assertEquals(UnaryExpression.class, memberExpression1.getHost().getClass());
        UnaryExpression unaryExpression = (UnaryExpression) memberExpression1.getHost();
        assertEquals(EExpressionType.Convert, unaryExpression.getExpressionType());
        //一元表达式操作数是 成员访问表达式2 访问的是can
        assertEquals(MemberExpression.class, unaryExpression.getOperand().getClass());
        var memberExpression2 = (MemberExpression) unaryExpression.getOperand();
        assertEquals("Can", memberExpression2.getMemberName());

        //右端 常量表达式
        assertEquals(ConstantExpression.class, binaryExpression.getRight().getClass());
        constantExpressionN = (ConstantExpression) binaryExpression.getRight();
        assertEquals(10L, constantExpressionN.getValue());
    }

    /**
     * 测试表达式翻译
     * 测试表达式中有多个布尔值的翻译
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testTranslate5() throws NoSuchMethodException {
        //构造表达式翻译器
        LambdaTranslator translator = new LambdaTranslator();

        //测试表达式中有多个布尔值的翻译
        //表达式的含义为 获取 大的但不是高质量的 或者 高质量但不是大的 或者 有盖子的
        //经JINQ解析后 转换为 (不是大的 并且 是大的 并且 是有盖子的) 或者 (不是大的 并且 不是大的 并且 是高质量的) 或者 (不是大的 并且 不是大的 并且 不是高质量的 并且 是有盖子的) 或者 (是大的 并且 不是高质量的) 或者 (是大的 并且 是高质量的 并且 是大的 并且 是有盖子的)
        // 或者 (是大的 并且 是高质量的 并且 不是大的 并且 是高质量的) 或者 (是大的 并且 是高质量的 并且 不是大的 并且 不是高质量的 并且 是有盖子的)
        SerializedPredicate<Box> predicate = box -> (box.getIsBig() && !box.getIsGood()) || (!box.getIsBig() && box.getIsGood()) || box.getHasCover();

        //进行翻译
        LambdaExpression lambdaExpression = translator.getLambdaExpression(predicate);

        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(lambdaExpression);
        assertNotNull(lambdaExpression.getBody());
        //此处不进行具体验证了 结构比较复杂
        //仅对JINQ进行化简
        //"(不是大的 并且 是大的 并且 是有盖子的)" 自相矛盾 不生效
        //"(不是大的 并且 不是大的 并且 是高质量的)" 化简为 (不是大的 并且 是高质量的)
        //"(不是大的 并且 不是大的 并且 不是高质量的 并且 是有盖子的)" 化简为 (不是大的 并且 不是高质量的 并且 是有盖子的)
        //"(是大的 并且 不是高质量的)" 化简为  (是大的 并且 不是高质量的)
        //"(是大的 并且 是高质量的 并且 是大的 并且 是有盖子的)" 化简为 (是大的 并且 是高质量的 并且 是有盖子的)
        //"(是大的 并且 是高质量的 并且 不是大的 并且 是高质量的)" 自相矛盾 不生效
        //"(是大的 并且 是高质量的 并且 不是大的 并且 不是高质量的 并且 是有盖子的)" 自相矛盾 不生效
        //初步化简为 (不是大的 并且 是高质量的) 或者 (不是大的 并且 不是高质量的 并且 是有盖子的) 或者 (是大的 并且 不是高质量的) 或者 (是大的 并且 是高质量的 并且 是有盖子的)
        //与原判断相同 但过于复杂 此时应使用谓词逻辑拼合器来处理

        //分别构造三个拼合器 对应条件的三个部分
        PredicateCombiner<Box> boxPredicateCombiner1 = new PredicateCombiner<>();
        boxPredicateCombiner1.and(box -> box.getIsBig());
        boxPredicateCombiner1.and(box -> !box.getIsGood());
        PredicateCombiner<Box> boxPredicateCombiner2 = new PredicateCombiner<>();
        boxPredicateCombiner2.and(box -> !box.getIsBig());
        boxPredicateCombiner2.and(box -> box.getIsGood());
        PredicateCombiner<Box> boxPredicateCombiner3 = new PredicateCombiner<>(box -> box.getHasCover());
        //使用静态方法来拼合最终的表达式
        //组合的结果是((是大的 并且 不是高质量的) 或者 (是高质量的 并且 不是大的)) 或者 (是有盖子的)
        LambdaExpression tempCombinerLambdaExpression = PredicateCombiner.or(boxPredicateCombiner1.getLambdaExpression(), boxPredicateCombiner2.getLambdaExpression());
        LambdaExpression combinerLambdaExpression = PredicateCombiner.or(tempCombinerLambdaExpression, boxPredicateCombiner3.getLambdaExpression());

        //校验结果
        //Lambda表达式 有表达式体
        assertNotNull(combinerLambdaExpression);
        assertNotNull(combinerLambdaExpression.getBody());

        //Lambda表达式的表达式体是一个二元表达式 操作符是或者 以下称之为二元表达式1
        //二元表达式1的左端是一个二元表达式 操作符是或者 以下称之为二元表达式2
        //二元表达式1的右端是一个成员访问表达式 访问的是hasCover 以下称之为成员访问表达式1
        //二元表达式2的左端是一个二元表达式 操作符是并且 以下称之为二元表达式3
        //二元表达式2的右端是一个二元表达式 操作符是并且 以下称之为二元表达式4
        //二元表达式3的左端是一个成员访问表达式 访问的是 isBig 以下称之为成员访问表达式2
        //二元表达式3的右端是一个一元表达式 操作符是取反 以下称之为一元表达式1
        //一元表达式1的操作数是一个成员访问表达式 访问的是 isGood 以下称之为成员访问表达式2
        //二元表达式4的左端是一个一元表达式 操作符是取反 以下称之为一元表达式2
        //一元表达式2的操作数是一个成员访问表达式 访问的是 isBig  以下称之为成员访问表达式3
        //二元表达式4的右端是一个成员访问表达式 访问的是isGood  以下称之为成员访问表达式4

        //表达式体是二元表达式1
        assertEquals(BinaryExpression.class, combinerLambdaExpression.getBody().getClass());
        BinaryExpression binaryExpression1 = (BinaryExpression) combinerLambdaExpression.getBody();
        assertEquals(EExpressionType.OrElse, binaryExpression1.getExpressionType());

        //二元表达式2
        assertEquals(BinaryExpression.class, binaryExpression1.getLeft().getClass());
        BinaryExpression binaryExpression2 = (BinaryExpression) binaryExpression1.getLeft();
        assertEquals(EExpressionType.OrElse, binaryExpression2.getExpressionType());

        //成员访问表达式1
        assertEquals(MemberExpression.class, binaryExpression1.getRight().getClass());
        MemberExpression memberExpression1 = (MemberExpression) binaryExpression1.getRight();
        assertEquals("HasCover", memberExpression1.getMemberName());

        //二元表达式3
        assertEquals(BinaryExpression.class, binaryExpression2.getLeft().getClass());
        BinaryExpression binaryExpression3 = (BinaryExpression) binaryExpression2.getLeft();
        assertEquals(EExpressionType.AndAlso, binaryExpression3.getExpressionType());

        //二元表达式4
        assertEquals(BinaryExpression.class, binaryExpression2.getRight().getClass());
        BinaryExpression binaryExpression4 = (BinaryExpression) binaryExpression2.getRight();
        assertEquals(EExpressionType.AndAlso, binaryExpression4.getExpressionType());

        //一元表达式1
        assertEquals(UnaryExpression.class, binaryExpression3.getRight().getClass());
        UnaryExpression unaryExpression1 = (UnaryExpression) binaryExpression3.getRight();
        assertEquals(EExpressionType.Not, unaryExpression1.getExpressionType());

        //成员访问表达式2
        assertEquals(MemberExpression.class, unaryExpression1.getOperand().getClass());
        MemberExpression memberExpression2 = (MemberExpression) unaryExpression1.getOperand();
        assertEquals("IsGood", memberExpression2.getMemberName());

        //一元表达式2
        assertEquals(UnaryExpression.class, binaryExpression4.getLeft().getClass());
        UnaryExpression unaryExpression2 = (UnaryExpression) binaryExpression4.getLeft();
        assertEquals(EExpressionType.Not, unaryExpression2.getExpressionType());

        //成员访问表达式3
        assertEquals(MemberExpression.class, unaryExpression2.getOperand().getClass());
        MemberExpression memberExpression3 = (MemberExpression) unaryExpression2.getOperand();
        assertEquals("IsBig", memberExpression3.getMemberName());

        //成员访问表达式4
        assertEquals(MemberExpression.class, binaryExpression4.getRight().getClass());
        MemberExpression memberExpression4 = (MemberExpression) binaryExpression4.getRight();
        assertEquals("IsGood", memberExpression4.getMemberName());

        //当然 也可以使用表达式的静态方法自己直接构造表达式 这里构造((box.getIsBig() == true || box.getIsGood() == false) || box.getHasCover() == true)的表达式
        //首先 需要一个参数表达式来表示box这个参数
        ParameterExpression parameterExpression = Expression.parameter("arg0", Box.class);
        //构造box.getIsBig() 和 true 这里的没有上级宿主 host传空 如果是getCan().getKey()这种 则host是访问can的成员访问表达式
        MemberExpression memberExpression = Expression.member(parameterExpression, Box.class.getMethod("getIsBig"), null, Box.class);
        ConstantExpression constantExpression = Expression.constant(true);
        //构造box.getIsBig() 和 !box.getIsGood() 这里使用二元相等表达式来表示 结果类型肯定是布尔值
        BinaryExpression binaryExpressionL1 = Expression.equal(memberExpression, constantExpression, Boolean.class);

        //构造box.getIsGood() 和 false 与之前的逻辑相同
        memberExpression = Expression.member(parameterExpression, Box.class.getMethod("getIsGood"), null, Box.class);
        constantExpression = Expression.constant(false);
        //组合成二元相等表达式
        BinaryExpression binaryExpressionR1 = Expression.equal(memberExpression, constantExpression, Boolean.class);

        //构造box.getHasCover() 和 tur这个部分
        memberExpression = Expression.member(parameterExpression, Box.class.getMethod("getHasCover"), null, Box.class);
        constantExpression = Expression.constant(true);
        //组合成二元相等表达式
        BinaryExpression binaryExpressionR2 = Expression.equal(memberExpression, constantExpression, Boolean.class);

        //L1 和 R1 用 或者 组成 L2
        BinaryExpression binaryExpressionL2 = Expression.or(binaryExpressionL1, binaryExpressionR1, Boolean.class);

        //L2 和 R2 用 或者 组成最终结果
        BinaryExpression binaryExpression = Expression.or(binaryExpressionL2, binaryExpressionR2, Boolean.class);

        //拼成Lambda表达式
        LambdaExpression lambdaExpressionF = Expression.lambda(new ParameterExpression[]{parameterExpression}, binaryExpression);
        //此处都是构造的 最终的结果就是((box.getIsBig() == true || box.getIsGood() == false) || box.getHasCover() == true)
        assertNotNull(lambdaExpressionF);
        assertNotNull(lambdaExpressionF.getBody());
    }

    /**
     * 测试表达式计算
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void testCalculate() {
        //测试结果为表达式作为filter anymatch allMatch的参数进行计算的

        //构造一个JavaBean列表
        var javaBean1 = new JavaBean();
        javaBean1.setIntNumber(1);
        javaBean1.setString("字符串1");

        var javaBean2 = new JavaBean();
        javaBean2.setIntNumber(2);
        javaBean2.setString("字符串2");

        var list = new ArrayList<JavaBean>();
        list.add(javaBean1);
        list.add(javaBean2);

        //构造表达式
        SerializedPredicate<JavaBean> predicate = javaBean -> javaBean.getIntNumber() == 1;

        //构造表达式翻译器
        LambdaTranslator translator = new LambdaTranslator();
        //进行翻译
        LambdaExpression lambdaExpression = translator.getLambdaExpression(predicate);

        //有一个int是1的 结果应当为true
        var matchResult = list.stream().anyMatch((Predicate<JavaBean>) lambdaExpression.compile());
        //校验结果
        assertTrue(matchResult);

        //不是所有都int是1的 结果应当为false
        matchResult = list.stream().allMatch((Predicate<JavaBean>) lambdaExpression.compile());
        //校验结果
        assertFalse(matchResult);

        //筛选出int为1的
        var javaBeans = list.stream().filter((Predicate<JavaBean>) lambdaExpression.compile()).collect(Collectors.toList());
        //校验结果
        assertNotNull(javaBeans);
        assertEquals(1, javaBeans.size());
        assertEquals(javaBean1, javaBeans.get(0));

        //测试结果为表达式作为map参数进行计算的

        //构造表达式
        SerializedFunction<JavaBean, JavaBeanSelectResult> function = javabean -> new JavaBeanSelectResult(javabean.getIntNumber(), javabean.getString());

        //进行翻译
        lambdaExpression = translator.getLambdaExpression(function);
        //传入map中 结果成为JavaBeanSelectResult的列表
        var selectResults = list.stream().map((Function<JavaBean, JavaBeanSelectResult>) lambdaExpression.compile()).collect(Collectors.toList());

        //校验结果
        assertNotNull(selectResults);
        assertEquals(2, selectResults.size());
        assertNotNull(selectResults.get(0));
        assertEquals(1, selectResults.get(0).getIntNumber());
        assertEquals("字符串1", selectResults.get(0).getString());

        assertNotNull(selectResults.get(1));
        assertEquals(2, selectResults.get(1).getIntNumber());
        assertEquals("字符串2", selectResults.get(1).getString());
    }

    /**
     * 简单JAVABEAN投影类
     */
    public static class JavaBeanSelectResult {

        /**
         * int类型数字
         */
        private final int intNumber;

        /**
         * 字符串类型
         */
        private final String string;

        /**
         * 构造简单JAVABEAN投影类
         *
         * @param i 布尔值
         * @param s 字符串
         */
        public JavaBeanSelectResult(int i, String s) {
            this.string = s;
            this.intNumber = i;
        }

        /**
         * 获取Int值
         *
         * @return Int值
         */
        public int getIntNumber() {
            return this.intNumber;
        }

        /**
         * 获取字符串
         *
         * @return 字符串
         */
        public String getString() {
            return this.string;
        }
    }
}
