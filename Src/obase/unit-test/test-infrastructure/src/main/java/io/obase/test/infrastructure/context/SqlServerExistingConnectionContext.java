package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.ExistingConnectionSqlExecutor;

import java.sql.Connection;

/**
 * 使用已存在的SqlServer连接上下文配置
 */
public class SqlServerExistingConnectionContext extends ObjectContext {

    /**
     * 初始化使用使用已存在的PostgreSql连接上下文
     *
     * @param connection 数据库连接
     */
    public SqlServerExistingConnectionContext(Connection connection) {
        super(new SqlServerExistingConnectionContextConfig(new ExistingConnectionSqlExecutor(connection, EDataSource.SqlServer), false));
    }
}
