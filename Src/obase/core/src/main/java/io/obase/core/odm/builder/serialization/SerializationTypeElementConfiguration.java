/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的需要设值类型元素配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:31:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.common.ActionWithTwoArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.core.odm.*;

import java.lang.reflect.Field;

/**
 * 序列化实体的需要设值类型元素配置
 *
 * @param <TStructural> 实体类型
 */
public abstract class SerializationTypeElementConfiguration<TStructural> extends SerializationElementConfiguration {

    /**
     * 属性的设值器
     */
    protected IValueSetter valueSetter;

    /**
     * 初始化序列化实体的类型元素配置
     *
     * @param valueType 类型元素的值类型
     */
    protected SerializationTypeElementConfiguration(Class<?> valueType) {
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
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field 字段
     * @return 自身
     */
    public SerializationTypeElementConfiguration<TStructural> hasValueGetter(Field field) {
        //构造一个字段取值器
        FieldValueGetter filedSetter = new FieldValueGetter(field);
        return this.hasValueGetter(filedSetter);
    }

    /**
     * 用委托设置取值器
     *
     * @param getValue    要取的值类型
     * @param <TProperty> 取值委托
     * @return 自身
     */
    public <TProperty> SerializationTypeElementConfiguration<TStructural> hasValueGetter(FunctionWithOneArg<TStructural, TProperty> getValue) {
        DelegateValueGetter<TStructural, TProperty> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasValueGetter(valueGetter);
    }

    /**
     * 设置取值器
     *
     * @param getter 取值器
     * @return 自身
     */
    public SerializationTypeElementConfiguration<TStructural> hasValueGetter(IValueGetter getter) {
        this.valueGetter = getter;
        return this;
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field 表示类型元素的字段
     * @return 自身
     */
    public SerializationTypeElementConfiguration<TStructural> hasValueSetter(Field field) {
        return this.hasValueSetter(ValueSetter.create(field));
    }

    /**
     * 使用能够修改元素值的委托为类型元素创建设值器
     *
     * @param setValue 表示属性访问器的Lambda表达式
     * @param mode     设值模式
     * @param <TValue> Assignment模式下为元素值的类型，Appending模式下为元素值序列项的类型
     * @return 自身
     */
    public <TValue> SerializationTypeElementConfiguration<TStructural> hasValueSetter(ActionWithTwoArg<TStructural, TValue> setValue, EValueSettingMode mode) {
        return this.hasValueSetter(ValueSetter.create(setValue, mode));
    }

    /**
     * 设置设值器
     *
     * @param valueSetter 设值器
     * @return 自身
     */
    public SerializationTypeElementConfiguration<TStructural> hasValueSetter(IValueSetter valueSetter) {
        this.valueSetter = valueSetter;
        return this;
    }
}
