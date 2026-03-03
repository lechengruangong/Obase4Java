/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构投影（退化）运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-30 12:24:55
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
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.AtrophyPath;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 异构投影（退化）运算执行器
 */
public class HeterogAtrophySelectionExecutor extends StandardHeterogOpExecutor {

    /**
     * 附加路径
     */
    private AtrophyPath attachingPath;

    /**
     * 附加引用
     */
    private ReferenceElement attachingReferenceElement;

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     * @param baseQueryProvider      基础查询提供器
     */
    public HeterogAtrophySelectionExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IBaseQueryProvider baseQueryProvider) {
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

            ObjectReferencePack<AtrophyPath> atrophyPaths = new ObjectReferencePack<>();
            ObjectReferencePack<AssociationTreeNode> associationTreeNodes = new ObjectReferencePack<>();
            ObjectReferencePack<ReferenceElement> referenceElements = new ObjectReferencePack<>();

            //极限分解退化路径
            AtrophyPath basePath =
                    selectOp.getAtrophyPath().decomposeExtremely(atrophyPaths, associationTreeNodes, referenceElements, null);
            this.attachingPath = atrophyPaths.realValue;
            this.attachingReferenceElement = referenceElements.realValue;

            QueryOp newTail = null;
            if (associationTreeNodes.realValue.getParent() != null)
                newTail = QueryOp.select(basePath, true, this.model, null);
            attachingRefs.realValue = new ReferenceElement[1];
            attachingRefs.realValue[0] = this.attachingReferenceElement;

            return heterogQuery.replace(heterogOp, newTail);
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
        SelectOp attachingOp = QueryOp.select(this.attachingPath, true, this.model, heterogOp.getNext());
        Object[] sourceObjs;
        if (baseResult instanceof Object[]) {
            sourceObjs = (Object[]) baseResult;
        } else {
            sourceObjs = new Object[1];
            sourceObjs[0] = baseResult;
        }

        QueryOp attachingQuery = this.attachingReferenceElement.generateLoadingQuery(sourceObjs, attachingOp);
        attachingRefs.realValue = new ReferenceElement[1];
        attachingRefs.realValue[0] = this.attachingReferenceElement;
        QueryOp[] result = new QueryOp[1];
        result[0] = attachingQuery;

        return result;
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
        for (Object obj : attachingResults) {
            if (attachRoot) {
                ObjectReferencePack<Object> local = new ObjectReferencePack<>();
                local.realValue = obj;
                if (this.model.getObjectType(obj.getClass()) != null)
                    attachObject.attachObject(local, true);
            }
        }

        return this.processResult(attachingResults, this.attachingPath.getAssociationPath().getRepresentedType().getClrType());
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
            return includingTree.select(selectOp.getAtrophyPath().getAssociationPath());
        }
        return null;
    }

    /**
     * 转成List
     *
     * @param result     目标数组
     * @param targetType 目标类型
     * @return 转换后的列表
     */
    private Object processResult(Object[] result, Class<?> targetType) {
        //targetType 用不到 类型已被擦除
        if (result == null)
            return null;

        return Arrays.stream(result).collect(Collectors.toList());
    }
}
