/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：保存提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-17 16:59:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.FunctionWithTwoArgs;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IMappingWorkflow;
import io.obase.core.IStorageProvider;
import io.obase.core.common.EIsolationLevel;
import io.obase.core.common.EventHandler;
import io.obase.core.common.Transaction;
import io.obase.core.common.TransactionScope;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.mapping.pipeline.*;
import io.obase.core.odm.*;
import org.jinq.tuples.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 保存提供程序
 */
public class SavingProvider implements ISavingPipeline, IDeletingPipeline, IDirectlyChangingPipeline {

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 一个委托，用于构造存储提供程序
     */
    private final Function<StorageSymbol, IStorageProvider> storageProviderCreator;

    /**
     * 实施持久化的存储提供程序
     */
    private final Map<StorageSymbol, IStorageProvider> storageProviders = new HashMap<>();

    /**
     * 存储标记判定器
     */
    private IStorageSymbolJudge storageSymbolJudge = new StorageSymbolJudge();

    /**
     * DeletingPreExecuteCommand事件
     */
    private EventHandler<PreExecuteCommandEventArgs> deletingPreExecuteCommand;

    /**
     * DeletingPostExecuteCommand事件
     */
    private EventHandler<PostExecuteCommandEventArgs> deletingPostExecuteCommand;

    /**
     * BeginDeleting事件
     */
    private EventHandler<EventObject> beginDeleting;

    /**
     * PostGenerateGroup事件
     */
    private EventHandler<EventObject> postGenerateGroup;

    /**
     * BeginDeletingGroup事件
     */
    private EventHandler<BeginDeletingGroupEventArgs> beginDeletingGroup;

    /**
     * EndDeletingGroup事件
     */
    private EventHandler<EndDeletingGroupEventArgs> endDeletingGroup;

    /**
     * EndDeleting事件
     */
    private EventHandler<EventObject> endDeleting;

    /**
     * DirectlyChangingPreExecuteCommand事件
     */
    private EventHandler<PreExecuteCommandEventArgs> directlyChangingPreExecuteCommand;

    /**
     * DirectlyChangingPostExecuteCommand事件
     */
    private EventHandler<PostExecuteCommandEventArgs> directlyChangingPostExecuteCommand;

    /**
     * BeginDirectlyChanging事件
     */
    private EventHandler<BeginDirectlyChangingEventArgs> beginDirectlyChanging;

    /**
     * EndDirectlyChanging事件
     */
    private EventHandler<EndDirectlyChangingEventArgs> endDirectlyChanging;

    /**
     * SavingPreExecuteCommand事件
     */
    private EventHandler<PreExecuteCommandEventArgs> savingPreExecuteCommand;

    /**
     * SavingPostExecuteCommand事件
     */
    private EventHandler<PostExecuteCommandEventArgs> savingPostExecuteCommand;

    /**
     * BeginSaving事件
     */
    private EventHandler<EventObject> beginSaving;

    /**
     * PostGenerateQueue事件
     */
    private EventHandler<EventObject> postGenerateQueue;

    /**
     * BeginSavingUnit事件
     */
    private EventHandler<BeginSavingUnitEventArgs> beginSavingUnit;

    /**
     * EndSavingUnit事件
     */
    private EventHandler<EndSavingUnitEventArgs> endSavingUnit;

    /**
     * EndSaving事件
     */
    private EventHandler<EventObject> endSaving;

    /**
     * 创建SavingProvider实例
     *
     * @param model                  对象数据模型
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param judge                  存储判定
     */
    public SavingProvider(ObjectDataModel model, Function<StorageSymbol, IStorageProvider> storageProviderCreator, IStorageSymbolJudge judge) {
        this.storageProviderCreator = storageProviderCreator;
        this.model = model;
        if (judge != null)
            this.storageSymbolJudge = judge;
    }

    /**
     * 一个委托，用于构造存储提供程序
     *
     * @return 构造存储提供程序委托
     */
    public Function<StorageSymbol, IStorageProvider> getStorageProviderCreator() {
        return this.storageProviderCreator;
    }

    /**
     * 返回为PreExecuteSql事件附加或移除事件处理程序的EventHandler
     *
     * @return PreExecuteSql事件
     */
    @Override
    public EventHandler<PreExecuteCommandEventArgs> getDeletingPreExecuteCommand() {
        if (this.deletingPreExecuteCommand == null)
            this.deletingPreExecuteCommand = new EventHandler<>();
        return this.deletingPreExecuteCommand;
    }

    /**
     * 返回为PostExecuteSql事件附加或移除事件处理程序的EventHandler
     *
     * @return 为PostExecuteSql事件
     */
    @Override
    public EventHandler<PostExecuteCommandEventArgs> getDeletingPostExecuteCommand() {
        if (this.deletingPostExecuteCommand == null)
            this.deletingPostExecuteCommand = new EventHandler<>();
        return this.deletingPostExecuteCommand;
    }

    /**
     * 为BeginDeleting事件附加或移除事件处理程序
     *
     * @return BeginDeleting事件
     */
    @Override
    public EventHandler<EventObject> getBeginDeleting() {
        if (this.beginDeleting == null)
            this.beginDeleting = new EventHandler<>();
        return this.beginDeleting;
    }

    /**
     * 为PostGenerateGroup事件附加或移除事件处理程序
     *
     * @return PostGenerateGroup事件
     */
    @Override
    public EventHandler<EventObject> getPostGenerateGroup() {
        if (this.postGenerateGroup == null)
            this.postGenerateGroup = new EventHandler<>();
        return this.postGenerateGroup;
    }

