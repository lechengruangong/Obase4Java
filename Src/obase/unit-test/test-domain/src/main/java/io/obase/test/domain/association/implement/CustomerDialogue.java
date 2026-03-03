package io.obase.test.domain.association.implement;

/**
 * 表示客户对话
 */
public class CustomerDialogue extends Dialogue {

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户备注
     */
    private String customerMemo;

    /**
     * 获取客户名称
     *
     * @return 客户名称
     */
    public String getCustomerName() {
        return this.customerName;
    }

    /**
     * 设置客户名称
     *
     * @param customerName 客户名称
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * 获取客户备注
     *
     * @return 客户备注
     */
    public String getCustomerMemo() {
        return this.customerMemo;
    }

    /**
     * 设置客户备注
     *
     * @param customerMemo 客户备注
     */
    public void setCustomerMemo(String customerMemo) {
        this.customerMemo = customerMemo;
    }
}
