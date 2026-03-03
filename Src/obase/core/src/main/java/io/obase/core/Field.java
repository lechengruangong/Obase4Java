/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示表中的一个字段.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:26:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.odm.PrimitiveType;
import org.apache.commons.lang3.StringUtils;

/**
 * 表示表中的一个字段
 */
public class Field {

    /**
     * 数据类型
     */
    private final PrimitiveType dataType;

    /**
     * 是否自增
     */
    private final boolean isSelfIncreasing;

    /**
     * 字段长度，以位为单位
     */
    private final int length;

    /**
     * 字段名
     */
    private final String name;

    /**
     * 指示字段值是否可空
     */
    private final boolean nullable;

    /**
     * 值的精度，以小数位数表示，0表示不限制
     */
    private final byte precision;

    /**
     * 初始化Field类的新实例
     *
     * @param name             字段名称
     * @param dataType         字段的数据类型
     * @param length           字段长度，以位为单位
     * @param isSelfIncreasing 是否自增
     * @param nullable         指示字段值是否可空
     * @param precision        值的精度，以小数位数表示，0表示不限制
     */
    public Field(String name, PrimitiveType dataType, int length, boolean isSelfIncreasing, boolean nullable, byte precision) {
        this.name = name;
        this.dataType = dataType;
        this.length = length;
        this.isSelfIncreasing = isSelfIncreasing;
        this.nullable = nullable;
        this.precision = precision;
    }

    /**
     * 获取数据类型
     *
     * @return 数据类型
     */
    public PrimitiveType getDataType() {
        return this.dataType;
    }

    /**
     * 获取字段长度，以位为单位
     *
     * @return 字段长度，以位为单位
     */
    public int getLength() {
        return this.length;
    }

    /**
     * 获取是否自增
     *
     * @return 是否自增
     */
    public boolean getSelfIncreasing() {
        return this.isSelfIncreasing;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getName() {
        return StringUtils.capitalize(this.name);
    }

    /**
     * 获取值的精度，以小数位数表示，0表示不限制。
     *
     * @return 值的精度，以小数位数表示，0表示不限制。
     */
    public byte getPrecision() {
        return this.precision;
    }

    /**
     * 获取指示字段值是否可空
     *
     * @return 字段值是否可空
     */
    public boolean getNullable() {
        return this.nullable;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Field{" +
                "dataType=" + this.dataType +
                ", isSelfIncreasing=" + this.isSelfIncreasing +
                ", length=" + this.length +
                ", name='" + this.name + '\'' +
                ", nullable=" + this.nullable +
                ", precision=" + this.precision +
                '}';
    }
}