    /**
     * 为BeginDeletingGroup事件附加或移除事件处理程序
     *
     * @return BeginDeletingGroup事件
     */
    @Override
    public EventHandler<BeginDeletingGroupEventArgs> getBeginDeletingGroup() {
        if (this.beginDeletingGroup == null)
            this.beginDeletingGroup = new EventHandler<>();
        return this.beginDeletingGroup;
    }

    /**
     * 为EndDeletingGroup事件附加或移除事件处理程序
     *
     * @return EndDeletingGroup事件
     */
    @Override
    public EventHandler<EndDeletingGroupEventArgs> getEndDeletingGroup() {
        if (this.endDeletingGroup == null)
            this.endDeletingGroup = new EventHandler<>();
        return this.endDeletingGroup;
    }

    /**
     * 为EndDeleting事件附加或移除事件处理程序
     *
     * @return EndDeleting事件
     */
    @Override
    public EventHandler<EventObject> getEndDeleting() {
        if (this.endDeleting == null)
            this.endDeleting = new EventHandler<>();
        return this.endDeleting;
    }

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    @Override
    public EventHandler<PreExecuteCommandEventArgs> getDirectlyChangingPreExecuteCommand() {
        if (this.directlyChangingPreExecuteCommand == null)
            this.directlyChangingPreExecuteCommand = new EventHandler<>();
        return this.directlyChangingPreExecuteCommand;
    }

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    @Override
    public EventHandler<PostExecuteCommandEventArgs> getDirectlyChangingPostExecuteCommand() {
        if (this.directlyChangingPostExecuteCommand == null)
            this.directlyChangingPostExecuteCommand = new EventHandler<>();
        return this.directlyChangingPostExecuteCommand;
    }

    /**
     * 为BeginDirectlyChanging事件附加或移除事件处理程序
     *
     * @return BeginDirectlyChanging事件
     */
    @Override
    public EventHandler<BeginDirectlyChangingEventArgs> getBeginDirectlyChanging() {
        if (this.beginDirectlyChanging == null)
            this.beginDirectlyChanging = new EventHandler<>();
        return this.beginDirectlyChanging;
    }

    /**
     * 为EndDirectlyChanging事件附加或移除事件处理程序
     *
     * @return EndDirectlyChanging事件
     */
    @Override
    public EventHandler<EndDirectlyChangingEventArgs> getEndDirectlyChanging() {
        if (this.endDirectlyChanging == null)
            this.endDirectlyChanging = new EventHandler<>();
        return this.endDirectlyChanging;
    }

    /**
     * 为PreExecuteSql事件附加或移除事件处理程序
     *
     * @return PreExecuteSql事件
     */
    @Override
    public EventHandler<PreExecuteCommandEventArgs> getSavingPreExecuteCommand() {
        if (this.savingPreExecuteCommand == null)
            this.savingPreExecuteCommand = new EventHandler<>();
        return this.savingPreExecuteCommand;
    }

    /**
     * 为PostExecuteSql事件附加或移除事件处理程序
     *
     * @return PostExecuteSql事件
     */
    @Override
    public EventHandler<PostExecuteCommandEventArgs> getSavingPostExecuteCommand() {
        if (this.savingPostExecuteCommand == null)
            this.savingPostExecuteCommand = new EventHandler<>();
        return this.savingPostExecuteCommand;
    }

    /**
     * 为BeginSaving事件附加或移除事件处理程序
     *
     * @return BeginSaving事件
     */
    @Override
    public EventHandler<EventObject> getBeginSaving() {
        if (this.beginSaving == null)
            this.beginSaving = new EventHandler<>();
        return this.beginSaving;
    }

    /**
     * 为PostGenerateQueue事件附加或移除事件处理程序
     *
     * @return PostGenerateQueue事件
     */
    @Override
    public EventHandler<EventObject> getPostGenerateQueue() {
        if (this.postGenerateQueue == null)
            this.postGenerateQueue = new EventHandler<>();
        return this.postGenerateQueue;
    }

    /**
     * 为BeginSavingUnit事件附加或移除事件处理程序
     *
     * @return BeginSavingUnit事件
     */
    @Override
    public EventHandler<BeginSavingUnitEventArgs> getBeginSavingUnit() {
        if (this.beginSavingUnit == null)
            this.beginSavingUnit = new EventHandler<>();
        return this.beginSavingUnit;
    }

    /**
     * 为EndSavingUnit事件附加或移除事件处理程序
     *
     * @return EndSavingUnit事件
     */
    @Override
    public EventHandler<EndSavingUnitEventArgs> getEndSavingUnit() {
        if (this.endSavingUnit == null)
            this.endSavingUnit = new EventHandler<>();
        return this.endSavingUnit;
    }

    /**
     * 为EndSaving事件附加或移除事件处理程序
     *
     * @return EndSaving事件
     */
    @Override
    public EventHandler<EventObject> getEndSaving() {
        if (this.endSaving == null)
            this.endSaving = new EventHandler<>();
        return this.endSaving;
    }

