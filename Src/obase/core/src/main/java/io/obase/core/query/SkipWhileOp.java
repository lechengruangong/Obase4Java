/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示SkipWhile运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 14:33:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示SkipWhile运算
 */
public class SkipWhileOp extends FilterOp {

    /**
     * 创建SkipWhileOp实例
     *
     * @param predicate 断言函数
     */
    SkipWhileOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.SkipWhile, predicate, false, model);
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
