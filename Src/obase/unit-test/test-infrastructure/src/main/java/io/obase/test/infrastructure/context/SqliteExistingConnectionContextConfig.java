package io.obase.test.infrastructure.context;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.providers.sql.ISqlExecutor;
import io.obase.providers.sql.SqlContextConfigurator;
import io.obase.test.infrastructure.modelRegister.CoreModelRegister;

/**
 * 使用已存在的Sqlite连接上下文配置
 */
public class SqliteExistingConnectionContextConfig extends SqlContextConfigurator {

    /**
     * 使用指定的Sql执行器初始化SqlContextConfigurator的新实例
     *
     * @param sqlExecutor         Sql语句执行器
     * @param enableStructMapping 指示是否启用存储架构映射
     */
    public SqliteExistingConnectionContextConfig(ISqlExecutor sqlExecutor, boolean enableStructMapping) {
        super(sqlExecutor, enableStructMapping);
    }

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 建模器
     */
    @Override
    protected void createModel(ModelBuilder modelBuilder) {
        //注册核心模型
        CoreModelRegister.registry(modelBuilder);
    }
}
