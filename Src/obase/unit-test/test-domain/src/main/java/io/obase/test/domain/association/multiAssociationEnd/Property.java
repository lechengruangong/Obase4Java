package io.obase.test.domain.association.multiAssociationEnd;

/**
 * 属性
 */
public class Property {

    /**
     * 属性编号
     */
    private String code;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 获取属性编号
     *
     * @return 属性编号
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置属性编号
     *
     * @param code 属性编号
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置属性名称
     *
     * @param name 属性名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
