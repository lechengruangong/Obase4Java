package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.ExistingConnectionSqlExecutor;

import java.sql.Connection;

/**
 * 使用已存在的Sqlite连接上下文
 */
public class SqliteExistingConnectionContext extends ObjectContext {

    /**
     * 初始化使用使用已存在的PostgreSql连接上下文
     *
     * @param connection 数据库连接
     */
    public SqliteExistingConnectionContext(Connection connection) {
        super(new SqliteExistingConnectionContextConfig(new ExistingConnectionSqlExecutor(connection, EDataSource.Sqlite), false));
    }
}
