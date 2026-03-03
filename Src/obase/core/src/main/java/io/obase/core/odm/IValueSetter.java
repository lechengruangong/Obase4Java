/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象设置器接口,向对象中的类型元素设置值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 16:20:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 对象设置器接口
 * 对象设置器用于为对象的属性、关联引用或关联端设置值。
 */
public interface IValueSetter {

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    EValueSettingMode getMode();

    /**
     * 为对象设值
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    void setValue(Object obj, Object value);
}
