/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为一些异构运算执行器定义标准化模板.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:40:50
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
import io.obase.core.odm.objectSys.*;
import io.obase.core.query.IncludeOp;
import io.obase.core.query.QueryOp;
import io.obase.core.query.StorageHeterogeneityPredicationProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 为一些异构运算执行器定义标准化模板
 */
public abstract class StandardHeterogOpExecutor extends HeterogOpExecutor {

    /**
     * 基础查询提供程序
     */
    private final IBaseQueryProvider baseQueryProvider;

    /**
     * 初始化HeterogOpExecutor类的新实例
     *
     * @param storageProviderCreator 创建存储提供程序的委托
     * @param model                  对象数据模型
     * @param preExecutionCallback   执行后回调委托
     * @param postExecutionCallback  用于在对象上下文中附加对象的委托
     * @param baseQueryProvider      基础查询提供器
     */
    protected StandardHeterogOpExecutor(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IBaseQueryProvider baseQueryProvider) {
        super(storageProviderCreator, model, preExecutionCallback, postExecutionCallback);
        this.baseQueryProvider = baseQueryProvider != null ? baseQueryProvider : new BaseQueryProvider(storageProviderCreator, model);
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
        ObjectReferencePack<ReferenceElement[]> baseOpAttachingRefs = new ObjectReferencePack<>();
        //生成基础查询
        QueryOp baseOp = this.generateBaseOp(heterogOp, heterogQuery, baseOpAttachingRefs);
        ObjectReferencePack<Object> executionState = new ObjectReferencePack<>();
        QueryOp complement = this.baseQueryProvider.separateOutComplement(baseOp, executionState);
        //依据基础运算裁剪包含树
        AssociationTree callerIncluding = this.cutIncluding(including, heterogOp == null ? null : heterogOp.getTail());
        //合并包含树并实施极限分解
        ObjectReferencePack<AssociationTreeAttachingItem[]> attachingItems = new ObjectReferencePack<>();
        AssociationTree baseIncluding = this.decomposeIncluding(complement, callerIncluding, attachingItems);
        Object baseInstances = this.baseQueryProvider.callService(executionState.realValue, baseIncluding, this.preExecutionCallback, this.postExecutionCallback, attachObject);
        List<AssociationTreeAttachingItem> filteredAttachingRefList = new ArrayList<>();
        if (attachingItems.realValue != null && attachingItems.realValue.length > 0) {
            for (AssociationTreeAttachingItem item : attachingItems.realValue) {
                //每个都不一样 则为不包含在baseOpAttachingRefs里
                boolean isInBaseOpAttachingRefs = Arrays.stream(baseOpAttachingRefs.realValue).allMatch(p -> p != item.getAttachingReference());
                if (isInBaseOpAttachingRefs)
                    filteredAttachingRefList.add(item);
            }
        }

        AssociationTreeAttachingItem[] filteredAttachingRefs = filteredAttachingRefList.toArray(new AssociationTreeAttachingItem[0]);
        //执行非重叠附加包含运算
        Object[] ins = new Object[1];
        ins[0] = baseInstances;
        this.executeAttachingIncluding(ins, filteredAttachingRefs);

        //执行补充运算
        if (complement != null) {
            //执行补充运算
            Object oopResult;
            if (baseInstances instanceof Iterable) {
                Iterable<Object> instances = (Iterable<Object>) baseInstances;
                oopResult = this.baseQueryProvider.executeComplement(complement, instances, executionState.realValue);
            } else {
                oopResult = this.baseQueryProvider.executeComplement(complement, baseInstances, executionState.realValue);
            }

            baseInstances = oopResult;
        }

        //不是同构查询 此处需要将Base查询查出
        if (this.getClass() != HomogOpExecutor.class) {
            this.processInstances(baseInstances);
        }

        ObjectReferencePack<ReferenceElement[]> attachingRefs = new ObjectReferencePack<>();
        //生成附加查询
        QueryOp[] attachingQueries = this.generateAttachingQuery(baseInstances, heterogOp, attachingRefs);
        this.heterogQueryProvider.setAttachRoot(false);

        List<Object> attachingResult = new ArrayList<>();
        if (attachingItems.realValue != null) {
            for (QueryOp attachingQuery : attachingQueries) {
                for (ReferenceElement attachingRef : attachingRefs.realValue) {
                    //获取重叠包含
                    Arrays.stream(attachingItems.realValue).filter(p -> p.getAttachingReference() == attachingRef).findFirst()
                            .ifPresent(include -> attachingResult.add(this.processInstances(this.heterogQueryProvider.execute(attachingQuery, include.getAttachingTree()))));

                }
            }
        } else {
            for (QueryOp attachingQuery : attachingQueries) {
                //执行附加查询
                attachingResult.add(this.processInstances(this.heterogQueryProvider.execute(attachingQuery, null)));
            }
        }

        //合并
        return this.combine(baseInstances, attachingResult.toArray(), attachObject, attachRoot);
    }

