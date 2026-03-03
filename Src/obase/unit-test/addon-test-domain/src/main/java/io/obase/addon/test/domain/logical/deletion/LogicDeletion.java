package io.obase.addon.test.domain.logical.deletion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 逻辑删除测试域类
 */
public class LogicDeletion {

    /**
     * int类型数字
     */
    private int intNumber;

    /**
     * decimal类型数字
     */
    private BigDecimal decimalNumber;

    /**
     * 时间类型
     */
    private LocalDateTime dateTime;

    /**
     * 字符串类型
     */
    private String string;

    /**
     * 布尔值类型
     */
    private boolean bool;

    /**
     * 获取int类型数字
     *
     * @return int类型数字
     */
    public int getIntNumber() {
        return this.intNumber;
    }

    /**
     * 设置int类型数字
     *
     * @param intNumber int类型数字
     */
    public void setIntNumber(int intNumber) {
        this.intNumber = intNumber;
    }

    /**
     * 获取decimal类型数字
     *
     * @return decimal类型数字
     */
    public BigDecimal getDecimalNumber() {
        return this.decimalNumber;
    }

    /**
     * 设置decimal类型数字
     *
     * @param decimalNumber decimal类型数字
     */
    public void setDecimalNumber(BigDecimal decimalNumber) {
        this.decimalNumber = decimalNumber;
    }

    /**
     * 获取时间类型
     *
     * @return 时间类型
     */
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * 设置时间类型
     *
     * @param dateTime 时间类型
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * 获取字符串类型
     *
     * @return 字符串类型
     */
    public String getString() {
        return this.string;
    }

    /**
     * 设置字符串类型
     *
     * @param string 字符串类型
     */
    public void setString(String string) {
        this.string = string;
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
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "LogicDeletion{" +
                "intNumber=" + this.intNumber +
                ", decimalNumber=" + this.decimalNumber +
                ", dateTime=" + this.dateTime +
                ", string='" + this.string + '\'' +
                ", bool=" + this.bool +
                '}';
    }
}
