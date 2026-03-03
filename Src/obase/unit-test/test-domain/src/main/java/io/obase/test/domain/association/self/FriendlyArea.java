package io.obase.test.domain.association.self;

import java.util.Date;

/**
 * 友好区域
 */
public class FriendlyArea {

    /**
     * 区域
     */
    private Area area;

    /**
     * 区域代码
     */
    private String areaCode;

    /**
     * 友好区域
     */
    private Area friend;

    /**
     * 友好区域代码
     */
    private String friendlyAreaCode;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 获取区域
     *
     * @return 区域
     */
    public Area getArea() {
        return this.area;
    }

    /**
     * 设置区域
     *
     * @param area 区域
     */
    public void setArea(Area area) {
        this.area = area;
    }

    /**
     * 获取区域代码
     *
     * @return 区域代码
     */
    public String getAreaCode() {
        return this.areaCode;
    }

    /**
     * 设置区域代码
     *
     * @param areaCode 区域代码
     */
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    /**
     * 获取友好区域
     *
     * @return 友好区域
     */
    public Area getFriend() {
        return this.friend;
    }

    /**
     * 设置友好区域
     *
     * @param friend 友好区域
     */
    public void setFriend(Area friend) {
        this.friend = friend;
    }

    /**
     * 获取友好区域代码
     *
     * @return 友好区域代码
     */
    public String getFriendlyAreaCode() {
        return this.friendlyAreaCode;
    }

    /**
     * 设置友好区域代码
     *
     * @param friendlyAreaCode 友好区域代码
     */
    public void setFriendlyAreaCode(String friendlyAreaCode) {
        this.friendlyAreaCode = friendlyAreaCode;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * 设置开始时间
     *
     * @param startTime 开始时间
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
