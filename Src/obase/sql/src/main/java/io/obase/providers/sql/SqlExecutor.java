/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:17:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.common.EIsolationLevel;
import io.obase.core.saving.NothingUpdatedException;
import io.obase.core.saving.RepeatInsertionException;
import io.obase.providers.sql.sqlobject.DataParameter;
import io.obase.providers.sql.sqlobject.IParameterCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sql执行器
 */
public abstract class SqlExecutor implements ISqlExecutor {

    /**
     * 数据库驱动类字符串
     */
    private final String driverName;

    /**
     * 连接字符串
     */
    private final String connString;

    /**
     * 连接用户名
     */
    private final String userName;

    /**
     * 连接密码
     */
    private final String passWord;


    /**
     * 数据源类型
     */
    private final EDataSource sourceType;
    /**
     * 数据库连接
     */
    protected Connection conn;
    /**
     * 受影响的行数
     */
    private int affectRows;
    /**
     * 执行超时时间（注：不是连接超时时间）
     */
    private int commandTimeout;
    /**
     * 数据库连接模式，即如何管理数据库连接的打开与关闭。
     */
    private EConnectionMode connectionMode;

    /**
     * Sql命令
     */
    private PreparedStatement sqlCommand;

    /**
     * 事务的个数
     */
    private int transNumber;

