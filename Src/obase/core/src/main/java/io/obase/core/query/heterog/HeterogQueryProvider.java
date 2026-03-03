/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构查询提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:07:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.IStorageProvider;
import io.obase.core.ObjectContext;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.HeterogeneityPredicationProvider;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.QueryContext;
import io.obase.core.query.QueryOp;
import io.obase.core.query.QueryProvider;
import io.obase.core.query.StorageHeterogeneityPredicationProvider;

/**
 * 异构查询提供程序
 */
public class HeterogQueryProvider extends QueryProvider {

    /**
     * 异构查询执行器
     */
    private final HeterogQueryDecomposer decomposer;

    /**
     * 一个委托，用于构造存储提供程序
     */
    private final FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator;

    /**
     * 分段执行器
     */
    private final IHeterogQuerySegmentallyExecutor segmentallyExecutor;

    /**
     * 断言器
     */
    private final HeterogeneityPredicationProvider heterogeneityPredicationProvider;

    /**
     * 基础查询提供者
     */
    private final IBaseQueryProvider baseQueryProvider;

    /**
     * 是否附加根
     */
    private boolean attachRoot = true;

    /**
     * 初始化HeterogQueryProvider类的新实例
     *
     * @param storageProviderCreator 一个委托，用于构造存储提供程序
     * @param model                  对象数据模型
     */
    public HeterogQueryProvider(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, IAttachObject attachObject,
                                HeterogeneityPredicationProvider heterogeneityPredicationProvider, IBaseQueryProvider baseQueryProvider, IHeterogQuerySegmentallyExecutor segmentallyExecutor, ObjectContext context) {
        super(model, attachObject, context);
        if (heterogeneityPredicationProvider == null)
            heterogeneityPredicationProvider = new StorageHeterogeneityPredicationProvider();
        this.heterogeneityPredicationProvider = heterogeneityPredicationProvider;
        if (baseQueryProvider == null)
            baseQueryProvider = new BaseQueryProvider(storageProviderCreator, model);
        this.baseQueryProvider = baseQueryProvider;
        this.decomposer = new HeterogQueryDecomposer(this.heterogeneityPredicationProvider);
        this.storageProviderCreator = storageProviderCreator;
        if (segmentallyExecutor == null)
            segmentallyExecutor = new HeterogQuerySegmentallyExecutor();
        this.segmentallyExecutor = segmentallyExecutor;
    }

    /**
     * 执行查询
     *
     * @param including 包含树
     * @param context   查询上下文
     * @param query     查询运算
     */
    @Override
    protected void execute(AssociationTree including, QueryContext context, QueryOp query) {
        if (query != null) {
            HeterogQuerySegments decomposeResult = query.accept(this.decomposer, including);
            Object result = this.segmentallyExecutor.execute(decomposeResult, this, this.attachObject, this.attachRoot);
            context.setResult(result);
        }
    }

    /**
     * 获取是否附加根
     *
     * @return 是否附加跟
     */
    public boolean getAttachRoot() {
        return this.attachRoot;
    }

    /**
     * 设置是否附加根
     *
     * @param attachRoot 是否附加根
     */
    public void setAttachRoot(boolean attachRoot) {
        this.attachRoot = attachRoot;
    }

    /**
     * 获取基础查询提供程序
     *
     * @return 基础查询提供程序
     */
    public IBaseQueryProvider getBaseQueryProvider() {
        return this.baseQueryProvider;
    }

    /**
     * 一个委托，用于构造存储提供程序。
     *
     * @return 构造存储提供程序委托
     */
    public FunctionWithOneArg<StorageSymbol, IStorageProvider> getStorageProviderCreator() {
        return this.storageProviderCreator;
    }
}
