/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结构化类型,提供结构化配置基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 16:33:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.*;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 为实体型配置项、关联型配置项和复杂类型配置项提供一个泛型类的基础实现，该泛型类的类型参数是上述三个类型对应的对象系统类型
 *
 * @param <TConfiguration> 配置
 */
public abstract class StructuralTypeConfigurationGeneric<TStructural, TConfiguration extends StructuralTypeConfigurationGeneric<TStructural, TConfiguration>>
        extends StructuralTypeConfiguration<TStructural> implements IStructuralTypeConfigurator {
    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    protected StructuralTypeConfigurationGeneric(Class<TStructural> clrType, ModelBuilder modelBuilder) {
        super(clrType, modelBuilder);
    }

    /**
     * 继承自谁
     *
     * @return 继承的类型
     */
    @Override
    public Class<?> getDerivedFromI() {
        return this.getDerivedFrom();
    }

    /**
     * 启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个
     *
     * @param name     属性名称，它将作为配置项的键
     * @param dataType 属性的数据类型
     * @return 属性配置项
     */
    @Override
    public IAttributeConfigurator attributeI(String name, Class<?> dataType) {
        return this.attribute(name, dataType);
    }

    /**
     * 指定当前类型的基类型
     *
     * @param type 基类型
     */
    @Override
    public void deriveFromI(Class<?> type) {
        this.deriveFrom(type);
    }

    /**
     * 根据名称获取元素配置器
     *
     * @param name 元素名称
     * @return 元素配置器
     */
    @Override
    public ITypeElementConfigurator getElementI(String name) {
        return this.getElement(name);
    }

    /**
     * 使用一个构造函数为类型创建实例构造器(覆盖现有配置)
     *
     * @param constructorInfo 构造函数
     * @return 实例构造器
     */
    @Override
    public IParameterConfigurator hasConstructorI(Constructor<?> constructorInfo) {
        return this.hasConstructorI(constructorInfo, true);
    }

    /**
     * 设置类型的实例构造器(覆盖现有配置)
     *
     * @param constructor 实例构造器
     */
    @Override
    public void hasConstructorI(IInstanceConstructor constructor) {
        this.hasConstructorI(constructor, true);
    }

    /**
     * 设置类型的实例构造器
     *
     * @param constructor 实例构造器
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasConstructorI(IInstanceConstructor constructor, boolean override) {
        //覆盖既有配置 直接调用配置方法
        if (override)
            this.hasConstructor(constructor);
        //否则 如果当前配置项没有构造器则使用传入的构造器
        if (this.constructor == null)
            this.hasConstructor(constructor);
    }

    /**
     * 为类型配置项设置一个扩展配置器
     *
     * @param configType 扩展配置器的类型，须继承自TypeExtensionConfiguration
     * @return 扩展配置器
     */
    @Override
    public TypeExtensionConfiguration hasExtensionI(Class<? extends TypeExtensionConfiguration> configType) {
        try {
            TypeExtensionConfiguration extensionConfiguration = configType.newInstance();
            this.extensionConfigs.add(extensionConfiguration);
            return extensionConfiguration;
        } catch (Exception e) {
            throw new IllegalArgumentException("添加扩展配置器失败," + configType.getName() + "没有适合的无参构造函数", e);
        }
    }

    /**
     * 设置类型的命名空间(覆盖现有配置)
     *
     * @param nameSpace 命名空间
     */
    @Override
    public void hasNamespaceI(String nameSpace) {
        this.hasNamespaceI(nameSpace, true);
    }

    /**
     * 设置类型的命名空间
     *
     * @param nameSpace 命名空间
     * @param override  是否覆盖既有配置
     */
    @Override
    public void hasNamespaceI(String nameSpace, boolean override) {
        //如果是覆盖既有配置
        if (override)
            //设置命名空间
            this.hasNamespace(nameSpace);
        else {
            //没有才设置
            if (Utils.getStringIsEmpty(this.namespace))
                this.hasNamespace(nameSpace);
        }
    }

    /**
     * 使用一个构造函数为类型创建实例构造器
     *
     * @param constructorInfo 构造函数
     * @param override        是否覆盖既有配置
     * @return 实例构造器
     */
    @Override
    public IParameterConfigurator hasConstructorI(Constructor<?> constructorInfo, boolean override) {
        //覆盖既有配置 直接调用配置方法
        if (override)
            return this.hasConstructor((Constructor<TStructural>) constructorInfo);
        //否则 如果当前配置项没有构造器则创建一个
        if (this.constructor == null) {
            //反射构造器
            this.constructor = new ReflectionConstructor(constructorInfo);
        }
        //并且返回构造器参数配置项
        Parameter[] parameters = constructorInfo.getParameters();
        return new ParameterConfiguration<>(parameters, (TConfiguration) this);
    }

    /**
     * 启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个。
     * 注意:此方法为手动配置属性,仅创建关联引用配置项,不检查参数类型,不配置默认取值器和设值器
     *
     * @param name     属性名称，它将作为配置项的键
     * @param dataType 属性的属性类型
     * @return 属性配置项
     */
    public AttributeConfigurationGeneric<TStructural, TConfiguration> attribute(String name, Class<?> dataType) {

        if (Utils.getStringIsEmpty(name))
            throw new IllegalArgumentException("属性名称不能为空");
        //转换为首字母大写
        name = StringUtils.capitalize(name);
        //声明一个属性配置项
        AttributeConfigurationGeneric<TStructural, TConfiguration> result;

        //已有配置项
        if (this.getElementConfigurations().containsKey(name)) //已有配置项
        {
            //从元素配置项中获取属性配置项
            result = (AttributeConfigurationGeneric<TStructural, TConfiguration>) this.getElementConfigurations().get(name);
        } else //新建配置项
        {
            result = new AttributeConfigurationGeneric<>(name, dataType, this.clrType, this);
            //添加到元素配置项
            this.getElementConfigurations().put(name, result);
        }

        //返回属性配置项
        return result;
    }

    /**
     * 启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个。
     * 此方法会检查传入名称是否存在于实体中,且使用属性的访问器名称作为属性名称,自动侦测属性类型,并且会尝试自动配置取值器和设值器
     *
     * @param name 属性名称，它将作为配置项的键
     * @return 属性配置项
     */
    public AttributeConfigurationGeneric<TStructural, TConfiguration> attribute(String name) {
        return this.createAttributeConfiguration(name, null);
    }

    /**
     * 根据Lambda表达式包含的信息启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个
     * 此方法会检查传入名称是否存在于实体中,且使用属性的访问器名称作为属性名称,自动侦测属性类型,并且会尝试自动配置取值器和设值器
     *
     * @param get       lambda表达式
     * @param <TResult> Lambda表达式的返回值
     * @return 属性配置项
     */
    public <TResult> AttributeConfigurationGeneric<TStructural, TConfiguration> attribute(SerializedFunction<TStructural, TResult> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            //获取表达式代表的属性名称
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.createAttributeConfiguration(memberName, null);
        }

        throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
    }

    /**
     * 根据Lambda表达式包含的信息启动一个属性配置项，如果要启动的实体型配置项未创建则新建一个
     * 此方法会检查传入名称是否存在于实体中,且使用属性的访问器名称作为属性名称,传入的属性类型作为属性的类型,并且会尝试自动配置取值器和设值器
     *
     * @param get       lambda表达式
     * @param dataType  属性的属性类型
     * @param <TResult> Lambda表达式的返回值
     * @return 属性配置项
     */
    public <TResult> AttributeConfigurationGeneric<TStructural, TConfiguration> attribute(SerializedFunction<TStructural, TResult> get, Class<?> dataType) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            //获取表达式代表的属性名称
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.createAttributeConfiguration(memberName, dataType);
        }

        throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
    }

    /**
     * 创建属性配置项
     *
     * @param name     属性名称
     * @param dataType 属性数据类型
     * @return 属性配置项
     */
    private AttributeConfigurationGeneric<TStructural, TConfiguration> createAttributeConfiguration(String name, Class<?> dataType) {

        Property property = Utils.getProperty(this.clrType, name);

        //如果没有传入属性的数据类型 调用类型侦测方法
        if (dataType == null)
            dataType = this.attributeTypeConvert(property);

        //已有配置项
        if (this.getElementConfigurations().containsKey(name))
            //直接从元素配置项中获取属性配置项
            return (AttributeConfigurationGeneric<TStructural, TConfiguration>) this.getElementConfigurations().get(name);

        //创建或获取配置项
        AttributeConfigurationGeneric<TStructural, TConfiguration> attribute = this.attribute(name, dataType);

        Utils.configureValueGetterAndSetter(property, attribute);

        //字段名默认使用属性名
        if (Utils.getStringIsEmpty(attribute.targetField))
            attribute.toField(name);

        return attribute;
    }

    /**
     * 针对未指定具体类型的属性配置时自动侦测的类型是否可配置
     * 不可配置则抛异常
     * 可配置则转换为可配置成的属性
     *
     * @param property 要侦测的目标属性
     * @return 侦测后得到的类型
     */
    private Class<?> attributeTypeConvert(Property property) {
        //要判断的首要类型
        Class<?> targetType = property.getPropertyType();
        if (property.getPropertyType() == null)
            throw new IllegalArgumentException("不能检测null的类型");

        //真实类型 等于目标类型
        Class<?> realType = property.getPropertyType();
        //是不是可枚举的
        boolean isIterable = false;

        if (Iterable.class.isAssignableFrom(targetType)) {
            //string就按string配置
            if (targetType.equals(String.class)) {
                realType = targetType;
            }
            //判断是否为一个泛型参数
            if (property.getPropertyElementType() != null && property.getPropertyElementType().length == 1) {
                realType = property.getPropertyElementType()[0];
            }

            if (targetType.isArray())
                realType = targetType.getComponentType();

            isIterable = true;
        }

        //再次检测
        if (realType == null)
            throw new IllegalArgumentException("不能侦测null的类型.");

        //如果是Obase的基元类型
        if (PrimitiveType.isObasePrimitive(realType)) {
            //如果是可枚举的类型 都按照string配置
            if (isIterable)
                return String.class;

            //不是可枚举的 返回自身
            return realType;
        }

        //如果是复杂类型 可以配置为属性
        if (this.getModelBuilder().findConfiguration(targetType) instanceof ComplexTypeConfiguration) {
            return realType;
        }

        //都不是
        throw new IllegalArgumentException(property.getPropertyType().getName() + "不能拆解为Obase的基元类型也没有配置为复杂类型,不能配置为属性,请使用带有类型参数的属性配置方法.");
    }

    /**
     * 设置类型的实例构造器
     *
     * @param constructor 构造器
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasConstructor(IInstanceConstructor constructor) {
        this.constructor = constructor;
        return this;
    }

    /**
     * 设置类型的新实例构造器
     *
     * @param constructor 构造器
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(IInstanceConstructor constructor) {
        this.newInstanceConstructor = constructor;
        return this;
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建实例构造器
     *
     * @param construct 构造类型实例的委托
     * @return 自身
     */
    public TConfiguration hasConstructor(FunctionWithNoArg<TStructural> construct) {
        this.constructor = new DelegateConstructor<>(construct);
        return (TConfiguration) this;
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建新实例构造器
     *
     * @param construct 构造类型新实例的委托
     * @param tClass    构造目标类型
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(FunctionWithNoArg<TStructural> construct, Class<TStructural> tClass) {
        this.newInstanceConstructor = new DelegateConstructor<>(construct);

        try {
            //获取构造函数
            tClass.getConstructor(tClass);
            //设置新实例构造器的参数类型 不需要后续配置
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(new ArrayList<>());

            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("没有的构造函数不存在");
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param <T>       参数1的类型
     * @return 参数配置
     */
    public <T> ParameterConfiguration<TStructural, TConfiguration> hasConstructor(FunctionWithOneArg<T, TStructural> construct, Class<T> t1Class) {
        this.constructor = new DelegateConstructorWithOneArg<>(construct);

        try {
            Constructor<TStructural> constructorInfo = this.clrType.getConstructor(t1Class);
            Parameter[] parameters = constructorInfo.getParameters();
            return new ParameterConfiguration<>(parameters, (TConfiguration) this);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("一个参数(类型为%s)的构造函数不存在", t1Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建新实例构造器
     *
     * @param construct 构造类型新实例的委托
     * @param t1Class   参数1的类型
     * @param <T>       参数1的类型
     * @return 自身
     */
    public <T> StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(FunctionWithOneArg<T, TStructural> construct, Class<T> t1Class) {
        this.newInstanceConstructor = new DelegateConstructorWithOneArg<>(construct);

        try {
            //获取构造函数
            this.clrType.getConstructor(this.clrType, t1Class);
            List<Class<?>> types = new ArrayList<>();
            types.add(t1Class);
            //设置新实例构造器的参数类型 不需要后续配置
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(types);

            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("一个参数(类型为%s)的构造函数不存在", t1Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @return 参数配置
     */
    public <T1, T2> ParameterConfiguration<TStructural, TConfiguration> hasConstructor(FunctionWithTwoArgs<T1, T2, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class) {
        this.constructor = new DelegateConstructorWithTwoArgs<>(construct);

        try {
            Constructor<TStructural> constructorInfo = this.clrType.getConstructor(t1Class, t2Class);
            Parameter[] parameters = constructorInfo.getParameters();
            return new ParameterConfiguration<>(parameters, (TConfiguration) this);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("两个参数(类型为%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建新实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @return 自身
     */
    public <T1, T2> StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(FunctionWithTwoArgs<T1, T2, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class) {
        this.newInstanceConstructor = new DelegateConstructorWithTwoArgs<>(construct);

        try {
            //获取构造函数
            this.clrType.getConstructor(this.clrType, t1Class, t2Class);
            List<Class<?>> types = new ArrayList<>();
            types.add(t1Class);
            types.add(t2Class);
            //设置新实例构造器的参数类型 不需要后续配置
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(types);

            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("两个参数(类型为%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param t3Class   参数3的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @param <T3>      参数3的类型
     * @return 参数配置
     */
    public <T1, T2, T3> ParameterConfiguration<TStructural, TConfiguration> hasConstructor(FunctionWithThreeArgs<T1, T2, T3, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class, Class<T3> t3Class) {
        this.constructor = new DelegateConstructorWithThreeArgs<>(construct);

        try {
            Constructor<TStructural> constructorInfo = this.clrType.getConstructor(t1Class, t2Class, t3Class);
            Parameter[] parameters = constructorInfo.getParameters();
            return new ParameterConfiguration<>(parameters, (TConfiguration) this);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("三个参数(类型为%s,%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName(), t3Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建新实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param t3Class   参数3的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @param <T3>      参数3的类型
     * @return 自身
     */
    public <T1, T2, T3> StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(FunctionWithThreeArgs<T1, T2, T3, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class, Class<T3> t3Class) {
        this.newInstanceConstructor = new DelegateConstructorWithThreeArgs<>(construct);

        try {
            //获取构造函数
            this.clrType.getConstructor(this.clrType, t1Class, t2Class, t3Class);
            List<Class<?>> types = new ArrayList<>();
            types.add(t1Class);
            types.add(t2Class);
            types.add(t3Class);
            //设置新实例构造器的参数类型 不需要后续配置
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(types);

            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("三个参数(类型为%s,%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName(), t3Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param t3Class   参数3的类型
     * @param t4Class   参数4的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @param <T3>      参数3的类型
     * @param <T4>      参数4的类型
     * @return 参数配置
     */
    public <T1, T2, T3, T4> ParameterConfiguration<TStructural, TConfiguration> hasConstructor(FunctionWithFourArgs<T1, T2, T3, T4, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class, Class<T3> t3Class, Class<T4> t4Class) {
        this.constructor = new DelegateConstructorWithFourArgs<>(construct);

        try {
            Constructor<TStructural> constructorInfo = this.clrType.getConstructor(t1Class, t2Class, t3Class, t4Class);
            Parameter[] parameters = constructorInfo.getParameters();
            return new ParameterConfiguration<>(parameters, (TConfiguration) this);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("四个参数(类型为%s,%s,%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName(), t3Class.getName(), t4Class.getName()));
        }
    }

    /**
     * 使用一个可以创建类型实例的委托为类型创建新实例构造器
     *
     * @param construct 构造类型实例的委托
     * @param t1Class   参数1的类型
     * @param t2Class   参数2的类型
     * @param t3Class   参数3的类型
     * @param t4Class   参数4的类型
     * @param <T1>      参数1的类型
     * @param <T2>      参数2的类型
     * @param <T3>      参数3的类型
     * @param <T4>      参数4的类型
     * @return 自身
     */
    public <T1, T2, T3, T4> StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(FunctionWithFourArgs<T1, T2, T3, T4, TStructural> construct, Class<T1> t1Class, Class<T2> t2Class, Class<T3> t3Class, Class<T4> t4Class) {
        this.newInstanceConstructor = new DelegateConstructorWithFourArgs<>(construct);

        try {
            //获取构造函数
            this.clrType.getConstructor(this.clrType, t1Class, t2Class, t3Class, t4Class);
            List<Class<?>> types = new ArrayList<>();
            types.add(t1Class);
            types.add(t2Class);
            types.add(t3Class);
            types.add(t4Class);
            //设置新实例构造器的参数类型 不需要后续配置
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(types);

            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("四个参数(类型为%s,%s,%s,%s)的构造函数不存在", t1Class.getName(), t2Class.getName(), t3Class.getName(), t4Class.getName()));
        }
    }

    /**
     * 使用一个构造函数为类型创建实例构造器
     *
     * @param constructorInfo 构造函数
     * @return 构造函数的参数配置
     */
    public ParameterConfiguration<TStructural, TConfiguration> hasConstructor(Constructor<TStructural> constructorInfo) {
        this.constructor = new ReflectionConstructor(constructorInfo);
        Parameter[] parameters = constructorInfo.getParameters();
        return new ParameterConfiguration<>(parameters, (TConfiguration) this);
    }

    /**
     * 使用一个构造函数为类型新实例构造器
     *
     * @param constructorInfo 新实例构造器
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasNewInstanceConstructor(Constructor<TStructural> constructorInfo) {
        this.newInstanceConstructor = new ReflectionConstructor(constructorInfo);
        Parameter[] parameters = constructorInfo.getParameters();
        if (parameters != null && parameters.length > 0) {
            ((InstanceConstructor) this.newInstanceConstructor).setParameterTypes(Arrays.stream(parameters).map(Parameter::getType).collect(Collectors.toList()));
        }

        return this;
    }

    /**
     * 设置此类型的具体类型判别规范
     * 用于判断此类型的要如何创建具体的类型
     *
     * @param concreteTypeDiscriminator 具体类型判别器
     * @param typeAttributeName         用于判断类型的字段名称
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasConcreteTypeDiscriminator(IConcreteTypeDiscriminator concreteTypeDiscriminator, String typeAttributeName) {
        this.concreteTypeDiscriminator = concreteTypeDiscriminator;
        this.typeAttributeName = typeAttributeName;
        return this;
    }

    /**
     * 设置此类型的判别字段和判别字段的值
     *
     * @param typeName 判别字段名称
     * @param value    判别字段的值
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasConcreteTypeSign(String typeName, Object value) {
        if (value == null)
            throw new IllegalArgumentException("不能设置空的类型判别字段.");
        Class<?> valueType = value.getClass();
        if (valueType != Integer.class && valueType != Long.class && valueType != String.class)
            throw new IllegalArgumentException("判别字段必须为string,int,long类型中的一种");

        this.concreteTypeSign = new TwoTuple<>(typeName, value);
        return this;
    }

    /**
     * 反射建模
     */
    @Override
    void reflectionModeling(ITypeMemberAnalyzer analyticPipeline) {

        //此类声明了继承类 则只查找到继承类为止
        List<Property> properties = this.derivingFrom != null ? ObaseIntrospector.getObaseBeanProperties(this.clrType, this.derivingFrom) : ObaseIntrospector.getObaseBeanProperties(this.clrType);
        List<Property> hangUps = new ArrayList<>();
        //遍历类型属性
        for (Property property : properties) {
            //class忽略掉
            if (property.getName().equalsIgnoreCase("class")) continue;
            //过滤属性挂起
            if (this.ignoreList.contains(property.getName())) {
                hangUps.add(property);
                continue;
            }

            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
            Utils.getIsMultiple(property, type);

            //首先 查找已有配置
            ITypeElementConfigurator configurator =
                    (ITypeElementConfigurator) this.getElementConfigurations().values().stream().filter(p -> p.getName().equalsIgnoreCase(property.getName())).findFirst().orElse(null);
            //使用管道判定结果为true 或者 被配置为了模型的一部分
            ObjectReferencePack<String> namePack = new ObjectReferencePack<>();
            if (this.asElement(property, analyticPipeline, namePack) ||
                    this.getModelBuilder().findConfiguration(type.realValue) != null || Utils.isTuple(type.realValue)) {
                if (configurator == null)
                    //没有配置 创建一个
                    configurator = this.createTypeElementConfigurator(property, namePack.realValue);
            }
            //挂起
            else {
                hangUps.add(property);
                continue;
            }

            //使用管道处理当前的配置
            ITypeMemberAnalyzer pipeLine = analyticPipeline;
            while (pipeLine != null) {
                if (configurator != null)
                    pipeLine.configure(property, configurator);
                pipeLine = pipeLine.getNext();
            }
        }

        //处理挂起的
        for (Property hangUp : hangUps) {
            ITypeMemberAnalyzer pipeLine = analyticPipeline;
            while (pipeLine != null) {
                pipeLine.configure(hangUp, this);
                pipeLine = pipeLine.getNext();
            }
        }
    }

    /**
     * 创建具体的配置
     *
     * @param property 属性
     * @param name     名称
     * @return 类型元素配置
     */
    private ITypeElementConfigurator createTypeElementConfigurator(Property property, String name) {
        //获取多重性
        ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
        Utils.getIsMultiple(property, type);

        //判断是否配置为复杂属性
        boolean isComplex = false;
        StructuralTypeConfiguration<?> complexConfig = this.getModelBuilder().findConfiguration(type.realValue);
        if (complexConfig != null) {
            if (complexConfig instanceof ComplexTypeConfiguration) {
                isComplex = complexConfig.clrType.equals(type.realValue);
            }
        }

        //基元类型或者枚举 或者被配置为复杂类型 按照属性处理
        if (PrimitiveType.isObasePrimitive(property.getPropertyType()) || type.realValue.isEnum() || isComplex && !property.getPropertyType().isEnum()) {
            Class<?> propType = Optional.class.isAssignableFrom(property.getPropertyType())
                    ? property.getPropertyElementType()[0]
                    : property.getPropertyType();

            //创建属性配置项
            return this.attribute(name, propType);
        }

        //不是简单属性 查找关联型或者实体型
        StructuralTypeConfiguration<?> structuralTypeConfiguration =
                this.getModelBuilder().findConfiguration(type.realValue);
        //被配置过 或者 是个元组 元组要继续处理
        if (structuralTypeConfiguration != null || Utils.isTuple(type.realValue)) {
            //仅为关联型
            if (this instanceof IAssociationTypeConfigurator && !(this instanceof IEntityTypeConfigurator))
                return this.createReferenceElement(property);
            //仅为实体型
            if (this instanceof IEntityTypeConfigurator && !(this instanceof IAssociationTypeConfigurator))
                return this.createReferenceElement(property);
            //同时做关联型和实体型
            if (this instanceof IEntityTypeConfigurator && this instanceof IAssociationTypeConfigurator)
                throw new IllegalArgumentException("暂不支持一个类型同时为关联型和实体型");
        }

        return null;
    }

    /**
     * 调用管道判断是否作为元素
     *
     * @param memberInfo       属性
     * @param analyticPipeline 类型元素配置管道
     * @param name             名称
     * @return 是否作为元素
     */
    private boolean asElement(Property memberInfo, ITypeMemberAnalyzer analyticPipeline, ObjectReferencePack<String> name) {
        boolean result = false;

        //后续管道的判定
        ITypeMemberAnalyzer pipeLine = analyticPipeline;
        while (pipeLine != null) {
            result |= pipeLine.asElement(memberInfo, name);
            pipeLine = pipeLine.getNext();
        }

        //管道没有定名字 取默认值
        if (name != null && Utils.getStringIsEmpty(name.realValue)) {
            name.realValue = memberInfo.getName();
        }

        return result;
    }

    /**
     * 添加类型扩展配置器
     *
     * @param extensionConfigurationType 类型扩展配置器类型
     * @param <TExtensionConfiguration>  类型扩展配置器
     * @return
     */
    public <TExtensionConfiguration extends TypeExtensionConfiguration> TExtensionConfiguration hasExtension(Class<TExtensionConfiguration> extensionConfigurationType) {
        try {
            TypeExtensionConfiguration extensionConfiguration =
                    extensionConfigurationType.newInstance();
            this.extensionConfigs.add(extensionConfiguration);
            return (TExtensionConfiguration) extensionConfiguration;
        } catch (Exception e) {
            throw new IllegalArgumentException("添加扩展配置器失败," + extensionConfigurationType.getName() + "没有适合的无参构造函数", e);
        }
    }

    /**
     * 忽略某个方法
     *
     * @param name 要忽略的方法名
     */
    public void ignore(String name) {
        //添加到过滤属性集合中
        this.ignoreList.add(StringUtils.capitalize(name));
    }

    /**
     * 忽略某个方法
     *
     * @param get       表达式
     * @param <TResult> 表达式的结果
     */
    public <TResult> void ignore(SerializedFunction<TStructural, TResult> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();

            //添加到过滤属性集合中
            this.ignoreList.add(memberName);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }


    /**
     * 指定当前类型的基类型
     *
     * @param type 基类型
     * @return 自身
     */
    public TConfiguration deriveFrom(Class<?> type) {
        if (!type.isAssignableFrom(this.clrType))
            throw new IllegalArgumentException(String.format("%s不是%s的基类", type.getName(), this.clrType));

        this.derivingFrom = type;
        return (TConfiguration) this;
    }

    /**
     * 创建引用元素
     *
     * @param property 属性
     * @return 引用元素配置
     */
    protected abstract ITypeElementConfigurator createReferenceElement(Property property);
}
