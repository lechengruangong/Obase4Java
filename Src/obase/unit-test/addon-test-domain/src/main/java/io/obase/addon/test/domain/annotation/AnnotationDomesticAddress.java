package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.EntityAttribute;

/**
 * 标注建模测试用国内地址
 */
@EntityAttribute(keyAttributes = {"Key"}, isSelfIncrease = false)
public class AnnotationDomesticAddress {

    /**
     * 市
     */
    private AnnotationCity city;

    /**
     * 省/直辖市
     */
    private AnnotationProvince province;

    /**
     * 区/县
     */
    private AnnotationRegion region;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 地址的键
     */
    private String key;

    /**
     * 获取市
     *
     * @return 市
     */
    public AnnotationCity getCity() {
        return this.city;
    }

    /**
     * 设置市
     *
     * @param city 市
     */
    public void setCity(AnnotationCity city) {
        this.city = city;
    }

    /**
     * 获取省/直辖市
     *
     * @return 省/直辖市
     */
    public AnnotationProvince getProvince() {
        return this.province;
    }

    /**
     * 设置省/直辖市
     *
     * @param province 省/直辖市
     */
    public void setProvince(AnnotationProvince province) {
        this.province = province;
    }

    /**
     * 获取区/县
     *
     * @return 区/县
     */
    public AnnotationRegion getRegion() {
        return this.region;
    }

    /**
     * 设置区/县
     *
     * @param region 区/县
     */
    public void setRegion(AnnotationRegion region) {
        this.region = region;
    }

    /**
     * 获取详细地址
     *
     * @return 详细地址
     */
    public String getDetailAddress() {
        return this.detailAddress;
    }

    /**
     * 设置详细地址
     *
     * @param detailAddress 详细地址
     */
    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    /**
     * 获取地址的键
     *
     * @return 地址的键
     */
    public String getKey() {
        return this.key;
    }

    /**
     * 设置地址的键
     *
     * @param key 地址的键
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AnnotationDomesticAddress{" +
                "city=" + this.city +
                ", province=" + this.province +
                ", region=" + this.region +
                ", detailAddress='" + this.detailAddress + '\'' +
                ", key='" + this.key + '\'' +
                '}';
    }
}
