package io.obase.test.domain.association.duplicateMapping;

/**
 * 表示在一个商品上为某一个属性设置了值，简称商品属性。
 */
public class GoodsAttribute {

    /**
     * 属性的标识
     */
    private long attributeId;

    /**
     * 商品的标识
     */
    private long goodsId;

    /**
     * 该商品属性的输入值（与标准值对举）
     */
    private String inputValue;

    /**
     * 初始化GoodsAttribute类的新实例
     *
     * @param attributeId 属性的标识
     * @param goodsId     商品的标识
     */
    public GoodsAttribute(long attributeId, long goodsId) {
        this.attributeId = attributeId;
        this.goodsId = goodsId;
    }

    /**
     * 供对象重建（如反持久化）使用的构造函数
     */
    protected GoodsAttribute() {

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
     * 获取该商品属性的输入值
     *
     * @return 该商品属性的输入值
     */
    public String getInputValue() {
        return this.inputValue;
    }

    /**
     * 设置该商品属性的输入值
     *
     * @param inputValue 该商品属性的输入值
     */
    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }
}
