/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Group聚合运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:26:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.EExpressionType;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

import java.util.Comparator;

/**
 * 表示Group（聚合）运算
 */
public class GroupAggregationOp extends GroupOp {

    /**
     * 聚合投影函数，用于对每个组生成聚合值
     */
    private final LambdaExpression resultSelector;

    /**
     * 创建GroupAggregationOp实例
     *
     * @param resultSelector  聚合投影函数，用于对每个组生成聚合值
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     */
    GroupAggregationOp(LambdaExpression resultSelector, LambdaExpression keySelector,
                       LambdaExpression elementSelector, ObjectDataModel model) {

        super(keySelector, elementSelector, model);
        this.resultSelector = resultSelector;
    }

    /**
     * 创建GroupAggregationOp实例
     *
     * @param resultSelector  聚合投影函数，用于对每个组生成聚合值
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param comparer        相等比较器，用于测试两个分组鍵是否相等
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     */
    GroupAggregationOp(LambdaExpression resultSelector, LambdaExpression keySelector,
                       Comparator<?> comparer, LambdaExpression elementSelector, ObjectDataModel model) {
        super(keySelector, comparer, elementSelector, model);

        this.resultSelector = resultSelector;
    }

    /**
     * 获取一个值，该值表示是否为实例化聚合
     *
     * @return 是否为实例化聚合
     */
    public boolean getIsNew() {
        return this.resultSelector.getBody().getExpressionType() == EExpressionType.New || this.resultSelector.getBody().getExpressionType() == EExpressionType.MemberInit;
    }

    /**
     * 获取聚合投影函数，该函数用于对每个组生成聚合值
     *
     * @return 聚合投影函数
     */
    public LambdaExpression getResultSelector() {
        return this.resultSelector;
    }

    /**
     * 获取聚合结果类型
     *
     * @return 聚合结果类型
     */
    public Class<?> getResultType() {
        return this.resultSelector.getBody().getType();
    }
}

