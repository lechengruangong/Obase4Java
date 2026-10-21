/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：构造参数配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 16:50:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.InstanceConstructor;
import io.obase.core.odm.PrimitiveType;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 构造参数配置项
 *
 * @param <TTypeConfiguration> 创建当前参数配置项的类型配置项的类型
 */
public class ParameterConfiguration<TStructural, TTypeConfiguration extends StructuralTypeConfigurationGeneric<TStructural, TTypeConfiguration>>
        implements IParameterConfigurator {

    /**
     * 构造函数的参数序列
     */
    private final Queue<Parameter> constructorParameters;

    /**
     * 创建参数配置项的类型配置项
     */
    private final StructuralTypeConfigurationGeneric<TStructural, TTypeConfiguration> typeConfiguration;

    /**
     * 创建ParameterConfiguration实例
     *
     * @param constructorParas  构造函数参数信息集合
     * @param typeConfiguration 创建当前参数配置项的类型配置项
     */
    ParameterConfiguration(Parameter[] constructorParas, TTypeConfiguration typeConfiguration) {
        this.constructorParameters = new ArrayDeque<>();
        this.typeConfiguration = typeConfiguration;
        for (Parameter para : constructorParas) {
            this.constructorParameters.offer(para);
        }
    }

    /**
     * 在所有参数配置完成后返回到当前类型
     * 参数队列中仍有未配置的参数时抛出IllegalArgumentException
     *
     * @return 当前类型
     */
    @Override
    public IStructuralTypeConfigurator endI() {
        return this.end();
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素(不使用类型转换器)
     *
     * @param elementName 绑定元素的名称
     */
    @Override
    public void mapI(String elementName) {
        this.map(elementName);
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素
     *
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     */
    @Override
    public void mapI(String elementName, FunctionWithOneArg<Object, Object> valueConverter) {
        this.map(elementName, valueConverter);
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素(不使用类型转换器)
     */
    @Override
    public void mapDefaultI() {
        this.mapDefault();
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素
     *
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     */
    @Override
    public void mapDefaultI(FunctionWithOneArg<Object, Object> valueConverter) {
        this.mapDefault(valueConverter);
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素。
     * 队列为空或当前类型配置项尚未配置构造函数时不做绑定（参数仍会被取出）；表达式主体不是MemberExpression时抛出IllegalArgumentException
     *
     * @param get            表示绑定元素名称的表达式
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值。
     * @param <TResult>      参数类型
     * @return 构造器参数配置
     */
    public <TResult> ParameterConfiguration<TStructural, TTypeConfiguration> map(SerializedFunction<TStructural, TResult> get, FunctionWithOneArg<Object, Object> valueConverter) {

        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            if (this.constructorParameters.size() > 0) {
                MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
                String memberName = memberExpression.getMemberName();
                Parameter parameter = this.constructorParameters.poll();
                //校验绑定元素 避免构造参数被绑定到关联引用、关联端等引用元素上
                this.ensureBindableElement(memberName, parameter.getName());
                InstanceConstructor constructor = (InstanceConstructor) this.typeConfiguration.constructor;
                if (constructor != null && parameter != null)
                    constructor.setParameter(parameter.getName(), memberName, valueConverter, memberExpression);
            }

            return this;

        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素(不使用类型转换器)。
     * 队列为空或当前类型配置项尚未配置构造函数时不做绑定（参数仍会被取出）；表达式主体不是MemberExpression时抛出IllegalArgumentException
     *
     * @param get       表示绑定元素名称的表达式
     * @param <TResult> 参数类型
     * @return 构造器参数配置
     */
    public <TResult> ParameterConfiguration<TStructural, TTypeConfiguration> map(SerializedFunction<TStructural, TResult> get) {
        return this.map(get, null);
    }


    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素。
     * 队列为空时不做任何处理；队列非空但当前类型配置项尚未配置构造函数时同样跳过该项（参数仍会被取出）
     *
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> map(String elementName, FunctionWithOneArg<Object, Object> valueConverter) {
        if (this.constructorParameters.size() > 0) {
            Parameter parameter = this.constructorParameters.poll();
            //校验绑定元素 避免构造参数被绑定到关联引用、关联端等引用元素上
            this.ensureBindableElement(elementName, parameter.getName());
            InstanceConstructor constructor = (InstanceConstructor) this.typeConfiguration.constructor;
            if (constructor != null && parameter != null)
                constructor.setParameter(parameter.getName(), elementName, valueConverter, null);
        }
        return this;
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素(不使用类型转换器)。
     * 队列为空时不做任何处理；队列非空但当前类型配置项尚未配置构造函数时同样跳过该项（参数仍会被取出）
     *
     * @param elementName 绑定元素的名称
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> map(String elementName) {
        return this.map(elementName, null);
    }


    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素。
     * 队列为空时不做任何处理；队列非空但当前类型配置项尚未配置构造函数时同样跳过该项（参数仍会被取出）
     *
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> mapDefault(FunctionWithOneArg<Object, Object> valueConverter) {
        if (this.constructorParameters.size() > 0) {
            Parameter parameter = this.constructorParameters.poll();
            //校验绑定元素 避免参数名恰好与导航属性同名时被静默绑定到关联引用、关联端等引用元素上
            this.ensureBindableElement(parameter.getName(), parameter.getName());
            InstanceConstructor constructor = (InstanceConstructor) this.typeConfiguration.constructor;
            if (constructor != null && parameter != null)
                constructor.setParameter(parameter.getName(), parameter.getName(), valueConverter, null);
        }

        return this;
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素(不使用类型转换器)。
     * 队列为空时不做任何处理；队列非空但当前类型配置项尚未配置构造函数时同样跳过该项（参数仍会被取出）
     *
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> mapDefault() {
        return this.mapDefault(null);
    }

    /**
     * 在所有参数配置完成后返回到当前类型
     * 参数队列中仍有未配置的参数时抛出IllegalArgumentException
     *
     * @return 创建参数配置项的类型配置项
     */
    public TTypeConfiguration end() {
        if (this.constructorParameters.size() >= 1) throw new IllegalArgumentException("还有参数没有配置，不能返回。");
        return (TTypeConfiguration) this.typeConfiguration;
    }

    /**
     * 校验构造参数的绑定元素是否合法。
     * 构造参数只能绑定到本类型的简单属性（包括复杂类型属性和多重性属性），不能绑定到关联引用、关联端等引用元素。
     * 说明
     * 本校验尽力而为：Map执行时元素配置项未必全部建立，能判定则抛出可读异常，判不了则交由建模阶段的其它检查兜底。
     *
     * @param elementName   绑定元素名称
     * @param parameterName 参数名称（用于报错定位）
     */
    private void ensureBindableElement(String elementName, String parameterName) {
        //名称都未给出 无法判定
        if (elementName == null || elementName.isEmpty()) return;

        Class<?> structuralType = this.typeConfiguration.getClrType();
        //① 优先使用已建立的元素配置项判断：引用元素（关联引用、关联端）一律不允许
        HashMap<String, TypeElementConfiguration> elementConfigurations = this.typeConfiguration.getElementConfigurations();
        if (elementConfigurations != null) {
            //元素配置项的名称可能与绑定名称仅大小写不同
            TypeElementConfiguration element = elementConfigurations.entrySet().stream()
                    .filter(p -> p.getKey() != null && p.getKey().equalsIgnoreCase(elementName))
                    .map(Map.Entry::getValue).findFirst().orElse(null);
            if (isReferenceElementConfiguration(element))
                throw new IllegalArgumentException("类型" + structuralType.getSimpleName() + "的构造函数参数" + parameterName + "不能绑定到" + elementName + "："
                        + "该元素是引用元素（关联引用或关联端），构造函数参数只能绑定到本类型的简单属性。"
                        + "关系请改用AssociationEnd + HasMapping + AssociationReference配置。");
        }

        //② 兜底：配置项可能尚未建立，按属性的类型和类型的注册情况判断
        //注：MapDefault按参数名绑定，参数名与属性名只是大小写不同（如homeCountry与HomeCountry），故忽略大小写取属性
        Property property = this.findBindableProperty(structuralType, elementName);
        //取不到属性（名称写错或非public成员）交由建模阶段的检查兜底
        if (property == null) return;
        //Obase基元类型 按简单属性处理
        if (PrimitiveType.isObasePrimitive(property.getPropertyType())) return;

        //集合或数组 取元素类型判断
        Class<?> elementType;
        try {
            Class<?>[] elementTypes = property.getPropertyElementType();
            //取不到元素类型或泛型参数多于一个（如元组）时无法在此判定
            if (elementTypes == null || elementTypes.length != 1) return;
            elementType = elementTypes[0];
        } catch (Exception e) {
            //泛型参数无法解析 无法在此判定
            return;
        }

        if (elementType == null) return;
        if (PrimitiveType.isObasePrimitive(elementType)) return;

        //未注册的类型不是结构化类型 不在此处判定
        StructuralTypeConfiguration<?> propertyConfig = this.typeConfiguration.getModelBuilder().findConfiguration(elementType);
        if (propertyConfig == null) return;

        //复杂类型允许作为构造参数
        if (propertyConfig instanceof ComplexTypeConfiguration) return;

        //实体型、关联型等结构化类型属于导航或关联 不是可落库的简单属性
        //绑定名与属性名大小写不同时（如MapDefault的参数名homeCountry与属性HomeCountry）把两者都报出来
        String bindingDescription = elementName.equals(property.getName())
                ? elementName
                : elementName + "（实际命中属性" + property.getName() + "）";
        throw new IllegalArgumentException("类型" + structuralType.getSimpleName() + "的构造函数参数" + parameterName + "不能绑定到" + bindingDescription + "："
                + "该属性的类型" + elementType.getSimpleName() + "是已注册的结构化类型（实体型或关联型），属于导航或关联，不是可落库的简单属性。"
                + "关系请改用AssociationEnd + HasMapping + AssociationReference配置。");
    }

    /**
     * 按名称查找属性，先按名称完全一致查找，再忽略大小写查找
     *
     * @param structuralType 类型
     * @param elementName    绑定元素名称
     * @return 属性；找不到返回null
     */
    private Property findBindableProperty(Class<?> structuralType, String elementName) {
        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(structuralType);
        if (properties == null) return null;
        Property property = properties.stream().filter(p -> elementName.equals(p.getName())).findFirst().orElse(null);
        if (property == null)
            property = properties.stream().filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(elementName)).findFirst().orElse(null);
        return property;
    }

    /**
     * 判断一个元素配置项是否为引用元素（关联引用或关联端）。
     * 说明
     * ReferenceElementConfiguration只有泛型形态ReferenceElementConfiguration&lt;TObject, TConfiguration&gt;，
     * 没有非泛型基类，因此沿基类链比对泛型定义。AssociationReferenceConfiguration与AssociationEndConfiguration都继承它，
     * AttributeConfiguration与ComplexTypeConfiguration不会命中。
     *
     * @param element 元素配置项
     * @return 是引用元素返回true
     */
    private static boolean isReferenceElementConfiguration(TypeElementConfiguration element) {
        if (element == null) return false;
        for (Class<?> type = element.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            Type genericSuperclass = type.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                if (rawType == ReferenceElementConfiguration.class)
                    return true;
            }
        }
        return false;
    }
}
