package io.obase.test.domain.association.implement;

/**
 * 实体礼物
 */
public class InKindPrize extends Prize {

    /**
     * 礼物名称
     */
    private String name;

    /**
     * 礼物名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 礼物名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    @Override
    public String getDisplayName() {
        return this.gotDescription("优质");
    }

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    @Override
    public void setDisplayName(String displayName) {
        this.name = displayName;
    }

    /**
     * 获取描述
     *
     * @param prefix 前缀
     * @return 描述
     */
    @Override
    public String gotDescription(String prefix) {
        return "这是一个" + prefix + "的礼物,里面是" + this.name;
    }
}