    /**
     * 将对象的当前状态持久化至存储服务
     *
     * @param added                   新增的对象
     * @param modified                已修改过的对象
     * @param deleted                 已删除的对象
     * @param addedComps              新增的伴随关联
     * @param deletedComps            已删除的伴随关联
     * @param attrHasChanged          一个委托，用于探测对象的属性是否已更改
     * @param attrOriginalValueGetter 用于获取属性原值的委托
     */
    public void save(List<Object> added, List<Object> modified, List<Object> deleted, List<Object> addedComps,
                     List<Object> deletedComps, FunctionWithTwoArgs<Object, String, Boolean> attrHasChanged,
                     IGetAttributeValue attrOriginalValueGetter) {

        //准备存储提供程序
        this.prepareStorageProvider(added, modified, deleted, addedComps, deletedComps);

        //是否在我开启事务前已经开启了事务
        boolean isOutTrBegun = this.storageProviders.values().stream().anyMatch(IStorageProvider::getTransactionBegun);

        //当前的环境事务
        TransactionScope transactionScope = null;

        if (added.size() + modified.size() + deleted.size() > 1 || addedComps.size() > 0 || deletedComps.size() > 0) {
            //开启事务
            ObjectReferencePack<TransactionScope> pack = new ObjectReferencePack<>();
            this.beginTransaction(pack);
            transactionScope = pack.realValue;
        } else {
            //一个对象 开普通的本地事务
            for (IStorageProvider transaction : this.storageProviders.values()) {
                if (!isOutTrBegun)
                    transaction.beginTransaction(EIsolationLevel.TRANSACTION_READ_COMMITTED);
            }
        }

        try {
            //处理具体的操作
            if (addedComps != null && addedComps.size() > 0) {
                this.saveNew(added);
                this.saveOld(modified, addedComps, deletedComps, attrHasChanged, attrOriginalValueGetter);
                this.delete(deleted);
            } else {
                this.saveOld(modified, addedComps, deletedComps, attrHasChanged, attrOriginalValueGetter);
                this.delete(deleted);
                this.saveNew(added);
            }

            for (IStorageProvider transaction : this.storageProviders.values()) {
                if (!isOutTrBegun)
                    transaction.commitTransaction();
            }

            if (transactionScope != null) {
                transactionScope.complete();
            }

        } catch (Exception ex) {

            for (IStorageProvider transaction : this.storageProviders.values()) {
                transaction.rollbackTransaction();
            }
            throw ex;
        } finally {
            for (IStorageProvider storageProvider : this.storageProviders.values()) {
                if (!isOutTrBegun)
                    storageProvider.releaseResource();
            }

            if (transactionScope != null) {
                transactionScope.close();
            }
        }
    }

    /**
     * 开启事务
     *
     * @param transactionScope 事务块
     */
    private void beginTransaction(ObjectReferencePack<TransactionScope> transactionScope) {
        //已开启环境事务
        if (Transaction.getInstance() != null) {
            //环境事务
            this.enlistTransaction(transactionScope);
        } else {
            //如果只有一个存储提供程序
            if (this.storageProviders.size() <= 1) {
                IStorageProvider first = this.storageProviders.values().stream().findFirst().orElse(null);
                if (first instanceof ITransactionable) {
                    ITransactionable transactionable = (ITransactionable) first;
                    transactionable.beginTransaction(EIsolationLevel.TRANSACTION_READ_COMMITTED);
                }
            }
            //多个 登记环境事务
            else {
                this.enlistTransaction(transactionScope);
            }
        }
    }

    /**
     * 登记事务
     *
     * @param transactionScope 事务块
     */
    private void enlistTransaction(ObjectReferencePack<TransactionScope> transactionScope) {
        for (IStorageProvider provider : this.storageProviders.values()) {
            if (provider instanceof IAmbientTransactionable) {
                IAmbientTransactionable ambientTransactionable = (IAmbientTransactionable) provider;
                if (transactionScope.realValue == null && Transaction.getInstance() == null)
                    transactionScope.realValue = new TransactionScope();
                ambientTransactionable.enlistTransaction();
            }
        }
    }

    /**
     * 根据待保存的对象准备存储提供程序
     *
     * @param added        新增的对象
     * @param modified     已修改过的对象
     * @param deleted      已删除的对象
     * @param addedComps   新增的伴随关联
     * @param deletedComps 已删除的伴随关联
     */
    private void prepareStorageProvider(List<Object> added, List<Object> modified, List<Object> deleted,
                                        List<Object> addedComps, List<Object> deletedComps) {

        List<Object> total = new ArrayList<>();
        total.addAll(added);
        total.addAll(modified);
        total.addAll(deleted);
        total.addAll(addedComps);
        total.addAll(deletedComps);

        Iterator<Object> enumerator = total.iterator();
        this.createStorageProvider(enumerator);
    }

    /**
     * 通过枚举器实现挨个处理
     *
     * @param enumerator 对象集合
     */
    private void createStorageProvider(Iterator<Object> enumerator) {
        while (enumerator.hasNext()) {
            Object current = enumerator.next();
            if (current != null) {
                //获取对象类型
                ObjectType objectType = this.model.getObjectType(current.getClass());
                this.generateSymbolByObjectType(current, objectType);
            }
        }
    }

    /**
     * 根据ObjectType获取存储提供器
     *
     * @param objectType 对象类型
     */
    private void generateSymbolByObjectType(Object obj, ObjectType objectType) {
        StorageSymbol storageSymbol = this.storageSymbolJudge.judge(obj, objectType);
        //不存在 添加一个新的
        if (!this.storageProviders.containsKey(storageSymbol)) {
            IStorageProvider storageProvider = this.storageProviderCreator.apply(storageSymbol);
            this.storageProviders.put(storageSymbol, storageProvider);
        }
    }

    /**
     * 根据ObjectType获取存储提供器
     *
     * @param objectType 对象类型
     */
    private void generateSymbolByObjectType(ObjectType objectType) {
        StorageSymbol[] storageSymbols = this.storageSymbolJudge.judge(objectType);
        for (StorageSymbol storageSymbol : storageSymbols) {
            //不存在 添加一个新的
            if (!this.storageProviders.containsKey(storageSymbol)) {
                IStorageProvider storageProvider = this.storageProviderCreator.apply(storageSymbol);
                this.storageProviders.put(storageSymbol, storageProvider);
            }
        }

    }

