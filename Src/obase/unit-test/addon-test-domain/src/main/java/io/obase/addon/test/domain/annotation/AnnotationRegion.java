package io.obase.addon.test.domain.annotation;


import io.obase.odm.annotation.ComplexAttribute;

/**
 * 区/县级行政区划
 */
@ComplexAttribute
public class AnnotationRegion {

    /**
     * 区/县名称
     */
    private String name;

    /**
     * 区/县代码
     */
    private int code;

    /**
     * 获取区/县名称
     *
     * @return 区/县名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置区/县名称
     *
     * @param name 区/县名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取区/县代码
     *
     * @return 区/县代码
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 设置区/县代码
     *
     * @param code 区/县代码
     */
    public void setCode(int code) {
        this.code = code;
    }
}
