/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构筛选运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:08:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IStorageProvider;
import io.obase.core.IdentityArray;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.query.OrFactor;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;
import io.obase.core.query.WhereOp;

import java.util.*;

/**
 * 异构筛选运算执行器
 */
public class HeterogWhereExecutor extends HeterogOpExecutor {

    /**
     * 用于执行同构运算的执行器。在执行异构筛选运算过程中，需要执行两次同构运算：一是执行同构子筛选；二是执行异构子筛选中的基础查询。
     */
    private final HomogOpExecutor homogOpExecutor;

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     * @param baseQueryProvider      基础查询提供器
     */
    public HeterogWhereExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IBaseQueryProvider baseQueryProvider) {
        super(storageProviderCreator, model, preExecutionCallback, postExecutionCallback);

        this.homogOpExecutor = new HomogOpExecutor(storageProviderCreator, model, preExecutionCallback, postExecutionCallback, baseQueryProvider);
    }

    /**
     * 执行异构运算
     *
     * @param heterogOp    要执行的异构运算
     * @param heterogQuery 要执行的异构运算所在的查询链，它是该查询链的末节点
     * @param including    包含树
     * @return 执行结果
     */
    @Override
    public Object execute(QueryOp heterogOp, QueryOp heterogQuery, AssociationTree including, IAttachObject attachObject, boolean attachRoot) {
        this.homogOpExecutor.setHeterogQueryProvider(this.heterogQueryProvider);
        if (heterogOp instanceof WhereOp) {
            WhereOp whereOp = (WhereOp) heterogOp;

            //或因子分解
            OrFactor[] orFactors = whereOp.Decompose(this.model);
            //同构和异构拆开
            OrFactor[] homoFactors = Arrays.stream(orFactors).filter(p -> !p.getHeterogeneous()).toArray(OrFactor[]::new);
            OrFactor[] heterogFactors = Arrays.stream(orFactors).filter(OrFactor::getHeterogeneous).toArray(OrFactor[]::new);

            if (homoFactors.length > 0) {
                ReferringType sourceType = heterogFactors[0].getSourceType();
                List<Object> result = new ArrayList<>();

                HashMap<IdentityArray, Object> resultDictionary = new HashMap<>();
                for (OrFactor homoFactor : homoFactors) {
                    LambdaExpression predicate = homoFactor.toLambda();
                    WhereOp homoSubWhere = new WhereOp(predicate, this.model);
                    QueryOp homoSubQuery = heterogOp.replaceTail(homoSubWhere);
                    Object homogSubInstances = this.homogOpExecutor.execute(homoSubWhere, homoSubQuery, including, attachObject, attachRoot);
                    Object[] homogObjs = this.processInstances(homogSubInstances);

                    Map<IdentityArray, Object> tempDictionary = sourceType.makeDictionary(homogObjs, null);
                    for (IdentityArray key : tempDictionary.keySet()) {
                        resultDictionary.put(key, tempDictionary.get(key));
                    }
                }

                //异构查询
                for (OrFactor heterogFactor : heterogFactors) {
                    this.executeHeterogSub(heterogFactor, including, heterogQuery, resultDictionary, attachObject, attachRoot);
                    result.addAll(resultDictionary.values());
                }

                return this.processResult(result, sourceType.getClrType());
            }
        }

        return new Object[0];
    }

    /**
     * 执行异构子筛选
     *
     * @param heterogFactor 作为筛选条件的异构或因子
     * @param including     包含树
     * @param mainQuery     主查询
     * @param resultDict    存储结果集的字典
     */
    private void executeHeterogSub(OrFactor heterogFactor, AssociationTree including, QueryOp mainQuery,
                                   Map<IdentityArray, Object> resultDict, IAttachObject attachObject, boolean attachRoot) {
        //执行基础查询
        WhereOp whereOp = null;
        OrFactor baseFactor = heterogFactor.getBaseFactor();
        if (baseFactor != null) {
            LambdaExpression predicate = baseFactor.toLambda();
            whereOp = new WhereOp(predicate, this.model);
        }

        QueryOp baseQuery = mainQuery.replaceTail(whereOp);
        //一定为object[]
        Object[] baseInstances = this.processInstances(this.homogOpExecutor.execute(whereOp, baseQuery, including, attachObject, attachRoot));

        ReferringType sourceType = heterogFactor.getSourceType();

        ObjectReferencePack<ViewAttribute[]> checkAttrs = new ObjectReferencePack<>();
        checkAttrs.realValue = new ViewAttribute[0];
        //生成校验视图
        TypeView checkView = heterogFactor.generateCheckView(checkAttrs);
        this.model.addType(checkView);
        //生成校验投影运算
        SelectOp selectOp = QueryOp.select(checkView, this.model, null);
        //生成校验查询
        WhereOp checkQuery = sourceType.generateFilterQuery(Arrays.stream(baseInstances).filter(p -> !resultDict.containsKey(sourceType.getIdentity(p))).toArray(), selectOp);
        //执行查询
        this.heterogQueryProvider.setAttachRoot(false);
        Object checkViewInstances = this.heterogQueryProvider.execute(checkQuery, including);
        Object[] sourceObjs = this.processInstances(checkViewInstances);
        //装入字典
        Map<IdentityArray, Object> checkDict = sourceType.makeDictionary(sourceObjs, checkView);

        for (Object baseResult : baseInstances) {
            IdentityArray identity = sourceType.getIdentity(baseResult);
            if (checkDict.containsKey(identity)) {
                Object checkIntanse = checkDict.get(identity);
                boolean check = true;
                for (ViewAttribute attribute : checkAttrs.realValue) {
                    String baseInstanceNeedCheckAttr = null;
                    if (attribute != null && attribute.getSources() != null && attribute.getSources().length > 0) {
                        baseInstanceNeedCheckAttr = attribute.getSources()[0].getAttributeNode().getAttributeName();
                    }
                    if (baseInstanceNeedCheckAttr == null) continue;
                    boolean checkValue = Objects.equals(attribute.getValue(checkIntanse).toString(), sourceType.getAttribute(baseInstanceNeedCheckAttr).getValue(baseResult).toString());
                    if (!checkValue) {
                        check = false;
                        break;
                    }
                }

                //放入字典
                if (check) resultDict.put(identity, baseResult);
            }
        }

        //获取实例标识
        sourceType.getIdentity(baseInstances);
    }

    /**
     * 处理某个子查询结果
     *
     * @param instances 结果
     * @return 处理后的结果
     */
    private Object[] processInstances(Object instances) {
        //将对象查出
        if (instances instanceof Iterable) {
            Iterable<Object> iEnumerable = (Iterable<Object>) instances;
            List<Object> tempResult = new ArrayList<>();
            for (Object o : iEnumerable) {
                tempResult.add(o);
            }

            instances = tempResult.size() == 0 ? null : tempResult.toArray(new Object[0]);
        }

        if (instances == null)
            return new Object[0];

        if (instances instanceof Object[]) {
            return (Object[]) instances;
        }
        return new Object[]{instances};
    }

    /**
     * 转成List
     *
     * @param result 目标列表
     * @param type   目标类型
     * @return 列表
     */
    private Object processResult(List<Object> result, Class<?> type) {
        //无需处理 直接返回
        return result;
    }
}
