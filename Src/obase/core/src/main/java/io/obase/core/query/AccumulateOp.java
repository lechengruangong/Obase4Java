/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Accumulate运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:02:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示Accumulate运算
 */
public class AccumulateOp extends AggregateOp {

    /**
     * 累加函数
     */
    private LambdaExpression accumulator;

    /**
     * 结果函数，用于将累加器的最终值转换为结果值
     */
    private LambdaExpression resultSelector;

    /**
     * 种子值
     */
    private Object seed;

    /**
     * 创建AccumulateOp实例
     *
     * @param accumulator 累加函数
     */
    AccumulateOp(LambdaExpression accumulator, ObjectDataModel model) {
        super(EQueryOpName.Accumulate, accumulator, model, QueryOp.getParameterHostType(accumulator));
        this.accumulator = accumulator;
        this.resultSelector = null;
        this.seed = null;
    }

    /**
     * 创建AccumulateOp实例
     *
     * @param accumulator 累加函数
     * @param seed        种子值
     */
    AccumulateOp(LambdaExpression accumulator, Object seed, ObjectDataModel model) {
        this(accumulator, model);
        this.seed = seed;
    }

    /**
     * 创建AccumulateOp实例
     *
     * @param accumulator    累加函数
     * @param resultSelector 结果函数
     */
    AccumulateOp(LambdaExpression accumulator, LambdaExpression resultSelector, ObjectDataModel model) {
        this(accumulator, model);
        this.resultSelector = resultSelector;
    }

    /**
     * 创建AccumulateOp实例
     *
     * @param accumulator    累加函数
     * @param seed           种子值
     * @param resultSelector 结果函数
     */
    AccumulateOp(LambdaExpression accumulator, Object seed, LambdaExpression resultSelector, ObjectDataModel model) {
        this(accumulator, seed, model);
        this.accumulator = accumulator;
        this.resultSelector = resultSelector;
        this.seed = seed;
    }

    /**
     * 获取累加函数
     *
     * @return 累加函数
     */
    public LambdaExpression getAccumulator() {
        return this.accumulator;
    }

    /**
     * 获取结果函数，该函数用于将累加器的最终值转换为结果值
     *
     * @return 结果函数
     */
    public LambdaExpression getResultSelector() {
        return this.resultSelector;
    }

    /**
     * 获取结果值类型
     *
     * @return 结果值类型
     */
    @Override
    public Class<?> getResultType() {
        return this.resultSelector.getBody().getType();
    }

    /**
     * 获取种子值
     *
     * @return 种子值
     */
    public Object getSeed() {
        return this.seed;
    }

    /**
     * 获取种子值类型
     *
     * @return 种子值类型
     */
    public Class<?> getSeedType() {
        if (this.seed == null)
            return null;
        return this.seed.getClass();
    }
}
