package io.obase.test.infrastructure.context;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.providers.mysql.MySqlContextConfigProvider;
import io.obase.test.infrastructure.configuration.RelationshipDataBaseConfigurationManager;
import io.obase.test.infrastructure.modelRegister.AddonModelRegister;

/**
 * MySql数据源的插件测试上下文配置提供者
 */
public class MySqlAddonContextConfigProvider extends MySqlContextConfigProvider {
    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 建模器
     */
    @Override
    protected void createModel(ModelBuilder modelBuilder) {
        //调用插件的模型注册器
        AddonModelRegister.registry(modelBuilder);
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
