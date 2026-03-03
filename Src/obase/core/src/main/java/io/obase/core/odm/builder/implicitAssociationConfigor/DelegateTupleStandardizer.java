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
     *
     * @param tupleItems 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）
     * @return 标准化元组的项序列
     */
    @Override
    public Object revert(Object[] tupleItems) {
        return this.revertingFunc.invoke(tupleItems);
    }

    /**
     * 元组标准化函数，将被引对象元组转换成标准元组。
     *
     * @param referredTuple 表示标准化元组的对象数组
     * @return 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）。被引对象是指关联引用指向的对象，如果关联引用是多重性的，它是指其中的一个。
     */
    @Override
    public Object[] standardize(Object referredTuple) {
        return (Object[]) this.standardizingFunc.invoke((TReferred) referredTuple);
    }
}
