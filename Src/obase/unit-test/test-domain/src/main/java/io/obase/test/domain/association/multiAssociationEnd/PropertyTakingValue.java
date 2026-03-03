package io.obase.test.domain.association.multiAssociationEnd;

/**
 * 属性取值
 */
public class PropertyTakingValue {

    /**
     * 产品
     */
    private Product product;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 属性
     */
    private Property property;

    /**
     * 属性编码
     */
    private String propertyCode;

    /**
     * 属性取值
     */
    private PropertyValue propertyValue;

    /**
     * 属性取值编码
     */
    private String propertyValueCode;

    /**
     * 属性取值图片
     */
    private String propertyPhotoUrl;

    /**
     * 获取产品
     *
     * @return 产品
     */
    public Product getProduct() {
        return this.product;
    }

    /**
     * 设置产品
     *
     * @param product 产品
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * 获取产品编码
     *
     * @return 产品编码
     */
    public String getProductCode() {
        return this.productCode;
    }

    /**
     * 设置产品编码
     *
     * @param productCode 产品编码
     */
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * 设置属性
     *
     * @param property 属性
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * 获取属性编码
     *
     * @return 属性编码
     */
    public String getPropertyCode() {
        return this.propertyCode;
    }

    /**
     * 设置属性编码
     *
     * @param propertyCode 属性编码
     */
    public void setPropertyCode(String propertyCode) {
        this.propertyCode = propertyCode;
    }

    /**
     * 获取属性取值
     *
     * @return 属性取值
     */
    public PropertyValue getPropertyValue() {
        return this.propertyValue;
    }

    /**
     * 获取属性取值
     *
     * @param propertyValue 属性取值
     */
    public void setPropertyValue(PropertyValue propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * 获取属性取值编码
     *
     * @return 属性取值编码
     */
    public String getPropertyValueCode() {
        return this.propertyValueCode;
    }

    /**
     * 设置属性取值编码
     *
     * @param propertyValueCode 属性取值编码
     */
    public void setPropertyValueCode(String propertyValueCode) {
        this.propertyValueCode = propertyValueCode;
    }

    /**
     * 获取属性取值图片
     *
     * @return 属性取值图片
     */
    public String getPropertyPhotoUrl() {
        return this.propertyPhotoUrl;
    }

    /**
     * 设置属性取值图片
     *
     * @param propertyPhotoUrl 属性取值图片
     */
    public void setPropertyPhotoUrl(String propertyPhotoUrl) {
        this.propertyPhotoUrl = propertyPhotoUrl;
    }
}
