/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql执行器接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 10:49:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.common.EIsolationLevel;
import io.obase.providers.sql.sqlobject.DataParameter;
import io.obase.providers.sql.sqlobject.IParameterCreator;

import java.sql.ResultSet;

/**
 * Sql执行器接口
 */
public interface ISqlExecutor {

    /**
     * 获取执行超时时间（注：不是连接超时时间）
     *
     * @return 执行超时时间
     */
    int getCommandTimeout();

    /**
     * 设置执行超时时间（注：不是连接超时时间）
     *
     * @param value 执行超时时间
     */
    void setCommandTimeout(int value);

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    EDataSource getSourceType();

    /**
     * 获取一个值，该值指示是否已开启本地事务
     *
     * @return 是否已开启本地事务
     */
    boolean getTransactionBegun();

    /**
     * 获取数据库连接模式，即如何管理数据库连接的打开与关闭。
     *
     * @return 据库连接模式
     */
    EConnectionMode getConnectionMode();

    /**
     * 执行参数化的查询Sql语句，返回IDataReader
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 结果集
     */
    ResultSet executeReader(String sql, DataParameter[] paras);

    /**
     * 执行非查询参数化Sql语句，并返回影响行数。
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 收益相的函数
     */
    int execute(String sql, DataParameter[] paras);

    /**
     * 执行返回单个值的参数化Sql语句
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 执行结果
     */
    Object executeScalar(String sql, DataParameter[] paras);

    /**
     * 执行无参的Sql语句
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     */
    void executeScalarNoResult(String sql, DataParameter[] paras);

    /**
     * 打开数据库连接
     * 如果连接已打开则不执行任何操作。
     */
    void openConnection();

    /**
     * 关闭数据库连接
     * 如果连接处于关闭状态则不执行任何操作。如果当时有事务未提交，则不执行任何操作。
     */
    void closeConnection();

    /**
     * 开启本地事务，事务隔离级别为ReadCommitted，即读时发布共享锁，读完即释放，可以防止读脏，但不能消除数据幻影。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     */
    void beginTransaction();

    /**
     * 以指定的隔离级别开启事务处理。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     *
     * @param iso 事务隔离级别
     */
    void beginTransaction(EIsolationLevel iso);

    /**
     * 回滚事务
     * 如果事务未开启，不执行任务操作
     */
    void rollbackTransaction();

    /**
     * 提交事务
     * 如果事务未开启，不执行任务操作
     */
    void commitTransaction();

    /**
     * 将当前执行器登记为环境事务的参与者
     */
    void enlistTransaction();

    /**
     * 创建参数构造器
     *
     * @return 参数构造器
     */
    IParameterCreator createParameterCreator();
}
