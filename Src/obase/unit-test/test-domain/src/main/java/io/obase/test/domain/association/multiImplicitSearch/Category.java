package io.obase.test.domain.association.multiImplicitSearch;

import java.util.List;

/**
 * 产品分类
 */
public class Category {

    /**
     * 产品分类ID
     */
    private int categoryId;

    /**
     * 产品分类名称
     */
    private String name;

    /**
     * 分类下的产品
     */
    private List<Product> products;

    /**
     * 获取产品分类ID
     *
     * @return 产品分类ID
     */
    public int getCategoryId() {
        return this.categoryId;
    }

    /**
     * 设置产品分类ID
     *
     * @param categoryId 产品分类ID
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取产品分类名称
     *
     * @return 产品分类名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置产品分类名称
     *
     * @param name 产品分类名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取分类下的产品
     *
     * @return 分类下的产品
     */
    public List<Product> getProducts() {
        return this.products;
    }

    /**
     * 设置分类下的产品
     *
     * @param products 分类下的产品
     */
    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
