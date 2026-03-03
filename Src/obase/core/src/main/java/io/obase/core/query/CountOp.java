/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Count运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:53:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示Count运算
 */
public class CountOp extends AggregateOp {

    /**
     * 创建CountOp实例
     *
     * @param predicate 断言函数，用于判定元素是否参与计数
     */
    CountOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.Count, predicate, model, QueryOp.getParameterHostType(predicate));
    }

    /**
     * 创建CountOp实例
     *
     * @param sourceType 查询源类型
     */
    CountOp(Class<?> sourceType) {
        super(EQueryOpName.Count, sourceType);
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getPredicate().getBody().getType();
    }
}
