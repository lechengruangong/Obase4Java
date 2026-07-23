package io.obase.test.domain.functional.keywords;

/**
 * Order关键字的同名类
 */
public class Order {

    /**
     * 主键
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 用户ID
     */
    private long userId;

    /**
     * 用户
     */
    private User user;

    /**
     * 获取主键
     *
     * @return 主键
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置主键
     *
     * @param code 主键
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户
     *
     * @return 用户
     */
    public User getUser() {
        return this.user;
    }

    /**
     * 设置用户
     *
     * @param user 用户
     */
    public void setUser(User user) {
        this.user = user;
    }
}
