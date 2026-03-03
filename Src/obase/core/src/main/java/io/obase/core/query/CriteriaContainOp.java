/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：测试序列元素是否满足指定条件的运算基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:31:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 为测试序列元素是否满足指定条件的运算提供基础实现
 */
public abstract class CriteriaContainOp extends QueryOp {

    /**
     * 断言函数，用于测试元素是否满足条件
     */
    private final LambdaExpression predicate;

    /**
     * 创建CriteriaContainOp实例
     *
     * @param name      运算名称
     * @param predicate 断言函数，用于测试元素是否满足条件
     */
    protected CriteriaContainOp(EQueryOpName name, LambdaExpression predicate, ObjectDataModel model) {
        super(name, QueryOp.getParameterHostType(predicate));
        this.predicate = predicate;
        this.model = model;
    }

    /**
     * 创建CriteriaContainOp实例
     *
     * @param name 运算名称
     * @param type 源类型
     */
    protected CriteriaContainOp(EQueryOpName name, Class<?> type) {
        super(name, type);
        this.predicate = null;
    }

    /**
     * 获取断言函数，该函数用于测试元素是否满足条件
     *
     * @return 断言函数
     */
    public LambdaExpression getPredicate() {
        return this.predicate;
    }
}
