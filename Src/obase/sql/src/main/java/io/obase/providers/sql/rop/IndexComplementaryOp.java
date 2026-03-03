/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：索引运算的补充运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:29:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.ObjectDataModel;
import io.obase.core.query.QueryOp;

/**
 * 索引运算（ElementAtOp）的补充运算
 */
public class IndexComplementaryOp extends QueryOp {

    /**
     * 被补充的运算
     */
    private final QueryOp complementedOp;

    /**
     * 初始化DetectionComplementaryOp的新实例
     *
     * @param complementedOp 被补充的运算
     */
    public IndexComplementaryOp(QueryOp complementedOp, ObjectDataModel model) {
        super(complementedOp.getName(), complementedOp.getSourceType());
        this.complementedOp = complementedOp;
        this.model = model;
    }

    /**
     * 获取被补充的运算
     *
     * @return 被补充的运算
     */
    public QueryOp getComplementedOp() {
        return this.complementedOp;
    }

    /**
     * 获取结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.complementedOp.getResultType();
    }
}