    /**
     * 处理某个查询结果
     *
     * @param instances 查询结果
     * @return 处理后的结果
     */
    private Object processInstances(Object instances) {
        //将源对象查出
        if (instances instanceof Iterable) {
            Iterable<Object> iEnumerable = (Iterable<Object>) instances;
            List<Object> tempResult = new ArrayList<>();
            for (Object o : iEnumerable) {
                tempResult.add(o);
            }
            instances = tempResult.toArray(new Object[0]);
        }

        return instances;
    }

    /**
     * 由派生类实现，为指定的异构运算生成基础查询
     *
     * @param heterogOp     异构运算
     * @param heterogQuery  以异构运算作为末节点的异构查询
     * @param attachingRefs 返回对异构运算执行极限分解形成的附加引用
     * @return 基础查询
     */
    protected abstract QueryOp generateBaseOp(QueryOp heterogOp, QueryOp heterogQuery, ObjectReferencePack<ReferenceElement[]> attachingRefs);

    /**
     * 由派生类实现，为指定的异构运算生成附加查询
     *
     * @param baseResult    基础查询的结果
     * @param heterogOp     异构运算
     * @param attachingRefs 返回生成的附加查询对应的附加引用，与方法返回值集合中的元素一一对应
     * @return 附加查询
     */
    protected abstract QueryOp[] generateAttachingQuery(Object baseResult, QueryOp heterogOp, ObjectReferencePack<ReferenceElement[]> attachingRefs);

    /**
     * 由派生类实现，合并基础查询与附加查询的结果
     *
     * @param baseResult       基础查询结果
     * @param attachingResults 各附加查询的结果，其顺序与GenerateAttachingQuery方法返回的附加查询的顺序一致
     * @param attachObject     附加对象委托
     * @param attachRoot       是否作为根对象附加
     * @return 合并结果
     */
    protected abstract Object combine(Object baseResult, Object[] attachingResults, IAttachObject attachObject, boolean attachRoot);

    /**
     * 根据基础运算对包含树T进行裁剪
     *
     * @param includingTree 待裁剪的包含树
     * @param basicOp       作为裁剪依据的基础运算
     * @return 裁剪后的包含树
     */
    protected abstract AssociationTree cutIncluding(AssociationTree includingTree, QueryOp basicOp);

    /**
     * 将补充查询中的包含运算（包括隐含包含）与调用运算执行器时传入的包含树合并，然后执行极限分解。
     *
     * @param complementQuery 补充查询
     * @param callerIncluding 调用方传入的包含树
     * @param attachingItems  返回分解出的代表附加包含树的项
     * @return 返回分解后的基础树
     */
    private AssociationTree decomposeIncluding(QueryOp complementQuery, AssociationTree callerIncluding, ObjectReferencePack<AssociationTreeAttachingItem[]> attachingItems) {
        //补充查询的包含树
        AssociationTree complementIncluding = null;
        if (complementQuery != null) {
            complementIncluding = complementQuery.getChainIncluding();
            if (complementIncluding == null && callerIncluding != null)
                complementIncluding = new AssociationTree(this.model.getReferringType(complementQuery.getSourceType()));
        }

        //增加补充链
        if (complementIncluding != null) {
            AtrophyCollector collector = new AtrophyCollector();
            AssociationTreeNode[] paths = complementQuery.accept(collector);
            //合并包含树
            complementIncluding.grow(callerIncluding, paths);
        }

        //包含树
        AssociationTree including = complementIncluding;

        if (including != null) {
            //执行强制包含
            IncludingEnforcer enforcer = new IncludingEnforcer();
            including.accept(enforcer);

            //进行分解
            AssociationTreeDecomposer decomposer = new AssociationTreeDecomposer(new StorageHeterogeneityPredicationProvider());
            decomposer.setArgument(false);
            including.accept(decomposer);

            if (decomposer.getResult() != null && decomposer.getResult().getSubCount() > 0) {
                attachingItems.realValue = decomposer.getOutArgument();
                return decomposer.getResult();
            }
        }

        attachingItems.realValue = null;
        return callerIncluding;
    }

