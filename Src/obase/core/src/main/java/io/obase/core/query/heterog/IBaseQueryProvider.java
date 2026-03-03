/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基础查询提供程序规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:02:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.QueryOp;

/**
 * 基础查询提供程序规范，提供在异构查询中执行基础查询的方案。
 */
public interface IBaseQueryProvider {

    /**
     * 调用存储服务
     *
     * @param executionState        一个状态对象，携带查询执行流程中生成的数据
     * @param including             指定由运算管道加载的对象须包含的引用，必须是同构的。
     * @param attachObject          用于将对象附加到对象上下文的委托
     * @param postExecutionCallback 执行命令后委托
     * @param preExecutionCallback  执行命令前委托
     * @return 调用结果
     */
    Object callService(Object executionState, AssociationTree including, ActionWithOneArg<QueryEventArgs> preExecutionCallback, ActionWithOneArg<QueryEventArgs> postExecutionCallback, IAttachObject attachObject);

    /**
     * 执行补充运算
     *
     * @param complement     要执行的补充查询
     * @param serviceResult  存储服务输出的结果
     * @param executionState 一个状态对象，携带查询执行流程中生成的数据
     * @return 补充运算执行结果
     */
    Object executeComplement(QueryOp complement, Object serviceResult, Object executionState);

    /**
     * 从基础查询中分离出补充查询
     * 补充运算是特定的存储服务无法执行，须以对象运算方式补充执行的片段
     *
     * @param baseQuery      要执行的基础查询
     * @param executionState 一个状态对象，携带查询执行流程中生成的数据
     * @return 分离出的补充查询
     */
    QueryOp separateOutComplement(QueryOp baseQuery, ObjectReferencePack<Object> executionState);
}
