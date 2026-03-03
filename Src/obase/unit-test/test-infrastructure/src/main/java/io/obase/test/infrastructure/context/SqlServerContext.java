package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;

/**
 * 测试用SqlServer数据上下文
 */
public class SqlServerContext extends ObjectContext {
    /**
     * 构造测试用SqlServer数据上下文
     */
    public SqlServerContext() {
        super(new SqlServerContextConfigProvider());
    }
}
