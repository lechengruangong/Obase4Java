/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型元素配置,提供类型元素配置项提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 10:51:25
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ActionWithTwoArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 为属性配置项、关联引用配置项、关联端配置项提供基础实现
 *
 * @param <TConfiguration> 配置项的具体类型
 */
public abstract class TypeElementConfigurationGeneric<TStructural,
        TConfiguration extends TypeElementConfigurationGeneric<TStructural, TConfiguration>> extends TypeElementConfiguration
        implements ITypeElementConfigurator {

    /**
     * 所属的元素类型
     * 保存类型参数的具体类型
     */
    protected final Class<TStructural> structuralType;

    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param isMultiple        指示元素是否具有多重性，即其值是否为集合
     * @param structuralType    所属的元素类型 保存类型参数的具体类型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    protected TypeElementConfigurationGeneric(String name, Boolean isMultiple, StructuralTypeConfiguration<TStructural> typeConfiguration, Class<TStructural> structuralType) {
        this.structuralType = structuralType;
        this.name = name;
        this.isMultiple = isMultiple;
        this.typeConfiguration = typeConfiguration;
    }

    /**
     * 类型配置项
     *
     * @return 类型配置项
     */
    protected StructuralTypeConfiguration<?> getTypeConfiguration() {
        return this.typeConfiguration;
    }

    /**
     * 为元素配置项设置一个扩展配置器
     *
     * @param configType 扩展配置器的类型，须继承自ElementExtensionConfiguration
     * @return 扩展配置器
     */
    @Override
    public <TExtensionConfiguration extends ElementExtensionConfiguration> ElementExtensionConfiguration hasExtensionI(Class<TExtensionConfiguration> configType) {
        return this.hasExtension(configType);
    }

    /**
     * 为类型元素设置取值器(覆盖现有配置)
     *
     * @param valueGetter 取值器
     */
    @Override
    public void hasValueGetterI(IValueGetter valueGetter) {
        this.hasValueGetterI(valueGetter, true);
    }

    /**
     * 为类型元素设置取值器
     *
     * @param valueGetter 取值器
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(IValueGetter valueGetter, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(valueGetter);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(valueGetter);
        }
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器(覆盖现有配置)
     *
     * @param method 获取元素值的方法
     */
    @Override
    public void hasValueGetterI(Method method) {
        this.hasValueGetterI(method, true);
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器
     *
     * @param method   获取元素值的方法
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Method method, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(method);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(method);
        }
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器(覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    @Override
    public void hasValueGetterI(Property property) {
        this.hasValueGetterI(property, true);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Property property, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(property);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(property);
        }
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    @Override
    public void hasValueGetterI(Field field) {
        this.hasValueGetterI(field, true);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Field field, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(field);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(field);
        }
    }

    /**
     * 使用指定的类成员为类型元素创建取值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    @Override
    public void hasValueGetterI(String memberName) {
        this.hasValueGetterI(memberName, true);
    }

    /**
     * 使用指定的类成员为类型元素创建取值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(String memberName, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(memberName);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(memberName);
        }
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器(覆盖现有配置)
     */
    @Override
    public void hasValueGetterI() {
        this.hasValueGetterI(true);
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(this.name);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(this.name);
        }
    }

    /**
     * 为类型元素设置设值器(覆盖现有配置)
     *
     * @param valueSetter 设值器
     */
    @Override
    public void hasValueSetterI(IValueSetter valueSetter) {
        this.hasValueSetterI(valueSetter, true);
    }

    /**
     * 为类型元素设置设值器
     *
     * @param valueSetter 设值器
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(IValueSetter valueSetter, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(valueSetter);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(valueSetter);
        }
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器(覆盖现有配置)
     *
     * @param method 为类型元素设值的方法
     * @param mode   设值模式
     */
    @Override
    public void hasValueSetterI(Method method, EValueSettingMode mode) {
        this.hasValueSetterI(method, mode, true);
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器
     *
     * @param method   为类型元素设值的方法
     * @param mode     设值模式
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Method method, EValueSettingMode mode, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(method, mode);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(method, mode);
        }
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    @Override
    public void hasValueSetterI(Property property) {
        this.hasValueSetterI(property, true);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Property property, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(property);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(property);
        }
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    @Override
    public void hasValueSetterI(Field field) {
        this.hasValueSetterI(field, true);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Field field, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(field);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(field);
        }
    }

    /**
     * 使用指定的类成员为类型元素创建设值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    @Override
    public void hasValueSetterI(String memberName) {
        this.hasValueSetterI(memberName, true);
    }

    /**
     * 使用指定的类成员为类型元素创建设值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(String memberName, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(memberName);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(memberName);
        }
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器(覆盖现有配置)
     */
    @Override
    public void hasValueSetterI() {
        this.hasValueSetterI(true);
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(this.name);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(this.name);
        }
    }

    /**
     * 进入当前元素所属类型的配置项
     *
     * @return 元素所属类型的配置项
     */
    @Override
    public IStructuralTypeConfigurator upwardI() {
        return (IStructuralTypeConfigurator) this.typeConfiguration;
    }

    /**
     * 设置取值器
     *
     * @param valueGetter 取值器
     * @return 自身
     */
    public TConfiguration hasValueGetter(IValueGetter valueGetter) {
        this.setValueGetter(valueGetter);
        return (TConfiguration) this;
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器。
     * 如果该方法的返回值类型与元素的IsMultiple属性不匹配，则引发异常
     *
     * @param method 获取元素值的方法
     * @return 自身
     */
    public TConfiguration hasValueGetter(Method method) {
        IValueGetter getter = Utils.makeDelegateValueGetter(method);
        return this.hasValueGetter(getter);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器
     *
     * @param property 获取元素值的属性访问器
     * @return 自身
     */
    public TConfiguration hasValueGetter(Property property) {
        return this.hasValueGetter(property.getGetterMethod());
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field 表示类型元素的字段
     * @return 自身
     */
    public TConfiguration hasValueGetter(Field field) {
        //是Iterable并且不是string 则认为是多重的
        boolean filedIsMulti = Iterable.class.isAssignableFrom((Class<?>) field.getGenericType()) && !field.getGenericType().equals(String.class);

        if (filedIsMulti != this.isMultiple)
            throw new IllegalArgumentException(String.format("%s与目标的多重性不一致.", field.getName()));

        FieldValueGetter filedGetter = new FieldValueGetter(field);

        return this.hasValueGetter(filedGetter);
    }

    /**
     * 使用指定的类成员为类型元素创建取值器。
     *
     * @param memberName 成员的名称
     * @return 自身
     */
    public TConfiguration hasValueGetter(String memberName) {
        try {
            Field field = this.structuralType.getField(memberName);
            return this.hasValueGetter(field);
        } catch (NoSuchFieldException e) {
            try {
                Method method = this.structuralType.getMethod(memberName);
                return this.hasValueGetter(method);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException(String.format("%s无法获取到成员.", memberName));
            }
        }
    }

    /**
     * 使用一个能够获取类型元素的值且返回值为单个对象的委托为不具备多重性的类型元素创建取值器
     *
     * @param getValue    获取元素值的委托
     * @param <TProperty> 表示元素的类型。对于属性，它表示属性值类型；对于关联引用，它表示关联类型；对于关联端，它表示关联端的类型。
     * @return 自身
     */
    public <TProperty> TConfiguration hasValueGetter(FunctionWithOneArg<TStructural, TProperty> getValue) {
        if (this.isMultiple)
            throw new IllegalArgumentException(String.format("%s类型的设值器为多重性,不能设置单一设值器.", this.getName()));
        DelegateValueGetter<TStructural, TProperty> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasValueGetter(valueGetter);
    }

    /**
     * 使用一个能够获取类型元素的值且返回值为对象序列的委托为具备多重性的类型元素创建取值器。
     *
     * @param getValue    获取元素值的委托
     * @param <TProperty> 表示元素的类型。对于属性，它表示属性值类型；对于关联引用，它表示关联类型；对于关联端，它表示关联端的类型。
     * @return 自身
     */
    public <TProperty> TConfiguration hasValueGetterMultiple(FunctionWithOneArg<TStructural, Iterable<TProperty>> getValue) {
        if (!this.isMultiple)
            throw new IllegalArgumentException(String.format("%s类型的设值器为单一性,不能设置多重设值器.", this.getName()));
        DelegateValueGetter<TStructural, Iterable<TProperty>> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasValueGetter(valueGetter);
    }


    /**
     * 设置设值器
     *
     * @param valueSetter 对象设值器接口
     * @return 自身
     */
    public TConfiguration hasValueSetter(IValueSetter valueSetter) {
        this.setValueSetter(valueSetter);
        return (TConfiguration) this;
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器
     *
     * @param method 为类型元素设值的方法
     * @param mode   设值模式
     * @return 自身
     */
    public TConfiguration hasValueSetter(Method method, EValueSettingMode mode) {
        if (method.getParameterCount() != 1)
            throw new IllegalArgumentException("设值器方法只能有一个参数");

        this.setValueSetter(ValueSetter.create(method, mode));
        return (TConfiguration) this;
    }

    /**
     * 使用一个能够为类型元素设值的Property为类型元素创建设值器
     *
     * @param property 为类型元素设值的属性访问器
     * @return 自身
     */
    public TConfiguration hasValueSetter(Property property) {
        Method setMethod = property.getSetterMethod();
        return this.hasValueSetter(setMethod, EValueSettingMode.Assignment);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field 字段
     * @return 自身
     */
    public TConfiguration hasValueSetter(Field field) {
        return this.hasValueSetter(ValueSetter.create(field));
    }

    /**
     * 使用指定的类成员为类型元素创建设值器
     *
     * @param memberName 成员的名称
     * @return 自身
     */
    public TConfiguration hasValueSetter(String memberName) {

        try {
            Field field = this.structuralType.getField(memberName);
            return this.hasValueSetter(field);
        } catch (NoSuchFieldException e) {
            try {
                Method method = this.structuralType.getMethod(memberName);
                //此处无法确定eValueSettingMode
                throw new IllegalArgumentException(String.format("%s暂不支持用Method构造设值器", method.getName()));
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException(String.format("%s无法获取到成员.", memberName));
            }
        }
    }

    /**
     * 为lambda表达式指示的元素创建设值器，该lambda表达式的主体须为MemberExpression，其访问的成员代表要设值的元素
     *
     * @param propertyExp  表示属性访问器的Lambda表达式
     * @param valueCreator 元素创建委托
     * @param <TProperty>  作为lambda表达式主体的MemberExpression的类型
     * @param <TElement>   值序列项的类型
     * @return 自身
     */
    public <TProperty extends Iterable<TElement>, TElement> TConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TProperty> propertyExp, Function<Iterable<TElement>, TProperty> valueCreator) {

        return this.hasValueSetter(new DelegateEnumerableValueSetterWithThreeArgs<>(propertyExp, valueCreator));
    }

    /**
     * 为lambda表达式指示的元素创建设值器，该lambda表达式的主体须为MemberExpression，其访问的成员代表要设值的元素
     *
     * @param propertyExp 表达式
     * @param <TProperty> 作为lambda表达式主体的MemberExpression的类型，亦即元素值的类型
     * @return 自身
     */
    public <TProperty> TConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TProperty> propertyExp) {
        return this.hasValueSetter(propertyExp, EValueSettingMode.Assignment);
    }

    /**
     * 使用能够修改元素值的委托为类型元素创建设值器
     *
     * @param setValue 表示属性访问器的Lambda表达式
     * @param mode     设值模式
     * @param <TValue> Assignment模式下为元素值的类型，Appending模式下为元素值序列项的类型
     * @return 自身
     */
    public <TValue> TConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TValue> setValue, EValueSettingMode mode) {
        return this.hasValueSetter(ValueSetter.create(setValue, mode));
    }
}
