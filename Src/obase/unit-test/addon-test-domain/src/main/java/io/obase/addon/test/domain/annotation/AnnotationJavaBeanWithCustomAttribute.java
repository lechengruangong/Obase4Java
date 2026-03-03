package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.ConstructorAttribute;
import io.obase.odm.annotation.EntityAttribute;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 标注建模测试用JAVABEAN 带有自定义取值器设值器
 */
@EntityAttribute(keyAttributes = {"IntNumber"}, isSelfIncrease = false)
public class AnnotationJavaBeanWithCustomAttribute {

    /**
     * int类型数字
     */
    private final int intNumber;

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
     * 以某种分隔符分割的数组
     */
    private String[] strings;

    /**
     * 构造插件测试的JAVABEAN
     *
     * @param intNumber     int类型数字
     * @param decimalNumber decimal类型数字
     * @param dateTime      时间类型
     * @param s             字符串类型
     * @param b             布尔值类型
     * @param strings       以某种分隔符分割的数组
     */
    public AnnotationJavaBeanWithCustomAttribute(int intNumber, BigDecimal decimalNumber, LocalDateTime dateTime, String s, boolean b, String[] strings) {
        this.intNumber = intNumber;
        this.decimalNumber = decimalNumber;
        this.dateTime = dateTime;
        this.string = s;
        this.bool = b;
        this.strings = strings;
    }

    /**
     * 构造插件测试的JAVABEAN
     *
     * @param intNumber     int类型数字
     * @param decimalNumber decimal类型数字
     * @param dateTime      时间类型
     * @param s             字符串类型
     * @param b             布尔值类型
     */
    @ConstructorAttribute(parameterNames = {"IntNumber", "DecimalNumber", "DateTime", "String", "Bool"})
    protected AnnotationJavaBeanWithCustomAttribute(int intNumber, BigDecimal decimalNumber, LocalDateTime dateTime, String s, boolean b) {
        this.intNumber = intNumber;
        this.decimalNumber = decimalNumber;
        this.dateTime = dateTime;
        this.string = s;
        this.bool = b;
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
     * 获取decimal类型数字
     *
     * @return decimal类型数字
     */
    public BigDecimal getDecimalNumber() {
        return this.decimalNumber;
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
     * 获取字符串类型
     *
     * @return 字符串类型
     */
    public String getString() {
        return this.string;
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
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AnnotationJavaBeanWithCustomAttribute{" +
                "intNumber=" + this.intNumber +
                ", decimalNumber=" + this.decimalNumber +
                ", dateTime=" + this.dateTime +
                ", string='" + this.string + '\'' +
                ", bool=" + this.bool +
                ", strings=" + Arrays.toString(this.strings) +
                '}';
    }
}
