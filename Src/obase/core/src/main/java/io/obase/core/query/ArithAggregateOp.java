/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示ArithAggregate运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:37:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示ArithAggregate运算
 */
public class ArithAggregateOp extends AggregateOp {

    /**
     * 算术聚合运算符
     */
    private final EAggregationOperator operator;

    /**
     * 投影函数，应用于每个元素然后以投影结果参与聚合。不指定投影函数则聚合元素自身
     */
    private final LambdaExpression selector;

    /**
     * 创建ArithAggregateOp实例
     *
     * @param operator 算术聚合运算符
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合。不指定投影函数则聚合元素自身
     */
    ArithAggregateOp(EAggregationOperator operator, ObjectDataModel model, LambdaExpression selector) {
        super(EQueryOpName.ArithAggregate, selector, model, selector.getParameters()[0].getType());

        this.operator = operator;
        this.selector = selector;
    }

    /**
     * 创建ArithAggregateOp实例
     *
     * @param operator   算术聚合运算符
     * @param sourceType 源类型
     */
    ArithAggregateOp(EAggregationOperator operator, Class<?> sourceType) {
        super(EQueryOpName.ArithAggregate, sourceType);

        this.operator = operator;
        this.selector = null;
    }

    /**
     * 获取算术聚合运算符
     *
     * @return 算术聚合运算符
     */
    public EAggregationOperator getOperator() {
        return this.operator;
    }

    /**
     * 获取聚合结果类型
     *
     * @return 聚合结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.selector.getBody().getType();
    }

    /**
     * 获取投影函数，该函数应用于每个元素然后以投影结果参与聚合。不指定投影函数则聚合元素自身。
     *
     * @return 投影函数
     */
    public LambdaExpression getSelector() {
        return this.selector;
    }
}
