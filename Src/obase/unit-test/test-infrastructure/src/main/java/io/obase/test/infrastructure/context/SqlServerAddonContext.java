package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.logical.deletion.LogicDeletionExtensions;
import io.obase.multi.tenant.MultiTenantExtensions;

/**
 * SqlServer数据源的插件测试上下文
 */
public class SqlServerAddonContext extends ObjectContext {
    /**
     * 构造Sqlite数据源的插件测试上下文
     */
    public SqlServerAddonContext() {
        super(new SqlServerAddonContextConfigProvider());
        //启用逻辑删除
        LogicDeletionExtensions.enableLogicDeletion(this);
        //启用多租户
        MultiTenantExtensions.enableMultiTenant(this);
    }
}