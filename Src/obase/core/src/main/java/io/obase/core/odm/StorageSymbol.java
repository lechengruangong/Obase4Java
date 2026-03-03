/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储标记.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 15:07:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 表示存储标记。
 * 存储标记是系统运行期间用来指代存储服务的临时标记。例如，规定某一SQL Server实例的存储标记为A，那么所有打上该标记的类型的实例都存储于该SQL
 * Server实例。
 */
public final class StorageSymbol {

    /**
     * 用于调试的名称
     */
    private String debugName;

    /**
     * 获取用于调试的名称
     *
     * @return 用于调试的名称
     */
    public String getDebugName() {
        return this.debugName;
    }

    /**
     * 设置用于调试的名称
     *
     * @param debugName 用于调试的名称
     */
    void setDebugName(String debugName) {
        this.debugName = debugName;
    }
}