    /**
     * 保存旧对象
     *
     * @param objects                      要保存的对象集合
     * @param added                        要新增的对象集合
     * @param deleted                      要删除的对象集合
     * @param attributeHasChanged          一个委托，用于检查对象的属性是否已更改。三个类型参数分别对应于要检查的对象、属性名称和是否已更改。
     * @param attributeOriginalValueGetter 用于获取属性原值的委托
     */
    private void saveOld(List<Object> objects, List<Object> added, List<Object> deleted,
                         FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged, IGetAttributeValue attributeOriginalValueGetter) {

        if (this.beginSaving != null)
            this.beginSaving.publishEvent(new EventObject(this));

        UpdateMappingSet set = this.generateMappingSet(objects, added, deleted);

        //执行集合中的每个映射
        int index = 0;

        while (index < set.getCount()) {
            MappingUnit unit = set.get(index);

            //触发开始保存事件
            if (this.beginSavingUnit != null)
                this.beginSavingUnit.publishEvent(new BeginSavingUnitEventArgs(this, unit, EObjectStatus.Modified));

            ObjectType objType;
            if (unit.getHostObject() != null) {
                objType = this.model.getObjectType(unit.getHostObject().getClass());
            } else {
                CompanionMapping obj = (CompanionMapping) unit.getMappingObjects().get(0);
                objType = this.model.getObjectType(obj.getAssociationObj().getClass());
            }

            StorageSymbol symbol = this.storageSymbolJudge.judge(null, objType);
            IStorageProvider provider = this.storageProviders.get(symbol);
            IMappingWorkflow workflow = provider.createMappingWorkflow();

            try {
                unit.saveOld(workflow, true, this.model, attributeHasChanged, o1 -> {
                    if (this.savingPreExecuteCommand != null)
                        this.savingPreExecuteCommand.publishEvent(o1);
                }, o1 -> {
                    if (this.savingPostExecuteCommand != null)
                        this.savingPostExecuteCommand.publishEvent(o1);
                }, attributeOriginalValueGetter);

                //触发结束保存事件
                if (this.endSavingUnit != null)
                    this.endSavingUnit.publishEvent(new EndSavingUnitEventArgs(this, unit, EObjectStatus.Modified, null));
            } catch (NothingUpdatedException ex) {
                //取出策略工厂
                ConcurrentConflictHandlerFactory factory =
                        ConcurrentConflictHandlerFactory.chooseFactory(objType.getConcurrentConflictHandlingStrategy());
                //忽略策略
                if (factory == null) {
                    index++;
                    continue;
                }

                //给工厂设值
                factory.setModel(this.model);
                factory.setStorageProvider(provider);
                factory.setAttributeHasChanged(attributeHasChanged);
                factory.setAttributeOriginalValueGetter(attributeOriginalValueGetter);

                try {
                    //处理此次冲突
                    IVersionConflictHandler handler = factory.createVersionConflictHandler();
                    handler.processVersionConflict(unit);
                } catch (NothingUpdatedException ex1) {
                    IUpdatingPhantomHandler newHandler = factory.createUpdatingPhantomHandler();
                    newHandler.processUpdatingPhantomConflict(unit);
                }
            } catch (Exception ex) {
                if (this.endSavingUnit != null)
                    this.endSavingUnit.publishEvent(new EndSavingUnitEventArgs(this, unit, EObjectStatus.Modified, ex));

                throw ex;
            }

            index++;
        }
    }

    /**
     * 生成更新映射集。将待执行更新映射的对象划分为一组映射单元，划分依据为：实体对象和独立关联取其键值、伴随关联取其伴随端的键值，键值相等者为一个单元。
     *
     * @param objects 要保存的对象的集合
     * @param added   新增的伴随关联对象集合
     * @param deleted 已删除的伴随关联对象集合
     * @return 更新映射集
     */
    private UpdateMappingSet generateMappingSet(List<Object> objects, List<Object> added, List<Object> deleted) {

        UpdateMappingSet set = new UpdateMappingSet();
        for (Object obj : objects) {
            ObjectType mt = this.model.getObjectType(obj.getClass());
            if (mt instanceof AssociationType) {
                AssociationType associationType = (AssociationType) mt;
                if (!associationType.getIndependent()) {
                    set.addCompanion(obj, associationType, EObjectStatus.Modified);
                } else {
                    set.addHost(obj, mt);
                }
            } else
                set.addHost(obj, mt);
        }

        for (Object delete : deleted) {
            AssociationType mt = this.model.getAssociationType(delete.getClass());
            set.addCompanion(delete, mt, EObjectStatus.Deleted);
        }

        for (Object add : added) {
            AssociationType mt = this.model.getAssociationType(add.getClass());
            set.addCompanion(add, mt, EObjectStatus.Added);
        }

        return set;
    }

