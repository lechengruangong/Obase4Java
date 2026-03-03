package io.obase.test.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模拟的租户中心
 */
public class TenantIdCenter {

    /**
     * 单例的租户列表
     */
    public static final List<UUID> TenantIds = new ArrayList<>();
    /**
     * 模拟的用户索引 切换此索引模拟切换了用户0和1分别是普通的用户ID 2是全局ID 全是0
     */
    private static int currentUserIndex;

    static {
        //静态初始化
        TenantIds.add(UUID.randomUUID());
        TenantIds.add(UUID.randomUUID());
        TenantIds.add(new UUID(0, 0));
    }

    /**
     * 获取模拟的用户索引 切换此索引模拟切换了用户0和1分别是普通的用户ID 2是全局ID 全是0
     *
     * @return 模拟的用户索引
     */
    public static int getCurrentUserIndex() {
        return currentUserIndex;
    }

    /**
     * 设置模拟的用户索引 切换此索引模拟切换了用户0和1分别是普通的用户ID 2是全局ID 全是0
     *
     * @param currentUserIndex 模拟的用户索引 切换此索引模拟切换了用户0和1分别是普通的用户ID 2是全局ID 全是0
     */
    public static void setCurrentUserIndex(int currentUserIndex) {
        TenantIdCenter.currentUserIndex = currentUserIndex;
    }
}
