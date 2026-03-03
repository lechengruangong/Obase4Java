package io.obase.test;

import io.obase.core.ObjectContext;
import io.obase.providers.sql.EDataSource;
import io.obase.test.infrastructure.context.*;

import java.sql.Connection;

/**
 * 上下文工具
 */
public class ContextUtils {

    /**
     * 创建一个新的上下文
     *
     * @param dataSource 数据源类型
     * @return 新的上下文
     */
    public static ObjectContext createContext(EDataSource dataSource) {
        return switch (dataSource) {
            case SqlServer -> new SqlServerContext();
            case MySql -> new MySqlContext();
            case Sqlite -> new SqliteContext();
            case PostgreSql -> new PostgreSqlContext();
            case Oledb, Oracle, Other -> throw new IndexOutOfBoundsException("暂无" + dataSource + "类型的上下文");
        };
    }

    /**
     * 创建一个新的插件上下文
     *
     * @param dataSource 数据源类型
     * @return 新的插件上下文
     */
    public static ObjectContext createAddonContext(EDataSource dataSource) {
        return switch (dataSource) {
            case SqlServer -> new SqlServerAddonContext();
            case MySql -> new MySqlAddonContext();
            case Sqlite -> new SqliteAddonContext();
            case PostgreSql -> new PostgreSqlAddonContext();
            case Oledb, Oracle, Other -> throw new IndexOutOfBoundsException("暂无" + dataSource + "类型的上下文");
        };
    }

    /**
     * 创建一个新的已有连接上下文
     *
     * @param connection 连接
     * @param dataSource 数据源类型
     * @return 新的已有连接上下文
     */
    public static ObjectContext createExistingConnectionContext(Connection connection, EDataSource dataSource) {
        return switch (dataSource) {
            case SqlServer -> new SqlServerExistingConnectionContext(connection);
            case MySql -> new MySqlExistingConnectionContext(connection);
            case Sqlite -> new SqliteExistingConnectionContext(connection);
            case PostgreSql -> new PostgreSqlExistingConnectionContext(connection);
            case Oledb, Oracle, Other -> throw new IndexOutOfBoundsException("暂无" + dataSource + "类型的上下文");
        };
    }
}
