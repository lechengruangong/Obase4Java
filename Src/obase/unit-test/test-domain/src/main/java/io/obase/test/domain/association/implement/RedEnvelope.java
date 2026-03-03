package io.obase.test.domain.association.implement;

/**
 * 红包
 */
public class RedEnvelope extends Prize {

    /**
     * 数额
     */
    private int amount;

    /**
     * 显示名称
     */
    private String displayName = "红包";

    /**
     * 获取数额
     *
     * @return 数额
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * 设置数额
     *
     * @param amount 数额
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = "红包";
    }

    /**
     * 获取描述
     *
     * @param prefix 前缀
     * @return 描述
     */
    @Override
    public String gotDescription(String prefix) {
        return "这是一个" + prefix + "的红包,里面" + this.amount + "元钱";
    }
}
