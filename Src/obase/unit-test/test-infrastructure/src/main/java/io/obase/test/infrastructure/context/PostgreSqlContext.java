package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;

/**
 * 测试用PostgreSql上下文
 */
public class PostgreSqlContext extends ObjectContext {
    /**
     * 构造测试用PostgreSql上下文
     */
    public PostgreSqlContext() {
        super(new PostgreSqlContextConfigProvider());
    }
}
