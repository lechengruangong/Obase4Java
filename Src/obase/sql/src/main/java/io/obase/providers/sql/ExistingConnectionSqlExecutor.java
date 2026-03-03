/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于已有连接的SQL执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:27:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.common.EIsolationLevel;
import io.obase.core.saving.NothingUpdatedException;
import io.obase.core.saving.RepeatInsertionException;
import io.obase.providers.sql.sqlobject.DataParameter;
import io.obase.providers.sql.sqlobject.IParameterCreator;

import java.sql.*;

/**
 * 基于已有连接的SQL执行器。
 */
public class ExistingConnectionSqlExecutor implements ISqlExecutor {

    /**
     * 数据库连接
     */
    private final Connection connection;

    /**
     * 数据库连接模式，即如何管理数据库连接的打开与关闭。
     */
    private final EConnectionMode connectionMode;

    /**
     * 数据源类型
     */
    private final EDataSource sourceType;

    /**
     * 受影响的行数
     */
    private int affectRows;

    /**
     * 执行超时时间（注：不是连接超时时间）
     */
    private int commandTimeout;

    /**
     * Sql命令
     */
    private PreparedStatement sqlCommand;

    /**
     * 使用指定的连接字符串创建特定于指定数据源类型的Sql执行器实例。
     *
     * @param connection 连接
     * @param sourceType 数据源类型
     */
    public ExistingConnectionSqlExecutor(Connection connection, EDataSource sourceType) {
        if (connection == null)
            throw new IllegalArgumentException("ExistingConnectionSqlExecutor传入的连接不能为空.");
        this.connection = connection;
        this.sourceType = sourceType;
        this.connectionMode = EConnectionMode.Caller;
    }

    /**
     * 获取执行超时时间（注：不是连接超时时间）
     *
     * @return 执行超时时间
     */
    @Override
    public int getCommandTimeout() {
        return this.commandTimeout;
    }

