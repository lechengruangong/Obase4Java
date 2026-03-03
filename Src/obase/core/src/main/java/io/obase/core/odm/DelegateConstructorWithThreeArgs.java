/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:27:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.FunctionWithThreeArgs;

/**
 * 委托构造器，使用指定的委托构造对象
 *
 * @param <T1>      第一个构造参数的类型
 * @param <T2>      第二个构造参数的类型
 * @param <T3>      第三个构造参数的类型
 * @param <TObject> 要构造的对象的类型
 */
public class DelegateConstructorWithThreeArgs<T1, T2, T3, TObject> extends InstanceConstructor {

    /**
     * 构造对象的委托
     */
    private final FunctionWithThreeArgs<T1, T2, T3, TObject> delegate;

    /**
     * 创建DelegateConstructor实例
     *
     * @param delegate 构造对象的委托
     */
    public DelegateConstructorWithThreeArgs(FunctionWithThreeArgs<T1, T2, T3, TObject> delegate) {
        this.delegate = delegate;
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造的对象
     */
    @Override
    public Object construct(Object[] arguments) throws UnknownTypeException {
        if (arguments == null || arguments.length != 3)
            return null;
        this.defaultArgumentConvert(arguments);
        return this.delegate.invoke((T1) arguments[0], (T2) arguments[1], (T3) arguments[2]);
    }
}
