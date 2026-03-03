/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：同构运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:51:24
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
import io.obase.core.query.QueryOp;

/**
 * 将同构运算视为特殊的异构运算，定义特殊算法
 */
public final class HomogOpExecutor extends StandardHeterogOpExecutor {

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     * @param baseQueryProvider      基础查询提供器
     */
    public HomogOpExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IBaseQueryProvider baseQueryProvider) {
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
        //本身作为基础查询
        attachingRefs.realValue = new ReferenceElement[0];
        return heterogQuery != null ? heterogQuery : heterogOp;
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
        attachingRefs.realValue = new ReferenceElement[0];
        return new QueryOp[0];
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
            if (this.model.getObjectType(obj.getClass()) != null && attachRoot) {
                ObjectReferencePack<Object> local = new ObjectReferencePack<>();
                local.realValue = obj;
                attachObject.attachObject(local, true);
            }
        }

        //没有合并操作
        return baseResult;
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
        //提取隐含投影视图或投影链
        AssociationTree including;
        if (basicOp == null) {
            including = null;
        } else {
            including = basicOp.getImpliedIncluding() != null ? basicOp.getImpliedIncluding() : basicOp.getChainIncluding();
        }
        if (including != null && includingTree != null) {
            AssociationTree result = includingTree.select(including.getNode());
            if (result != null)
                return result;
        }

        return includingTree;
    }

    /**
     * 设置异构查询提供程序
     *
     * @param provider 异构查询提供程序
     */
    void setHeterogQueryProvider(HeterogQueryProvider provider) {
        this.heterogQueryProvider = provider;
    }
}
