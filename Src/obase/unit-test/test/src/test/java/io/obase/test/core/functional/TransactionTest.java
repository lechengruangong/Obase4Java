package io.obase.test.core.functional;

import io.obase.core.saving.RepeatCreationException;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.simpleType.NullableJavaBean;
import io.obase.test.infrastructure.configuration.RelationshipDataBaseConfigurationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事务测试
 */
@ExtendWith(ConfigSetUp.class)
public class TransactionTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(NullableJavaBean.class).delete(p -> p.getIntNumber() >= 0, NullableJavaBean.class);

            for (var i = 1; i < 11; i++) {
                var javaBean = new NullableJavaBean();
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
                javaBean.setLongNumber(Long.parseLong(String.valueOf(i)));
                javaBean.setByteNumber((byte) i);
                javaBean.setCharNumber('\u006A');
                javaBean.setFloatNumber((float) Math.pow(Math.PI, i));
                javaBean.setDoubleNumber(Math.pow(Math.PI, i));
                javaBean.setDate(LocalDate.now());
                javaBean.setTime(LocalTime.now());
                javaBean.setUuid(UUID.randomUUID());

                context.createSet(NullableJavaBean.class).attach(javaBean);
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
            context.createSet(NullableJavaBean.class).delete(p -> p.getIntNumber() >= 0, NullableJavaBean.class);
        }
    }

    /**
     * 自动事务测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void autoTransactionTest(EDataSource dataSource) {
        //自动事务 指的是Obase自动附加的事务
        //在Obase中 每次SaveChanges时会把此次保存和上次保存之间的所有上下文管理的对象修改操作包含在一个事务块内
        //如果是首次SaveChanges则是从构造上下文开始的所有上下文管理的对象修改
        //此处的上下文管理的对象指的是附加到上下文的新对象和由上下文查询得到的旧对象

        var context = ContextUtils.createContext(dataSource);

        //此时 有10个对象 都查出来
        var list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());

        //此时修改其中的首个的Guid属性
        list.get(0).setUuid(new UUID(0, 0));
        list.get(1).setUuid(new UUID(0, 0));
        list.get(2).setUuid(new UUID(0, 0));

        //保存 这三个都被修改
        context.saveChanges();

        var emptyGuid = new UUID(0, 0).toString();
        context = ContextUtils.createContext(dataSource);
        list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());
        //前三个被修改
        assertEquals(emptyGuid, list.get(0).getUuid().toString());
        assertEquals(emptyGuid, list.get(1).getUuid().toString());
        assertEquals(emptyGuid, list.get(2).getUuid().toString());

        //此时模拟一个修改失败 第二个数值会超过数据库的限制 所以这三个修改都没有被保存
        list.get(0).setIntNumber(11);
        list.get(1).setIntNumber(null);
        list.get(2).setIntNumber(12);

        try {
            context.saveChanges();
        } catch (Exception ignored) {
            // 忽略掉异常
        }

        context = ContextUtils.createContext(dataSource);
        list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());
        //前三个值没有变化
        assertEquals(1, list.get(0).getIntNumber());
        assertEquals(2, list.get(1).getIntNumber());
        assertEquals(3, list.get(2).getIntNumber());
    }

    /**
     * 检查指定数据源的连接池 断言其中所有连接均已归还
     * 从连接池统计信息中解析出"Pool: 可用数/总数" 若可用数小于总数 说明有连接未归还 存在连接泄漏
     *
     * @param dataSource 数据源类型
     */
    private static void AssertConnectionPoolReturned(EDataSource dataSource) {
        var poolName = dataSource + " ConnectionPool";
        var statistics = ObaseConnectionPool.getInstance().getStatistics();
        //找到当前数据源对应的连接池统计行 形如:"MySql ConnectionPool / Pool: 4/5, Get wait: 0, GetAsync wait: 0"

        var split = statistics.split("\\R");
        var current = Arrays.stream(split).filter(p -> p.startsWith(poolName)).findFirst().orElse(null);
        assertNotNull(current, "未找到连接池" + poolName + "的统计信息");
        //解析"Pool: 可用数/总数"
        var totalPattern = Pattern.compile("totalConnections:(\\d+)");
        var idlePattern = Pattern.compile("idleConnections:(\\d+)");
        var total = extract(totalPattern, current);
        int idle = extract(idlePattern, current);
        assertEquals(total, idle, "连接池【" + poolName + "】存在未归还的连接(可用" + idle + "条/共" + total + "条),请检查是否存在连接泄漏.");
    }

    /**
     * 提取方法
     *
     * @param p    正则
     * @param line 文本
     * @return 数量
     */
    private static int extract(Pattern p, String line) {
        var m = p.matcher(line);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;  // -1 表示未匹配到
    }

    /**
     * 已存在连接的事务测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void existingConnectionTransactionTest(EDataSource dataSource) {
        //测试和连接提供方使用同一个事务一起提交的情形
        //模拟获取一个连接 此连接实际由连接提供方负责管理
        //打开连接
        var connection = this.getConnection(dataSource);

        //普通的上下文 实质上是一个数据源 此上下文用于验证数据
        var context = ContextUtils.createContext(dataSource);
        var count = -1L;
        try {
            //构造一个插入语句 并且开启事务 模拟是连接提供方的逻辑
            connection.setAutoCommit(false);

            //模拟加入一个主键为22的对象
            var statement = dataSource == EDataSource.PostgreSql
                    ? connection.prepareStatement("INSERT INTO \"NullableJavaBean\" (\"IntNumber\") VALUES(?)")
                    : connection.prepareStatement("INSERT INTO NullableJavaBean (IntNumber) VALUES(?)");
            statement.setInt(1, 21);

            //当前没有插入
            count = context.createSet(NullableJavaBean.class).count(p -> p.getIntNumber() == 21 || p.getIntNumber() == 22);
            assertEquals(0, count);

            //此下的所有开启事务 提交事务 回滚事务 关闭连接在实际使用时都是由连接提供方负责
            //执行Sql 模拟是连接提供方的逻辑
            statement.executeUpdate();

            //创建已有连接的上下文 此处构造用的连接是提供方的
            var exContext = ContextUtils.createExistingConnectionContext(connection, dataSource);
            var newObj = new NullableJavaBean();
            newObj.setIntNumber(22);
            exContext.attach(newObj);
            //Obase保存
            exContext.saveChanges();

            //连接提供方的提交事务
            connection.commit();
            //关闭
            statement.close();

        } catch (Exception e) {
            //回滚事务
            try {
                connection.rollback();
            } catch (SQLException ex) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
            //发生异常 失败
            fail("已存在连接的事务发生异常:" + e.getMessage());
        } finally {
            //关闭连接
            try {
                connection.close();
            } catch (SQLException e) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
        }

        //当前插入了两条数据
        count = context.createSet(NullableJavaBean.class).count(p -> p.getIntNumber() == 21 || p.getIntNumber() == 22);
        assertEquals(2, count);

        //测试和连接提供方使用同一个事务一起回滚的情形
        //模拟获取一个连接 此连接实际由连接提供方负责管理
        connection = this.getConnection(dataSource);

        try {
            //构造一个插入语句 并且开启事务 模拟是连接提供方的逻辑
            connection.setAutoCommit(false);

            //模拟加入一个主键为23的对象
            var statement = dataSource == EDataSource.PostgreSql
                    ? connection.prepareStatement("INSERT INTO \"NullableJavaBean\" (\"IntNumber\") VALUES(?)")
                    : connection.prepareStatement("INSERT INTO NullableJavaBean (IntNumber) VALUES(?)");
            statement.setInt(1, 23);

            //此下的所有开启事务 提交事务 回滚事务 关闭连接在实际使用时都是由连接提供方负责
            //执行Sql 模拟是连接提供方的逻辑
            statement.executeUpdate();

            //创建已有连接的上下文 此处构造用的连接是提供方的
            var exContext = ContextUtils.createExistingConnectionContext(connection, dataSource);
            var newObj = new NullableJavaBean();
            newObj.setIntNumber(24);
            exContext.attach(newObj);
            //Obase保存
            exContext.saveChanges();

            //连接提供方的回滚事务
            connection.rollback();
            //关闭
            statement.close();

        } catch (Exception e) {
            //回滚事务
            try {
                connection.rollback();
            } catch (SQLException ex) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
            //发生异常 失败
            fail("已存在连接的事务发生异常:" + e.getMessage());
        } finally {
            //关闭连接
            try {
                connection.close();
            } catch (SQLException e) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
        }
        //都回滚了 没有插入数据
        count = context.createSet(NullableJavaBean.class).count(p -> p.getIntNumber() == 23 || p.getIntNumber() == 24);
        assertEquals(0, count);

        //测试和连接提供方使用同一个事务但Obase执行的部分发生异常导致回滚的情形
        //模拟获取一个连接 此连接实际由连接提供方负责管理
        connection = this.getConnection(dataSource);

        try {
            //构造一个插入语句 并且开启事务 模拟是连接提供方的逻辑
            connection.setAutoCommit(false);

            //模拟加入一个主键为23的对象
            var statement = dataSource == EDataSource.PostgreSql
                    ? connection.prepareStatement("INSERT INTO \"NullableJavaBean\" (\"IntNumber\") VALUES(?)")
                    : connection.prepareStatement("INSERT INTO NullableJavaBean (IntNumber) VALUES(?)");
            statement.setInt(1, 23);

            //此下的所有开启事务 提交事务 回滚事务 关闭连接在实际使用时都是由连接提供方负责
            //执行Sql 模拟是连接提供方的逻辑
            statement.executeUpdate();

            //创建已有连接的上下文 此处构造用的连接是提供方的
            var exContext = ContextUtils.createExistingConnectionContext(connection, dataSource);
            //在这里执行Obase的逻辑 由于重复插入插入主键为1的对象 所以此处会发生一个RepeatCreationException
            var newObj = new NullableJavaBean();
            newObj.setIntNumber(1);
            exContext.attach(newObj);
            //Obase保存
            exContext.saveChanges();

            //连接提供方的提交事务
            connection.commit();
            //关闭
            statement.close();

        } catch (Exception e) {
            if (!(e instanceof RepeatCreationException))
                //发生非重复插入异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            //回滚事务
            try {
                connection.rollback();
            } catch (SQLException ex) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
        } finally {
            //关闭连接
            try {
                connection.close();
            } catch (SQLException e) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
        }

        //都回滚了 没有插入数据
        count = context.createSet(NullableJavaBean.class).count(p -> p.getIntNumber() == 23);
        assertEquals(0, count);

        //测试连接提供方不提供事务的情形
        connection = this.getConnection(dataSource);

        try {

            //此时就是没有事务的 就直接创建命令 打开连接
            var statement = dataSource == EDataSource.PostgreSql
                    ? connection.prepareStatement("INSERT INTO \"NullableJavaBean\" (\"IntNumber\") VALUES(?)")
                    : connection.prepareStatement("INSERT INTO NullableJavaBean (IntNumber) VALUES(?)");
            statement.setInt(1, 25);

            //此下的所有开启事务 提交事务 回滚事务 关闭连接在实际使用时都是由连接提供方负责
            //执行Sql 模拟是连接提供方的逻辑
            statement.executeUpdate();

            //创建已有连接的上下文 此处构造用的连接是提供方的
            var exContext = ContextUtils.createExistingConnectionContext(connection, dataSource);
            //在这里执行Obase的逻辑
            var newObj = new NullableJavaBean();
            newObj.setIntNumber(26);
            exContext.attach(newObj);
            //Obase保存
            exContext.saveChanges();

            //关闭
            statement.close();
        } catch (Exception e) {
            //回滚事务
            try {
                connection.rollback();
            } catch (SQLException ex) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
            //发生异常 失败
            fail("已存在连接的事务发生异常:" + e.getMessage());
        } finally {
            //关闭连接
            try {
                connection.close();
            } catch (SQLException e) {
                //发生异常 失败
                fail("已存在连接的事务发生异常:" + e.getMessage());
            }
        }

        //插入了两条数据
        count = context.createSet(NullableJavaBean.class).count(p -> p.getIntNumber() == 25 || p.getIntNumber() == 26);
        assertEquals(2, count);

        //清理可能的冗余数据 防止污染其它的测试
        context.createSet(NullableJavaBean.class).delete(p -> p.getIntNumber() > 20, NullableJavaBean.class);
    }

    /**
     * 模拟的外部方法
     * 当传入的i为奇数时会抛异常
     *
     * @param i 整数
     */
    private void outerMethod(int i) {
        if (i % 2 == 1)
            throw new IllegalArgumentException("只允许偶数!");
    }

    /**
     * 根据数据源获取连接
     *
     * @param dataSource 数据源
     * @return 连接
     */
    private Connection getConnection(EDataSource dataSource) {
        var url = "";
        var username = "";
        var password = "";

        switch (dataSource) {

            case SqlServer -> {
                url = RelationshipDataBaseConfigurationManager.getSqlServerConnectionString();
                username = RelationshipDataBaseConfigurationManager.getSqlServerUserName();
                password = RelationshipDataBaseConfigurationManager.getSqlServerPassWord();
            }
            case MySql -> {
                url = RelationshipDataBaseConfigurationManager.getMySqlConnectionString();
                username = RelationshipDataBaseConfigurationManager.getMySqlUserName();
                password = RelationshipDataBaseConfigurationManager.getMySqlPassWord();
            }
            case Sqlite -> url = RelationshipDataBaseConfigurationManager.getSqliteConnectionString();
            case PostgreSql -> {
                url = RelationshipDataBaseConfigurationManager.getPostgreSqlConnectionString();
                username = RelationshipDataBaseConfigurationManager.getPostgreSqlUserName();
                password = RelationshipDataBaseConfigurationManager.getPostgreSqlPassWord();
            }
            default -> throw new IllegalArgumentException("暂无" + dataSource + "对应的数据库连接字符串.");
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("创建连接失败.", e);
        }
    }

    /**
     * 手动事务测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void manualTransactionTest(EDataSource dataSource) {
        //手动事务 指的是调用Obase的手动事务方法自己控制事务
        //Obase的手动事务方法遵循JDBC的try-Begin-Commit-Catch-RollBack-Finally-Release模式

        var context = ContextUtils.createContext(dataSource);

        //此时 有10个对象 都查出来
        var list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());

        try {
            //手动开启事务
            context.beginTransaction();

            //在这个事务里还可查询其他对象
            var emptyList = context.createSet(NullableJavaBean.class).filter(p -> p.getIntNumber() > 20).toList();
            //没有满足条件的
            assertNotNull(emptyList);
            assertEquals(0, emptyList.size());

            //修改前三个的LongNumber
            list.get(0).setLongNumber(11L);
            list.get(1).setLongNumber(12L);
            list.get(2).setLongNumber(13L);
            //保存之前的修改
            context.saveChanges();
            //调用模拟的外部方法 此处传入的是偶数 不会抛异常
            this.outerMethod(2);

            //提交修改
            context.commit();
        } catch (Exception ignored) {
            //发生异常 回滚
            context.rollbackTransaction();
        } finally {
            //最后释放资源
            context.release();
        }

        context = ContextUtils.createContext(dataSource);
        list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());
        //前三个被修改
        assertEquals(11, list.get(0).getLongNumber());
        assertEquals(12, list.get(1).getLongNumber());
        assertEquals(13, list.get(2).getLongNumber());

        //此时再检查连接池的信息 手动事务与就地修改方法结束后所有连接都应归还
        AssertConnectionPoolReturned(dataSource);

        try {
            //手动开启事务
            context.beginTransaction();

            //在这个事务里还可查询其他对象
            var emptyList = context.createSet(NullableJavaBean.class).filter(p -> p.getIntNumber() > 20).toList();
            //没有满足条件的
            assertNotNull(emptyList);
            assertEquals(0, emptyList.size());

            //修改前三个的LongNumber
            list.get(0).setLongNumber(14L);
            list.get(1).setLongNumber(15L);
            list.get(2).setLongNumber(16L);
            //保存之前的修改
            context.saveChanges();
            //调用模拟的外部方法 此处传入的是奇数 会抛异常
            this.outerMethod(1);

            //提交修改
            context.commit();
        } catch (Exception ignored) {
            //发生异常 回滚
            context.rollbackTransaction();
        } finally {
            //最后释放资源
            context.release();
        }

        context = ContextUtils.createContext(dataSource);
        list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());
        //前三个没有被修改
        assertEquals(11, list.get(0).getLongNumber());
        assertEquals(12, list.get(1).getLongNumber());
        assertEquals(13, list.get(2).getLongNumber());

        //此时再检查连接池的信息 手动事务与就地修改方法结束后所有连接都应归还
        AssertConnectionPoolReturned(dataSource);
    }

    /**
     * 手动事务与就地修改方法联合使用测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void manualTransactionTestWithDirectlyChange(EDataSource dataSource) {
        //手动事务 指的是调用Obase的手动事务方法自己控制事务
        //Obase的手动事务方法遵循JDBC的try-Begin-Commit-Catch-RollBack-Finally-Release模式

        var context = ContextUtils.createContext(dataSource);

        //此时 有10个对象 都查出来
        var list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());

        try {
            //手动开启事务
            context.beginTransaction();

            //在这个事务里使用就地修改方法
            var emptyDelete = context.createSet(NullableJavaBean.class).delete(p -> p.getIntNumber() > 20, NullableJavaBean.class);
            //没有满足条件的
            assertEquals(0, emptyDelete);

            //修改前三个的LongNumber
            list.get(0).setLongNumber(11L);
            list.get(1).setLongNumber(12L);
            list.get(2).setLongNumber(13L);
            //保存之前的修改
            context.saveChanges();
            //调用模拟的外部方法 此处传入的是偶数 不会抛异常
            this.outerMethod(2);

            //提交修改
            context.commit();
        } catch (Exception ignored) {
            //发生异常 回滚
            context.rollbackTransaction();
        } finally {
            //最后释放资源
            context.release();
        }

        context = ContextUtils.createContext(dataSource);
        list = context.createSet(NullableJavaBean.class).sorted(NullableJavaBean::getIntNumber).toList();
        //10个
        assertNotNull(list);
        assertEquals(10, list.size());
        //前三个被修改
        assertEquals(11, list.get(0).getLongNumber());
        assertEquals(12, list.get(1).getLongNumber());
        assertEquals(13, list.get(2).getLongNumber());

        //此时再检查连接池的信息 手动事务与就地修改方法结束后所有连接都应归还
        AssertConnectionPoolReturned(dataSource);
    }
}
