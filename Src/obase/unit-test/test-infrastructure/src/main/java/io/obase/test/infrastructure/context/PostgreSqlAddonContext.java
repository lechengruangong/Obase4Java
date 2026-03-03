package io.obase.test.infrastructure.context;

import io.obase.core.ObjectContext;
import io.obase.logical.deletion.LogicDeletionExtensions;
import io.obase.multi.tenant.MultiTenantExtensions;

/**
 * PostgreSql数据源的插件测试上下文
 */
public class PostgreSqlAddonContext extends ObjectContext {
    /**
     * 构造PostgreSql数据源的插件测试上下文
     */
    public PostgreSqlAddonContext() {
        super(new PostgreSqlAddonContextConfigProvider());
        //启用逻辑删除
        LogicDeletionExtensions.enableLogicDeletion(this);
        //启用多租户
        MultiTenantExtensions.enableMultiTenant(this);
    }
}
