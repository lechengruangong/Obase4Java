package io.obase.test.domain.association.implement;

import java.util.List;

/**
 * 表示自行车
 */
public class Bike {

    /**
     * 自行车编码
     */
    private String code;

    /**
     * 自行车灯
     */
    private BikeLight light;

    /**
     * 车灯编码
     */
    private String lightCode;

    /**
     * 自行车名称
     */
    private String name;

    /**
     * 1-普通车 2-MyBikeA 3-MyBikeB
     */
    private int type;

    /**
     * 自行车轮
     */
    private List<BikeWheel> wheels;

    /**
     * 获取自行车编码
     *
     * @return 自行车编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置自行车编码
     *
     * @param code 自行车编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取自行车灯
     *
     * @return 自行车灯
     */
    public BikeLight getLight() {
        return this.light;
    }

    /**
     * 设置自行车灯
     *
     * @param light 自行车灯
     */
    public void setLight(BikeLight light) {
        this.light = light;
    }

    /**
     * 获取车灯编码
     *
     * @return 车灯编码
     */
    public String getLightCode() {
        return this.lightCode;
    }

    /**
     * 设置车灯编码
     *
     * @param lightCode 车灯编码
     */
    public void setLightCode(String lightCode) {
        this.lightCode = lightCode;
    }

    /**
     * 获取自行车名称
     *
     * @return 自行车名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置自行车名称
     *
     * @param name 自行车名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取类型
     * 1-普通车 2-MyBikeA 3-MyBikeB
     *
     * @return 类型
     */
    public int getType() {
        return this.type;
    }

    /**
     * 设置类型
     * 0-普通车 1-MyBikeA 2-MyBikeB
     *
     * @param type 类型
     */
    void setType(int type) {
        this.type = type;
    }

    /**
     * 获取自行车轮
     *
     * @return 自行车轮
     */
    public List<BikeWheel> getWheels() {
        return this.wheels;
    }

    /**
     * 设置自行车轮
     *
     * @param wheels 自行车轮
     */
    public void setWheels(List<BikeWheel> wheels) {
        this.wheels = wheels;
    }
}
