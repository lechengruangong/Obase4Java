/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:24:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithNoArg;

/**
 * 委托构造器，使用指定的委托构造对象
 */
public class DelegateConstructor<TObject> extends InstanceConstructor {

    /**
     * 构造对象的委托
     */
    private final FunctionWithNoArg<TObject> delegate;

    /**
     * 创建DelegateConstructor实例
     *
     * @param delegateFunction 构造对象的委托
     */
    public DelegateConstructor(FunctionWithNoArg<TObject> delegateFunction) {
        this.delegate = delegateFunction;
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    @Override
    public Object construct(Object[] arguments) {
        return this.delegate.invoke();
    }
}

