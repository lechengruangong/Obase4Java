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
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.InstanceConstructor;

import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
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
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素。
     *
     * @param get       表示绑定元素名称的表达式
     * @param <TResult> 参数类型
     * @return 构造器参数配置
     */
    public <TResult> ParameterConfiguration<TStructural, TTypeConfiguration> map(SerializedFunction<TStructural, TResult> get) {
        return this.map(get, null);
    }


    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素
     *
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> map(String elementName, FunctionWithOneArg<Object, Object> valueConverter) {
        if (this.constructorParameters.size() > 0) {
            Parameter parameter = this.constructorParameters.poll();
            InstanceConstructor constructor = (InstanceConstructor) this.typeConfiguration.constructor;
            if (constructor != null && parameter != null)
                constructor.setParameter(parameter.getName(), elementName, valueConverter, null);
        }
        return this;
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素
     *
     * @param elementName 绑定元素的名称
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> map(String elementName) {
        return this.map(elementName, null);
    }


    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素
     *
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> mapDefault(FunctionWithOneArg<Object, Object> valueConverter) {
        if (this.constructorParameters.size() > 0) {
            Parameter parameter = this.constructorParameters.poll();
            InstanceConstructor constructor = (InstanceConstructor) this.typeConfiguration.constructor;
            if (constructor != null && parameter != null)
                constructor.setParameter(parameter.getName(), parameter.getName(), valueConverter, null);
        }

        return this;
    }

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素
     *
     * @return 构造器参数配置
     */
    public ParameterConfiguration<TStructural, TTypeConfiguration> mapDefault() {
        return this.mapDefault(null);
    }

    /**
     * 在所有参数配置完成后返回到当前类型
     *
     * @return 创建参数配置项的类型配置项
     */
    public TTypeConfiguration end() {
        if (this.constructorParameters.size() >= 1) throw new IllegalArgumentException("还有参数没有配置，不能返回。");
        return (TTypeConfiguration) this.typeConfiguration;
    }
}
