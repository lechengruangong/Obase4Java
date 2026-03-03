package io.obase.test.domain.association.multiImplicitSearch;

/**
 * 显式化的产品分类隐式关联型
 */
public class ProductCategory {

    /**
     * 分类
     */
    private Category category;

    /**
     * 分类ID
     */
    private int categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 产品
     */
    private Product product;

    /**
     * 产品Code
     */
    private String productCode;

    /**
     * 获取分类
     *
     * @return 分类
     */
    public Category getCategory() {
        return this.category;
    }

    /**
     * 设置分类
     *
     * @param category 分类
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    public int getCategoryId() {
        return this.categoryId;
    }

    /**
     * 设置分类ID
     *
     * @param categoryId 分类ID
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取分类名称
     *
     * @return 分类名称
     */
    public String getCategoryName() {
        return this.categoryName;
    }

    /**
     * 设置分类名称
     *
     * @param categoryName 分类名称
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

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
     * 获取产品Code
     *
     * @return 产品Code
     */
    public String getProductCode() {
        return this.productCode;
    }

    /**
     * 设置产品Code
     *
     * @param productCode 产品Code
     */
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
}
