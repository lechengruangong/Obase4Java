/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的基础查询提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:29:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IStorageProvider;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.OpExecutor;
import io.obase.core.query.QueryOp;
import io.obase.core.query.oop.OopExecutor;
import io.obase.core.query.oop.OopPipelineBuilder;

/**
 * 默认的基础查询提供程序
 */
public class BaseQueryProvider implements IBaseQueryProvider {
    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 用于构造存储提供程序的委托
     */
    private final FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator;

    /**
     * 默认的基础查询提供程序
     *
     * @param storageProviderCreator 存储提供程序构建委托
     * @param model                  对象数据模型
     */
    public BaseQueryProvider(FunctionWithOneArg<StorageSymbol, IStorageProvider> storageProviderCreator, ObjectDataModel model) {
        this.model = model;
        this.storageProviderCreator = storageProviderCreator;
    }

    /**
     * 调用存储服务
     *
     * @param executionState        一个状态对象，携带查询执行流程中生成的数据
     * @param including             指定由运算管道加载的对象须包含的引用，必须是同构的。
     * @param preExecutionCallback  执行命令前委托
     * @param postExecutionCallback 执行命令后委托
     * @param attachObject          用于将对象附加到对象上下文的委托
     * @return 调用结果
     */
    @Override
    public Object callService(Object executionState, AssociationTree including, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IAttachObject attachObject) {
        if (executionState instanceof QueryExecutionState) {
            QueryExecutionState queryExecutionState = (QueryExecutionState) executionState;
            IStorageProvider provider = queryExecutionState.StorageProvider;
            OpExecutor pipeline = queryExecutionState.Pipeline;
            return provider.executePipeline(pipeline, including, attachObject, preExecutionCallback, postExecutionCallback, false);
        }

        return null;
    }

    /**
     * 执行补充运算
     *
     * @param complement     要执行的补充查询
     * @param serviceResult  存储服务输出的结果
     * @param executionState 一个状态对象，携带查询执行流程中生成的数据
     * @return 补充运算执行结果
     */
    @Override
    public Object executeComplement(QueryOp complement, Object serviceResult, Object executionState) {
        if (executionState instanceof QueryExecutionState) {
            QueryExecutionState queryExecutionState = (QueryExecutionState) executionState;
            OopPipelineBuilder builder = queryExecutionState.ComplementBuilder;
            OopExecutor oopPipeline = complement.generatePipeline(builder);
            if (serviceResult instanceof Iterable) {
                Iterable<Object> instances = (Iterable<Object>) serviceResult;
                return oopPipeline.execute(instances);
            } else {
                return oopPipeline.execute(serviceResult, false);
            }
        }

        return null;
    }

    /**
     * 从基础查询中分离出补充查询
     * 补充运算是特定的存储服务无法执行，须以对象运算方式补充执行的片段
     *
     * @param baseQuery      要执行的基础查询
     * @param executionState 一个状态对象，携带查询执行流程中生成的数据
     * @return 分离出的补充查询
     */
    @Override
    public QueryOp separateOutComplement(QueryOp baseQuery, ObjectReferencePack<Object> executionState) {
        //获取基点存储标记
        ObjectType objectType = this.model.getObjectType(baseQuery.getSourceType());
        TypeExtension typeExtension = null;
        if (objectType != null)
            typeExtension = objectType.getExtension(HeterogStorageExtension.class);
        StorageSymbol storageSymbol = typeExtension == null ? this.model.getStorageSymbol() : ((HeterogStorageExtension) typeExtension).getStorageSymbol();
        if (storageSymbol == null)
            storageSymbol = StorageSymbols.getCurrent().getDefault();
        IStorageProvider provider = this.storageProviderCreator.invoke(storageSymbol);

        ObjectReferencePack<QueryOp> complement = new ObjectReferencePack<>();
        ObjectReferencePack<OopPipelineBuilder> complementBuilder = new ObjectReferencePack<>();
        OpExecutor pipeline = provider.generatePipeline(baseQuery, complement, complementBuilder);
        QueryExecutionState state = new QueryExecutionState();
        state.ComplementBuilder = complementBuilder.realValue;
        state.Pipeline = pipeline;
        state.StorageProvider = provider;
        executionState.realValue = state;
        return complement.realValue;
    }

    /**
     * 一个数据结构，用于存储查询执行流程中生成的数据
     */
    private static class QueryExecutionState {

        /**
         * 用于生成补充对象运算管道的生成器。如果不指定，将使用默认生成器（OopPipelineBuilder）
         */
        public OopPipelineBuilder ComplementBuilder;

        /**
         * 基础查询链可由存储服务执行部分生成的运算管道
         */
        public OpExecutor Pipeline;

        /**
         * 存储提供程序
         */
        public IStorageProvider StorageProvider;

    }
}
