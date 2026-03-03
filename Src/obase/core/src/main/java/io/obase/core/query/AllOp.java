/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示All运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:32:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示All运算
 */
public class AllOp extends CriteriaContainOp {

    /**
     * 创建AllOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     */
    AllOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.All, predicate, model);
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return boolean.class;
    }
}
