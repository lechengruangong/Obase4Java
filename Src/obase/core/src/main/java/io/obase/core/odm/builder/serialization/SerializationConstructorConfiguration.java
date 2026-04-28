/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体类型构造器配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-4-1 10:49:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.odm.DelegateValueGetter;
import io.obase.core.odm.FieldValueGetter;
import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.PrimitiveType;
import io.obase.core.odm.serialization.SerializationConstructor;
import io.obase.core.odm.serialization.SerializationConstructorParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

/**
 * 序列化实体类型构造器配置
 *
 * @param <TStructural> 实体类型
 */
public class SerializationConstructorConfiguration<TStructural> {

    /**
     * 构造函数
     */
    private final Constructor<TStructural> constructorInfo;

    /**
     * 获取构造函数的形式参数
     */
    private final HashMap<String, SerializationConstructorParameterConfiguration> parameters;

    /**
     * 构造器的真实参数个数
     */
    private final int realParameterCount;

    /**
     * 当前配置的参数索引
     */
    private int currentParameterIndex;

    /**
     * 初始化序列化实体类型构造器配置
     *
     * @param constructorInfo 构造函数
     */
    public SerializationConstructorConfiguration(Constructor<TStructural> constructorInfo) {
        this.constructorInfo = constructorInfo;
        this.realParameterCount = constructorInfo.getParameterCount();
        this.parameters = new HashMap<>();
    }

    /**
     * 配置构造函数的参数
     *
     * @param field       取值字段
     * @param valueType   取得的值类型 如果设置needStorage为true 则在序列化时会检查取值器取得的值是否是此类型的
     * @param needStorage 是否需要存储 如果是true 则取值器会在序列化时被调用 取得的值进行存储 此时传入的取值器的参数为当前要序列化的对象 如果是false 则取值器会在反序列化被调用 取得的值用于构造函数 此时传入的取值器的参数为null
     * @return 自身
     */
    public SerializationConstructorConfiguration<TStructural> hasParameter(Field field, Class<?> valueType, boolean needStorage) {
        //构造一个字段取值器
        FieldValueGetter filedGetter = new FieldValueGetter(field);
        return this.hasParameter(filedGetter, valueType, needStorage);
    }

    /**
     * 配置构造函数的参数
     *
     * @param getValue    取值委托
     * @param valueType   取得的值类型 如果设置needStorage为true 则在序列化时会检查取值器取得的值是否是此类型的
     * @param needStorage 是否需要存储 如果是true 则取值器会在序列化时被调用 取得的值进行存储 此时传入的取值器的参数为当前要序列化的对象 如果是false 则取值器会在反序列化被调用 取得的值用于构造函数 此时传入的取值器的参数为null
     * @return 自身
     */
    public <TProperty> SerializationConstructorConfiguration<TStructural> hasParameter(FunctionWithOneArg<TStructural, TProperty> getValue, Class<?> valueType, boolean needStorage) {
        //创建一个委托取值器
        DelegateValueGetter<TStructural, TProperty> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasParameter(valueGetter, valueType, needStorage);
    }

    /**
     * 配置构造函数的参数
     *
     * @param valueGetter 取值器
     * @param valueType   取得的值类型 如果设置needStorage为true 则在序列化时会检查取值器取得的值是否是此类型的
     * @param needStorage 是否需要存储 如果是true 则取值器会在序列化时被调用 取得的值进行存储 此时传入的取值器的参数为当前要序列化的对象 如果是false 则取值器会在反序列化被调用 取得的值用于构造函数 此时传入的取值器的参数为null
     * @return 自身
     */
    public SerializationConstructorConfiguration<TStructural> hasParameter(IValueGetter valueGetter, Class<?> valueType, boolean needStorage) {
        //如果是需要存储的 检查值类型是否是Obase基础类型
        if (!PrimitiveType.isObasePrimitive(valueType) && needStorage)
            throw new IllegalArgumentException("需要存储的构造函数参数值类型必须是Obase基础类型。");
        String name = "#" + this.currentParameterIndex;
        //如果参数个数超过了构造函数的真实参数个数，抛出异常
        if (this.currentParameterIndex >= this.realParameterCount)
            throw new IllegalArgumentException("构造函数的参数个数超过了构造函数的真实参数个数。");
        //如果配置的参数类型与构造函数的参数类型不匹配，抛出异常
        if (!Objects.equals(this.constructorInfo.getParameters()[this.currentParameterIndex].getType(), valueType))
            throw new IllegalArgumentException("构造函数的第" + this.currentParameterIndex + "个参数的类型与配置的值类型不匹配。");
        //添加参数配置
        this.parameters.put(name,
                new SerializationConstructorParameterConfiguration(name, needStorage, valueGetter, valueType));
        this.currentParameterIndex++;
        return this;
    }

    /**
     * 创建序列化实体类型构造器
     *
     * @return 序列化实体类型构造器
     */
    public SerializationConstructor create() {
        //创建一个序列化实体类型构造器
        SerializationConstructor constructor = new SerializationConstructor(this.constructorInfo);
        for (String parameter : this.parameters.keySet()) {
            constructor.getParameters().put(parameter, (SerializationConstructorParameter) this.parameters.get(parameter).create());
        }
        return constructor;
    }
}
