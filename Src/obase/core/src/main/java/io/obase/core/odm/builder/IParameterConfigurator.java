/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置参数的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:47:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;

/**
 * 定义配置参数的规范
 */
public interface IParameterConfigurator {

    /**
     * 在所有参数配置完成后返回到当前类型
     *
     * @return 当前类型
     */
    IStructuralTypeConfigurator endI();

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素(不使用类型转换器)
     *
     * @param elementName 绑定元素的名称
     */
    void mapI(String elementName);

    /**
     * 从构造函数参数队列取出一项，将之绑定到指定的类型元素
     *
     * @param elementName    绑定元素的名称
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     */
    void mapI(String elementName, FunctionWithOneArg<Object, Object> valueConverter);

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素(不使用类型转换器)
     */
    void mapDefaultI();

    /**
     * 从构造函数参数队列取出一项，将之绑定到同名类型元素
     *
     * @param valueConverter 值转换器，用于将存储源中的值转换为元素的值
     */
    void mapDefaultI(FunctionWithOneArg<Object, Object> valueConverter);
}
