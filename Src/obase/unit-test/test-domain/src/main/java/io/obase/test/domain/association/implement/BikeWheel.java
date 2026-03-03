package io.obase.test.domain.association.implement;

/**
 * 车轮
 */
public class BikeWheel {

    /**
     * 车编码
     */
    private String bikeCode;

    /**
     * 车轮编码
     */
    private String code;

    /**
     * 获取车编码
     *
     * @return 车编码
     */
    public String getBikeCode() {
        return this.bikeCode;
    }

    /**
     * 设置车编码
     *
     * @param bikeCode 车编码
     */
    public void setBikeCode(String bikeCode) {
        this.bikeCode = bikeCode;
    }

    /**
     * 获取车轮编码
     *
     * @return 车轮编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置车轮编码
     *
     * @param code 车轮编码
     */
    public void setCode(String code) {
        this.code = code;
    }
}
