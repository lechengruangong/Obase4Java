/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示ElementAt运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:00:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示ElementAt运算
 */
public class ElementAtOp extends QueryOp {

    /**
     * 要检索的从零开始的元素索引
     */
    private final int index;

    /**
     * 指示当指定索引处无元素时是否返回默认值
     */
    private final boolean returnDefault;

    /**
     * 创建ElementAtOp实例
     *
     * @param sourceType    查询源类型
     * @param index         要检索的从零开始的元素索引
     * @param returnDefault 指示当指定索引处无元素时是否返回默认值
     */
    ElementAtOp(Class<?> sourceType, int index, boolean returnDefault) {
        super(EQueryOpName.ElementAt, sourceType);
        this.index = index;
        this.returnDefault = returnDefault;
    }

    /**
     * 获取要检索的从零开始的元素索引
     *
     * @return 要检索的从零开始的元素索引
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取一个值，该值指示当指定索引处无元素时是否返回默认值
     *
     * @return 无元素时是否返回默认值
     */
    public boolean getReturnDefault() {
        return this.returnDefault;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getSourceType();
    }
}
