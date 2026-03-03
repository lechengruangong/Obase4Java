/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托设值器，使用指定的委托为元素设置值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:35:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.ActionWithTwoArg;

import static io.obase.core.common.Utils.convertDbValue;

/**
 * 委托设值器，使用指定的委托为元素设置值
 *
 * @param <TObject> 要设值的元素的属主类型
 * @param <TValue>  在Assignment模式下为值序列的类型，在Appending模式下为值序列项的类型
 */
public class DelegateValueSetter<TObject, TValue> extends ValueSetter {

    /**
     * 设值模式
     */
    private final EValueSettingMode mode;

    /**
     * 为属性设值的委托
     */
    private final ActionWithTwoArg<TObject, TValue> delegate;

    /**
     * 目标类型
     */
    private final Class<?> tValueType;

    /**
     * 创建DelegateValueSetter实例
     *
     * @param delegate   为属性设值的委托
     * @param mode       设值模式
     * @param tValueType 目标值的类型
     */
    public DelegateValueSetter(ActionWithTwoArg<TObject, TValue> delegate, EValueSettingMode mode, Class<?> tValueType) {
        this.delegate = delegate;
        this.mode = mode;
        this.tValueType = tValueType;
    }

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    @Override
    public EValueSettingMode getMode() {
        return this.mode;
    }

    /**
     * 执行为对象设值的核心逻辑。由派生类实现
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    @Override
    protected void setValueCore(Object obj, Object value) {

        if (value == null || obj == null)
            return;

        if (this.tValueType != null) {
            value = convertDbValue(value, this.tValueType);
        }

        if (this.mode == EValueSettingMode.Appending) {
            if (value instanceof Iterable) {
                Iterable<?> iterable = (Iterable<Object>) value;
                for (Object o : iterable) {
                    this.delegate.invoke((TObject) obj, (TValue) o);
                }
            } else {
                this.delegate.invoke((TObject) obj, (TValue) value);
            }
        } else {
            this.delegate.invoke((TObject) obj, (TValue) value);
        }
    }
}
