/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象构造器接口,提供构造对象的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-23 12:00:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.List;

/**
 * 对象构造器接口。
 * 对象构造器用于构造类型（实体型、复杂类型、关联型）的对象。
 */
public interface IInstanceConstructor {

    /**
     * 获取构造函数的形式参数
     *
     * @return 构造函数的形式参数
     */
    List<Parameter> getParameters();

    /**
     * 获取要构造的对象的类型
     *
     * @return 要构造的对象的类型
     */
    StructuralType getInstanceType();

    /**
     * 设置要构造的对象的类型
     *
     * @param instanceType 要构造的对象的类型
     */
    void setInstanceType(StructuralType instanceType);

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    Object construct(Object[] arguments);

    /**
     * 构造对象
     *
     * @return 构造出的对象
     */
    Object construct();

    /**
     * 获取绑定到指定元素的构造函数参数
     *
     * @param elementName 元素名称
     * @return 构造函数参数
     */
    Parameter getParameterByElement(String elementName);
}
