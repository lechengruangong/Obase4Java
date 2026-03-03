package io.obase.test.infrastructure.context;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.providers.sqlite.SqliteContextConfigProvider;
import io.obase.test.infrastructure.configuration.RelationshipDataBaseConfigurationManager;
import io.obase.test.infrastructure.modelRegister.AddonModelRegister;

/**
 * Sqlite数据源的插件测试上下文配置提供者
 */
public class SqliteAddonContextConfigProvider extends SqliteContextConfigProvider {
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
        return RelationshipDataBaseConfigurationManager.getSqliteConnectionString();
    }

    /**
     * 由派生类实现 获取数据库用户名
     *
     * @return 数据库用户名
     */
    @Override
    protected String getConnectionUserName() {
        return "";
    }

    /**
     * 由派生类实现 获取数据库密码
     *
     * @return 数据库密码
     */
    @Override
    protected String getConnectionPassWord() {
        return "";
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
