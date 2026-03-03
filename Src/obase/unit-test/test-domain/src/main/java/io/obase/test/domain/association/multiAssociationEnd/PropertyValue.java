package io.obase.test.domain.association.multiAssociationEnd;

/**
 * 属性值
 */
public class PropertyValue {

    /**
     * 属性值编码
     */
    private String code;

    /**
     * 具体值
     */
    private String value;

    /**
     * 获取属性值编码
     *
     * @return 属性值编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置属性值编码
     *
     * @param code 属性值编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取具体值
     *
     * @return 具体值
     */
    public String getValue() {
        return this.value;
    }

    /**
     * 设置具体值
     *
     * @param value 具体值
     */
    public void setValue(String value) {
        this.value = value;
    }
}
