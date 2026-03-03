package io.obase.test.domain.simpleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * 可空的JavaBean
 */
public class NullableJavaBean {

    /**
     * Short类型数字
     */
    private Short shortNumber;

    /**
     * int类型数字
     */
    private Integer intNumber;

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
    private Boolean bool;

    /**
     * 以某种分隔符分割的数组
     */
    private String[] strings;

    /**
     * 长整型
     */
    private Long longNumber;

    /**
     * byte
     */
    private Byte byteNumber;

    /**
     * Char
     */
    private Character charNumber;

    /**
     * float
     */
    private Float floatNumber;

    /**
     * double
     */
    private Double doubleNumber;

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
     */
    public Integer getIntNumber() {
        return this.intNumber;
    }

    /**
     * 设置int类型数字
     */
    public void setIntNumber(Integer intNumber) {
        this.intNumber = intNumber;
    }

    /**
     * 获取decimal类型数字
     */
    public BigDecimal getDecimalNumber() {
        return this.decimalNumber;
    }

    /**
     * 设置decimal类型数字
     */
    public void setDecimalNumber(BigDecimal decimalNumber) {
        this.decimalNumber = decimalNumber;
    }

    /**
     * 获取时间类型
     */
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * 设置时间类型
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * 获取字符串类型
     */
    public String getString() {
        return this.string;
    }

    /**
     * 设置字符串类型
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * 获取布尔值类型
     */
    public Boolean getBool() {
        return this.bool;
    }

    /**
     * 设置布尔值类型
     */
    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    /**
     * 获取以某种分隔符分割的数组
     */
    public String[] getStrings() {
        return this.strings;
    }

    /**
     * 设置以某种分隔符分割的数组
     */
    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    /**
     * 获取长整型
     */
    public Long getLongNumber() {
        return this.longNumber;
    }

    /**
     * 设置长整型
     */
    public void setLongNumber(Long longNumber) {
        this.longNumber = longNumber;
    }

    /**
     * 获取byte
     */
    public Byte getByteNumber() {
        return this.byteNumber;
    }

    /**
     * 设置byte
     */
    public void setByteNumber(Byte byteNumber) {
        this.byteNumber = byteNumber;
    }

    /**
     * 获取Char
     */
    public Character getCharNumber() {
        return this.charNumber;
    }

    /**
     * 设置Char
     */
    public void setCharNumber(Character charNumber) {
        this.charNumber = charNumber;
    }

    /**
     * 获取float
     */
    public Float getFloatNumber() {
        return this.floatNumber;
    }

    /**
     * 设置float
     */
    public void setFloatNumber(Float floatNumber) {
        this.floatNumber = floatNumber;
    }

    /**
     * 获取double
     */
    public Double getDoubleNumber() {
        return this.doubleNumber;
    }

    /**
     * 设置double
     */
    public void setDoubleNumber(Double doubleNumber) {
        this.doubleNumber = doubleNumber;
    }

    /**
     * 获取time
     */
    public LocalTime getTime() {
        return this.time;
    }

    /**
     * 设置time
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    /**
     * 获取Date
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * 设置Date
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
    public Short getShortNumber() {
        return this.shortNumber;
    }

    /**
     * 设置Short类型数字
     */
    public void setShortNumber(Short shortNumber) {
        this.shortNumber = shortNumber;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "NullableJavaBean{" +
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
                ", uuid=" + this.uuid +
                '}';
    }
}
