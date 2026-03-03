/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Reverse运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:34:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示Reverse运算
 */
public class ReverseOp extends QueryOp {

    /**
     * 创建ReverseOp实例
     *
     * @param sourceType 查询源类型
     */
    ReverseOp(Class<?> sourceType) {
        super(EQueryOpName.Reverse, sourceType);
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
