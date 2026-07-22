package io.obase.test.domain.functional.keywords;

import java.util.List;

/**
 * USER关键字同名类
 */
public class User {

    /**
     * 主键
     */
    private long userId;

    /**
     * 名称
     */
    private String userName;

    /**
     * 订单
     */
    private List<Order> orders;

    /**
     * 获取主键
     *
     * @return 主键
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * 设置主键
     *
     * @param userId 主键
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * 设置名称
     *
     * @param userName 名称
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取订单
     *
     * @return 订单
     */
    public List<Order> getOrders() {
        return this.orders;
    }

    /**
     * 设置订单
     *
     * @param orders 订单
     */
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
