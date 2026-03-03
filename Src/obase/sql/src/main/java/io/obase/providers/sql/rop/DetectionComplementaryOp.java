/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：测定类运算的补充运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:20:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.ObjectDataModel;
import io.obase.core.query.QueryOp;

/**
 * 测定类运算（AllOp, AnyOp, ContainsOp, SingleOp）的补充运算
 */
public class DetectionComplementaryOp extends QueryOp {

    /**
     * 被补充的运算
     */
    private final QueryOp complementedOp;

    /**
     * 初始化DetectionComplementaryOp的新实例
     *
     * @param complementedOp 补充运算
     * @param model          对象数据模型
     */
    public DetectionComplementaryOp(QueryOp complementedOp, ObjectDataModel model) {
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
