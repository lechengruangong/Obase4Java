/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：扩展构件注册器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:38:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.odm.builder.ModelBuilder;

/**
 * 扩展构件注册器
 */
public interface IAddonRegister {

    /**
     * 为某个插件注册
     *
     * @param modelBuilder 建模器
     */
    void registry(ModelBuilder modelBuilder);
}
