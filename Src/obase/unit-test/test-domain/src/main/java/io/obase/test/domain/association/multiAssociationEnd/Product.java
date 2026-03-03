package io.obase.test.domain.association.multiAssociationEnd;

import io.obase.common.TwoTuple;

import java.util.List;

/**
 * 产品
 */
public class Product {

    /**
     * 产品编号
     */
    private String code;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 属性取值
     */
    private List<PropertyTakingValue> propertyTakingValues;

    /**
     * 隐式的属性取值
     */
    private List<TwoTuple<Property, PropertyValue>> propertyValues;

    /**
     * 获取产品编号
     *
     * @return 产品编号
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置产品编号
     *
     * @param code 产品编号
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取产品名称
     *
     * @return 产品名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置产品名称
     *
     * @param name 产品名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取属性取值
     *
     * @return 属性取值
     */
    public List<PropertyTakingValue> getPropertyTakingValues() {
        return this.propertyTakingValues;
    }

    /**
     * 设置属性取值
     *
     * @param propertyTakingValues 属性取值
     */
    public void setPropertyTakingValues(List<PropertyTakingValue> propertyTakingValues) {
        this.propertyTakingValues = propertyTakingValues;
    }

    /**
     * 获取隐式的属性取值
     *
     * @return 隐式的属性取值
     */
    public List<TwoTuple<Property, PropertyValue>> getPropertyValues() {
        return this.propertyValues;
    }

    /**
     * 设置隐式的属性取值
     *
     * @param propertyValues 隐式的属性取值
     */
    public void setPropertyValues(List<TwoTuple<Property, PropertyValue>> propertyValues) {
        this.propertyValues = propertyValues;
    }
}
