package io.obase.test.domain.association.explicitlySelf;

import java.util.List;

/**
 * 宾客
 */
public class Guest {

    /**
     * 朋友是我的人
     */
    private List<Friend> friendOfMes;

    /**
     * 我的朋友
     */
    private List<Friend> myFriends;

    /**
     * 宾客iD
     */
    private int guestId;

    /**
     * 宾客姓名
     */
    private String name;

    /**
     * 获取朋友是我的人
     *
     * @return 朋友是我的人
     */
    public List<Friend> getFriendOfMes() {
        return this.friendOfMes;
    }

    /**
     * 设置朋友是我的人
     *
     * @param friendOfMes 朋友是我的人
     */
    public void setFriendOfMes(List<Friend> friendOfMes) {
        this.friendOfMes = friendOfMes;
    }

    /**
     * 获取我的朋友
     *
     * @return 我的朋友
     */
    public List<Friend> getMyFriends() {
        return this.myFriends;
    }

    /**
     * 设置我的朋友
     *
     * @param myFriends 我的朋友
     */
    public void setMyFriends(List<Friend> myFriends) {
        this.myFriends = myFriends;
    }

    /**
     * 获取宾客iD
     *
     * @return 宾客iD
     */
    public int getGuestId() {
        return this.guestId;
    }

    /**
     * 设置宾客iD
     *
     * @param guestId 宾客iD
     */
    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    /**
     * 获取宾客姓名
     *
     * @return 宾客姓名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置宾客姓名
     *
     * @param name 宾客姓名
     */
    public void setName(String name) {
        this.name = name;
    }
}
