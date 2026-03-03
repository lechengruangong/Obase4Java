/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户工具类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:45:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;

/**
 * 多租户工具类
 */
public class MultiTenantExtensions {

    /**
     * 使用多租户
     *
     * @param context 对象上下文
     */
    public static void enableMultiTenant(ObjectContext context) {
        context.registerModule(new MultiTenantModule());
    }

    /**
     * 获取租户ID
     *
     * @param contextType 对象上下文类型
     * @return 租户ID
     */
    public static Object getTenantId(Class<?> contextType) {
        ITenantIdReader reader = Utils.getDependencyInjectionService(contextType, ITenantIdReader.class);

        return reader.getTenantId();
    }
}