    /**
     * 设置执行超时时间（注：不是连接超时时间）
     *
     * @param value 执行超时时间
     */
    @Override
    public void setCommandTimeout(int value) {
        this.commandTimeout = value;
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public EDataSource getSourceType() {
        return this.sourceType;
    }

    /**
     * 获取一个值，该值指示是否已开启本地事务
     *
     * @return 是否已开启本地事务
     */
    @Override
    public boolean getTransactionBegun() {
        try {
            return this.connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("获取是否已开启本地事务失败,请参照内部异常.", e);
        }
    }

    /**
     * 获取数据库连接模式，即如何管理数据库连接的打开与关闭。
     *
     * @return 据库连接模式
     */
    @Override
    public EConnectionMode getConnectionMode() {
        return this.connectionMode;
    }

    /**
     * 执行参数化的查询Sql语句，返回IDataReader
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 结果集
     */
    @Override
    public ResultSet executeReader(String sql, DataParameter[] paras) {
        //检查连接
        this.checkConnection();
        try {
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            //执行语句
            this.sqlCommand.execute();
            return this.sqlCommand.getResultSet();

        } catch (SQLException ex) {
            throw new RuntimeException("发生SqlException" + ex.getMessage(), ex);
        }
    }

    /**
     * 执行非查询参数化Sql语句，并返回影响行数。
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 收益相的函数
     */
    @Override
    public int execute(String sql, DataParameter[] paras) {
        //检查连接
        this.checkConnection();
        try {
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            //执行语句
            this.affectRows = this.sqlCommand.executeUpdate();
            //如果影响结果为0 则抛出没更新任何值异常
            if (this.affectRows == 0)
                throw new NothingUpdatedException();

        } catch (Exception ex) {
            //发生异常 是否为主键重复插入异常
            this.getRepeatInsertionException(ex);
        }
        //返回查询结果
        return this.affectRows;
    }

    /**
     * 执行返回单个值的参数化Sql语句
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 执行结果
     */
    @Override
    public Object executeScalar(String sql, DataParameter[] paras) {
        Object res = null;
        ResultSet set;
        //检查连接
        this.checkConnection();
        try {
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            if (sql.toLowerCase().startsWith("select")) {
                //执行语句
                set = this.sqlCommand.executeQuery();
                if (set.next()) {
                    res = set.getObject(1);
                }
            } else {

                if (this.sourceType == EDataSource.Sqlite) {
                    //执行语句
                    int result = this.sqlCommand.executeUpdate();
                    set = this.connection.prepareStatement("Select last_insert_rowid();").executeQuery();
                    if (set.next()) {
                        res = set.getObject(1);
                    }
                    if (res == null)
                        res = result;
                } else if (this.sourceType == EDataSource.PostgreSql) {
                    //执行语句
                    int result = this.sqlCommand.executeUpdate();
                    set = this.connection.prepareStatement("SELECT lastval();").executeQuery();
                    if (set.next()) {
                        res = set.getObject(1);
                    }
                    if (res == null)
                        res = result;
                } else {
                    //执行语句
                    int result = this.sqlCommand.executeUpdate();
                    set = this.sqlCommand.getGeneratedKeys();
                    if (set.next()) {
                        res = set.getObject(1);
                    }
                    if (res == null)
                        res = result;
                }
            }
            //影响行数设为1
            this.affectRows = 1;

        } catch (Exception ex) {
            //发生异常 是否为主键重复插入异常
            this.getRepeatInsertionException(ex);
        }

        return res;
    }

    /**
     * 执行无参的Sql语句
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     */
    @Override
    public void executeScalarNoResult(String sql, DataParameter[] paras) {
        //检查连接
        this.checkConnection();
        try {
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            this.sqlCommand.executeUpdate();

        } catch (SQLException ex) {
            //发生异常 是否为主键重复插入异常
            this.getRepeatInsertionException(ex);
        }
    }

    /**
     * 打开数据库连接
     * 如果连接已打开则不执行任何操作。
     */
    @Override
    public void openConnection() {
        //由传入连接的提供方处理 此处仅检查一下
        this.checkConnection();
    }

    /**
     * 关闭数据库连接
     * 如果连接处于关闭状态则不执行任何操作。如果当时有事务未提交，则不执行任何操作。
     */
    @Override
    public void closeConnection() {
        //不需要处理连接的关闭 实质上此执行器也不会关闭连接
    }

    /**
     * 开启本地事务，事务隔离级别为ReadCommitted，即读时发布共享锁，读完即释放，可以防止读脏，但不能消除数据幻影。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     */
    @Override
    public void beginTransaction() {
        //事务由传入连接的提供方处理 此执行器不进行实质的处理
    }

    /**
     * 以指定的隔离级别开启事务处理。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     *
     * @param iso 事务隔离级别
     */
    @Override
    public void beginTransaction(EIsolationLevel iso) {
        //事务由传入连接的提供方处理 此执行器不进行实质的处理
    }

    /**
     * 回滚事务
     * 如果事务未开启，不执行任务操作
     */
    @Override
    public void rollbackTransaction() {
        //事务由传入连接的提供方处理 此执行器不进行实质的处理
    }

    /**
     * 提交事务
     * 如果事务未开启，不执行任务操作
     */
    @Override
    public void commitTransaction() {
        //事务由传入连接的提供方处理 此执行器不进行实质的处理
    }

    /**
     * 将当前执行器登记为环境事务的参与者
     */
    @Override
    public void enlistTransaction() {
        //事务由传入连接的提供方处理 此执行器不进行实质的处理
    }

    /**
     * 创建参数构造器
     *
     * @return 参数构造器
     */
    @Override
    public IParameterCreator createParameterCreator() {
        return new StandardParameterCreator();
    }

    /**
     * 构造重复插入异常
     *
     * @param ex 原始异常
     */
    private void getRepeatInsertionException(Exception ex) {
        if (this.isRepeatInsertionError(ex)) {
            RepeatInsertionException ex1 = new RepeatInsertionException(this.sourceType == EDataSource.PostgreSql);
            if (this.sourceType == EDataSource.PostgreSql)
                ex1.setUnSupportMessage("PostgreSQL不支持在单一事务块中发生异常后再次执行其他命令.");
            throw ex1;
        }
        throw new RuntimeException("发生SqlException" + ex.getMessage(), ex);
    }

    /**
     * 创建命令私有方法
     *
     * @param sql 命令的Sql语句
     */
    private void interiorCreateCommand(String sql) {
        try {
            if ((sql.endsWith(";select @@identity;") || sql.endsWith(";select last_insert_rowid();"))) {
                sql = sql.replace(";select @@identity;", "").replace(";select last_insert_rowid();", "");
                this.sqlCommand = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            this.sqlCommand = this.connection.prepareStatement(sql);
            this.sqlCommand.setQueryTimeout(this.commandTimeout);
        } catch (SQLException e) {
            throw new IllegalArgumentException("ExistingConnectionSqlExecutor无法创建命令.", e);
        }
    }

    /**
     * 检查当前的连接
     */
    private void checkConnection() {
        //判断是否为空 或者 连接是关闭的
        boolean isOpenByExecutor;
        try {
            isOpenByExecutor = this.connection.isClosed();
        } catch (SQLException e) {
            throw new IllegalArgumentException("ExistingConnectionSqlExecutor传入的连接没有打开或者是空.");
        }
        if (isOpenByExecutor)
            throw new IllegalArgumentException("ExistingConnectionSqlExecutor传入的连接没有打开或者是空.");
    }

    /**
     * 判定指定的异常是否归因于插入重复记录。判定逻辑特定于数据库类型
     *
     * @param exception 被判定的异常实例
     * @return 是否归因于插入重复记录
     */
    private boolean isRepeatInsertionError(Exception exception) {
        if (exception instanceof SQLException) {
            SQLException sqlException = (SQLException) exception;
            if (this.getSourceType() == EDataSource.Sqlite) {
                if (exception.getMessage().contains("Error " + this.getRepeatInsertionErrorNumber()))
                    return true;
            }
            if (this.getSourceType() == EDataSource.PostgreSql) {
                if (exception.getMessage().contains("duplicate key"))
                    return true;
            }
            return sqlException.getErrorCode() == this.getRepeatInsertionErrorNumber();
        }
        return false;
    }

    /**
     * 获取表示“重复插入”错误的代码，该代码特定于数据库引擎
     *
     * @return 表示“重复插入”错误的代码
     */
    private int getRepeatInsertionErrorNumber() {
        switch (this.getSourceType()) {
            case SqlServer:
                return 2627;
            case Oracle:
                return 1;
            case MySql:
                return 1062;
            case Sqlite:
                return 19;
            case PostgreSql:
                return 23505;
            case Other:
            case Oledb:
                return -1;
            default:
                throw new IllegalArgumentException("未知的数据源类型: " + this.getSourceType());
        }
    }
}
