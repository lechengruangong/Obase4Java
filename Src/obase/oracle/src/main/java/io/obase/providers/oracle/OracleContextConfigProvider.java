/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于Oracle数据源的对象上下文配置提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 15:06:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.oracle;

import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.SqlContextConfigProvider;

/**
 * 适用于Oracle数据源的对象上下文配置提供程序
 */
public abstract class OracleContextConfigProvider extends SqlContextConfigProvider {

    /**
     * 由派生类实现 获取数据库驱动名称字符串
     *
     * @return 数据库驱动名称字符串
     */
    @Override
    protected String getDbDriverClass() {
        return "oracle.jdbc.OracleDriver";
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    protected EDataSource getSourceType() {
        return EDataSource.Oracle;
    }
}
