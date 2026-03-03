/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示集合中介投影运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：22025-12-30 11:53:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.typeviews.TypeView;

/**
 * 表示集合中介投影运算。
 * 集合中介投影是指，首先投影到一个多重性元素，然后对每个投影结果中的每个元素再次投影，得到最终结果。
 */
public class CollectionSelectOp extends SelectOp {

    /**
     * 中介投影函数
     */
    private final LambdaExpression collectionSelector;

    /**
     * 创建CollectionSelectOp实例
     *
     * @param resultSelector     结果投影函数
     * @param collectionSelector 中介投影函数
     */
    CollectionSelectOp(LambdaExpression resultSelector, LambdaExpression collectionSelector, ObjectDataModel model) {
        super(resultSelector, model);
        this.collectionSelector = collectionSelector;
    }

    /**
     * 创建表示退化投影运算的CollectionSelectOp实例
     *
     * @param atrophyPath 退化路径
     */
    CollectionSelectOp(AtrophyPath atrophyPath, ObjectDataModel model) {
        super(atrophyPath, model);
        this.collectionSelector = null;
    }

    /**
     * 创建表示一般投影运算的CollectionSelectOp实例
     *
     * @param resultView 投影结果视图
     */
    CollectionSelectOp(TypeView resultView, ObjectDataModel model) {
        super(resultView, model);
        this.collectionSelector = null;
    }

    /**
     * 获取中介投影函数
     *
     * @return 中介投影函数
     */
    public LambdaExpression getCollectionSelector() {
        return this.collectionSelector;
    }

    /**
     * 获取中介投影结果类型
     *
     * @return 中介投影结果类型
     */
    public Class<?> getElementType() {
        return this.collectionSelector.getBody().getType();
    }

    /**
     * 获取一个值，该值指示投影运算是否将元素在序列中的索引作为（第二个）参数
     *
     * @return 指示投影运算是否将元素在序列中的索引作为（第二个）参数
     */
    @Override
    public boolean getIndexReferred() {
        boolean indexReferred = false;
        if (this.collectionSelector != null) {
            ParameterExpression[] parameterExpressions = this.collectionSelector.getParameters();
            if (parameterExpressions.length == 2 && parameterExpressions[1].getType() == int.class)
                indexReferred = true;
        }
        return indexReferred;
    }
}
