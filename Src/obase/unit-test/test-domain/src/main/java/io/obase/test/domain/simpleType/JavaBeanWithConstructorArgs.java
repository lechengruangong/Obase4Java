package io.obase.test.domain.simpleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * 仅有构造函数的JavaBean
 */
public class JavaBeanWithConstructorArgs {
    /**
     * decimal类型数字
     */
    private final BigDecimal decimalNumber;
    /**
     * 时间类型
     */
    private final LocalDateTime dateTime;
    /**
     * 字符串类型
     */
    private final String string;
    /**
     * 布尔值类型
     */
    private final boolean bool;
    /**
     * int类型数字
     */
    private final int intNumber;
    /**
     * 长整型
     */
    private final long longNumber;
    /**
     * byte
     */
    private final byte byteNumber;
    /**
     * Char
     */
    private final char charNumber;
    /**
     * float
     */
    private final float floatNumber;
    /**
     * double
     */
    private final double doubleNumber;
    /**
     * time
     */
    private final LocalTime time;
    /**
     * Date
     */
    private final LocalDate date;
    /**
     * 以某种分隔符分割的数组
     */
    private String[] strings;

    /**
     * 构造函数
     */
    public JavaBeanWithConstructorArgs(BigDecimal decimalNumber, LocalDateTime dateTime, String string, boolean bool, int intNumber, long longNumber, byte byteNumber, char charNumber, float floatNumber, double doubleNumber, LocalTime time, LocalDate date) {
        this.decimalNumber = decimalNumber;
        this.dateTime = dateTime;
        this.string = string;
        this.bool = bool;
        this.intNumber = intNumber;
        this.longNumber = longNumber;
        this.byteNumber = byteNumber;
        this.charNumber = charNumber;
        this.floatNumber = floatNumber;
        this.doubleNumber = doubleNumber;
        this.time = time;
        this.date = date;
    }

    /**
     * 获取int类型数字
     *
     * @return int类型数字
     */
    public int getIntNumber() {
        return this.intNumber;
    }

    /**
     * 获取日期
     *
     * @return 日期
     */
    public LocalDateTime getDateTime() {
        return this.dateTime;
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
     * 获取字符串
     *
     * @return 字符串
     */
    public String getString() {
        return this.string;
    }

    /**
     * 获取字符串数组
     *
     * @return 字符串数组
     */
    public String[] getStrings() {
        return this.strings;
    }

    /**
     * 设置字符串数组
     *
     * @param strings 字符串数组
     */
    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    /**
     * 获取布尔值
     *
     * @return 布尔值
     */
    public boolean getBool() {
        return this.bool;
    }

    /**
     * 获取长整型
     */
    public long getLongNumber() {
        return this.longNumber;
    }

    /**
     * 获取byte
     */
    public byte getByteNumber() {
        return this.byteNumber;
    }

    /**
     * 获取Char
     */
    public char getCharNumber() {
        return this.charNumber;
    }

    /**
     * 获取float
     */
    public float getFloatNumber() {
        return this.floatNumber;
    }

    /**
     * 获取double
     */
    public double getDoubleNumber() {
        return this.doubleNumber;
    }

    /**
     * 获取time
     */
    public LocalTime getTime() {
        return this.time;
    }

    /**
     * 获取Date
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "JavaBeanWithConstructorArgs{" +
                "decimalNumber=" + this.decimalNumber +
                ", dateTime=" + this.dateTime +
                ", string='" + this.string + '\'' +
                ", bool=" + this.bool +
                ", strings=" + Arrays.toString(this.strings) +
                ", intNumber=" + this.intNumber +
                ", longNumber=" + this.longNumber +
                ", byteNumber=" + this.byteNumber +
                ", charNumber=" + this.charNumber +
                ", floatNumber=" + this.floatNumber +
                ", doubleNumber=" + this.doubleNumber +
                ", time=" + this.time +
                ", date=" + this.date +
                '}';
    }
}
