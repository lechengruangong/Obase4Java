package io.obase.test.domain.simpleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * 一个类似于JavaBean的失血模型 包含若干常用的数据类型属性访问器
 */
public class JavaBean implements IModel {

    /**
     * Short类型数字
     */
    private short shortNumber;

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
     * 以某种分隔符分割的数组
     */
    private String[] strings;

    /**
     * 长整型
     */
    private long longNumber;

    /**
     * byte
     */
    private byte byteNumber;

    /**
     * Char
     */
    private char charNumber;

    /**
     * float
     */
    private float floatNumber;

    /**
     * double
     */
    private double doubleNumber;

    /**
     * time
     */
    private LocalTime time;

    /**
     * Date
     */
    private LocalDate date;

    /**
     * UUID
     */
    private UUID uuid;

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
     * 获取以某种分隔符分割的数组
     *
     * @return 以某种分隔符分割的数组
     */
    public String[] getStrings() {
        return this.strings;
    }

    /**
     * 设置以某种分隔符分割的数组
     *
     * @param strings 以某种分隔符分割的数组
     */
    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    /**
     * 获取长整型
     */
    public long getLongNumber() {
        return this.longNumber;
    }

    /**
     * 设置长整型
     */
    public void setLongNumber(long longNumber) {
        this.longNumber = longNumber;
    }

    /**
     * 获取byte
     */
    public byte getByteNumber() {
        return this.byteNumber;
    }

    /**
     * 设置byte
     */
    public void setByteNumber(byte byteNumber) {
        this.byteNumber = byteNumber;
    }

    /**
     * 获取char
     */
    public char getCharNumber() {
        return this.charNumber;
    }

    /**
     * 设置char
     */
    public void setCharNumber(char charNumber) {
        this.charNumber = charNumber;
    }

    /**
     * 获取float
     */
    public float getFloatNumber() {
        return this.floatNumber;
    }

    /**
     * 设置float
     */
    public void setFloatNumber(float floatNumber) {
        this.floatNumber = floatNumber;
    }

    /**
     * 获取double
     */
    public double getDoubleNumber() {
        return this.doubleNumber;
    }

    /**
     * 设置double
     */
    public void setDoubleNumber(double doubleNumber) {
        this.doubleNumber = doubleNumber;
    }

    /**
     * 获取LocalTime
     */
    public LocalTime getTime() {
        return this.time;
    }

    /**
     * 设置LocalTime
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    /**
     * 获取LocalDate
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * 设置LocalDate
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * 获取UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * 设置UUID
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取Short类型数字
     */
    public short getShortNumber() {
        return this.shortNumber;
    }

    /**
     * 设置Short类型数字
     */
    public void setShortNumber(short shortNumber) {
        this.shortNumber = shortNumber;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "JavaBean{" +
                "intNumber=" + this.intNumber +
                ", decimalNumber=" + this.decimalNumber +
                ", dateTime=" + this.dateTime +
                ", string='" + this.string + '\'' +
                ", bool=" + this.bool +
                ", strings=" + Arrays.toString(this.strings) +
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
