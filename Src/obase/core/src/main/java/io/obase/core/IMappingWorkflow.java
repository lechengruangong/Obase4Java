/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：跟踪对象修改并实施持久化的工作流机制.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:33:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.mapping.pipeline.PostExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.PreExecuteCommandEventArgs;
import io.obase.core.odm.ObjectType;

/**
 * 跟踪对象修改并实施持久化的工作流机制
 */
public interface IMappingWorkflow {

    /**
     * 开始跟踪修改
     */
    void begin();

    /**
     * 接受本次工作流的存储源名称（如数据库表名）
     *
     * @param targetSource 存储源名称
     * @return 工作流
     */
    IMappingWorkflow setSource(String targetSource);

    /**
     * 指示本次工作流将向存储源插入新对象
     *
     * @return 工作流
     */
    IMappingWorkflow forInserting();

    /**
     * 指示本次工作流将修改存储源中已有的对象
     *
     * @return 工作流
     */
    IMappingWorkflow forUpdating();

    /**
     * 指示本次工作流将删除存储源中的对象
     *
     * @return 工作流
     */
    IMappingWorkflow forDeleting();

    /**
     * 设置指定域（如数据库表的字段）的值
     *
     * @param field 字段
     * @param value 值
     * @return 工作流
     */
    IMappingWorkflow setField(String field, Object value);

    /**
     * 对指定域（如数据库表的字段）的值施加一个增量
     *
     * @param field     字段
     * @param increment 值
     * @return 工作流
     */
    IMappingWorkflow increaseField(String field, Object increment);

    /**
     * 指示本次工作流应当忽略指定域（如数据库表的字段），如果已跟踪到了该域的修改，应当将其排除
     *
     * @param field 字段
     * @return 工作流
     */
    IMappingWorkflow ignoreField(String field);

    /**
     * 为当前工作流新增一个映射筛选器，该筛选器与已存在的筛选器进行逻辑“与”运算。
     *
     * @return 新增的映射筛选器
     */
    MappingFilter and();

    /**
     * 为当前工作流新增一个映射筛选器，该筛选器与已存在的筛选器进行逻辑“或”运算
     *
     * @return 新增的映射筛选器
     */
    MappingFilter or();

    /**
     * 级联删除，即从基点类型开始沿关联关系递归删除。实施者制定具体的级联规则
     *
     * @param initType 基点类型
     */
    void deleteCascade(ObjectType initType);

    /**
     * 提交工作流
     *
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     */
    void commit(ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback);

    /**
     * 提交工作流
     *
     * @param preExecutionCallback  一个委托，代表在执行存储指令（如SQL语句）前回调的方法
     * @param postExecutionCallback 一个委托，代表在执行存储指令（如SQL语句）后回调的方法
     * @param identity              返回存储服务为新对象生成的标识
     */
    void commit(ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback, ObjectReferencePack<Object> identity);
}
