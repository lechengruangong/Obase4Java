/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:33:43
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.core.IStorageProvider;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

/**
 * 异构运算执行器
 */
public abstract class HeterogOpExecutor {

    /**
     * 对象数据模型
     */
    protected final ObjectDataModel model;

    /**
     * 执行后回调委托
     */
    protected final ActionWithOneArg<QueryEventArgs> postExecutionCallback;

    /**
     * 执行前回调委托
     */
    protected final ActionWithOneArg<QueryEventArgs> preExecutionCallback;

    /**
     * 用于构造存储提供程序的委托
     */
    protected final FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator;

    /**
     * 异构查询提供程序器
     */
    protected HeterogQueryProvider heterogQueryProvider;

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     */
    protected HeterogOpExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model,
                                ActionWithOneArg<QueryEventArgs> preExecutionCallback,
                                ActionWithOneArg<QueryEventArgs> postExecutionCallback) {
        this.storageProviderCreator = storageProviderCreator;
        this.model = model;
        this.preExecutionCallback = preExecutionCallback;
        this.postExecutionCallback = postExecutionCallback;
    }

    /**
     * 为指定的异构运算创建执行器
     *
     * @param heterogOp              要执行的异构运算
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行前回调委托
     * @param postExecutionCallback  执行后回调委托
     * @param heterogQueryProvider   异构运算执行器的创建者
     * @param baseQueryProvider      基础查询提供程序
     * @return 异构运算执行器
     */
    public static HeterogOpExecutor create(QueryOp heterogOp,
                                           FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model,
                                           ActionWithOneArg<QueryEventArgs> preExecutionCallback,
                                           ActionWithOneArg<QueryEventArgs> postExecutionCallback, HeterogQueryProvider heterogQueryProvider, IBaseQueryProvider baseQueryProvider) {
        QueryOp tail = heterogOp.getTail();

        //是否为异构的
        if (!tail.getHeterogeneous(null)) {
            //不是异构的 返回同构执行器
            HomogOpExecutor result = new HomogOpExecutor(storageProviderCreator, model, preExecutionCallback,
                    postExecutionCallback, baseQueryProvider);
            result.heterogQueryProvider = heterogQueryProvider;
            return result;
        }

        switch (tail.getName()) {

            case Select: {
                if (tail instanceof SelectOp) {
                    SelectOp selectOp = (SelectOp) tail;

                    HeterogOpExecutor result;
                    if (selectOp.getIsNew()) {
                        result = new HeterogSelectionExecutor(storageProviderCreator, model,
                                preExecutionCallback,
                                postExecutionCallback, baseQueryProvider);
                    } else {
                        result = new HeterogAtrophySelectionExecutor(storageProviderCreator, model,
                                preExecutionCallback,
                                postExecutionCallback, baseQueryProvider);
                    }
                    result.heterogQueryProvider = heterogQueryProvider;
                    return result;
                }
                throw new IllegalArgumentException("创建异构执行器失败:操作对应的类型不符");
            }
            case Where: {
                HeterogWhereExecutor result = new HeterogWhereExecutor(storageProviderCreator, model, preExecutionCallback,
                        postExecutionCallback, baseQueryProvider);
                result.heterogQueryProvider = heterogQueryProvider;
                return result;
            }
            case Group: {

                HeterogGroupExecutor result = new HeterogGroupExecutor(storageProviderCreator, model, preExecutionCallback,
                        postExecutionCallback);
                result.heterogQueryProvider = heterogQueryProvider;
                return result;
            }
            default:
                throw new IllegalArgumentException("创建异构执行器失败:" + tail.getName() + "无对应的异构运算执行器.");
        }
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
    public abstract Object execute(QueryOp heterogOp, QueryOp heterogQuery, AssociationTree including, IAttachObject attachObject, boolean attachRoot);
}
