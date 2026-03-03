/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:41:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示属性
 */
public class Attribute extends TypeElement implements IOrderBy {

    /**
     * 数据类型
     */
    private final Class<?> dataType;

    /**
     * 修改触发器集合
     */
    private List<IBehaviorTrigger> changeTriggers = new ArrayList<>();

    /**
     * 属性的合并处理器，负责在对象执行版本合并期间对属性进行处理。
     */
    private IAttributeCombinationHandler combinationHandler;

    /**
     * 指示属性的值是否由数据库生成
     */
    private boolean dbGenerateValue;

    /**
     * 指示字段值是否可空。
     */
    private boolean nullable = true;

    /**
     * 值的精度，以小数位数表示，0表示不限制。
     */
    private byte precision;

    /**
     * 映射字段
     */
    private String targetField;

    /**
     * 属性值的长度，以位为单位，值为0表示不限制长度。
     * 即数据类型所占字节数 * 8 对于字符串类型 默认为0 不限制具体的长度
     */
    private int valueLength;

    /**
     * 是否为复杂属性
     */
    private boolean isComplex;

    /**
     * 是否是由外键保证机制定义的
     */
    private boolean isForeignKeyDefineMissing = false;

    /**
     * 创建Attribute实例
     *
     * @param dataType 数据类型
     * @param name     属性名称
     */
    public Attribute(Class<?> dataType, String name) {
        super(name, EElementType.Attribute);

        this.dataType = dataType;
        if (!(this instanceof ComplexAttribute) && !this.dataType.equals(String.class))
            this.valueLength = Utils.getValueLength(dataType);
        else
            this.valueLength = 0;
    }


    /**
     * 获取一个值，该值指示是否为复杂属性
     *
     * @return 是否为复杂属性
     */
    public boolean getIsComplex() {
        return this.isComplex;
    }

    /**
     * 设置是否为复杂属性
     *
     * @param isComplex 是否为复杂属性
     */
    public void setIsComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    /**
     * 获取修改触发器集合
     *
     * @return 修改触发器集合
     */
    public List<IBehaviorTrigger> getChangeTriggers() {
        return this.changeTriggers;
    }

    /**
     * 设置修改触发器集合
     *
     * @param changeTriggers 修改触发器集合
     */
    public void setChangeTriggers(List<IBehaviorTrigger> changeTriggers) {
        this.changeTriggers = changeTriggers;
    }

    /**
     * 获取或设置属性的数据类型。（给字段设的值就是这个类型，考虑能否和数据库类型兼容）
     *
     * @return 数据类型
     */
    public Class<?> getDataType() {
        return this.dataType;
    }

    /**
     * 指示属性的值是否由数据库生成
     *
     * @return 指示属性的值是否由数据库生成
     */
    public boolean getDbGenerateValue() {
        return this.dbGenerateValue;
    }

    /**
     * 设置属性的值是否由数据库生成
     *
     * @param dbGenerateValue 指示属性的值是否由数据库生成
     */
    public void setDbGenerateValue(boolean dbGenerateValue) {
        this.dbGenerateValue = dbGenerateValue;
    }

    /**
     * 获取属性的合并处理器，负责在对象执行版本合并期间对属性进行处理
     *
     * @return 属性的合并处理器
     */
    public IAttributeCombinationHandler getCombinationHandler() {
        return this.combinationHandler;
    }

    /**
     * 设置属性的合并处理器，负责在对象执行版本合并期间对属性进行处理
     *
     * @param combinationHandler 属性的合并处理器
     */
    public void setCombinationHandler(IAttributeCombinationHandler combinationHandler) {
        this.combinationHandler = combinationHandler;
    }

    /**
     * 获取元素值的类型
     *
     * @return 获取元素值的类型
     */
    @Override
    public TypeBase getValueType() {
        return this.getHostType().getModel().getType(this.dataType);
    }

    /**
     * 是否是由外键保证机制定义的
     */
    public boolean getIsForeignKeyDefineMissing() {
        return this.isForeignKeyDefineMissing;
    }

    /**
     * 是否是由外键保证机制定义的
     */
    public void setIsForeignKeyDefineMissing(boolean foreignKeyDefineMissing) {
        this.isForeignKeyDefineMissing = foreignKeyDefineMissing;
    }

    /**
     * 属性值的长度，以位为单位，值为0表示不限制长度。
     *
     * @return 属性值的长度
     */
    public int getValueLength() {
        return this.valueLength;
    }

    /**
     * 设置属性值的长度
     *
     * @param valueLength 值的长度
     */
    public void setValueLength(int valueLength) {
        this.valueLength = valueLength;
    }

    /**
     * 获取精度 以小数位数表示，0表示不限制
     *
     * @return 以小数位数表示，0表示不限制
     */
    public byte getPrecision() {
        return this.precision;
    }

    /**
     * 设置精度
     *
     * @param precision 以小数位数表示，0表示不限制
     */
    public void setPrecision(byte precision) {
        this.precision = precision;
    }

    /**
     * 获取是否可空
     *
     * @return 是否可空
     */
    public boolean getNullable() {
        return this.nullable;
    }

    /**
     * 设置是否可空
     *
     * @param nullable 是否可空
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * 获取映射字段
     *
     * @return 映射字段
     */
    @Override
    public String getTargetField() {
        return this.targetField;
    }

    /**
     * 设置映射字段
     *
     * @param targetField 映射字段
     */
    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Attribute:{{Name-\"" + this.getName() + "\",DataType-\"" + this.getDataType().getName() + "\"}}";
    }
}
