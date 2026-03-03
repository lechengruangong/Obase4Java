/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示OfType运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:26:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示OfType运算
 */
public class OfTypeOp extends QueryOp {

    /**
     * 作为筛选依据的类型
     */
    private final Class<?> resultType;

    /**
     * 创建OfType实例
     *
     * @param resultType 作为筛选依据的类型
     * @param sourceType 查询源类型
     */
    OfTypeOp(Class<?> resultType, Class<?> sourceType) {
        super(EQueryOpName.OfType, sourceType);
        this.resultType = resultType;
    }

    /**
     * 获取结果类型
     *
     * @return 结果类型
     */
    public Class<?> getResultType() {
        return this.resultType;
    }

}
