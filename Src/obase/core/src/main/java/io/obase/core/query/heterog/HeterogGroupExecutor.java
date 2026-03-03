/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构分组运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:16:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IStorageProvider;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.query.GroupAggregationOp;
import io.obase.core.query.GroupOp;
import io.obase.core.query.IncludeOp;
import io.obase.core.query.QueryOp;
import io.obase.core.query.typeView.GroupingParser;

import java.util.Comparator;

/**
 * 异构分组运算执行器
 */
public class HeterogGroupExecutor extends HeterogOpExecutor {

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     */
    public HeterogGroupExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback) {
        super(storageProviderCreator, model, preExecutionCallback, postExecutionCallback);
    }

    /**
     * 执行异构运算
     *
     * @param heterogOp    要执行的异构运算
     * @param heterogQuery 要执行的异构运算所在的查询链，它是该查询链的末节点
     * @param including    包含树
     * @param attachObject 附加对象委托
     * @param attachRoot   是否附加为根对象
     * @return 执行结果
     */
    @Override
    public Object execute(QueryOp heterogOp, QueryOp heterogQuery, AssociationTree including, IAttachObject attachObject, boolean attachRoot) {
        if (heterogOp instanceof GroupOp) {
            GroupOp groupOp = (GroupOp) heterogOp;

            GroupingParser groupParser = new GroupingParser();
            TypeView typeView = groupParser.parse(heterogOp, this.model);

            LambdaExpression typeViewExp = typeView.generateExpression(new ObjectReferencePack<>());

            LambdaExpression resultSelector = null;
            if (groupOp instanceof GroupAggregationOp) {
                GroupAggregationOp groupAggregationOp = (GroupAggregationOp) groupOp;
                resultSelector = groupAggregationOp.getResultSelector();
            }
            Comparator<?> comparer = groupOp.getComparator();
            LambdaExpression elementSelector = this.getElementSelector(typeView, typeViewExp);
            LambdaExpression keySelector = this.getKeySelector(typeView, typeViewExp);

            QueryOp newOp = resultSelector == null ? QueryOp.groupBy(keySelector, elementSelector, comparer, this.model, null)
                    : QueryOp.groupBy(keySelector, elementSelector, resultSelector, comparer, this.model, null);
            newOp = QueryOp.select(typeView, this.model, newOp);

            if (including != null) {
                ObjectReferencePack<AssociationTreeNode> tail = new ObjectReferencePack<>();
                AssociationTree newIncluding = elementSelector.extractAssociation(this.model, tail, null);
                tail.realValue.addChild(including.getNode().getChildren());
                newOp = IncludeOp.create(newIncluding, this.model, newOp);
            }

            QueryOp newQuery = heterogQuery.replaceTail(newOp);
            this.heterogQueryProvider.setAttachRoot(false);
            return this.heterogQueryProvider.execute(newQuery, including);
        }

        return new Object[0];
    }

    /**
     * 获取键选择器
     *
     * @param typeView    类型视图
     * @param typeViewExp 类型视图表达式
     * @return 键选择器
     */
    private LambdaExpression getKeySelector(TypeView typeView, LambdaExpression typeViewExp) {
        Class<?> type = typeViewExp.getType();
        ParameterExpression parameterExp = Expression.parameter("p", type);
        Property property = Utils.getProperty(type, typeView.getAttributes().get(0).getName());
        MemberExpression memberExp = Expression.member(parameterExp, property.getGetterMethod(), parameterExp, parameterExp.getType());
        return Expression.lambda(new ParameterExpression[]{parameterExp}, memberExp);
    }

    /**
     * 根据视图获取组元素表达式
     *
     * @param typeView    类型视图
     * @param typeViewExp 类型视图表达式
     * @return 组元素表达式
     */
    private LambdaExpression getElementSelector(TypeView typeView, LambdaExpression typeViewExp) {
        Class<?> type = typeViewExp.getType();
        ParameterExpression parameterExp = Expression.parameter("p", type);
        Property property = Utils.getProperty(type, typeView.getAttributes().get(1).getName());
        MemberExpression memberExp = Expression.member(parameterExp, property.getGetterMethod(), parameterExp, parameterExp.getType());
        return Expression.lambda(new ParameterExpression[]{parameterExp}, memberExp);
    }
}
