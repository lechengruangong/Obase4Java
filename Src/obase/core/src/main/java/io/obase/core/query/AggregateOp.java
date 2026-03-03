/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：聚合类运算基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:02:47
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 为聚合类运算提供基础实现
 */
public abstract class AggregateOp extends QueryOp {

    /**
     * 断言函数，用于判定元素是否参与聚合
     */
    private final LambdaExpression predicate;

    /**
     * 创建AggregateOp实例
     *
     * @param name      运算名
     * @param predicate 断言函数，用于判定元素是否参与聚合
     */
    protected AggregateOp(EQueryOpName name, LambdaExpression predicate, ObjectDataModel model, Class<?> type) {
        super(name, type);
        this.predicate = predicate;
        this.model = model;
    }

    /**
     * 创建AggregateOp实例
     *
     * @param name       运算名称
     * @param sourceType 查询源模型类型
     */
    protected AggregateOp(EQueryOpName name, Class<?> sourceType) {
        super(name, sourceType);
        this.predicate = null;
    }

    /**
     * 获取断言函数，该函数用于判定元素是否参与聚合
     *
     * @return 获取断言函数，该函数用于判定元素是否参与聚合
     */
    public LambdaExpression getPredicate() {
        return this.predicate;
    }
}
