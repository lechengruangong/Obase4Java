package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.keywords.Order;
import io.obase.test.domain.functional.keywords.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 关键字同名类测试类
 */
@ExtendWith(ConfigSetUp.class)
public class KeyWordsTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(User.class).delete(p -> p.getUserId() >= 0, User.class);
            context.createSet(Order.class).delete(p -> p.getCode() != "", Order.class);
            //插入数据
            var user = new User();
            user.setUserName("张三");

            var order1 = new Order();
            order1.setCode("001");
            order1.setName("订单1");
            order1.setUser(user);


            var order2 = new Order();
            order2.setCode("002");
            order2.setName("订单2");
            order2.setUser(user);
            //附加
            context.attach(user);
            context.attach(order1);
            context.attach(order2);
            //保存
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
            context.createSet(User.class).delete(p -> p.getUserId() >= 0, User.class);
            context.createSet(Order.class).delete(p -> p.getCode() != "", Order.class);
        }
    }

    /**
     * 测试方法
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        //构造上下文
        var context = ContextUtils.createContext(dataSource);

        var user = context.createSet(User.class).include(User::getOrders).findFirst(p -> p.getUserName() == "张三").orElse(null);
        //验证
        assertNotNull(user);
        assertNotNull(user.getOrders());
        assertEquals(2, user.getOrders().size());
    }
}
