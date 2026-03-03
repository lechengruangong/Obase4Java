/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义视图元素规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:32:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.expression.Expression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.EElementType;
import io.obase.core.odm.IValueGetter;
import io.obase.core.odm.IValueSetter;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.AssociationTreeNode;

/**
 * 定义视图元素规范
 */
public interface ITypeViewElement {

    /**
     * 获取元素的类型
     *
     * @return 元素的类型
     */
    EElementType getElementType();

    /**
     * 获取一个值，该值指示元素是否具有多重性，即其值是否为集合类型。
     *
     * @return 否具有多重性
     */
    boolean getIsMultiple();

    /**
     * 设置一个值，该值指示元素是否具有多重性，即其值是否为集合类型。
     *
     * @param isMultiple 否具有多重性
     */
    void setIsMultiple(boolean isMultiple);

    /**
     * 获取元素的名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 获取元素宿主对象的类型
     *
     * @return 获取元素宿主对象的类型
     */
    StructuralType getHostType();

    /**
     * 获取影子元素
     *
     * @return 影子元素
     */
    ITypeViewElement getShadow();

    /**
     * 设置影子元素
     *
     * @param shadow 影子元素
     */
    void setShadow(ITypeViewElement shadow);

    /**
     * 获取取值器
     *
     * @return 取值器
     */
    IValueGetter getValueGetter();

    /**
     * 设置取值器
     *
     * @param valueGetter 取值器
     */
    void setValueGetter(IValueGetter valueGetter);

    /**
     * 获取设值器
     *
     * @return 设值器
     */
    IValueSetter getValueSetter();

    /**
     * 设置设值器
     *
     * @param valueSetter 设值器
     */
    void setValueSetter(IValueSetter valueSetter);

    /**
     * 生成在视图表达式中定义当前元素的表达式，它规定了该元素的锚点和绑定。
     *
     * @param sourcePara           代表视图源的形参
     * @param flatteningParaGetter 一个委托，用于获取代表指定平展点的形参。
     */
    Expression generateExpression(ParameterExpression sourcePara,
                                  FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter);

    /**
     * 从指定对象取出当前元素的值。
     *
     * @param targetObj 要取其元素值的对象
     * @return 如果元素具有多重性，返回IEnumerable`1[T]，否则返回object
     */
    Object getValue(Object targetObj);

    /**
     * 为指定对象的当前元素设置值，适用于具有多重性的元素。
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    void setValue(Object targetObj, Iterable<Object> value);

    /**
     * 为指定对象的当前元素设置值，适用于不具多重性的元素。
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    void setValue(Object targetObj, Object value);
}