    /**
     * 删除对象
     *
     * @param objects 要删除的对象的集合
     */
    private void delete(List<Object> objects) {
        //剔除连带对象
        ObjectReferencePack<List<Object>> rejObjs = new ObjectReferencePack<>();
        rejObjs.realValue = objects;
        this.rejectJointObjects(rejObjs);
        objects = rejObjs.realValue;

        if (this.beginDeleting != null)
            this.beginDeleting.publishEvent(new EventObject(this));

        Map<? extends Class<?>, List<Object>> groups = objects.stream().collect(Collectors.groupingBy(Object::getClass));
        //触发结束分组事件
        if (this.postGenerateGroup != null)
            this.postGenerateGroup.publishEvent(new EventObject(this));
        //删除
        for (Class<?> key : groups.keySet()) {
            ObjectType objectType = this.model.getObjectType(key);
            //准备存储程序
            StorageSymbol symbol = this.storageSymbolJudge.judge(null, objectType);
            IStorageProvider provider = this.storageProviders.get(symbol);
            IMappingWorkflow workflow = provider.createMappingWorkflow();
            //要删除的对象们
            Object[] objs = groups.get(key).toArray();

            //开始删除一组对象事件
            if (this.beginDeletingGroup != null)
                this.beginDeletingGroup.publishEvent(new BeginDeletingGroupEventArgs(this, objectType, objs));

            try {
                this.deleteGroup(objs, objectType, workflow, o1 -> {
                    if (this.deletingPreExecuteCommand != null)
                        this.deletingPreExecuteCommand.publishEvent(o1);
                }, o1 -> {
                    if (this.deletingPostExecuteCommand != null)
                        this.deletingPostExecuteCommand.publishEvent(o1);
                });

                if (this.endDeletingGroup != null)
                    this.endDeletingGroup.publishEvent(new EndDeletingGroupEventArgs(this, objectType, objs, null));
            } catch (NothingUpdatedException nothingUpdatedException) {
                if (this.endDeletingGroup != null)
                    this.endDeletingGroup.publishEvent(new EndDeletingGroupEventArgs(this, objectType, objs, null));
            } catch (Exception ex) {
                if (this.endDeletingGroup != null)
                    this.endDeletingGroup.publishEvent(new EndDeletingGroupEventArgs(this, objectType, objs, ex));
                throw ex;
            }

        }

        if (this.endDeleting != null)
            this.endDeleting.publishEvent(new EventObject(this));
    }

    /**
     * 删除一组对象
     *
     * @param objects               要删除的对象的集合
     * @param objectType            对象类型
     * @param mappingWorkflow       工作流
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     */
    private void deleteGroup(Object[] objects, ObjectType objectType, IMappingWorkflow mappingWorkflow,
                             ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                             ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        mappingWorkflow.begin();

        ObjectMapper objectMapper = new ObjectMapper(mappingWorkflow);
        objectMapper.generateCriteria(objects, objectType);

        if (objectType instanceof EntityType || (objectType instanceof AssociationType && ((AssociationType) objectType).getIndependent())) {
            mappingWorkflow.forDeleting();
        } else {
            if (objectType instanceof AssociationType && !((AssociationType) objectType).getCompanionEnd().getIsAggregated()) {
                mappingWorkflow.forUpdating();
                AssociationType associationType = (AssociationType) objectType;
                for (AssociationEnd end : associationType.getAssociationEnds()) {
                    if (!end.isCompanionEnd()) {
                        for (AssociationEndMapping mapping : end.getMappings()) {
                            mappingWorkflow.setField(mapping.getTargetField(), null);
                        }
                    }
                }
            }
        }

        mappingWorkflow.deleteCascade(objectType);

        mappingWorkflow.commit(preExecutionCallback, postExecutionCallback);
    }

    /**
     * 剔除连带对象
     *
     * @param objs 要从中筛选出连带对象并加以剔除的对象组
     */
    private void rejectJointObjects(ObjectReferencePack<List<Object>> objs) {
        Set<ObjectKey> hashSet = new HashSet<>();
        List<Pair<ReferenceElement, Object>> nullRefs = new ArrayList<>();
        for (Object obj : objs.realValue) {
            StructuralType mt = this.model.getStructuralType(obj.getClass());
            ObjectKey objectKey = ObjectSystemVisitor.getObjectKey(obj, mt);

            if (!hashSet.contains(objectKey)) {
                if (mt instanceof EntityType) {
                    EntityType entityType = (EntityType) mt;
                    this.analyzeObject(obj, entityType, hashSet, nullRefs);
                } else if (mt instanceof AssociationType) {
                    AssociationType associationType = (AssociationType) mt;
                    this.analyzeAssociation(obj, associationType, "", hashSet, nullRefs);
                }
            }
        }
        //剔除
        for (int i = objs.realValue.size() - 1; i >= 0; i--) {
            Object obj = objs.realValue.get(i);
            if (hashSet.contains(ObjectSystemVisitor.getObjectKey(obj, this.model.getStructuralType(obj.getClass()))))
                objs.realValue.remove(obj);
        }

        for (Pair<ReferenceElement, Object> tuple : nullRefs) {
            Object[] filterObj = objs.realValue.toArray(new Object[0]);
            ObjectReferencePack<Object[]> fObj = new ObjectReferencePack<>();
            fObj.realValue = filterObj;
            tuple.getOne().filterTarget(fObj, tuple.getTwo(), null, true);
            objs.realValue = Arrays.stream(fObj.realValue).collect(Collectors.toList());
        }
    }

    /**
     * 分析关联
     *
     * @param associationObj  目标关联对象
     * @param associationType 目标关联对象的类型
     * @param excludedEnd     过滤端
     * @param jointObjects    连带对象的容器，在分析过程中发现的连带对象将被放入此容器
     */
    private void analyzeAssociation(Object associationObj, AssociationType associationType, String excludedEnd,
                                    Set<ObjectKey> jointObjects, List<Pair<ReferenceElement, Object>> nullRefs) {
        for (AssociationEnd end : associationType.getAssociationEnds()) {
            boolean aggregated = end.getIsAggregated();
            if (Objects.equals(excludedEnd, end.getName())) {
                if (aggregated) {
                    Object endObj = ObjectSystemVisitor.getValue(associationObj, end);
                    if (endObj == null) {
                        nullRefs.add(new Pair<>(end, associationObj));
                    } else {
                        EntityType entityType = end.getEntityType();
                        ObjectKey key = ObjectSystemVisitor.getObjectKey(endObj, entityType);
                        if (!jointObjects.contains(key)) {
                            jointObjects.add(key);
                            this.analyzeObject(endObj, entityType, jointObjects, nullRefs);
                        }
                    }
                }
            }
        }
    }

