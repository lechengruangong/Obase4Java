/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Order运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:31:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

import java.util.Comparator;

/**
 * 表示Order运算
 */
public class OrderOp extends QueryOp {

    /**
     * 比较器，用于比较排序鍵的大小
     */
    private final Comparator<?> comparator;
    /**
     * 指示是否反序排列
     */
    private final boolean descending;
    /**
     * 鍵函数，用于从每个元素抽取排序鍵。
     */
    private final LambdaExpression keySelector;
    /**
     * 指示是否清除之前的排序结果
     */
    private boolean clearPrevious = true;

    /**
     * 创建OrderOp实例
     *
     * @param keySelector 鍵函数，用于从每个元素抽取排序鍵
     * @param comparer    比较器，用于比较排序鍵的大小
     */
    OrderOp(LambdaExpression keySelector, Comparator<?> comparer, ObjectDataModel model) {
        super(EQueryOpName.Order, keySelector.getParameters()[0].getType());

        this.keySelector = keySelector;
        this.comparator = comparer;
        this.descending = false;
        this.model = model;
    }

    /**
     * 创建OrderOp实例
     *
     * @param keySelector   鍵函数，用于从每个元素抽取排序鍵
     * @param descending    指示是否反序排列
     * @param clearPrevious 指示是否清除之前的排序结果
     * @param comparer      比较器，用于比较排序鍵的大小
     */
    OrderOp(LambdaExpression keySelector, boolean descending, boolean clearPrevious,
            Comparator<?> comparer, ObjectDataModel model) {
        super(EQueryOpName.Order, keySelector.getParameters()[0].getType());
        this.keySelector = keySelector;
        this.comparator = comparer;
        this.descending = descending;
        this.clearPrevious = clearPrevious;
        this.model = model;
    }

    /**
     * 获取一个值，该值指示是否清除之前的排序结果
     *
     * @return 是否清除之前的排序结果
     */
    public boolean getClearPrevious() {
        return this.clearPrevious;
    }

    /**
     * 获取比较器，该比较器用于比较排序鍵的大小
     *
     * @return 比较器
     */
    public Comparator<?> getComparator() {
        return this.comparator;
    }

    /**
     * 获取一个值，该值指示是否反序排列
     *
     * @return 是否反序排列
     */
    public boolean getDescending() {
        return this.descending;
    }

    /**
     * 获取鍵函数，该函数用于从每个元素抽取排序鍵
     *
     * @return 鍵函数
     */
    public LambdaExpression getKeySelector() {
        return this.keySelector;
    }

    /**
     * 获取排序键类型
     *
     * @return 排序键类型
     */
    public Class<?> getKeyType() {
        return this.keySelector.getBody().getType();
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

