/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：委托构建的元组标准化器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 17:53:45
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.common.FunctionWithOneArg;

/**
 * 基于两个分别充当标准化函数和反函数的委托构建的元组标准化器
 */
public class DelegateTupleStandardizer<TReferred> implements ITupleStandardizer {

    /**
     * 元组标准化函数
     */
    private final FunctionWithOneArg<TReferred, Object> standardizingFunc;

    /**
     * 标准化函数的反函数
     */
    private final FunctionWithOneArg<Object, TReferred> revertingFunc;

    /**
     * 初始化DelegateTupleStandardizer类的新实例
     *
     * @param standardizingFunc 元组标准化函数
     * @param revertingFunc     标准化函数的反函数
     */
    public DelegateTupleStandardizer(FunctionWithOneArg<TReferred, Object> standardizingFunc, FunctionWithOneArg<Object, TReferred> revertingFunc) {
        this.standardizingFunc = standardizingFunc;
        this.revertingFunc = revertingFunc;
    }

    /**
     * 元组标准化函数的反函数，将标准元组转换成被引对象元组。
     * 直接调用构造时传入的revertingFunc完成转换
     *
     * @param tupleItems 标准元组，即被引对象按其在关联端建模登记次序排列（跳过引用源对象）组成的对象数组
     * @return 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）
     */
    @Override
    public Object revert(Object[] tupleItems) {
        return this.revertingFunc.invoke(tupleItems);
    }

    /**
     * 元组标准化函数，将被引对象元组转换成标准元组。
     * 直接调用构造时传入的standardizingFunc完成转换，其返回值必须是Object数组
     *
     * @param referredTuple 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）。被引对象是指关联引用指向的对象，如果关联引用是多重性的，它是指其中的一个。
     * @return 表示标准化元组的对象数组，元素按关联端建模次序排列（跳过引用源对象）
     */
    @Override
    public Object[] standardize(Object referredTuple) {
        return (Object[]) this.standardizingFunc.invoke((TReferred) referredTuple);
    }
}
