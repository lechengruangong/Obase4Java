/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的基础查询提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:08:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.common.EIsolationLevel;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.mapping.pipeline.PostExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.PreExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.QueryEventArgs;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.OpExecutor;
import io.obase.core.query.QueryOp;
import io.obase.core.query.oop.OopPipelineBuilder;

import java.util.Map;

/**
 * 定义存储提供程序规范
 */
public interface IStorageProvider {

    /**
     * 准备存储资源,如打开数据库连接
     */
    @Deprecated
    void prepareResource();

    /**
     * 释放存储资源,如关闭数据库连接
     */
    @Deprecated
    void releaseResource();

    /**
     * 获取一个值，该值指示是否已开启本地事务
     *
     * @return 指示是否已开启本地事务
     */
    boolean getTransactionBegun();

    /**
     * 开始本地事务
     *
     * @param isolationLevel 事务隔离级别
     */
    void beginTransaction(EIsolationLevel isolationLevel);

    /**
     * 开始本地事务
     */
    void beginTransaction();

    /**
     * 提交本地事务
     */
    void commitTransaction();

    /**
     * 回滚本地事务
     */
    void rollbackTransaction();

    /**
     * 启动一个新的映射工作流
     *
     * @return 一个用于跟踪工作流的对象，它实现了IMappingWorkflow接口
     */
    IMappingWorkflow createMappingWorkflow();

    /**
     * 删除符合指定条件的对象
     *
     * @param objType               要删除的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    int delete(ObjectType objType, LambdaExpression filterExpression,
               ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
               ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback);

    /**
     * 搜索符合指定条件的对象，为其属性（部分或全部）设置新值。
     *
     * @param objType               要修改其属性的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param newValues             存储属性新值的字典
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    int setAttributes(ObjectType objType, LambdaExpression filterExpression,
                      Map<String, Object> newValues,
                      ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                      ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback);

    /**
     * 搜索符合指定条件的对象，为其属性（部分或全部）施加一个增量
     *
     * @param objType               要修改其属性的对象的类型
     * @param filterExpression      用于测试对象是否符合条件的断言函数
     * @param increaseValues        存储增量值的字典
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @return 受影响的行数
     */
    int increaseAttributes(ObjectType objType, LambdaExpression filterExpression,
                           Map<String, Object> increaseValues, ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                           ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback);

    /**
     * 为指定的查询生成运算管道
     *
     * @param query             要执行的查询
     * @param complement        返回后续查询（或称后续链）
     * @param complementBuilder 返回用于生成补充运算管道的生成器
     * @return 操作执行器
     */
    OpExecutor generatePipeline(QueryOp query, ObjectReferencePack<QueryOp> complement, ObjectReferencePack<OopPipelineBuilder> complementBuilder);

    /**
     * 执行运算管道
     *
     * @param pipeline              要执行的运算管道
     * @param resultIncluding       指定由运算管道加载的对象须包含的引用（相对于结果类型），必须是同构的
     * @param attachObject          用于在对象上下文中附加对象的委托
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @param attachRoot            是否作为根对象附加
     * @return 执行结果
     */
    Object executePipeline(OpExecutor pipeline, AssociationTree resultIncluding, IAttachObject attachObject,
                           ActionWithOneArg<QueryEventArgs> preExecutionCallback,
                           ActionWithOneArg<QueryEventArgs> postExecutionCallback, boolean attachRoot);
}
