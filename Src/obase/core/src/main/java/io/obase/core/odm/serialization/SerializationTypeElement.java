/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的需要设值类型元素.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:34:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import io.obase.core.odm.IValueSetter;

import java.util.Arrays;

/**
 * 序列化实体的需要设值类型元素
 */
public abstract class SerializationTypeElement extends SerializationElement {

    /**
     * 属性的设值器
     */
    private IValueSetter valueSetter;

    /**
     * 初始化序列化实体的类型元素
     *
     * @param valueType 类型元素的值类型
     */
    protected SerializationTypeElement(Class<?> valueType) {
        super(valueType);
    }

    /**
     * 获取属性的设值器
     *
     * @return 属性的设值器
     */
    public IValueSetter getValueSetter() {
        return this.valueSetter;
    }

    /**
     * 设置属性的设值器
     *
     * @param valueSetter 属性的设值器
     */
    public void setValueSetter(IValueSetter valueSetter) {
        this.valueSetter = valueSetter;
    }

    /**
     * 为指定对象的当前元素设置值，适用于具有多重性的元素
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    public void setValue(Object targetObj, Iterable<Object> value) {
        switch (this.valueSetter.getMode()) {
            case Assignment:
                this.valueSetter.setValue(targetObj, this.valueSetter.getMode());
                break;
            case Appending:
                if (value == null) return;
                for (Object valueItem : value) {
                    this.valueSetter.setValue(targetObj, valueItem);
                }
                break;
        }
    }

    /**
     * 为指定对象的当前元素设置值，适用于不具多重性的元素。
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    public void setValue(Object targetObj, Object value) {

        //前置过滤，如果value实现了IEnumerable或IEnumerable<>，调用另一重载。
        if (!value.getClass().equals(String.class) && Arrays.asList(value.getClass().getInterfaces()).contains(Iterable.class)) {
            Iterable<Object> iEnumerableValue = (Iterable<Object>) value;
            this.setValue(targetObj, iEnumerableValue);
        } else {
            this.valueSetter.setValue(targetObj, value);
        }
    }
}
