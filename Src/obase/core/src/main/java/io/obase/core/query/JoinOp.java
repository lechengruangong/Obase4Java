/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Join运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:16:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

import java.util.Comparator;

/**
 * 表示Join运算
 */
public class JoinOp extends QueryOp {

    /**
     * 相等比较器，用于测试来自两个元素的联接鍵是否相等
     */
    private final Comparator<?> comparer;

    /**
     * 联接鍵函数，用于从第二个序列的每个元素提取联接鍵
     */
    private final LambdaExpression innerKeySelector;

    /**
     * 获取要与第一个序列联接的序列
     */
    private final Iterable<?> innerSource;

    /**
     * 联接鍵函数，用于从第一个序列的每个元素提取联接鍵
     */
    private final LambdaExpression outerKeySelector;

    /**
     * 结果投影函数，用于从两个匹配元素创建结果元素
     */
    private final LambdaExpression resultSelector;

    /**
     * 创建JoinOp实例
     *
     * @param innerSource      要与第一个序列联接的序列
     * @param outerKeySelector 联接鍵函数，用于从第一个序列的每个元素提取联接鍵
     * @param innerKeySelector 联接鍵函数，用于从第二个序列的每个元素提取联接鍵
     * @param resultSelector   结果投影函数，用于从两个匹配元素创建结果元素
     * @param comparer         相等比较器，用于测试来自两个元素的联接鍵是否相等
     */
    JoinOp(Iterable<?> innerSource, LambdaExpression outerKeySelector, LambdaExpression innerKeySelector,
           LambdaExpression resultSelector, Comparator<?> comparer, ObjectDataModel model) {
        super(EQueryOpName.Join, QueryOp.getParameterHostType(outerKeySelector));

        this.innerSource = innerSource;
        this.outerKeySelector = outerKeySelector;
        this.innerKeySelector = innerKeySelector;
        this.resultSelector = resultSelector;
        this.comparer = comparer;
        this.model = model;
    }

    /**
     * 获取相等比较器，该比较器用于测试来自两个元素的联接鍵是否相等
     *
     * @return 相等比较器
     */
    public Comparator<?> getComparer() {
        return this.comparer;
    }

    /**
     * 获取一个联接鍵函数，该函数用于从第二个序列的每个元素提取联接鍵
     *
     * @return 联接鍵函数
     */
    public LambdaExpression getInnerKeySelector() {
        return this.innerKeySelector;
    }

    /**
     * 获取第二个序列联接鍵的类型
     *
     * @return 第二个序列联接鍵的类型
     */
    public Class<?> getInnerKeyType() {
        return this.innerKeySelector.getBody().getType();
    }

    /**
     * 要与第一个序列联接的序列
     *
     * @return 第一个序列联接的序列
     */
    public Iterable<?> getInnerSource() {
        return this.innerSource;
    }

    /**
     * 获取第二个序列元素的类型
     *
     * @return 第二个序列元素的类型
     */
    public Class<?> getInnerType() {
        return Object.class;
    }

    /**
     * 获取一个值，该值指示是否对第二个序列按其联接鍵分组、以组为单位与第一个序列联接
     *
     * @return 是否对第二个序列按其联接鍵分组
     */
    public boolean getIsGrouping() {
        return Iterable.class.isAssignableFrom(this.resultSelector.getParameters()[1].getType());
    }

    /**
     * 获取一个联接鍵函数，该函数用于从第一个序列的每个元素提取联接鍵
     *
     * @return 联接鍵函数
     */
    public LambdaExpression getOuterKeySelector() {
        return this.outerKeySelector;
    }

    /**
     * 获取第一个序列联接鍵的类型
     *
     * @return 第一个序列联接鍵的类型
     */
    public Class<?> getOuterKeyType() {
        return this.outerKeySelector.getBody().getType();
    }

    /**
     * 获取第一个序列元素的类型
     *
     * @return 第一个序列元素的类型
     */
    public Class<?> getOuterType() {
        return this.outerKeySelector.getParameters()[0].getType();
    }

    /**
     * 获取结果投影函数，该函数用于从两个匹配元素创建结果元素
     *
     * @return 结果投影函数
     */
    public LambdaExpression getResultSelector() {
        return this.resultSelector;
    }

    /**
     * 获取结果序列元素的类型
     *
     * @return 结果序列元素的类型
     */
    public Class<?> getResultType() {
        return this.resultSelector.getBody().getType();
    }
}
