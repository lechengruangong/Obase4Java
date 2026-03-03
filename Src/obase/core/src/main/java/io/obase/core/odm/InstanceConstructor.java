/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象构造器基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 16:12:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 为对象构造器提供基础实现
 */
public abstract class InstanceConstructor implements IInstanceConstructor {

    /**
     * 要构造的实例的类型
     */
    private StructuralType instanceType;

    /**
     * 构造器的参数
     */
    private List<Parameter> parameters;

    /**
     * 构造器的参数类型
     */
    private List<Class<?>> parameterTypes;

    /**
     * 获取构造器的参数类型
     *
     * @return 构造器的参数类型
     */
    public List<Class<?>> getParameterTypes() {
        if (this.parameterTypes == null || this.parameterTypes.isEmpty()) {
            this.parameterTypes = this.parameters.stream().map(Parameter::getType).collect(Collectors.toList());
        }
        return this.parameterTypes;
    }

    /**
     * 设置构造器的参数类型
     *
     * @param parameterTypes 构造器的参数类型
     */
    public void setParameterTypes(List<Class<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * 获取构造函数的形式参数
     *
     * @return 构造函数的形式参数
     */
    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * 获取要构造的对象的类型
     *
     * @return 要构造的对象的类型
     */
    @Override
    public StructuralType getInstanceType() {
        return this.instanceType;
    }

    /**
     * 设置要构造的对象的类型
     *
     * @param instanceType 要构造的对象的类型
     */
    @Override
    public void setInstanceType(StructuralType instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    @Override
    public abstract Object construct(Object[] arguments);

    /**
     * 构造对象
     *
     * @return 构造出的对象
     */
    @Override
    public Object construct() {
        return this.construct(null);
    }

    /**
     * 获取绑定到指定元素的构造函数参数
     *
     * @param elementName 元素名称
     * @return 构造函数参数
     */
    @Override
    public Parameter getParameterByElement(String elementName) {
        if (this.parameters == null)
            return null;
        Optional<Parameter> parameter = this.parameters.stream().filter(p -> Objects.equals(p.getElementName(), elementName)).findFirst();
        return parameter.orElse(null);
    }

    /**
     * 设置构造参数，并指定其绑定元素的名称
     *
     * @param name           参数名称
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     * @param expression     如果此参数为投影得出的 此项表示此参数对应的表达式
     */
    public void setParameter(String name, String elementName, FunctionWithOneArg<Object, Object> valueConverter, Expression expression) {
        Parameter para = new Parameter(name, this);
        para.setElementName(elementName);
        para.setValueConverter(valueConverter);
        para.setExpression(expression);

        if (this.parameters == null) this.parameters = new ArrayList<>();
        this.parameters.add(para);
    }

    /**
     * 设置构造参数，并指定其绑定元素的名称
     *
     * @param name           参数名称
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器
     */
    public void setParameter(String name, String elementName, FunctionWithOneArg<Object, Object> valueConverter) {
        this.setParameter(name, elementName, valueConverter, null);
    }

    /**
     * 设置构造参数，并指定其绑定元素的名称
     *
     * @param name        参数名称
     * @param elementName 绑定元素的名称
     */
    public void setParameter(String name, String elementName) {
        this.setParameter(name, elementName, null, null);
    }

    /**
     * 默认的构造参数转换
     *
     * @param result    目标类型
     * @param typeClass 值
     * @return 默认的构造参数转换
     */
    protected Object defaultConvert(Class<?> typeClass, Object result) {
        if (result == null) return null;
        result = Utils.convertDbValue(result, typeClass);
        return result;
    }

    /**
     * 默认的参数转换
     *
     * @param arguments 参数集合
     */
    protected void defaultArgumentConvert(Object[] arguments) {
        if (this.getParameters() != null) {
            for (int i = 0; i < arguments.length; i++) {
                if (this.getParameters().get(i) != null && this.getParameters().get(i).getValueConverter() != null)
                    arguments[i] = this.getParameters().get(i).getValueConverter().invoke(arguments[i]);
                else
                    arguments[i] = this.defaultConvert(this.getParameters().get(i).getType(), arguments[i]);
            }
        }
    }
}
