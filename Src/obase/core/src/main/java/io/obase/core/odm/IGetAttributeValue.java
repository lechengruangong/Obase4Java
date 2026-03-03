/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：获取属性值委托.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:43:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 属性取值委托。用于从指定对象取出指定属性的值
 */
public interface IGetAttributeValue {

    /**
     * 属性取值委托。用于从指定对象取出指定属性的值
     *
     * @param obj       目标对象
     * @param attribute 要取其值的属性
     * @param parent    属性的父属性
     * @return 属性的值
     */
    Object getAttributeValue(Object obj, Attribute attribute, AttributePath parent);
}