    /**
     * 分析对象
     *
     * @param entityObj  目标对象
     * @param entityType 目标对象的类型
     * @param jointObjs  连带对象的容器，在分析过程中发现的连带对象将被放入此容器
     */
    private void analyzeObject(Object entityObj, EntityType entityType, Set<ObjectKey> jointObjs, List<Pair<ReferenceElement, Object>> nullRefs) {

        for (AssociationReference item : entityType.getAssociationReferences()) {
            //取出（重数大于1）关联型集合
            Iterable<Object> assObjs = ObjectSystemVisitor.associationNavigate(entityObj, item);

            if (assObjs == null) {
                nullRefs.add(new Pair<>(item, entityObj));
            } else {
                for (Object assObj : assObjs) {
                    ObjectKey key = ObjectSystemVisitor.getObjectKey(assObj, item.getAssociationType());
                    if (!jointObjs.contains(key)) {
                        jointObjs.add(key);
                        this.analyzeAssociation(assObj, item.getAssociationType(), item.getLeftEnd(), jointObjs, nullRefs);
                    }
                }
            }
        }
    }

    /**
     * 保存新对象
     *
     * @param objects 要保存的对象集合
     */
    private void saveNew(List<Object> objects) {
        if (objects != null && objects.size() > 0) {
            //生成对象参照图
            ObjectReferenceGraphic g = this.generateObjectReferenceGraphic(objects, objects::contains);
            //生成映射队列（边缘节点先插入）
            Queue<MappingUnit> queue = new ArrayDeque<>();
            if (g != null) {
                queue = this.generateMappingQueue(g);
            }

            //触发结束分组
            if (this.postGenerateQueue != null)
                this.postGenerateQueue.publishEvent(new EventObject(this));

            while (queue.size() > 0) {
                MappingUnit unit = queue.poll();
                if (unit != null) {

                    //如果主体对象是空
                    if (unit.getHostObject() == null)
                        throw new IllegalArgumentException("无法获取保存单元的主体对象,请参考映射单元的映射对象" + String.join(",", this.genNullHostObjectExceptionMessage(unit)) + "检查相应的配置.");

                    if (this.beginSavingUnit != null)
                        this.beginSavingUnit.publishEvent(new BeginSavingUnitEventArgs(this, unit, EObjectStatus.Added));

                    ObjectType objType = this.model.getObjectType(unit.getHostObject().getClass());
                    StorageSymbol symbol = this.storageSymbolJudge.judge(unit.getHostObject(), objType);
                    IStorageProvider provider = this.storageProviders.get(symbol);
                    IMappingWorkflow workflow = provider.createMappingWorkflow();

                    try {

                        unit.saveNew(workflow, this.model, o1 -> {
                            if (this.savingPreExecuteCommand != null)
                                this.savingPreExecuteCommand.publishEvent(o1);
                        }, o1 -> {
                            if (this.savingPostExecuteCommand != null)
                                this.savingPostExecuteCommand.publishEvent(o1);
                        });

                        if (this.endSavingUnit != null)
                            this.endSavingUnit.publishEvent(new EndSavingUnitEventArgs(this, unit, EObjectStatus.Added, null));
                    } catch (RepeatInsertionException ex) {

                        //如果是不支持导致的 就只能处理为抛出异常
                        if (ex.isUnSupported() && objType.getConcurrentConflictHandlingStrategy() != EConcurrentConflictHandlingStrategy.ThrowException) {
                            throw new UnSupportedException(unit.getHostObject(), objType, ex);
                        }

                        //取出策略工厂
                        ConcurrentConflictHandlerFactory factory =
                                ConcurrentConflictHandlerFactory.chooseFactory(objType
                                        .getConcurrentConflictHandlingStrategy());

                        if (factory == null) continue;

                        //给工厂设值
                        factory.setModel(this.model);
                        factory.setStorageProvider(provider);
                        factory.setInnerException(ex);

                        //处理此次冲突
                        IRepeatCreationHandler handler = factory.createRepeatCreationHandler();
                        handler.processRepeatConflict(unit);
                    } catch (Exception ex) {
                        //触发结束保存事件
                        if (this.endSavingUnit != null)
                            this.endSavingUnit.publishEvent(new EndSavingUnitEventArgs(this, unit, EObjectStatus.Added, ex));
                        throw ex;
                    }
                }
            }
        }

        if (this.endSaving != null)
            this.endSaving.publishEvent(new EventObject(this));
    }

    /**
     * 生成对象参照图
     *
     * @param objs     要生成对象参照图的对象集合
     * @param isSaving 一个委托，用于检查传入的对象是否为正在执行保存操作的对象，如果是返回true。第一个参数为传入的对象，第二个参数为返回值。
     * @return 对象参照图
     */
    private ObjectReferenceGraphic generateObjectReferenceGraphic(List<Object> objs, FunctionWithOneArg<Object, Boolean> isSaving) {
        ObjectReferenceGraphic graphic = new ObjectReferenceGraphic();
        if (objs == null || objs.size() == 0) return null;
        ObjectReferenceGraphicGenerator graphicGenerator = new ObjectReferenceGraphicGenerator();

        for (Object item : objs) {
            StructuralType mt = this.model.getStructuralType(item.getClass());
            if (mt instanceof EntityType) {
                graphicGenerator.analyzeObject(item, isSaving::invoke, graphic);
            } else if (mt instanceof AssociationType) {
                AssociationType associationType = (AssociationType) mt;
                graphicGenerator.analyzeAssociation(item, associationType, isSaving::invoke, graphic);
            }
        }

        //返回图
        return graphic;
    }

