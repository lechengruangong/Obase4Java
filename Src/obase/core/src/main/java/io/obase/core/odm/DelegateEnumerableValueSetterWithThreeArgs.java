/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于可枚举类型的委托设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:42:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.ActionWithTwoArg;

import java.util.function.Function;

/**
 * 特定于可枚举类型的委托设值器，使用指定的委托创建可枚举类型的值，然后使用指定的委托将该值赋给元素
 *
 * @param <TObject>  要设值的元素的属主类型
 * @param <TValue>   值的类型
 * @param <TElement> 值序列项的类型
 */
public class DelegateEnumerableValueSetterWithThreeArgs<TObject, TValue extends Iterable<TElement>, TElement> extends DelegateValueSetter<TObject, TValue> {

    /**
     * 一个委托，代表基于IEnumerable序列创建可枚举类型值的方法
     */
    private final Function<Iterable<TElement>, TValue> valueCreator;

    /**
     * 为元素设值的委托
     */
    private final ActionWithTwoArg<TObject, TValue> delegate;

    /**
     * 创建DelegateValueSetter实例
     *
     * @param delegate     为属性设值的委托
     * @param valueCreator 一个委托，代表基于IEnumerable序列创建可枚举类型值的方法
     */
    public DelegateEnumerableValueSetterWithThreeArgs(ActionWithTwoArg<TObject, TValue> delegate, Function<Iterable<TElement>, TValue> valueCreator) {
        super(delegate, EValueSettingMode.Assignment, null);
        this.delegate = delegate;
        this.valueCreator = valueCreator;
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
        Iterable<?> objValues = ((Iterable<Object>) value);
        this.delegate.invoke((TObject) obj, (TValue) value);
        Iterable<TElement> newValue = (Iterable<TElement>) objValues;
        super.setValueCore(obj, this.valueCreator.apply(newValue));
    }
}
