/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于多重投影运算的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:54:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.expression.*;
import io.obase.core.odm.FieldDescriptor;
import io.obase.core.odm.ImpliedTypeManager;
import io.obase.core.odm.objectSys.EParameterReferring;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.query.CollectionSelectOp;
import io.obase.core.query.CombiningSelectOp;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定于多重投影运算的视图查询解析器。
 * 多重投影是指投影到一个具有多重性的引用元素或其下级元素（下级元素不要求多重性）的运算。
 * 下级元素是指关联树中代表当前元素的节点的后代所代表的元素，或者是当前节点或其后代所含属性树节点所代表的属性。
 * 它有一个可选的集合选择器参数collectionSelector和一个投影函数参数resultSelector，其中：
 * （1）collectionSelector的类型为Func`2[TSource, IEnumerable[TCollection]] 或Func`3[TSource, Int32,
 * IEnumerable[TCollection]]；
 * （2）resultSelector的类型可能为Func`2[TSource, TResult]、Func`3[TSource, Int32, TResult] 或Func`3[TSource, TCollection,
 * TResult]（仅当collectionSelector存在）。
 */
public class MultipleSelectionParser extends ExpressionBasedViewQueryParser {
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

        if (queryOp instanceof CombiningSelectOp) {
            CombiningSelectOp combiningSelectOp = (CombiningSelectOp) queryOp;
            if (combiningSelectOp.getResultSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(combiningSelectOp.getResultSelector().getParameters()[1],
                        EParameterReferring.Index, null));
        } else if (queryOp instanceof CollectionSelectOp) {
            CollectionSelectOp collectionSelectOp = (CollectionSelectOp) queryOp;

            if (collectionSelectOp.getResultSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(collectionSelectOp.getResultSelector().getParameters()[1],
                        collectionSelectOp.getCollectionSelector().getBody()));

            if (collectionSelectOp.getCollectionSelector().getParameters().length == 2)
                bindings.add(new ParameterBinding(collectionSelectOp.getCollectionSelector().getParameters()[1],
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
        if (!(queryOp instanceof SelectOp))
            throw new IllegalArgumentException("MultipleSelectionParser视图查询解析器只能解析SelectOp");

        SelectOp selectOp = (SelectOp) queryOp;

        //创建隐含视图
        Class<?> baseType = MultipleSelectionResult.class;
        ParameterBinding[] bindings = this.extractParameterBinding(queryOp);
        FieldDescriptor viewRef = new FieldDescriptor(selectOp.getResultSelector(), bindings);
        viewRef.setHasGetter(true);
        viewRef.setHasSetter(true);

        FieldDescriptor[] fields = new FieldDescriptor[1];
        fields[0] = viewRef;

        IdentityArray subIdentity = new IdentityArray(selectOp.getSourceType().getName() + "[" + selectOp.getResultSelector().getType().getName() + "]" + "[" + viewRef.getValueExpression().getType().getName() + "]");
        Class<?> typeImpliedView = ImpliedTypeManager.getCurrent().applyType(baseType, fields, subIdentity, null);

        /*2.初始化成员表达式*/
        NewExpression newExpression = Expression.news(typeImpliedView);
        newExpression.setConstructor(typeImpliedView.getConstructors()[0]);

        final int[] index = {0};

        //视图属性
        MemberAssignment refBindExp = Expression.bind(getViewRefMethod(viewRef, fields, typeImpliedView, index), selectOp.getResultSelector().getBody());
        MemberInitExpression memberInitExp = Expression.memberInit(newExpression, new MemberBinding[]{refBindExp});

        return Expression.lambda(selectOp.getResultSelector().getParameters(), memberInitExp);
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