    /**
     * 使用指定的连接字符串创建特定于指定数据源类型的Sql执行器实例。
     *
     * @param connString 连接字符串
     * @param sourceType 数据源类型
     */
    protected SqlExecutor(String driverName, String connString, String userName, String passWord, EDataSource sourceType) {
        this.driverName = driverName;
        this.connString = connString;
        this.userName = userName;
        this.passWord = passWord;
        this.sourceType = sourceType;
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
     * @param commandTimeout 执行超时时间
     */
    @Override
    public void setCommandTimeout(int commandTimeout) {
        this.commandTimeout = commandTimeout;
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
        return this.transNumber > 0;
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
     * 执行非查询参数化Sql语句，并返回影响行数。
     * 不论执行前连接是否已打开，执行完后就将保持连接打开状态，调用方必须在合适时间手动关闭连接。
     *
     * @param sql   参数化的查询Sql语句
     * @param paras 参数列表
     * @return 收益相的函数
     */
    @Override
    public int execute(String sql, DataParameter[] paras) {
        try {
            //判断是否为空 或者 连接是关闭的
            this.makeExecutionConnection();
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
        } finally {
            if (this.connectionMode == EConnectionMode.Execution) {
                this.closeConnection();
            }
        }
        //返回查询结果
        return this.affectRows;
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
        try {
            //判断是否为空 或者 连接是关闭的
            this.makeExecutionConnection();
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            //执行语句
            this.sqlCommand.execute();
            return this.sqlCommand.getResultSet();

        } catch (SQLException ex) {
            if (this.connectionMode == EConnectionMode.Execution) {
                this.closeConnection();
            }
            throw new RuntimeException("发生SqlException" + ex.getMessage(), ex);
        }
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
        try {
            //判断是否为空 或者 连接是关闭的
            this.makeExecutionConnection();
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
                    set = this.conn.prepareStatement("Select last_insert_rowid();").executeQuery();
                    if (set.next()) {
                        res = set.getObject(1);
                    }
                    if (res == null)
                        res = result;
                } else if (this.sourceType == EDataSource.PostgreSql) {
                    //执行语句
                    int result = this.sqlCommand.executeUpdate();
                    set = this.conn.prepareStatement("SELECT lastval();").executeQuery();
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
        } finally {
            if (this.connectionMode == EConnectionMode.Execution) {
                this.closeConnection();
            }
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
        try {
            //判断是否为空 或者 连接是关闭的
            this.makeExecutionConnection();
            //构造命令
            this.interiorCreateCommand(sql);
            //设置具体内容 清除原有的参数
            for (DataParameter item : paras)
                this.sqlCommand.setObject(item.Index, item.Value);
            this.sqlCommand.executeUpdate();

        } catch (SQLException ex) {
            //发生异常 是否为主键重复插入异常
            this.getRepeatInsertionException(ex);
        } finally {
            if (this.connectionMode == EConnectionMode.Execution) {
                this.closeConnection();
            }
        }
    }

    /**
     * 打开数据库连接
     * 如果连接已打开则不执行任何操作。
     */
    @Override
    public void openConnection() {
        if (this.conn == null)
            this.conn = this.createConnection(this.driverName, this.connString, this.userName, this.passWord);
        try {
            if (this.conn.isClosed())
                this.conn = this.createConnection(this.driverName, this.connString, this.userName, this.passWord);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接状态失败,请参照内部异常", e);
        }
        this.connectionMode = EConnectionMode.Caller;
    }

    /**
     * 关闭数据库连接
     * 如果连接处于关闭状态则不执行任何操作。如果当时有事务未提交，则不执行任何操作。
     */
    @Override
    public void closeConnection() {
        if (this.transNumber > 0)
            return;

        try {
            //先释放SqlCommand
            if (this.sqlCommand != null) {
                this.sqlCommand.close();
                this.sqlCommand = null;
            }
            //再关闭连接
            if (this.conn == null)
                return;
            if (!this.conn.isClosed()) {
                this.conn.close();
            } else {
                this.conn = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("关闭连接失败,请参照内部异常", e);
        }
    }

    /**
     * 开启本地事务，事务隔离级别为ReadCommitted，即读时发布共享锁，读完即释放，可以防止读脏，但不能消除数据幻影。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     */
    @Override
    public void beginTransaction() {
        try {
            //判断是否为空 或者 连接是关闭的
            boolean isOpenByExecutor = this.conn == null || this.conn.isClosed();
            //如果是 则判定为要由执行器打开 将EConnectionMode更改为事务模式
            if (isOpenByExecutor) {
                this.openConnection();
                this.connectionMode = EConnectionMode.Transaction;
            }

            if (this.conn == null)
                throw new RuntimeException("开启事本地事务失败,连接为空.");
            //首次才新建事务对象
            if (this.transNumber == 0) {
                this.conn.setAutoCommit(false);
                this.conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            this.transNumber++;
        } catch (SQLException e) {
            throw new RuntimeException("开启事务失败,请参照内部异常.", e);
        }
    }

    /**
     * 以指定的隔离级别开启事务处理。
     * 在事务结束前调用本方法不会开启另一个事务，也不会引发异常
     *
     * @param iso 事务隔离级别
     */
    @Override
    public void beginTransaction(EIsolationLevel iso) {
        try {
            //判断是否为空 或者 连接是关闭的
            boolean isOpenByExecutor = this.conn == null || this.conn.isClosed();
            //如果是 则判定为要由执行器打开 将EConnectionMode更改为事务模式
            if (isOpenByExecutor) {
                this.openConnection();
                this.connectionMode = EConnectionMode.Transaction;
            }

            if (this.conn == null)
                throw new RuntimeException("以指定的隔离级别开启事务失败,连接为空.");
            //首次才新建事务对象
            if (this.transNumber == 0) {
                this.conn.setAutoCommit(false);
                this.conn.setTransactionIsolation(iso.getLevel());
            }

            this.transNumber++;
        } catch (SQLException e) {
            throw new RuntimeException("开启事务失败,请参照内部异常.", e);
        }
    }

    /**
     * 回滚事务
     * 如果事务未开启，不执行任务操作
     */
    @Override
    public void rollbackTransaction() {
        try {
            if (!this.conn.getAutoCommit()) {
                this.conn.rollback();
                this.transNumber = 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("回滚事务失败,请参照内部异常.", e);
        }
        //如果数据库连接模式为“事务模式”则自动关闭连接。
        if (this.connectionMode == EConnectionMode.Transaction)
            this.closeConnection();
    }

    /**
     * 提交事务
     * 如果事务未开启，不执行任务操作
     */
    @Override
    public void commitTransaction() {
        if (this.transNumber > 0)
            this.transNumber--;

        try {
            this.conn.commit();
            this.transNumber = 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("提交事务失败,请参照内部异常.", e);
        }

        //如果数据库连接模式为“事务模式”则自动关闭连接。
        if (this.connectionMode == EConnectionMode.Transaction)
            this.closeConnection();
    }

    /**
     * 将当前执行器登记为环境事务的参与者
     */
    @Override
    public void enlistTransaction() {
        throw new IllegalArgumentException("环境事务未实现,暂不支持异构事务,跨上下文事务");
    }

    /**
     * 创建参数构造器
     *
     * @return 参数构造器
     */
    @Override
    public abstract IParameterCreator createParameterCreator();

    /**
     * 判定指定的异常是否归因于插入重复记录。判定逻辑特定于数据库类型
     *
     * @param exception 被判定的异常实例
     * @return 是否归因于插入重复记录
     */
    protected abstract boolean isRepeatInsertionError(Exception exception);

    /**
     * 由派生类实现以提供一个连接对象，该对象用于建立到数据源的连接。
     *
     * @param driverName 驱动名称
     * @param connString 连接字符串
     * @param userName   用户名
     * @param passWord   密码
     * @return 连接
     */
    protected abstract Connection createConnection(String driverName, String connString, String userName, String passWord);

    /**
     * 由派生类实现以提供一个命令对象，该对象用于执行Sql语句
     *
     * @param sql Sql语句
     * @return 命令对象
     */
    protected abstract PreparedStatement createCommand(String sql);

    /**
     * 创建命令私有方法
     */
    private void interiorCreateCommand(String sql) {
        try {
            this.sqlCommand = this.createCommand(sql);
            this.sqlCommand.setQueryTimeout(this.commandTimeout);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Sql执行器创建命令失败,请参照内部异常.", e);
        }
    }

    /**
     * 构造执行模式的连接
     *
     * @throws SQLException Sql异常
     */
    private void makeExecutionConnection() throws SQLException {
        //判断是否为空 或者 连接是关闭的
        boolean isOpenByExecutor = this.conn == null || this.conn.isClosed();
        //如果是关闭 或者空 则自己开
        if (isOpenByExecutor) {
            this.openConnection();
            this.connectionMode = EConnectionMode.Execution;
        }
    }

    /**
     * 构造重复插入异常
     *
     * @param ex 原始异常
     */
    private void getRepeatInsertionException(Exception ex) {

        if (ex instanceof NothingUpdatedException) {
            throw (NothingUpdatedException) ex;
        }

        if (!this.isRepeatInsertionError(ex)) {
            throw new RuntimeException("发生SqlException" + ex.getMessage(), ex);
        }
        RepeatInsertionException ex1 = new RepeatInsertionException(this.sourceType == EDataSource.PostgreSql, ex);
        if (this.sourceType == EDataSource.PostgreSql)
            ex1.setUnSupportMessage("PostgreSQL不支持在单一事务块中发生异常后再次执行其他命令.");
        throw ex1;
    }
}
