/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于实例化投影运算的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:32:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.objectSys.EParameterReferring;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.query.CollectionSelectOp;
import io.obase.core.query.CombiningSelectOp;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定于实例化投影运算的视图查询解析器。
 * 实例化投影运算是指投影函数的Body为New或MemberInit表达式的投影运算。
 * 它有一个可选的集合选择器参数collectionSelector和一个投影函数参数resultSelector，其中：
 * （1）collectionSelector的类型为Func`2[TSource, IEnumerable`1[TCollection]] 或Func`3[TSource, Int32,
 * IEnumerable`1[TCollection]]；
 * （2）resultSelector的类型可能为Func`2[TSource, TResult]、Func`3[TSource, Int32, TResult]或Func`3[TSource, TCollection,
 * TResult]（仅当collectionSelector存在）。
 */
public class NewSelectionParser extends ExpressionBasedViewQueryParser {

    /**
     * 从查询运算抽取代表平展点的表达式
     *
     * @param queryOp        要解析的查询运算
     * @param flatteningPara 返回平展形参
     * @return 代表平展点的表达式
     */
    @Override
    protected LambdaExpression extractFlatteningExpression(QueryOp queryOp, ObjectReferencePack<ParameterExpression> flatteningPara) {

        flatteningPara.realValue = null;
        if (queryOp instanceof CollectionSelectOp) {
            CollectionSelectOp collectionSelectOp = (CollectionSelectOp) queryOp;
            flatteningPara.realValue = collectionSelectOp.getCollectionSelector().getParameters()[0];
            return collectionSelectOp.getCollectionSelector();
        }

        return null;
    }

    /**
     * 从查询运算抽取视图的标识属性
     *
     * @param queryOp 要解析的查询运算
     * @return 标识属性
     */
    @Override
    protected String[] extractKeyAttributes(QueryOp queryOp) {
        return new String[0];
    }

    /**
     * 从查询运算抽取视图表达式涉及的形参绑定
     *
     * @param queryOp 要解析的查询运算
     * @return 形参绑定
     */
    @Override
    protected ParameterBinding[] extractParameterBinding(QueryOp queryOp) {
        List<ParameterBinding> bindings = new ArrayList<>();

        if (queryOp instanceof CollectionSelectOp) {
            CollectionSelectOp collectionSelectOp = (CollectionSelectOp) queryOp;
            if (collectionSelectOp.getCollectionSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(collectionSelectOp.getCollectionSelector().getParameters()[1],
                        EParameterReferring.Index, null));
            if (collectionSelectOp.getResultSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(collectionSelectOp.getResultSelector().getParameters()[1],
                        collectionSelectOp.getCollectionSelector().getBody()));
        } else if (queryOp instanceof CombiningSelectOp) {
            CombiningSelectOp combiningSelectOp = (CombiningSelectOp) queryOp;
            if (combiningSelectOp.getResultSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(combiningSelectOp.getResultSelector().getParameters()[1],
                        EParameterReferring.Index, null));
        }

        return bindings.toArray(new ParameterBinding[0]);
    }

    /**
     * 从查询运算抽取描述视图结构的Lambda表达式（简称视图表达式），后续将据此表达式构造TypeView实例。
     *
     * @param queryOp  要解析的查询运算
     * @param viewType 视图的CLR类型
     * @return 视图表达式
     */
    @Override
    protected LambdaExpression extractViewExpression(QueryOp queryOp, Class<?> viewType) {
        if (queryOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) queryOp;
            return selectOp.getResultSelector();
        }
        return null;
    }

    /**
     * 从查询运算抽取视图源的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的CLR类型
     */
    @Override
    protected Class<?> extractSourceType(QueryOp queryOp) {
        return queryOp.getSourceType();
    }

    /**
     * 从查询运算抽取视图的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的视图CLR类型
     */
    @Override
    protected Class<?> extractViewType(QueryOp queryOp) {
        if (queryOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) queryOp;
            return selectOp.getResultType();
        }
        return null;
    }
}

