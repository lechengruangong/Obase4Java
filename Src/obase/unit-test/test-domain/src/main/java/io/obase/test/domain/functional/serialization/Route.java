package io.obase.test.domain.functional.serialization;

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
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Route{" +
                "action=" + this.action +
                ", rule='" + this.rule + '\'' +
                '}';
    }
}
