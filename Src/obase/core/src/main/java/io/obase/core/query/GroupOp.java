/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Group运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:13:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.Expression;
import io.obase.core.expression.IGroupingBy;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 表示Group运算
 */
public class GroupOp extends QueryOp {

    /**
     * 组元素函数，用于从每个元素提取组元素
     */
    private final LambdaExpression elementSelector;
    /**
     * 鍵函数，用于从每个元素提取分组鍵
     */
    private final LambdaExpression keySelector;
    /**
     * 相等比较器，用于测试两个分组鍵是否相等
     */
    private Comparator<?> comparator;

    /**
     * 创建GroupOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     */
    GroupOp(LambdaExpression keySelector, LambdaExpression elementSelector, ObjectDataModel model) {
        super(EQueryOpName.Group, QueryOp.getParameterHostType(keySelector));

        this.keySelector = keySelector;
        this.elementSelector = elementSelector;
        this.model = model;
    }

    /**
     * 创建GroupOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param comparer        相等比较器
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     */
    GroupOp(LambdaExpression keySelector, Comparator<?> comparer,
            LambdaExpression elementSelector, ObjectDataModel model) {
        this(keySelector, elementSelector, model);
        this.comparator = comparer;
    }

    /**
     * 获取组元素函数，该函数用于从每个元素提取组元素
     *
     * @return 获取组元素函数，该函数用于从每个元素提取组元素
     */
    public LambdaExpression getElementSelector() {
        return this.elementSelector;
    }

    /**
     * 获取组元素类型
     *
     * @return 组元素类型
     */
    public Class<?> getElementType() {
        return this.elementSelector.getBody().getType() == null ? this.getSourceType() : this.elementSelector.getBody().getType();
    }

    /**
     * 获取鍵函数，该函数用于从每个元素提取分组鍵
     *
     * @return 鍵函数
     */
    public LambdaExpression getKeySelector() {
        return this.keySelector;
    }

    /**
     * 获取分组键类型
     *
     * @return 分组键类型
     */
    public Class<?> getKeyType() {
        return this.keySelector.getBody().getType();
    }

    /**
     * 获取用于测试两个分组鍵是否相等比较器
     *
     * @return 用于测试两个分组鍵是否相等比较器
     */
    public Comparator<?> getComparator() {
        return this.comparator;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return IGroupingBy.class;
    }

    /**
     * 由实现类重写 获取表达式参数
     *
     * @return 获取表达式参数
     */
    @Override
    protected Expression[] gotArguments() {
        Expression[] member = new MemberExpressionExtractor(new SubTreeEvaluator(this.getKeySelector())).extractMember(this.getKeySelector()).stream().distinct().toArray(Expression[]::new);
        List<Expression> result = new ArrayList<>();
        Collections.addAll(result, member);
        if (this.getElementSelector() != null) {
            member = new MemberExpressionExtractor(new SubTreeEvaluator(this.getElementSelector())).extractMember(this.getElementSelector()).stream().distinct().toArray(Expression[]::new);
            Collections.addAll(result, member);
        }

        return result.toArray(new Expression[0]);
    }
}
