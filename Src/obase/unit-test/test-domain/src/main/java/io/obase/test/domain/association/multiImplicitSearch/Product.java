package io.obase.test.domain.association.multiImplicitSearch;

import java.util.List;

/**
 * 产品
 */
public class Product {

    /**
     * 所属的分类
     */
    private List<Category> categories;

    /**
     * 产品Code
     */
    private String code;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 获取所属的分类
     *
     * @return 所属的分类
     */
    public List<Category> getCategories() {
        return this.categories;
    }

    /**
     * 设置所属的分类
     *
     * @param categories 所属的分类
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    /**
     * 获取产品Code
     *
     * @return 产品Code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置产品Code
     *
     * @param code 产品Code
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
}
