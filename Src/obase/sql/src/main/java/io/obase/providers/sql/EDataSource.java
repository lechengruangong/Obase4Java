/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举各种类型的数据源.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 16:00:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

public enum EDataSource {

    /**
     * SqlServer数据源
     */
    SqlServer,

    /**
     * Oracle数据源
     */
    Oracle,

    /**
     * OLEDB数据提供程序
     */
    Oledb,

    /**
     * MySql数据源
     */
    MySql,

    /**
     * Sqlite数据源
     */
    Sqlite,

    /**
     * PostgresSQL数据源
     */
    PostgreSql,

    /**
     * 其他数据源
     */
    Other
}
