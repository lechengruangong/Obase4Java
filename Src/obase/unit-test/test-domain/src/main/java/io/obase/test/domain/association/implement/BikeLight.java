package io.obase.test.domain.association.implement;

/**
 * 车灯
 */
public class BikeLight {
    /**
     * 车灯编码
     */
    private String code;

    /**
     * 亮度
     */
    private int value;

    /**
     * 获取车灯编码
     *
     * @return 车灯编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置车灯编码
     *
     * @param code 车灯编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取亮度
     *
     * @return 亮度
     */
    public int getValue() {
        return this.value;
    }

    /**
     * 设置亮度
     *
     * @param value 亮度
     */
    public void setValue(int value) {
        this.value = value;
    }
}
