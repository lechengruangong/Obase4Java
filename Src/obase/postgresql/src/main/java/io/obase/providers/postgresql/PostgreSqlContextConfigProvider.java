/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于PostgreSQL数据源的对象上下文配置提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 15:07:32
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.postgresql;

import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.SqlContextConfigProvider;

/**
 * 适用于PostgreSQL数据源的对象上下文配置提供程序
 */
public abstract class PostgreSqlContextConfigProvider extends SqlContextConfigProvider {

    /**
     * 由派生类实现 获取数据库驱动名称字符串
     *
     * @return 数据库驱动名称字符串
     */
    @Override
    protected String getDbDriverClass() {
        return "org.postgresql.Driver";
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    protected EDataSource getSourceType() {
        return EDataSource.PostgreSql;
    }
}
