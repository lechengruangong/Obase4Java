package io.obase.test.configuration;

import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;
import io.obase.test.infrastructure.configuration.RelationshipDataBaseConfigurationManager;
import io.obase.test.infrastructure.context.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * 测试用例数据源配置管理器
 */
public class TestCaseSourceConfigurationManager implements ArgumentsProvider {

    /**
     * 获取测试用例数据源
     * 根据获取数据源的结果来构造测试用例数据源
     *
     * @return 根据获取数据源的结果来构造测试用例数据源
     */
    public static Stream<Arguments> getDataSourceTestCases() {
        //获取数据源 然后包转为Junit使用的Stream<Arguments>
        var result = new ArrayList<Arguments>();
        var dataSources = getDataSources();
        for (var dataSource : dataSources) {
            result.add(Arguments.of(dataSource));
        }
        return result.stream();
    }

    /**
     * 获取数据源
     * 根据RelationshipDataBaseConfigurationManager的配置来获取测试用例数据源
     *
     * @return 已配置的数据源集合
     */
    public static Iterable<EDataSource> getDataSources() {
        var result = new ArrayList<EDataSource>();

        //根据配置是否存在返回相应的数据源类型
        if (!Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getMySqlConnectionString()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getMySqlUserName()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getMySqlPassWord()))
            result.add(EDataSource.MySql);
        if (!Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getSqliteConnectionString()))
            result.add(EDataSource.Sqlite);
        if (!Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getSqlServerConnectionString()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getSqlServerUserName()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getSqlServerPassWord()))
            result.add(EDataSource.SqlServer);
        if (!Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getPostgreSqlConnectionString()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getPostgreSqlUserName()) && !Utils.getStringIsEmpty(RelationshipDataBaseConfigurationManager.getPostgreSqlPassWord()))
            result.add(EDataSource.PostgreSql);

        return result;
    }

    /**
     * 获取数据源对应的对象上下文类型
     *
     * @param dataSource 数据源类型
     * @return 数据源对应的对象上下文类型
     */
    public static Class<? extends ObjectContext> getDataSourceContextType(EDataSource dataSource) {
        return switch (dataSource) {
            case SqlServer -> SqlServerContext.class;
            case MySql -> MySqlContext.class;
            case Sqlite -> SqliteContext.class;
            case PostgreSql -> PostgreSqlContext.class;
            case Oledb, Oracle, Other -> throw new IndexOutOfBoundsException("暂无" + dataSource + "类型的上下文");
        };
    }

    /**
     * 获取数据源对应的插件对象上下文类型
     *
     * @param dataSource 数据源类型
     * @return 数据源对应的插件对象上下文类型
     */
    public static Class<? extends ObjectContext> getDataSourceAddonContextType(EDataSource dataSource) {
        return switch (dataSource) {
            case SqlServer -> SqlServerAddonContext.class;
            case MySql -> MySqlAddonContext.class;
            case Sqlite -> SqliteAddonContext.class;
            case PostgreSql -> PostgreSqlAddonContext.class;
            case Oledb, Oracle, Other -> throw new IndexOutOfBoundsException("暂无" + dataSource + "类型的上下文");
        };
    }

    /**
     * 返回数据源测试参数
     *
     * @param extensionContext 上下文
     * @return 数据源测试参数
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return TestCaseSourceConfigurationManager.getDataSourceTestCases();
    }
}
