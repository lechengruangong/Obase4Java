package io.obase.test.infrastructure.context;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.test.infrastructure.configuration.RelationshipDataBaseConfigurationManager;
import io.obase.test.infrastructure.modelRegister.CoreModelRegister;

/**
 * 测试用MySql数据上下文配置器
 */
public class MySqlContextConfigProvider extends io.obase.providers.mysql.MySqlContextConfigProvider {
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

    /**
     * 由派生类实现，获取数据库连接字符串
     *
     * @return 数据库连接字符串
     */
    @Override
    protected String getConnectionString() {
        return RelationshipDataBaseConfigurationManager.getMySqlConnectionString();
    }

    /**
     * 由派生类实现 获取数据库用户名
     *
     * @return 数据库用户名
     */
    @Override
    protected String getConnectionUserName() {
        return RelationshipDataBaseConfigurationManager.getMySqlUserName();
    }

    /**
     * 由派生类实现 获取数据库密码
     *
     * @return 数据库密码
     */
    @Override
    protected String getConnectionPassWord() {
        return RelationshipDataBaseConfigurationManager.getMySqlPassWord();
    }

    /**
     * 获取一个值，该值指示是否启用存储结构映射
     *
     * @return 是否启用存储结构映射
     */
    @Override
    protected boolean getEnableStructMapping() {
        return RelationshipDataBaseConfigurationManager.getNeedStructMapping() != null && RelationshipDataBaseConfigurationManager.getNeedStructMapping();
    }
}
