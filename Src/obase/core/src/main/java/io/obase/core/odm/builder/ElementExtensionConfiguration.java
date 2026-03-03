/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元素扩展的配置器,提供类型元素配置的扩展配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:47:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.ElementExtension;

/**
 * 元素扩展的配置器
 */
public abstract class ElementExtensionConfiguration {

    /**
     * 获取元素扩展的类型
     *
     * @return 元素扩展的类型
     */
    public abstract Class<? extends ElementExtension> getExtensionType();

    /**
     * 根据配置元数据生成元素扩展实例
     *
     * @return 元素扩展实例
     */
    public abstract ElementExtension makeExtension();
}
