package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.ExistingConnectionSqlExecutor;

import java.sql.Connection;

/**
 * 使用已存在的MySql连接上下文
 */
public class MySqlExistingConnectionContext extends ObjectContext {

    /**
     * 初始化使用已存在的MySql连接上下文
     *
     * @param connection 数据库连接
     */
    public MySqlExistingConnectionContext(Connection connection) {
        super(new MySqlExistingConnectionContextConfig(new ExistingConnectionSqlExecutor(connection, EDataSource.MySql), false));
    }
}
