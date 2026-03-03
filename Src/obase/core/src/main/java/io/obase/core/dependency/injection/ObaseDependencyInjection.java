/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase依赖注入器,Obase依赖注入的入口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:43:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

import io.obase.core.ObjectContext;

/**
 * Obase依赖注入器
 */
public class ObaseDependencyInjection {

    /**
     * 创建Obase依赖注入的建造器
     *
     * @param contextType 所属的上下文类型
     * @return Obase依赖注入的建造器
     */
    public static <TContext extends ObjectContext> ServiceContainerBuilder createBuilder(Class<TContext> contextType) {
        return new ServiceContainerBuilder(contextType);
    }
}
