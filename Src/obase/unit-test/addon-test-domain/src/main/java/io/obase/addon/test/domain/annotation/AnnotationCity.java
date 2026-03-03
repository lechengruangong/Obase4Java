package io.obase.addon.test.domain.annotation;


import io.obase.odm.annotation.ComplexAttribute;

/**
 * 市级行政区划
 */
@ComplexAttribute
public class AnnotationCity {
    /**
     * 市名称
     */
    private String name;

    /**
     * 市代码
     */
    private int code;

    /**
     * 获取市名称
     *
     * @return 市名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置市名称
     *
     * @param name 市名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取市代码
     *
     * @return 市代码
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 设置市代码
     *
     * @param code 市代码
     */
    public void setCode(int code) {
        this.code = code;
    }
}
