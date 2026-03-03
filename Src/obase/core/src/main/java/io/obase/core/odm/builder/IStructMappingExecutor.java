/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：模型结构映射执行器定义规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:56:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.ObjectDataModel;

/**
 * 为模型结构映射执行器定义规范
 */
public interface IStructMappingExecutor {

    /**
     * 执行结构映射
     *
     * @param model 对象数据模型
     */
    void execute(ObjectDataModel model);
}
