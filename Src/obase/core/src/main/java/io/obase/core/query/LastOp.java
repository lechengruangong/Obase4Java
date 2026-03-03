/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Last运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:20:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示Last运算
 */
public class LastOp extends FilterOp {

    /**
     * 创建LastOp实例
     *
     * @param predicate     断言函数，用于测试每个元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    LastOp(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        super(EQueryOpName.Last, predicate, returnDefault, model);
    }

    /**
     * 创建LastOp实例
     *
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    LastOp(Class<?> sourceType, boolean returnDefault) {
        super(EQueryOpName.Last, sourceType, returnDefault);
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
