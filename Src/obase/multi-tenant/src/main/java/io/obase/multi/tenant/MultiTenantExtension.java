/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:24:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.odm.TypeExtension;

/**
 * 多租户扩展
 */
public class MultiTenantExtension extends TypeExtension {

    /**
     * 全局租户ID
     */
    private Object globalTenantId;

    /**
     * 是否包含全局Id进行查询
     */
    private boolean loadingGlobal;

    /**
     * 多租户标记的映射字段
     */
    private String tenantIdField;

    /**
     * 多租户标记的属性的名称
     */
    private String tenantIdMark;

    /**
     * 多租户标记的类型
     */
    private Class<?> tenantIdType;

    /**
     * 获取多租户标记的映射字段
     *
     * @return 多租户标记的映射字段
     */
    public String getTenantIdField() {
        return this.tenantIdField;
    }

    /**
     * 设置多租户标记的映射字段
     *
     * @param tenantIdField 多租户标记的映射字段
     */
    public void setTenantIdField(String tenantIdField) {
        this.tenantIdField = tenantIdField;
    }

    /**
     * 获取多租户标记的属性的名称
     *
     * @return 多租户标记的属性的名称
     */
    public String getTenantIdMark() {
        return this.tenantIdMark;
    }

    /**
     * 设置多租户标记的属性的名称
     *
     * @param tenantIdMark 多租户标记的属性的名称
     */
    public void setTenantIdMark(String tenantIdMark) {
        this.tenantIdMark = tenantIdMark;
    }

    /**
     * 获取多租户标记的类型
     *
     * @return 多租户标记的类型
     */
    public Class<?> getTenantIdType() {
        return this.tenantIdType;
    }

    /**
     * 设置多租户标记的类型
     *
     * @param tenantIdType 多租户标记的类型
     */
    public void setTenantIdType(Class<?> tenantIdType) {
        this.tenantIdType = tenantIdType;
    }

    /**
     * 获取全局租户ID
     *
     * @return 全局租户ID
     */
    public Object getGlobalTenantId() {
        return this.globalTenantId;
    }

    /**
     * 设置全局租户ID
     *
     * @param globalTenantId 全局租户ID
     */
    public void setGlobalTenantId(Object globalTenantId) {
        this.globalTenantId = globalTenantId;
    }

    /**
     * 获取是否包含全局Id进行查询
     *
     * @return 是否包含全局Id进行查询
     */
    public boolean getLoadingGlobal() {
        return this.loadingGlobal;
    }

    /**
     * 设置是否包含全局Id进行查询
     *
     * @param loadingGlobal 是否包含全局Id进行查询
     */
    public void setLoadingGlobal(boolean loadingGlobal) {
        this.loadingGlobal = loadingGlobal;
    }
}
