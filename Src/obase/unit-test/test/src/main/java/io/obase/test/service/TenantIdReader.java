package io.obase.test.service;

import io.obase.multi.tenant.ITenantIdReader;

/**
 * 多租户读取器
 */
public class TenantIdReader implements ITenantIdReader {
    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    @Override
    public Object getTenantId() {
        return TenantIdCenter.TenantIds.get(TenantIdCenter.getCurrentUserIndex());
    }
}