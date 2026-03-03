package io.obase.test.domain.association.self;

import java.util.List;

/**
 * 表示一个区域
 */
public class Area {

    /**
     * 区域代码
     */
    private String code;

    /**
     * 友好区域
     */
    private List<FriendlyArea> friendlyAreas;

    /**
     * 名字
     */
    private String name;

    /**
     * 父级区域
     */
    private Area parentArea;

    /**
     * 父级区域代码
     */
    private String parentCode;

    /**
     * 子区域
     */
    private List<Area> subAreas;

    /**
     * 获取区域代码
     *
     * @return 区域代码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置区域代码
     *
     * @param code 区域代码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取友好区域
     *
     * @return 友好区域
     */
    public List<FriendlyArea> getFriendlyAreas() {
        return this.friendlyAreas;
    }

    /**
     * 设置友好区域
     *
     * @param friendlyAreas 友好区域
     */
    public void setFriendlyAreas(List<FriendlyArea> friendlyAreas) {
        this.friendlyAreas = friendlyAreas;
    }

    /**
     * 获取名字
     *
     * @return 名字
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置名字
     *
     * @param name 名字
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取父级区域
     *
     * @return 父级区域
     */
    public Area getParentArea() {
        return this.parentArea;
    }

    /**
     * 设置父级区域
     *
     * @param parentArea 父级区域
     */
    public void setParentArea(Area parentArea) {
        this.parentArea = parentArea;
    }

    /**
     * 获取父级区域代码
     *
     * @return 父级区域代码
     */
    public String getParentCode() {
        return this.parentCode;
    }

    /**
     * 设置父级区域代码
     *
     * @param parentCode 父级区域代码
     */
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    /**
     * 获取子区域
     *
     * @return 子区域
     */
    public List<Area> getSubAreas() {
        return this.subAreas;
    }

    /**
     * 设置子区域
     *
     * @param subAreas 子区域
     */
    public void setSubAreas(List<Area> subAreas) {
        this.subAreas = subAreas;
    }
}
