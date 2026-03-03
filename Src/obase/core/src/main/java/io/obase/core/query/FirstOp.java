/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示First运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:09:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示First运算
 */
public class FirstOp extends FilterOp {

    /**
     * 创建FirstOp实例
     *
     * @param predicate     断言函数，用于测试每个元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    FirstOp(LambdaExpression predicate, ObjectDataModel model, boolean returnDefault) {
        super(EQueryOpName.First, predicate, returnDefault, model);
    }

    /**
     * 创建FirstOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    FirstOp(Class<?> sourceType, boolean returnDefault) {
        super(EQueryOpName.First, sourceType, returnDefault);
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
