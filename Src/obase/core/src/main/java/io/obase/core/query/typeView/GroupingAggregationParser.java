/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于分组聚合运算的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:36:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.objectSys.EParameterReferring;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.query.GroupAggregationOp;
import io.obase.core.query.QueryOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定于分组聚合运算的视图查询解析器。
 * 分组聚合运算是指GroupBy(keySelector, elementSelector, resultSelector)，其中：
 * （1）keySelector为键选择器，类型为，类型为Func`2[TSource, TKey];
 * （2）elementSelector为组元素选择器，为可选参数，类型为Func`2[TSource, TElement];
 * （3）resultSelector为投影函数，当eleementSelector为空时其类型为Func`3[TKey, IEnumerable`1[TSource], TResult]，否则其类型为Func`3[TKey,
 * IEnumerable`1[TElement], TResult]。
 */
public class GroupingAggregationParser extends ExpressionBasedViewQueryParser {
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

        if (queryOp instanceof GroupAggregationOp) {
            GroupAggregationOp groupAggregationOp = (GroupAggregationOp) queryOp;
            bindings.add(new ParameterBinding(groupAggregationOp.getResultSelector().getParameters()[0],
                    groupAggregationOp.getKeySelector().getBody())); //resultSelector第一个形参绑定到keySelector；
            if (groupAggregationOp.getResultSelector().getParameters().length == 2) {
                //elementSelector不存在
                if (groupAggregationOp.getElementSelector() == null)
                    //resultSelector第二个形参指代到Sequence
                    bindings.add(new ParameterBinding(groupAggregationOp.getResultSelector().getParameters()[1],
                            EParameterReferring.Sequence, null));
                    //elementSelector存在
                else
                    //resultSelector第二个形参绑定到elementSelector并指代Sequence
                    bindings.add(new ParameterBinding(groupAggregationOp.getResultSelector().getParameters()[1], EParameterReferring.Sequence,
                            groupAggregationOp.getElementSelector().getBody()));
            }
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
        if (queryOp instanceof GroupAggregationOp) {
            GroupAggregationOp groupAggregationOp = (GroupAggregationOp) queryOp;
            return groupAggregationOp.getResultSelector();
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
        if (queryOp instanceof GroupAggregationOp) {
            GroupAggregationOp groupAggregationOp = (GroupAggregationOp) queryOp;
            return groupAggregationOp.getResultType();
        }
        return null;
    }
}
