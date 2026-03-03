package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.ExistingConnectionSqlExecutor;

import java.sql.Connection;

/**
 * 使用使用已存在的PostgreSql连接上下文
 */
public class PostgreSqlExistingConnectionContext extends ObjectContext {

    /**
     * 初始化使用使用已存在的PostgreSql连接上下文
     *
     * @param connection 数据库连接
     */
    public PostgreSqlExistingConnectionContext(Connection connection) {
        super(new PostgresSqlExistingConnectionContextConfig(new ExistingConnectionSqlExecutor(connection, EDataSource.PostgreSql), false));
    }
}
