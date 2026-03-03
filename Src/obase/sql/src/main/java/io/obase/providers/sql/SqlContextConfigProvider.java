/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于SQL数据源的对象上下文配置提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:15:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.ContextConfigProvider;
import io.obase.core.IStorageProvider;
import io.obase.core.IStorageStructMappingProvider;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;

/**
 * 适用于SQL数据源的对象上下文配置提供程序
 */
public abstract class SqlContextConfigProvider extends ContextConfigProvider {

    /**
     * 由派生类实现 获取数据库驱动名称字符串
     *
     * @return 数据库驱动名称字符串
     */
    protected abstract String getDbDriverClass();

    /**
     * 由派生类实现，获取数据库连接字符串
     *
     * @return 数据库连接字符串
     */
    protected abstract String getConnectionString();

    /**
     * 由派生类实现 获取数据库用户名
     *
     * @return 数据库用户名
     */
    protected abstract String getConnectionUserName();

    /**
     * 由派生类实现 获取数据库密码
     *
     * @return 数据库密码
     */
    protected abstract String getConnectionPassWord();

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    protected EDataSource getSourceType() {
        return EDataSource.Other;
    }

    /**
     * 获取一个值，该值指示是否启用存储结构映射
     *
     * @return 是否启用存储结构映射
     */
    protected boolean getEnableStructMapping() {
        return false;
    }

    /**
     * 由派生类实现，获取指定存储标记对应的存储提供程序
     *
     * @param symbol 存储标记
     * @return 存储提供程序
     */
    @Override
    protected final IStorageProvider createStorageProvider(StorageSymbol symbol, ObjectDataModel model) {
        EDataSource sourceType = this.getSourceType();
        //未重写SourceType
        if (sourceType == EDataSource.Other) {
            switch (this.getDbDriverClass()) {
                case "com.mysql.jdbc.Driver":
                case "com.mysql.cj.jdbc.Driver":
                    sourceType = EDataSource.MySql;
                    break;
                case "com.microsoft.jdbc.sqlserver.SQLServerDriver":
                    sourceType = EDataSource.SqlServer;
                    break;
                case "oracle.jdbc.driver.OracleDriver":
                    sourceType = EDataSource.Oracle;
                    break;
                case "org.sqlite.JDBC":
                    sourceType = EDataSource.Sqlite;
                    break;
                case "org.postgresql.Driver":
                    sourceType = EDataSource.PostgreSql;
                    break;
                default:
                    throw new IllegalArgumentException("未能识别数据提供程序工厂的名称,请重写SqlContextConfigProvider的SourceType显式指定数据源类型");
            }
        }

        StandardSqlExecutor sqlExecutor = new StandardSqlExecutor(this.getDbDriverClass(), this.getConnectionString(), this.getConnectionUserName(), this.getConnectionPassWord(), sourceType, this.getObjectContext().getClass());
        return new SqlStorageProvider(sqlExecutor);
    }

    /**
     * 创建面向指定存储服务的存储结构映射提供程序
     *
     * @param storageSymbol 存储标记
     * @return 默认返回空
     */
    @Override
    protected IStorageStructMappingProvider createStorageStructMappingProvider(StorageSymbol storageSymbol) {
        if (this.getEnableStructMapping()) {
            EDataSource sourceType = this.getSourceType();
            //未重写SourceType
            if (sourceType == EDataSource.Other) {
                switch (this.getDbDriverClass()) {
                    case "oracle.jdbc.driver.OracleDriver":
                        sourceType = EDataSource.Oracle;
                        break;
                    case "org.sqlite.JDBC":
                        sourceType = EDataSource.Sqlite;
                        break;
                    case "org.postgresql.Driver":
                        sourceType = EDataSource.PostgreSql;
                        break;
                    case "com.mysql.jdbc.Driver":
                    case "com.mysql.cj.jdbc.Driver":
                        sourceType = EDataSource.MySql;
                        break;
                    case "com.microsoft.jdbc.sqlserver.SQLServerDriver":
                        sourceType = EDataSource.SqlServer;
                        break;
                    default:
                        throw new IllegalArgumentException("未能识别数据提供程序工厂的名称,请重写SqlContextConfigProvider的SourceType显式指定数据源类型");
                }
            }

            StandardSqlExecutor sqlExecutor = new StandardSqlExecutor(this.getDbDriverClass(), this.getConnectionString(), this.getConnectionUserName(), this.getConnectionPassWord(), sourceType, this.getObjectContext().getClass());

            return new SqlStorageStructMappingProvider(sqlExecutor);
        }

        return null;
    }
}
