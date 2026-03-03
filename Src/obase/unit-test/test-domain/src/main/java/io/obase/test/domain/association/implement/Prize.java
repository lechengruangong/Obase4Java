package io.obase.test.domain.association.implement;

/**
 * 奖品(抽象基类)
 */
public abstract class Prize {

    /**
     * 活动ID
     */
    private int activityId;

    /**
     * 奖品ID
     */
    private int id;

    /**
     * 获取活动ID
     *
     * @return 活动ID
     */
    public int getActivityId() {
        return this.activityId;
    }

    /**
     * 活动ID
     */
    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    /**
     * 奖品ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * 奖品ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public abstract String getDisplayName();

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    public abstract void setDisplayName(String displayName);

    /**
     * 获取描述
     *
     * @param prefix 前缀
     * @return 描述
     */
    public abstract String gotDescription(String prefix);
}
