package io.obase.addon.test.domain.logical.deletion;

import io.obase.logical.deletion.LogicDeletionAttribute;
import io.obase.odm.annotation.EntityAttribute;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 无定义字段逻辑删除标注测试域类
 */
@EntityAttribute(isSelfIncrease = false, keyAttributes = {"IntNumber"})
@LogicDeletionAttribute(deletionField = "Bool")
public class LogicDeletionNoDefAnnotation {

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
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "LogicDeletionNoDef{" +
                "intNumber=" + this.intNumber +
                ", decimalNumber=" + this.decimalNumber +
                ", dateTime=" + this.dateTime +
                ", string='" + this.string + '\'' +
                '}';
    }
}
