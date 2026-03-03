package io.obase.test.domain.association.duplicateMapping;

/**
 * 表示为一个商品属性从可选值清单中选定了一个值
 */
public class StandardValue {

    /**
     * 取值别名，即属性值作为当前商品属性的取值时的别名
     */
    private String alias;

    /**
     * 属性的标识
     */
    private long attributeId;

    /**
     * 类目的标识
     */
    private long categoryId;

    /**
     * 商品的标识
     */
    private long goodsId;

    /**
     * 商品属性
     */
    private GoodsAttribute goodsAttribute;

    /**
     * 被选中的属性值
     */
    private SelectableValue selectedValue;

    /**
     * 属性图片，即针对商品的特定属性（如颜色为红色）拍摄的展示图片
     */
    private String photo;

    /**
     * 属性值的标识
     */
    private long valueId;

    /**
     * 初始化StandardValue类的新实例
     *
     * @param goodsAttribute 商品属性
     * @param selectedValue  被选中的属性值
     */
    public StandardValue(GoodsAttribute goodsAttribute, SelectableValue selectedValue) {
        this.goodsAttribute = goodsAttribute;
        this.selectedValue = selectedValue;
    }

    /**
     * 供对象重建（如反持久化）使用的构造函数
     */
    protected StandardValue() {
    }

    /**
     * 获取别名
     *
     * @return 别名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 设置别名
     *
     * @param alias 别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 获取属性的标识
     *
     * @return 属性的标识
     */
    public long getAttributeId() {
        return this.attributeId;
    }

    /**
     * 设置属性的标识
     *
     * @param attributeId 属性的标识
     */
    void setAttributeId(long attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * 获取类目的标识
     *
     * @return 类目的标识
     */
    public long getCategoryId() {
        return this.categoryId;
    }

    /**
     * 设置类目的标识
     *
     * @param categoryId 类目的标识
     */
    void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取商品的标识
     *
     * @return 商品的标识
     */
    public long getGoodsId() {
        return this.goodsId;
    }

    /**
     * 设置商品的标识
     *
     * @param goodsId 商品的标识
     */
    void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    /**
     * 获取商品属性
     *
     * @return 商品属性
     */
    public GoodsAttribute getGoodsAttribute() {
        return this.goodsAttribute;
    }

    /**
     * 设置商品属性
     *
     * @param goodsAttribute 商品属性
     */
    void setGoodsAttribute(GoodsAttribute goodsAttribute) {
        this.goodsAttribute = goodsAttribute;
    }

    /**
     * 获取被选中的属性值
     *
     * @return 被选中的属性值
     */
    public SelectableValue getSelectedValue() {
        return this.selectedValue;
    }

    /**
     * 设置被选中的属性值
     *
     * @param selectedValue 被选中的属性值
     */
    void setSelectedValue(SelectableValue selectedValue) {
        this.selectedValue = selectedValue;
    }

    /**
     * 获取图片
     *
     * @return 图片
     */
    public String getPhoto() {
        return this.photo;
    }

    /**
     * 设置图片
     *
     * @param photo 图片
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * 获取属性值的标识
     *
     * @return 属性值的标识
     */
    public long getValueId() {
        return this.valueId;
    }

    /**
     * 设置属性值的标识
     *
     * @param valueId 属性值的标识
     */
    void setValueId(long valueId) {
        this.valueId = valueId;
    }
}
