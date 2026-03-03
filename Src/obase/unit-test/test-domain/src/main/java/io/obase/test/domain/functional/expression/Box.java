package io.obase.test.domain.functional.expression;

/**
 * 表示盒子
 */
public class Box {

    /**
     * ID
     */
    private long id;

    /**
     * 配套的罐子
     */
    private Can can;

    /**
     * 是否是大型盒子
     */
    private Boolean isBig;

    /**
     * 是否是高质量盒子
     */
    private Boolean isGood;

    /**
     * 有没有盖子
     */
    private boolean hasCover;

    /**
     * 获取ID
     *
     * @return ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * 获取配套的罐子
     *
     * @return 配套的罐子
     */
    public Can getCan() {
        return this.can;
    }

    /**
     * 设置配套的罐子
     *
     * @param can 配套的罐子
     */
    public void setCan(Can can) {
        this.can = can;
    }

    /**
     * 获取是否是大型盒子
     *
     * @return 是否是大型盒子
     */
    public Boolean getIsBig() {
        return this.isBig;
    }

    /**
     * 设置
     *
     * @param big 是否是大型盒子
     */
    public void setIsBig(Boolean big) {
        this.isBig = big;
    }

    /**
     * 获取是否是高质量盒子
     *
     * @return 是否是高质量盒子
     */
    public Boolean getIsGood() {
        return this.isGood;
    }

    /**
     * 设置是否是高质量盒子
     *
     * @param good 是否是高质量盒子
     */
    public void setIsGood(Boolean good) {
        this.isGood = good;
    }

    /**
     * 获取有没有盖子
     *
     * @return 有没有盖子
     */
    public boolean getHasCover() {
        return this.hasCover;
    }

    /**
     * 设置有没有盖子
     *
     * @param hasCover 有没有盖子
     */
    public void setHasCover(boolean hasCover) {
        this.hasCover = hasCover;
    }
}
