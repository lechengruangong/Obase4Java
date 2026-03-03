/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结构化表示的连接字符串.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-4 12:37:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.AssemblyUtil;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StorageSymbol;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.QueryProvider;
import io.obase.core.query.heterog.HeterogQueryProvider;
import io.obase.core.saving.SavingProvider;

import java.util.*;

/**
 * 特定于功能的上下文配置提供程序
 */
public abstract class ContextConfigProvider {

    /**
     * 提供程序字典
     */
    private final Map<StorageSymbol, IStorageProvider> storageProviders = new HashMap<>();
    /**
     * 对象数据模型
     */
    protected ObjectDataModel model;
    /**
     * 查询提供程序
     */
    private QueryProvider queryProvider;
    /**
     * 保存提供程序
     */
    private SavingProvider savingProvider;
    /**
     * 所属于的上下文
     */
    private ObjectContext objectContext;

    /**
     * 获取所属于的上下文
     *
     * @return 所属于的上下文
     */
    public ObjectContext getObjectContext() {
        return this.objectContext;
    }

    /**
     * 设置所属于的上下文
     *
     * @param objectContext 所属于的上下文
     */
    public void setObjectContext(ObjectContext objectContext) {
        this.objectContext = objectContext;
    }

    /**
     * 获取保存提供程序
     *
     * @return 保存提供程序
     */
    public SavingProvider getSavingProvider() {
        if (this.savingProvider == null)
            this.savingProvider = new SavingProvider(this.createModel(), this::getStorageProvider, null);
        return this.savingProvider;
    }

    /**
     * 获取查询提供程序
     *
     * @return 查询提供程序
     */
    protected QueryProvider getQueryProvider() {
        if (this.queryProvider == null)
            this.queryProvider = new HeterogQueryProvider(this::getStorageProvider, this.createModel(), new IAttachObject() {
                /**
                 * 用于将对象附加到对象上下文的委托。
                 * 如果要附加斩对象在对象上下文中不存在，则附加该对象，否则将该对象合并至已存在的对象，并将参数的引用修改为已存在的对象。
                 *
                 * @param obj    对要附加的对象的引用
                 * @param asRoot 是否作为根对象
                 */
                @Override
                public <T> void attachObject(ObjectReferencePack<T> obj, boolean asRoot) {
                    ContextConfigProvider.this.getObjectContext().attach(obj, false, asRoot);
                }
            }, null, null, null, this.getObjectContext());
        return this.queryProvider;
    }

    /**
     * 获取存储提供者
     *
     * @return 存储提供者
     */
    public Map<StorageSymbol, IStorageProvider> getStorageProviders() {
        return this.storageProviders;
    }

    /**
     * 获取一个值，该值指示是否自动创建对象集
     * 此方法不可用
     *
     * @return 获取一个值，该值指示是否自动创建对象集
     */
    protected boolean getWhetherCreateSet() {
        return false;
    }

    /**
     * 创建模型方法
     *
     * @return 模型
     */
    ObjectDataModel createModel() {
        //如果关联引用_model已初始化，直接返回其值。
        // 否则，实例化一个ModelBuilder，然后调用CreateModel(ModelBuilder)，最后调用ModelBuilder.
        // Build方法建造模型，并以建造的模型初始化关联引用_model。

        if (this.objectContext != null && this.objectContext.model != null)
            this.model = this.objectContext.model;

        if (this.model != null)
            return this.model;

        ModelBuilder modelBuilder = new ModelBuilder(this.getObjectContext());

        //查找指定的扩展构件
        List<String> extList = new ArrayList<>();
        extList.add("io.obase.odm.annotation");
        extList.add("io.obase.logical.deletion");
        extList.add("io.obase.multi.tenant");

        List<Class<?>> extClasses = new ArrayList<>();
        for (String ext : extList) {
            Set<Class<?>> set = AssemblyUtil.getAllClassByPackageName(ext);
            extClasses.addAll(set);
        }

        //调用IAddonRegister进行注册
        for (Class<?> assemblyType : extClasses) {
            if (IAddonRegister.class.isAssignableFrom(assemblyType)) {
                try {
                    IAddonRegister register = (IAddonRegister) assemblyType.newInstance();
                    register.registry(modelBuilder);
                } catch (IllegalAccessException | InstantiationException e) {
                    //出现异常忽略即可
                }

            }
        }

        this.createModel(modelBuilder);
        this.model = modelBuilder.build(new StorageStructMappingExecutor(this::createStorageStructMappingProvider));

        return this.model;
    }

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 建模器
     */
    protected abstract void createModel(ModelBuilder modelBuilder);

    /**
     * 由派生类实现，获取指定存储标记对应的存储提供程序。
     *
     * @param symbol 存储标记
     * @return 存储提供程序
     */
    private IStorageProvider getStorageProvider(StorageSymbol symbol) {
        //调用CreateStorageProvider(StorageSymbol,
        // ObjectDataModel)生成提供程序实例。如果模型未生成，则调用CreateModel()方法生成模型。
        // 建立一个内部字典用于寄存已生成的提供程序实例，其键为StorageSymbol。调用CreateStorageProvider之
        // 前先查询该字典，如果已存在则直接返回。如果已开启本地事务，且所需提供程序在字典中不存在，
        // 则引发异常“你已启用本地事务，不能再创建另一个存储提供程序实例。如果需要多个存
        // 储提供程序实例，可以使用环境事务”。

        if (this.storageProviders.containsKey(symbol)) {
            return this.storageProviders.get(symbol);
        } else {
            //不存在
            if (this.storageProviders.values().stream().anyMatch(IStorageProvider::getTransactionBegun))
                throw new UnsupportedOperationException("你已启用本地事务，不能再创建另一个存储提供程序实例。如果需要多个存储提供程序实例，可以使用环境事务");
            ObjectDataModel model = this.createModel();
            IStorageProvider provider = this.createStorageProvider(symbol, model);
            this.storageProviders.put(symbol, provider);
            return provider;
        }
    }

    /**
     * 创建面向指定存储服务的存储结构映射提供程序
     *
     * @param storageSymbol 存储标记
     * @return 默认返回空
     */
    protected IStorageStructMappingProvider createStorageStructMappingProvider(StorageSymbol storageSymbol) {
        return null;
    }

    /**
     * 由派生类实现，创建指定存储标记对应的存储提供程序。
     *
     * @param symbol 存储标记
     * @param model  模型
     * @return 存储提供程序
     */
    protected abstract IStorageProvider createStorageProvider(StorageSymbol symbol, ObjectDataModel model);
}
