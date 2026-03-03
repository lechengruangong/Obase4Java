/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标准的参数构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:18:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import io.obase.providers.sql.sqlobject.IParameterCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 标准Sql执行器，该执行器基于.NET数据提供程序工厂模型构造特定于数据源提供程序实例，然后使用该提供程序实例访问数据源
 */
public class StandardSqlExecutor extends SqlExecutor {

    /**
     * 上下文类型
     */
    private final Class<? extends ObjectContext> contextType;

    /**
     * 初始化StandardSqlExecutor类的新实例
     *
     * @param sourceType  源类型
     * @param connString  连接字符串
     * @param contextType 上下文类型
     * @param driverName  驱动名称
     * @param passWord    数据库密码
     * @param userName    数据库用户名
     */
    public StandardSqlExecutor(String driverName, String connString, String userName, String passWord, EDataSource sourceType, Class<? extends ObjectContext> contextType) {
        super(driverName, connString, userName, passWord, sourceType);
        this.contextType = contextType;
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
     * 判定指定的异常是否归因于插入重复记录。判定逻辑特定于数据库类型
     *
     * @param exception 被判定的异常实例
     * @return 是否归因于插入重复记录
     */
    @Override
    protected boolean isRepeatInsertionError(Exception exception) {
        if (exception instanceof SQLException) {
            SQLException sqlException = (SQLException) exception;
            if (this.getSourceType() == EDataSource.PostgreSql) {
                if (exception.getMessage().contains("duplicate key"))
                    return true;
            }
            if (this.getSourceType() == EDataSource.Sqlite) {
                if (exception.getMessage().contains("Error " + this.getRepeatInsertionErrorNumber()))
                    return true;
            }

            return sqlException.getErrorCode() == this.getRepeatInsertionErrorNumber();
        }
        return false;
    }

    /**
     * 由派生类实现以提供一个连接对象，该对象用于建立到数据源的连接。
     *
     * @param driverName 驱动名称
     * @param connString 连接字符串
     * @param userName   用户名
     * @param passWord   密码
     * @return 连接
     */
    @Override
    protected Connection createConnection(String driverName, String connString, String userName, String passWord) {
        Connection conn;
        if (Utils.getStringIsEmpty(driverName))
            throw new IllegalArgumentException("未正确设置数据库驱动类");

        if (Utils.getStringIsEmpty(connString))
            throw new IllegalArgumentException("未正确设置数据库连接字符串");

        if (this.getSourceType() != EDataSource.Sqlite) {
            if (Utils.getStringIsEmpty(userName))
                throw new IllegalArgumentException("未正确设置数据库登录名");

            if (Utils.getStringIsEmpty(passWord))
                throw new IllegalArgumentException("未正确设置数据库登录密码");
        }

        DataSource source = ObaseConnectionPool.getInstance().getPool(driverName, connString, userName, passWord, this.contextType);
        try {
            conn = source.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败,请参照内部异常.", e);
        }
        return conn;
    }

    /**
     * 由派生类实现以提供一个命令对象，该对象用于执行Sql语句
     *
     * @param sql Sql语句
     * @return 命令对象
     */
    @Override
    protected PreparedStatement createCommand(String sql) {
        try {
            if ((sql.endsWith(";select @@identity;") || sql.endsWith(";select last_insert_rowid();"))) {
                sql = sql.replace(";select @@identity;", "").replace(";select last_insert_rowid();", "");
                return this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            return this.conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new IllegalArgumentException("创建命令失败,请参照内部异常.", e);
        }
    }

    /**
     * 获取表示“重复插入”错误的代码，该代码特定于数据库引擎
     *
     * @return 表示“重复插入”错误的代码
     */
    private int getRepeatInsertionErrorNumber() {
        switch (this.getSourceType()) {
            case MySql:
                return 1062;
            case Sqlite:
                return 19;
            case PostgreSql:
                return 23505;
            case SqlServer:
                return 2627;
            case Oracle:
                return 1;
            case Other:
            case Oledb:
                return -1;
            default:
                throw new IllegalArgumentException("未知的数据源类型: " + this.getSourceType());
        }
    }
}
