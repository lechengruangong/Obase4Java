/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:32:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.serialization.SerializationElement;
import io.obase.core.odm.serialization.SerializationEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 序列化实体配置
 *
 * @param <T> 实体类型
 */
public class SerializationEntityConfigurationGeneric<T> extends SerializationEntityConfiguration {

    /**
     * 忽略的属性集合
     */
    private final HashSet<String> ignoredProperties = new HashSet<>();

    /**
     * 实体类型
     */
    private final Class<T> typeClass;

    /**
     * 建模器
     */
    private final ModelBuilder builder;

    /**
     * 序列化元素的字典
     * 对于属性 key为属性名称 value为属性的配置项
     * 对于引用 key为引用名称 value为引用的配置项
     */
    private final HashMap<String, SerializationTypeElementConfiguration<T>> serializeTypeElementConfigurations = new HashMap<>();

    /**
     * 使用的构造器
     */
    private SerializationConstructorConfiguration<T> constructor;

    /**
     * 初始化序列化实体配置
     *
     * @param typeClass 实体类型
     * @param builder   建模器
     */
    public SerializationEntityConfigurationGeneric(Class<T> typeClass, ModelBuilder builder) {
        this.typeClass = typeClass;
        this.builder = builder;
    }

    /**
     * 手动配置属性方法
     * 根据名称和值类型创建一个属性配置项并添加到序列化元素的字典中
     * 不会设置取值器和设值器 需要用户手动设置
     *
     * @param name      名称
     * @param valueType 值类型
     * @return 属性配置项
     */
    public SerializationAttributeConfiguration<T> attribute(String name, Class<?> valueType) {
        if (!PrimitiveType.isObasePrimitive(valueType))
            throw new IllegalArgumentException("只有Obase的基元类型可以作为序列化实体属性.");
        //如果有 从字典中取出 否则创建一个新的属性配置项并添加到字典中
        SerializationAttributeConfiguration<T> result;
        if (this.serializeTypeElementConfigurations.containsKey(name)) {
            result = (SerializationAttributeConfiguration<T>) this.serializeTypeElementConfigurations.get(name);
        } else {
            result = new SerializationAttributeConfiguration<>(name, valueType);
            this.serializeTypeElementConfigurations.put(name, result);
        }

        return result;
    }

    /**
     * 自动配置属性方法
     * 根据名称和自动侦测的类型创建一个属性配置项并添加到序列化元素的字典中
     *
     * @param name 名称
     * @return 属性配置项
     */
    public SerializationAttributeConfiguration<T> attribute(String name) {
        Property property = Utils.getProperty(this.typeClass, name);
        //进行配置
        SerializationAttributeConfiguration<T> attribute = this.attribute(name, property.getPropertyType());
        //取值器和设值器
        attribute.hasValueGetter(this.makeValueGetter(property));
        attribute.hasValueSetter(this.makeValueSetter(property));
        return attribute;
    }

    /**
     * 自动配置属性方法
     * 根据表达式代表的名称和自动侦测的类型创建一个属性配置项并添加到序列化元素的字典中
     *
     * @param expression 表达式
     * @param <TResult>  目标类型
     * @return 属性配置项
     */
    public <TResult> SerializationAttributeConfiguration<T> attribute(SerializedFunction<T, TResult> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            //获取表达式代表的属性名称
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.attribute(memberName);
        }

        throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
    }

    /**
     * 启动一个序列化构造器配置
     *
     * @param constructor 构造函数
     * @return 构造函数配置
     */
    public SerializationConstructorConfiguration<T> hasConstructor(Constructor<T> constructor) {
        if (constructor == null)
            throw new IllegalArgumentException("不能使用空的构造函数配置序列化构造器.");
        this.constructor = new SerializationConstructorConfiguration<>(constructor);
        return this.constructor;
    }

    /**
     * 手动配置引用方法
     * 根据名称和是否为多重的创建一个引用配置项并添加到序列化元素的字典中
     * 不会设置取值器和设值器 需要用户手动设置
     *
     * @param name       名称
     * @param isMultiple 引用是单值的还是多重的
     * @return 引用配置项
     */
    public SerializationReferenceConfiguration<T> reference(String name, boolean isMultiple) {
        //如果有 从字典中取出 否则创建一个新的属性配置项并添加到字典中
        SerializationReferenceConfiguration<T> result;
        if (this.serializeTypeElementConfigurations.containsKey(name)) {
            result = (SerializationReferenceConfiguration<T>) this.serializeTypeElementConfigurations.get(name);
        } else {
            result = new SerializationReferenceConfiguration<>(isMultiple, name);
            this.serializeTypeElementConfigurations.put(name, result);
        }

        return result;
    }

    /**
     * 自动配置引用方法
     * 根据名称和自动侦测的多重性创建一个引用配置项并添加到序列化元素的字典中
     * 不会设置取值器和设值器 需要用户手动设置
     *
     * @param name 名称
     * @return 引用配置项
     */
    public SerializationReferenceConfiguration<T> reference(String name) {
        Property property = Utils.getProperty(this.typeClass, name);
        boolean isMultiple = Utils.getIsMultiple(property, new ObjectReferencePack<>());
        //进行配置
        SerializationReferenceConfiguration<T> reference = this.reference(name, isMultiple);
        //取值器和设值器
        reference.hasValueGetter(this.makeValueGetter(property));
        reference.hasValueSetter(this.makeValueSetter(property));
        return reference;
    }

    /**
     * 自动配置引用方法
     * 根据名称和自动侦测的多重性创建一个引用配置项并添加到序列化元素的字典中
     * 不会设置取值器和设值器 需要用户手动设置
     *
     * @param expression 表达式
     * @return 引用配置项
     */
    public <TResult> SerializationReferenceConfiguration<T> reference(SerializedFunction<T, TResult> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            //获取表达式代表的属性名称
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.reference(memberName);
        }

        throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
    }

    /**
     * 忽略属性
     *
     * @param name 属性名称
     * @return 自身
     */
    public SerializationEntityConfigurationGeneric<T> ignore(String name) {
        Utils.getProperty(this.typeClass, name);
        this.ignoredProperties.add(name);
        this.serializeTypeElementConfigurations.remove(name);

        return this;
    }

    /**
     * 忽略属性
     *
     * @param expression 属性表达式
     * @return 自身
     */
    public <TResult> SerializationEntityConfigurationGeneric<T> ignore(SerializedFunction<T, TResult> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            //获取表达式代表的属性名称
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.ignore(memberName);
        }

        throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     * 本方法由派生类实现
     *
     * @return 序列化实体类型
     */
    @Override
    protected SerializationEntity createReally() {
        //在这里创建和配置好所有的元素集合 包含属性和构造参数
        //属性部分 所有属性访问器中 属性类型为Obase基元类型的 设定为属性
        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(this.typeClass);
        List<Property> simpleProperties = properties.stream().filter(p -> PrimitiveType.isObasePrimitive(p.getPropertyType())).collect(Collectors.toList());

        //反射配置属性
        for (Property propertyInfo : simpleProperties) {
            //如果属性被用户配置为忽略 则跳过
            if (this.ignoredProperties.contains(propertyInfo.getName()))
                continue;
            //创建配置
            SerializationAttributeConfiguration<T> attributeConfig = this.attribute(propertyInfo.getName(), propertyInfo.getPropertyType());
            //配置取值器和设值器
            if (attributeConfig.getValueGetter() == null)
                attributeConfig.hasValueGetter(this.makeValueGetter(propertyInfo));
            if (attributeConfig.getValueSetter() == null)
                attributeConfig.hasValueSetter(this.makeValueSetter(propertyInfo));
        }

        //取出构造函数
        Constructor<?>[] constructors = this.typeClass.getDeclaredConstructors();
        if (constructors.length > 0) {
            //如果没有用户配置的构造器 则默认使用无参构造器
            Constructor<?> constructor = Arrays.stream(constructors).filter(p -> p.getParameterCount() == 0).findFirst().orElse(null);
            if (constructor != null && this.constructor == null)
                this.hasConstructor((Constructor<T>) constructor);
        }

        List<Property> complexProperties = properties.stream().filter(p -> !PrimitiveType.isObasePrimitive(p.getPropertyType())).collect(Collectors.toList());

        //处理引用
        for (Property complexProperty : complexProperties) {
            //如果属性被用户配置为忽略 则跳过
            if (this.ignoredProperties.contains(complexProperty.getName()))
                continue;
            //取出真实类型
            ObjectReferencePack<Class<?>> realType = new ObjectReferencePack<>();
            Utils.getIsMultiple(complexProperty, realType);
            //如果此类型已经被注册过了 则表示这个属性是引用类型 需要配置一个引用元素
            if (this.builder.existSerializationEntityConfiguration(realType.realValue)) {
                //创建配置
                SerializationReferenceConfiguration<T> referenceConfiguration = this.reference(complexProperty.getName());
                //配置取值器和设值器
                if (referenceConfiguration.getValueGetter() == null)
                    referenceConfiguration.hasValueGetter(this.makeValueGetter(complexProperty));
                if (referenceConfiguration.getValueSetter() == null)
                    referenceConfiguration.hasValueSetter(this.makeValueSetter(complexProperty));
            }
        }

        //构造一个序列化实体类型
        SerializationEntity serializationEntity = new SerializationEntity(this.typeClass);
        if (this.constructor != null)
            serializationEntity.setConstructor(this.constructor.create());

        //加入配置的元素
        for (SerializationTypeElementConfiguration<T> typeElement : this.serializeTypeElementConfigurations.values()) {
            SerializationElement element = typeElement.create();
            serializationEntity.getElements().add(element);
        }

        //返回
        return serializationEntity;
    }

    /**
     * 构造取值器
     *
     * @param property 属性
     * @return 取值器
     */
    private IValueGetter makeValueGetter(Property property) {
        if (property.getGetterMethod() == null)
            return null;
        return Utils.makeDelegateValueGetter(property.getGetterMethod());
    }

    /**
     * 构造设值器
     *
     * @param property 属性
     * @return 设值器
     */
    private IValueSetter makeValueSetter(Property property) {
        //设值方法是可读还是公开的
        if (property.getSetterMethod() != null) {
            //有公开的设值方法
            Class<?> parType = property.getPropertyType();

            EValueSettingMode mode = EValueSettingMode.Assignment;
            if (parType != String.class && Iterable.class.isAssignableFrom(parType))
                mode = EValueSettingMode.Appending;

            return ValueSetter.create(property.getSetterMethod(), mode);
        } else {
            try {
                //找set+属性名
                Method method = property.getGetterMethod().getDeclaringClass().getDeclaredMethod("set" + property.getName(), property.getPropertyType());
                return new MethodValueSetter(method);
            } catch (NoSuchMethodException ignored) {
                //没有 忽略掉
                return null;
            }
        }
    }
}
