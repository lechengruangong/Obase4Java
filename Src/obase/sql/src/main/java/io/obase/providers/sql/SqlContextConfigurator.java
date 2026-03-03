/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：需要自主控制Sql执行器的对象上下文配置提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-7-1 16:36:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.ContextConfigProvider;
import io.obase.core.IStorageProvider;
import io.obase.core.IStorageStructMappingProvider;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;

/**
 * 需要自主控制Sql执行器的对象上下文配置提供程序
 */
public abstract class SqlContextConfigurator extends ContextConfigProvider {

    /**
     * Sql语句执行器
     */
    private final ISqlExecutor sqlExecutor;

    /**
     * 指示是否启用存储架构映射
     */
    private final boolean enableStructMapping;

    /**
     * 使用指定的Sql执行器初始化SqlContextConfigurator的新实例
     *
     * @param sqlExecutor Sql语句执行器
     */
    public SqlContextConfigurator(ISqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        this.enableStructMapping = false;
    }

    /**
     * 使用指定的Sql执行器初始化SqlContextConfigurator的新实例
     *
     * @param sqlExecutor         Sql语句执行器
     * @param enableStructMapping 指示是否启用存储架构映射
     */
    public SqlContextConfigurator(ISqlExecutor sqlExecutor, boolean enableStructMapping) {
        this.sqlExecutor = sqlExecutor;
        this.enableStructMapping = enableStructMapping;
    }

    /**
     * 由派生类实现，创建指定存储标记对应的存储提供程序。
     *
     * @param symbol 存储标记
     * @param model  模型
     * @return 存储提供程序
     */
    @Override
    protected IStorageProvider createStorageProvider(StorageSymbol symbol, ObjectDataModel model) {
        return new SqlStorageProvider(this.sqlExecutor);
    }

    /**
     * 创建面向Sql服务器的存储结构映射提供程序
     *
     * @param storageSymbol 存储标记
     * @return 存储映射
     */
    @Override
    protected IStorageStructMappingProvider createStorageStructMappingProvider(StorageSymbol storageSymbol) {
        if (this.enableStructMapping)
            return new SqlStorageStructMappingProvider(this.sqlExecutor);
        return null;
    }
}
