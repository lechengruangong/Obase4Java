package io.obase.core.mapping.pipeline;

import java.io.Serializable;

/**
 * 对象属性取值数据结构
 */
public class ObjectAttribute implements Serializable {

    /**
     * 属性名称
     */
    private String attribute;

    /**
     * 属性的值
     */
    private Object value;

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getAttribute() {
        return this.attribute;
    }

    /**
     * 设置属性名称
     *
     * @param attribute 属性名称
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * 获取属性的值
     *
     * @return 属性的值
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * 设置属性的值
     *
     * @param value 属性的值
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return String.format("ObjectAttributeValue:{Attribute-\"%s\",Value-\"%s\"}", this.getAttribute(), this.getValue());
    }
}

