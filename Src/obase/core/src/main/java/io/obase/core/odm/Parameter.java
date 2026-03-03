/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：构造参数,用于描述类型构造函数的参数
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:53:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.expression.Expression;

/**
 * 描述构造参数。构造参数用于描述类型构造函数的参数。
 * 构造参数必须绑定到该类型的一个元素，该元素称为绑定元素。绑定元素为关联引用时，该关联引用的类型不能为显式关联型，即构造函数不能以关联对象作为参数。
 */
public class Parameter {

    /**
     * 参数所属的构造器
     */
    private final IInstanceConstructor constructor;

    /**
     * 参数名称
     */
    private final String name;

    /**
     * 绑定元素的名称
     */
    private String elementName;

    /**
     * 值转换器，用于将存储源中的值转换为元素的值
     */
    private FunctionWithOneArg<Object, Object> valueConverter;

    /**
     * 如果为投影得出的 此参数绑定的表达式
     */
    private Expression expression;

    /**
     * 创建Parameter实例
     *
     * @param name        参数名称
     * @param constructor 构造参数所属的构造器
     */
    Parameter(String name, IInstanceConstructor constructor) {
        this.name = name;
        this.constructor = constructor;
    }

    /**
     * 获取绑定元素的名称
     *
     * @return 绑定元素的名称
     */
    public String getElementName() {
        return this.elementName;
    }

    /**
     * 设置绑定元素的名称
     *
     * @param elementName 绑定元素的名称
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * 获取参数名称
     *
     * @return 参数名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取参数绑定元素的类别 属性、关联引用等
     *
     * @return 参数绑定元素的类别 属性、关联引用等
     */
    public EElementType getElementType() {
        return this.getElement().getElementType();
    }

    /**
     * 获取值转换器
     *
     * @return 值转换器
     */
    public FunctionWithOneArg<Object, Object> getValueConverter() {
        return this.valueConverter;
    }

    /**
     * 设置值转换器
     *
     * @param valueConverter 值转换器
     */
    public void setValueConverter(FunctionWithOneArg<Object, Object> valueConverter) {
        this.valueConverter = valueConverter;
    }

    /**
     * 获取参数绑定的表达式
     *
     * @return 参数绑定的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 设置参数绑定的表达式
     *
     * @param expression 参数绑定的表达式
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * 获取构造参数的绑定元素
     *
     * @return 构造参数的绑定元素
     */
    public TypeElement getElement() {
        TypeElement result;
        //如果获取的是具体类型区分标识的元素
        if (this.constructor.getInstanceType().getConcreteTypeSign() != null && this.constructor.getInstanceType().getConcreteTypeSign().getItem1().equalsIgnoreCase(this.elementName)) {
            //返回映射字段为标识字段的元素
            result = this.constructor.getInstanceType().findAttributeByTargetField(this.constructor.getInstanceType().getConcreteTypeSign()
                    .getItem1());
        } else {
            //否则返回普通的元素
            result = this.constructor.getInstanceType().getElement(this.elementName);
        }
        return result;
    }

    /**
     * 获取构造参数的类型
     *
     * @return 构造参数的类型
     */
    public Class<?> getType() {
        return this.getElement().getValueType().getClrType();
    }
}
