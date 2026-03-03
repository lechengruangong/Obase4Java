/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Cast运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:42:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示Cast运算
 */
public class CastOp extends QueryOp {

    /**
     * 转换目标类型
     */
    private final Class<?> resultType;

    /**
     * 创建CastOp实例
     *
     * @param resultType 转换目标类型
     * @param sourceType 查询源类型
     */
    CastOp(Class<?> resultType, Class<?> sourceType) {
        super(EQueryOpName.Cast, sourceType);
        this.resultType = resultType;
    }

    /**
     * 获取转换目标类型
     *
     * @return 获取转换目标类型
     */
    public Class<?> getResultType() {
        return this.resultType;
    }
}
