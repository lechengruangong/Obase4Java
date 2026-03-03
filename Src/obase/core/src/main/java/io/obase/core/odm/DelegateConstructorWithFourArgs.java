/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:26:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.FunctionWithFourArgs;

/**
 * 委托构造器，使用指定的委托构造对象
 *
 * @param <T1>      第一个构造参数的类型
 * @param <T2>      第二个构造参数的类型
 * @param <T3>      第三个构造参数的类型
 * @param <T4>      第四个构造参数的类型
 * @param <TObject> 要构造的对象的类型
 */
public class DelegateConstructorWithFourArgs<T1, T2, T3, T4, TObject> extends InstanceConstructor {

    /**
     * 构造对象的委托
     */
    private final FunctionWithFourArgs<T1, T2, T3, T4, TObject> delegate;

    /**
     * 创建DelegateConstructor实例
     *
     * @param delegate 构造对象的委托
     */
    public DelegateConstructorWithFourArgs(FunctionWithFourArgs<T1, T2, T3, T4, TObject> delegate) {
        this.delegate = delegate;
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造的对象
     */
    @Override
    public Object construct(Object[] arguments) {
        if (arguments == null || arguments.length != 4)
            return null;
        this.defaultArgumentConvert(arguments);
        return this.delegate.invoke((T1) arguments[0], (T2) arguments[1], (T3) arguments[2], (T4) arguments[3]);
    }
}
