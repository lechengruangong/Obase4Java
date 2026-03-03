package io.obase.test.domain.simpleType;

/**
 * 小型的JavaBean的失血模型
 */
public class SmallJavaBeanLikeModel {

    /**
     * decimal类型数字
     */
    private double decimalNumber;

    /**
     * 布尔值类型
     */
    private boolean bool;

    /**
     * 获取decimal类型数字
     *
     * @return decimal类型数字
     */
    public double getDecimalNumber() {
        return this.decimalNumber;
    }

    /**
     * 设置decimal类型数字
     *
     * @param decimalNumber decimal类型数字
     */
    public void setDecimalNumber(double decimalNumber) {
        this.decimalNumber = decimalNumber;
    }

    /**
     * 获取布尔值类型
     *
     * @return 布尔值类型
     */
    public boolean getBool() {
        return this.bool;
    }

    /**
     * 设置布尔值类型
     *
     * @param bool 布尔值类型
     */
    public void setBool(boolean bool) {
        this.bool = bool;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "SmallJavaBeanLikeModel{" +
                "decimalNumber=" + this.decimalNumber +
                ", bool=" + this.bool +
                '}';
    }
}
