/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：二方关联的元组标准化函数及其反函数.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 17:37:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

/**
 * 适用于二方关联的元组标准化函数及其反函数。
 * 对于二方关联，关联引用指向的元组只有一个元素，因此标准化函数只需要生成一个单元素数组，其反函数即取出该元素。
 */
public class TwoAssociationTupleStandardizer implements ITupleStandardizer {
    /**
     * 元组标准化函数的反函数，将标准元组转换成被引对象元组。
     *
     * @param tupleItems 标准元组，即被引对象按其在关联端建模登记次序排列（跳过引用源对象）组成的对象数组
     * @return 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）
     */
    @Override
    public Object revert(Object[] tupleItems) {
        return tupleItems[0];
    }

    /**
     * 元组标准化函数，将被引对象元组转换成标准元组。
     *
     * @param referredTuple 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）。被引对象是指关联引用指向的对象，如果关联引用是多重性的，它是指其中的一个。
     * @return 表示标准化元组的对象数组，元素按关联端建模次序排列（跳过引用源对象）
     */
    @Override
    public Object[] standardize(Object referredTuple) {
        return new Object[]{referredTuple};
    }
}
