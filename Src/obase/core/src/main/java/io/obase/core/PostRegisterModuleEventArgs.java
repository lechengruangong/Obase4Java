/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：注册模块后事件数据.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 17:13:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.mapping.pipeline.IMappingModule;

import java.util.EventObject;

/**
 * PostRegisterModule事件的事件参数
 */
public class PostRegisterModuleEventArgs extends EventObject {

    /**
     * 刚注册的映射模块
     */
    private final IMappingModule module;

    /**
     * 初始化PostRegisterModuleEventArgs的新实例
     *
     * @param source 源
     * @param module 映射模块
     */
    public PostRegisterModuleEventArgs(Object source, IMappingModule module) {
        super(source);
        this.module = module;
    }

    /**
     * 获取刚注册的映射模块
     *
     * @return 映射模块
     */
    public IMappingModule getModule() {
        return this.module;
    }
}