    /**
     * 执行附加包含
     *
     * @param sourceObjs     作为查询源的对象集
     * @param attachingItems 表示要执行的附加包含的附加项
     */
    private void executeAttachingIncluding(Object[] sourceObjs, AssociationTreeAttachingItem[] attachingItems) {
        //构造获取器
        IncludingTargetGetter targetGetter = new IncludingTargetGetter(sourceObjs);
        if (attachingItems != null && attachingItems.length > 0) {
            for (AssociationTreeAttachingItem attachingItem : attachingItems) {
                Object[] includingTargets = attachingItem.getAttachingNode().asTree().accept(targetGetter);
                IncludeOp includeOp = new IncludeOp(attachingItem.getAttachingTree(), this.model);
                QueryOp attachingQuery = attachingItem.getAttachingReference().generateLoadingQuery(includingTargets, includeOp);
                Object obj = this.heterogQueryProvider.execute(attachingQuery, null);
                Object[] attachingObs;
                if (obj instanceof Object[]) {
                    attachingObs = (Object[]) obj;
                } else {
                    attachingObs = new Object[]{obj};
                }

                for (Object target : includingTargets) {
                    ObjectReferencePack<Object[]> pack = new ObjectReferencePack<>();
                    pack.realValue = attachingObs;
                    Object[] refValue = attachingItem.getAttachingReference().filterTarget(pack, target, null, false);
                    attachingObs = pack.realValue;
                    attachingItem.getAttachingReference().setValue(target, refValue);
                }
            }
        }
    }

    /**
     * 作为关联树向上访问者，获取包含目标对象
     */
    private static class IncludingTargetGetter implements IAssociationTreeUpwardVisitorWithResult<Object[]> {

        /**
         * 源对象
         */
        private final Object[] sourceObjects;

        /**
         * 结果
         */
        private Object[] result = new Object[0];

        /**
         * 初始化IncludingTargetGetter类的新实例
         *
         * @param sourceObjs 源对象
         */
        public IncludingTargetGetter(Object[] sourceObjs) {
            this.sourceObjects = sourceObjs;
        }

        /**
         * 获取遍历关联树的结果
         *
         * @return 遍历关联树的结果
         */
        @Override
        public Object[] getResult() {
            return this.result;
        }

        /**
         * 前置访问，即在访问父级前执行操作
         *
         * @param subTree          被访问的子树
         * @param childState       访问子级时产生的状态数据
         * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
         * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
         * @return 是否继续访问
         */
        @Override
        public boolean preVisit(AssociationTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
            //Nothing to do
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        }

        /**
         * 后置访问，即在访问父级后执行操作
         *
         * @param subTree       被访问的子树
         * @param childState    访问子级时产生的状态数据
         * @param preVisitState 前置访问产生的状态数据
         */
        @Override
        public void postVisit(AssociationTree subTree, Object childState, Object preVisitState) {
            AssociationTree parent = subTree.getParent();
            if (parent == null) {
                this.result = this.sourceObjects;
            } else {
                ReferenceElement referenceElement = parent.getRepresentedType().getReferenceElement(subTree.getElementName());
                //取每个值
                this.result = Arrays.stream(this.result).map(referenceElement::getValue).toArray();
            }
        }

        /**
         * 重置访问者
         */
        @Override
        public void reset() {
            //Nothing to do
        }
    }
}
