package io.obase.test.domain.association.duplicateMapping;

/**
 * 表示一个属性值可以作为某一类目属性的可选值
 */
public class SelectableValue {

    /**
     * 作为当前属性的可选值时的别名
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
     * 在当前属性可选值集合中的排序
     */
    private int sequence;

    /**
     * 初始化SelectableValue类的新实例
     *
     * @param attributeId 属性的标识
     * @param categoryId  类目的标识
     */
    public SelectableValue(long categoryId, long attributeId) {
        this.attributeId = attributeId;
        this.categoryId = categoryId;
    }

    /**
     * 供对象重建（如反持久化）使用的构造函数
     */
    protected SelectableValue() {

    }

    /**
     * 获取作为当前属性的可选值时的别名
     *
     * @return 作为当前属性的可选值时的别名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 设置作为当前属性的可选值时的别名
     *
     * @param alias 作为当前属性的可选值时的别名
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
     * 获取在当前属性可选值集合中的排序
     *
     * @return 在当前属性可选值集合中的排序
     */
    public int getSequence() {
        return this.sequence;
    }

    /**
     * 设置在当前属性可选值集合中的排序
     *
     * @param sequence 在当前属性可选值集合中的排序
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