    /**
     * 根据对象参照图生成映射队列
     *
     * @param graphic 对象参照图
     * @return 映射队列
     */
    private Queue<MappingUnit> generateMappingQueue(ObjectReferenceGraphic graphic) {
        Queue<MappingUnit> queue = new ArrayDeque<>();
        Set<Object> hashSet = new HashSet<>();

        while (graphic.getCount() > 0) {
            int enqueueNum = 0;

            for (int i = 0; i < graphic.getCount(); i++) {
                MappingUnit unit = graphic.get(i);
                //{参照对象总数减(-)已放入队列的参照对象}(及为 未放入队列的参照对象数)
                int countReference = 0;
                if (unit.getReferredObjects() != null && unit.getReferredObjects().size() > 0)
                    for (Object item : unit.getReferredObjects()) {
                        if (!hashSet.contains(item)) {
                            countReference++;
                            break;
                        }
                    }

                if (countReference == 0) {
                    queue.offer(unit);
                    hashSet.add(unit.getHostObject());
                    graphic.remove(i);
                    i--;
                    enqueueNum++;
                }
            }

            if (enqueueNum == 0) {
                List<String> typeNames = graphic.getUnits().stream().filter(p -> p.getHostObject() != null).map(p -> p.getHostObject().getClass().getSimpleName()).collect(Collectors.toList());
                String typeName = String.join(",", typeNames);
                String message = "生成映射队列引用个数错误,无法决定对象保存的优先顺序,可能是因为在数据库中存在循环参照关系(如两个对象间存在互相引用且为这两个引用各自配置了关联端相同但不是同一个的关联型),请检查以下类型及其映射表:" + typeName + ".";
                throw new IllegalArgumentException(message);
            }
        }

        return queue;
    }

    /**
     * 生成映射单元的主体对象为空时的异常消息
     *
     * @param unit 映射单元
     * @return 异常消息
     */
    private String[] genNullHostObjectExceptionMessage(MappingUnit unit) {
        List<String> result = new ArrayList<>();
        List<Object> mappingObjs = unit.getMappingObjects();
        for (Object mappingObj : mappingObjs) {
            if (mappingObj != null)
                result.add(mappingObj.toString());
        }
        return result.toArray(new String[0]);
    }

    /**
     * 按条件删除对象。
     *
     * @param objectType 要删除的对象的类型
     * @param filter     对象筛选条件
     * @return 受影响的行数
     */
    public int delete(ObjectType objectType, Expression filter) {

        //事务块
        TransactionScope transactionScope;
        //获取存储提供程序
        ObjectReferencePack<Boolean> isOutTrBegunPack = new ObjectReferencePack<>();
        ObjectReferencePack<TransactionScope> transactionScopePack = new ObjectReferencePack<>();
        StorageSymbol[] storageSymbols = this.prepareDirectlyChangeTransaction(objectType, isOutTrBegunPack, transactionScopePack);
        //是否在我开启事务前已经开启了事务
        boolean isOutTrBegun = isOutTrBegunPack.realValue;
        //事务块
        transactionScope = transactionScopePack.realValue;

        try {
            int affectCount = 0;
            for (StorageSymbol symbol : storageSymbols) {
                if (this.beginDirectlyChanging != null)
                    this.beginDirectlyChanging.publishEvent(new BeginDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Delete, objectType.getClrType(), null));

                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                affectCount += storageProvider.delete(objectType, (LambdaExpression) filter, o1 -> {
                    if (this.directlyChangingPreExecuteCommand != null)
                        this.directlyChangingPreExecuteCommand.publishEvent(o1);
                }, o1 -> {
                    if (this.deletingPostExecuteCommand != null)
                        this.deletingPostExecuteCommand.publishEvent(o1);
                });

                if (this.endDirectlyChanging != null)
                    this.endDirectlyChanging.publishEvent(
                            new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Delete, objectType.getClrType(), null, affectCount,
                                    null));
                if (!isOutTrBegun)
                    storageProvider.commitTransaction();
            }

            if (transactionScope != null) {
                transactionScope.complete();
            }

