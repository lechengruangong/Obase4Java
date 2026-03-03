/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Take运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 14:44:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示Take运算
 */
public class TakeOp extends QueryOp {

    /**
     * 要提取的个数
     */
    private final int count;

    /**
     * 创建TakeOp实例
     *
     * @param sourceType 查询源类型
     * @param count      要提取的个数
     */
    TakeOp(Class<?> sourceType, int count) {
        super(EQueryOpName.Take, sourceType);

        this.count = count;
    }

    /**
     * 获取要提取的个数
     *
     * @return 要提取的个数
     */
    public int getCount() {
        return this.count;
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
