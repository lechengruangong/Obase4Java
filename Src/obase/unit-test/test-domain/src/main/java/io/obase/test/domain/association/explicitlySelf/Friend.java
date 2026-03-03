package io.obase.test.domain.association.explicitlySelf;

/**
 * 宾客的朋友关系
 */
public class Friend {

    /**
     * 朋友
     */
    private Guest friendGuest;

    /**
     * 朋友的ID
     */
    private int friendId;

    /**
     * 于哪个游戏里遇见的
     */
    private String meetIn;

    /**
     * 自己
     */
    private Guest mySelf;

    /**
     * 自己ID
     */
    private int mySelfId;

    /**
     * 获取朋友
     *
     * @return 朋友
     */
    public Guest getFriendGuest() {
        return this.friendGuest;
    }

    /**
     * 设置朋友
     *
     * @param friendGuest 朋友
     */
    public void setFriendGuest(Guest friendGuest) {
        this.friendGuest = friendGuest;
    }

    /**
     * 获取朋友的ID
     *
     * @return 朋友的ID
     */
    public int getFriendId() {
        return this.friendId;
    }

    /**
     * 设置朋友的ID
     *
     * @param friendId 朋友的ID
     */
    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    /**
     * 获取于哪个游戏里遇见的
     *
     * @return 于哪个游戏里遇见的
     */
    public String getMeetIn() {
        return this.meetIn;
    }

    /**
     * 设置于哪个游戏里遇见的
     *
     * @param meetIn 于哪个游戏里遇见的
     */
    public void setMeetIn(String meetIn) {
        this.meetIn = meetIn;
    }

    /**
     * 获取自己
     *
     * @return 自己
     */
    public Guest getMySelf() {
        return this.mySelf;
    }

    /**
     * 设置自己
     *
     * @param mySelf 自己
     */
    public void setMySelf(Guest mySelf) {
        this.mySelf = mySelf;
    }

    /**
     * 获取自己ID
     *
     * @return 自己ID
     */
    public int getMySelfId() {
        return this.mySelfId;
    }

    /**
     * 设置自己ID
     *
     * @param mySelfId 自己ID
     */
    public void setMySelfId(int mySelfId) {
        this.mySelfId = mySelfId;
    }
}
