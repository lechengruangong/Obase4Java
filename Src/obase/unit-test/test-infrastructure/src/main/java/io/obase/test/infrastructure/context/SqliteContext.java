package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;

/**
 * 测试用Sqlite上下文
 */
public class SqliteContext extends ObjectContext {
    /**
     * 构造ObjectContext对象
     */
    public SqliteContext() {
        super(new SqliteContextConfigProvider());
    }
}