            return affectCount;
        } catch (Exception ex) {
            if (this.endDirectlyChanging != null)
                this.endDirectlyChanging.publishEvent(
                        new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Delete, objectType.getClrType(), null, 0,
                                ex));
            for (IStorageProvider transaction : this.storageProviders.values()) {
                transaction.rollbackTransaction();
            }

            throw ex;
        } finally {
            //释放资源
            for (StorageSymbol symbol : storageSymbols) {
                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                if (!isOutTrBegun)
                    storageProvider.releaseResource();
            }

            if (transactionScope != null) {
                transactionScope.close();
            }
        }
    }

    /**
     * 为符合条件的对象的属性设置新值
     *
     * @param objectType 要设置其属性值的对象的类型
     * @param filter     对象筛选条件
     * @param newValues  存储属性新值的键值对集合，其中键为属性名称，值为属性的新值
     * @return 受影响的行数
     */
    public int setAttributes(ObjectType objectType, Expression filter, Map<String, Object> newValues) {
        //是否在我开启事务前已经开启了事务
        boolean isOutTrBegun;
        //获取存储提供程序
        ObjectReferencePack<Boolean> isOutTrBegunPack = new ObjectReferencePack<>();
        ObjectReferencePack<TransactionScope> transactionScopePack = new ObjectReferencePack<>();

        StorageSymbol[] storageSymbols = this.prepareDirectlyChangeTransaction(objectType, isOutTrBegunPack, transactionScopePack);
        isOutTrBegun = isOutTrBegunPack.realValue;
        //事务块
        TransactionScope transactionScope = transactionScopePack.realValue;

        try {

            int affectCount = 0;
            for (StorageSymbol symbol : storageSymbols) {
                if (this.beginDirectlyChanging != null)
                    this.beginDirectlyChanging.publishEvent(new BeginDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Update, objectType.getClrType(), newValues));

                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                affectCount += storageProvider.setAttributes(objectType, (LambdaExpression) filter, newValues, o1 -> {
                    if (this.directlyChangingPreExecuteCommand != null)
                        this.directlyChangingPreExecuteCommand.publishEvent(o1);
                }, o1 -> {
                    if (this.deletingPostExecuteCommand != null)
                        this.deletingPostExecuteCommand.publishEvent(o1);
                });

                if (this.endDirectlyChanging != null)
                    this.endDirectlyChanging.publishEvent(
                            new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Update, objectType.getClrType(), newValues, affectCount,
                                    null));

                if (!isOutTrBegun)
                    storageProvider.commitTransaction();

            }

            if (transactionScope != null) {
                transactionScope.complete();
            }

            return affectCount;
        } catch (Exception ex) {
            if (this.endDirectlyChanging != null)
                this.endDirectlyChanging.publishEvent(
                        new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Update, objectType.getClrType(), newValues, 0,
                                ex));
            for (IStorageProvider transaction : this.storageProviders.values()) {
                transaction.rollbackTransaction();
            }

            throw ex;
        } finally {
            //释放资源
            for (StorageSymbol symbol : storageSymbols) {
                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                if (!isOutTrBegun)
                    storageProvider.releaseResource();
            }

            if (transactionScope != null) {
                transactionScope.close();
            }
        }
    }

    /**
     * 为符合条件的对象的属性设置新值，其中新值为原值加上增量值。属性必须为数值类型。
     *
     * @param objectType     要设置其属性值的对象的类型
     * @param filter         对象筛选条件
     * @param increaseValues 存储增量值的键值对集合，其中键为属性名称，值为增量值
     * @return 受影响的行数
     */
    public int increaseAttributes(ObjectType objectType, Expression filter,
                                  Map<String, Object> increaseValues) {

        //获取存储提供程序
        ObjectReferencePack<TransactionScope> transactionScopePack = new ObjectReferencePack<>();
        ObjectReferencePack<Boolean> isOutTrBegunPack = new ObjectReferencePack<>();
        StorageSymbol[] storageSymbols = this.prepareDirectlyChangeTransaction(objectType, isOutTrBegunPack, transactionScopePack);
        //是否在我开启事务前已经开启了事务
        boolean isOutTrBegun = isOutTrBegunPack.realValue;
        //事务块
        TransactionScope transactionScope = transactionScopePack.realValue;

        try {
            int affectCount = 0;
            for (StorageSymbol symbol : storageSymbols) {
                if (this.beginDirectlyChanging != null)
                    this.beginDirectlyChanging.publishEvent(new BeginDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Update, objectType.getClrType(), increaseValues));

                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                affectCount += storageProvider.increaseAttributes(objectType, (LambdaExpression) filter, increaseValues, o1 -> {
                    if (this.directlyChangingPreExecuteCommand != null)
                        this.directlyChangingPreExecuteCommand.publishEvent(o1);
                }, o1 -> {
                    if (this.deletingPostExecuteCommand != null)
                        this.deletingPostExecuteCommand.publishEvent(o1);
                });

                if (this.endDirectlyChanging != null)
                    this.endDirectlyChanging.publishEvent(
                            new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Update, objectType.getClrType(), increaseValues, affectCount,
                                    null));

                if (!isOutTrBegun)
                    storageProvider.commitTransaction();

            }

            if (transactionScope != null) {
                transactionScope.complete();
            }

            return affectCount;
        } catch (Exception ex) {
            if (this.endDirectlyChanging != null)
                this.endDirectlyChanging.publishEvent(
                        new EndDirectlyChangingEventArgs(this, filter, EDirectlyChangeType.Increment, objectType.getClrType(), increaseValues, 0,
                                ex));

            for (IStorageProvider transaction : this.storageProviders.values()) {
                transaction.rollbackTransaction();
            }

            throw ex;
        } finally {
            //释放资源
            for (StorageSymbol symbol : storageSymbols) {
                IStorageProvider storageProvider = this.storageProviders.get(symbol);
                if (!isOutTrBegun)
                    storageProvider.releaseResource();
            }

            if (transactionScope != null) {
                transactionScope.close();
            }
        }
    }

    /**
     * 准备就地修改事务
     *
     * @param objectType       对象类型
     * @param isOutTrBegun     是否外部开启了事务
     * @param transactionScope 事务块
     * @return 存储标记集合
     */
    private StorageSymbol[] prepareDirectlyChangeTransaction(ObjectType objectType, ObjectReferencePack<Boolean> isOutTrBegun,
                                                             ObjectReferencePack<TransactionScope> transactionScope) {
        //获取存储提供程序
        this.generateSymbolByObjectType(objectType);
        StorageSymbol[] storageSymbols = this.storageSymbolJudge.judge(objectType);

        //是否在我开启事务前已经开启了事务
        isOutTrBegun.realValue = this.storageProviders.values().stream().anyMatch(IStorageProvider::getTransactionBegun);

        //开启事务
        this.beginTransaction(transactionScope);

        return storageSymbols;
    }
}
