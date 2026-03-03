package io.obase.test.domain.association.implement;

import java.util.List;

/**
 * 活动
 */
public class Activity {

    /**
     * 活动ID
     */
    private int id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 礼物
     */
    private List<Prize> prizeList;

    /**
     * 获取活动ID
     *
     * @return 活动ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * 设置活动ID
     *
     * @param id 活动ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取活动名称
     *
     * @return 活动名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置活动名称
     *
     * @param name 活动名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取礼物
     *
     * @return 礼物
     */
    public List<Prize> getPrizeList() {
        return this.prizeList;
    }

    /**
     * 设置礼物
     *
     * @param prizeList 礼物
     */
    public void setPrizeList(List<Prize> prizeList) {
        this.prizeList = prizeList;
    }
}
