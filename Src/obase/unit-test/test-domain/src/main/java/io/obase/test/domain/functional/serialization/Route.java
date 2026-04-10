package io.obase.test.domain.functional.serialization;

import java.math.BigDecimal;

/**
 * 某种路由
 */
public class Route {

    /**
     * 路由操作
     */
    private EAction action;

    /**
     * 路由规则
     */
    private String rule;

    /**
     * 空对象
     */
    private String PalaceHolder;

    /**
     * 排序
     */
    private int sort;

    /**
     * 权重
     */
    private double weight;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 内部值
     */
    private BigDecimal inner;

    /**
     * 初始化某种路由
     *
     * @param rule   路由规则
     * @param action 路由操作
     */
    public Route(String rule, EAction action) {
        this.rule = rule;
        this.action = action;
    }

    /**
     * 供反序列化使用
     */
    protected Route() {
    }

    /**
     * 路由操作
     *
     * @return 路由操作
     */
    public EAction getAction() {
        return this.action;
    }

    /**
     * 路由操作
     *
     * @param action 路由操作
     */
    public void setAction(EAction action) {
        this.action = action;
    }

    /**
     * 路由规则
     *
     * @return 路由规则
     */
    public String getRule() {
        return this.rule;
    }

    /**
     * 路由规则
     *
     * @param rule 路由规则
     */
    void setRule(String rule) {
        this.rule = rule;
    }

    /**
     * 空对象
     *
     * @return 空对象
     */
    public String getPalaceHolder() {
        return this.PalaceHolder;
    }

    /**
     * 空对象
     *
     * @param palaceHolder 空对象
     */
    public void setPalaceHolder(String palaceHolder) {
        this.PalaceHolder = palaceHolder;
    }

    /**
     * 排序
     *
     * @return 排序
     */
    public int getSort() {
        return this.sort;
    }

    /**
     * 排序
     *
     * @param sort 排序
     */
    public void setSort(int sort) {
        this.sort = sort;
    }

    /**
     * 权重
     *
     * @return 权重
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * 权重
     *
     * @param weight 权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * 是否启用
     *
     * @return 是否启用
     */
    public boolean getEnabled() {
        return this.enabled;
    }

    /**
     * 是否启用
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 内部值
     *
     * @return 内部值
     */
    public BigDecimal getInner() {
        return this.inner;
    }

    /**
     * 内部值
     *
     * @param inner 内部值
     */
    public void setInner(BigDecimal inner) {
        this.inner = inner;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Route{" +
                "action=" + this.action +
                ", rule='" + this.rule + '\'' +
                ", sort='" + this.sort + '\'' +
                ", weight='" + this.weight + '\'' +
                ", enabled='" + this.enabled + '\'' +
                ", inner='" + this.inner + '\'' +
                '}';
    }
}
