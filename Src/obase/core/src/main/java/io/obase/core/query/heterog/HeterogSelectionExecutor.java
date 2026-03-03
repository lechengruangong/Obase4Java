/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构投影（一般）运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:56:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IStorageProvider;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.odm.typeviews.AttachingInstanceSet;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.TypeViewAttachingItem;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异构投影（一般）运算执行器
 */
public class HeterogSelectionExecutor extends StandardHeterogOpExecutor {

    /**
     * 投影的结果视图
     */
    private TypeView heteroView;

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     * @param baseQueryProvider      基础查询提供器
     */
    public HeterogSelectionExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IBaseQueryProvider baseQueryProvider) {
        super(storageProviderCreator, model, preExecutionCallback, postExecutionCallback, baseQueryProvider);
    }

    /**
     * 由派生类实现，为指定的异构运算生成基础查询
     *
     * @param heterogOp     异构运算
     * @param heterogQuery  以异构运算作为末节点的异构查询
     * @param attachingRefs 返回对异构运算执行极限分解形成的附加引用
     * @return 基础查询
     */
    @Override
    protected QueryOp generateBaseOp(QueryOp heterogOp, QueryOp heterogQuery, ObjectReferencePack<ReferenceElement[]> attachingRefs) {
        if (heterogOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) heterogOp;
            this.heteroView = selectOp.getResultView();
            TypeView baseView = this.heteroView.getBaseView(null);
            SelectOp baseOp = QueryOp.select(baseView, this.model, null);

            attachingRefs.realValue = this.heteroView.getReferenceElements();
            return heterogQuery.replaceTail(baseOp);
        }
        return null;
    }

    /**
     * 由派生类实现，为指定的异构运算生成附加查询
     *
     * @param baseResult    基础查询的结果
     * @param heterogOp     异构运算
     * @param attachingRefs 返回生成的附加查询对应的附加引用，与方法返回值集合中的元素一一对应
     * @return 附加查询
     */
    @Override
    protected QueryOp[] generateAttachingQuery(Object baseResult, QueryOp heterogOp, ObjectReferencePack<ReferenceElement[]> attachingRefs) {
        TypeViewAttachingItem[] attachingItems = this.heteroView.getAttachedViews(null);

        List<QueryOp> resultOps = new ArrayList<>();
        List<ReferenceElement> attachingRefList = new ArrayList<>();

        if (attachingItems != null && attachingItems.length > 0) {
            for (TypeViewAttachingItem attachingItem : attachingItems) {
                SelectOp nextOp = QueryOp.select(attachingItem.getAttachingView(), this.model, null);
                Object[] sourceObjs;
                if (baseResult instanceof Object[]) {
                    sourceObjs = (Object[]) baseResult;
                } else {
                    sourceObjs = new Object[1];
                    sourceObjs[0] = baseResult;
                }
                resultOps.add(attachingItem.getAttachingReference().generateLoadingQuery(sourceObjs, nextOp));
                attachingRefList.add(attachingItem.getAttachingReference());
            }
        }

        attachingRefs.realValue = attachingRefList.toArray(new ReferenceElement[0]);
        return resultOps.toArray(new QueryOp[0]);
    }

    /**
     * 由派生类实现，合并基础查询与附加查询的结果
     *
     * @param baseResult       基础查询结果
     * @param attachingResults 各附加查询的结果，其顺序与GenerateAttachingQuery方法返回的附加查询的顺序一致
     * @param attachObject     附加对象委托
     * @param attachRoot       是否作为根对象附加
     * @return 合并结果
     */
    @Override
    protected Object combine(Object baseResult, Object[] attachingResults, IAttachObject attachObject, boolean attachRoot) {
        TypeViewAttachingItem[] attachingItems = this.heteroView.getAttachedViews(null);

        List<AttachingInstanceSet> itemResult = new ArrayList<>();
        for (TypeViewAttachingItem attachingItem : attachingItems) {
            AttachingInstanceSet attachingInstanceSet = new AttachingInstanceSet(attachingItem.getAttachingView(), attachingItem.getAttachingReference(), attachingResults);
            itemResult.add(attachingInstanceSet);
        }

        Object[] sourceObjs;
        if (baseResult instanceof Object[]) {
            sourceObjs = (Object[]) baseResult;
        } else {
            sourceObjs = new Object[]{baseResult};
        }
        Object[] result = this.heteroView.instantiate(sourceObjs, itemResult.toArray(new AttachingInstanceSet[0]));
        return this.processResult(result);
    }

    /**
     * 根据基础运算对包含树T进行裁剪
     *
     * @param includingTree 待裁剪的包含树
     * @param basicOp       作为裁剪依据的基础运算
     * @return 裁剪后的包含树
     */
    @Override
    protected AssociationTree cutIncluding(AssociationTree includingTree, QueryOp basicOp) {
        if (basicOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) basicOp;
            //此处为一般投影运算
            if (includingTree == null)
                return null;
            return includingTree.select(selectOp.getResultView());
        }
        return null;
    }

    /**
     * 转成List
     *
     * @param result 要处理的结合
     * @return 转换后的列表
     */
    private Object processResult(Object[] result) {
        if (result == null)
            return null;

        return Arrays.stream(result).collect(Collectors.toList());
    }
}
