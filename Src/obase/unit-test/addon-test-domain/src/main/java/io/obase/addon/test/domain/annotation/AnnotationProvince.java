package io.obase.addon.test.domain.annotation;


import io.obase.odm.annotation.ComplexAttribute;

/**
 * 省级行政区划
 */
@ComplexAttribute
public class AnnotationProvince {

    /**
     * 省名称
     */
    private String name;

    /**
     * 省代码
     */
    private int code;

    /**
     * 获取省名称
     *
     * @return 省名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置省名称
     *
     * @param name 省名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取省代码
     *
     * @return 省代码
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 设置省代码
     *
     * @param code 省代码
     */
    public void setCode(int code) {
        this.code = code;
    }
}
