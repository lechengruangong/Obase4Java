/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于可枚举类型的委托设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:44:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.ActionWithTwoArg;

/**
 * 特定于可枚举类型的委托设值器，使用指定的委托为可枚举类型的元素设置值
 */
public class DelegateEnumerableValueSetterWithTwoArgs<TObject, TElement> extends DelegateValueSetter<TObject, Iterable<TElement>> {

    /**
     * 为元素设值的委托
     */
    private final ActionWithTwoArg<TObject, Iterable<TElement>> delegate;

    /**
     * 创建DelegateEnumerableValueSetter实例
     *
     * @param delegate 为元素设值的委托
     */
    public DelegateEnumerableValueSetterWithTwoArgs(ActionWithTwoArg<TObject, Iterable<TElement>> delegate, Class<?> tValueType) {
        super(delegate, EValueSettingMode.Assignment, tValueType);
        this.delegate = delegate;
    }

    /**
     * 执行为对象设值的核心逻辑
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    @Override
    protected void setValueCore(Object obj, Object value) {
        if (value == null) return;
        Iterable<Object> objValues = ((Iterable<Object>) value);
        Iterable<TElement> newValue = (Iterable<TElement>) objValues;
        this.delegate.invoke((TObject) obj, newValue);
    }
}
