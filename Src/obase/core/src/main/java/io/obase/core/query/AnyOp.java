/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Any运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:34:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示Any运算
 */
public class AnyOp extends CriteriaContainOp {

    /**
     * 创建AnyOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     */
    AnyOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.Any, predicate, model);
    }

    /**
     * 创建AnyOp实例
     *
     * @param type 查询源类型
     */
    AnyOp(Class<?> type) {
        super(EQueryOpName.Any, type);
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
