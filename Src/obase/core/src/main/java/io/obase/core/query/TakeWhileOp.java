/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示TakeWhile运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:13:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示TakeWhile运算
 */
public class TakeWhileOp extends FilterOp {

    /**
     * 创建TakeWhileOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     */
    TakeWhileOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.TakeWhile, predicate, false, model);
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
