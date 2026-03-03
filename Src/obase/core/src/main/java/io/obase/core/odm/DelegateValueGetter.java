/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托取值器，使用指定的委托获取属性值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:33:57
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;

/**
 * 委托取值器，使用指定的委托获取属性值
 */
public class DelegateValueGetter<TObject, TValue> implements IValueGetter {

    /**
     * 用于获取属性值的委托
     */
    private final FunctionWithOneArg<TObject, TValue> delegate;

    /**
     * 创建DelegateValueGetter实例
     *
     * @param delegateFunction 用于获取属性值的委托
     */
    public DelegateValueGetter(FunctionWithOneArg<TObject, TValue> delegateFunction) {
        this.delegate = delegateFunction;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 对象的值
     */
    public Object getValue(Object obj) {
        return this.delegate.invoke((TObject) obj);
    }
}
