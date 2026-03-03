/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型扩展的配置器,根据配置生成类型扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:55:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.TypeExtension;

/**
 * 类型扩展的配置器
 */
public abstract class TypeExtensionConfiguration {

    /**
     * 获取类型扩展的类型
     *
     * @return 类型扩展的类型
     */
    public abstract Class<? extends TypeExtension> getExtensionType();

    /**
     * 根据配置元数据生成类型扩展实例
     *
     * @return 类型扩展实例
     */
    public abstract TypeExtension makeExtension();
}
