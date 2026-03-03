package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;

/**
 * 测试用MySql数据上下文
 */
public class MySqlContext extends ObjectContext {
    /**
     * 初始化测试用MySql数据上下文
     */
    public MySqlContext() {
        super(new MySqlContextConfigProvider());
    }
}
