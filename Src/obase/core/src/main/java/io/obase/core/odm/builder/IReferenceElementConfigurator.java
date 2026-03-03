/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置引用元素的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 16:23:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.odm.IBehaviorTrigger;

import java.lang.reflect.Method;

/**
 * 定义配置引用元素的规范
 */
public interface IReferenceElementConfigurator extends ITypeElementConfigurator {

    /**
     * 是否已启用延迟加载
     *
     * @return 已启用延迟加载
     */
    boolean getEnableLazyLoadingI();

    /**
     * 设置是否支持延迟加载(覆盖现有配置)
     *
     * @param enableLazyLoading 是否支持延迟加载
     */
    void hasEnableLazyLoadingI(boolean enableLazyLoading);

    /**
     * 设置是否支持延迟加载
     *
     * @param enableLazyLoading 是否支持延迟加载
     * @param override          是否覆盖既有配置
     */
    void hasEnableLazyLoadingI(boolean enableLazyLoading, boolean override);

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载(覆盖现有配置)
     *
     * @param loadingPriority 加载优先级
     */
    void hasLoadingPriorityI(int loadingPriority);

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载
     *
     * @param loadingPriority 加载优先级
     * @param override        是否覆盖既有配置
     */
    void hasLoadingPriorityI(int loadingPriority, boolean override);

    /**
     * 设置加载触发器(覆盖现有配置)
     *
     * @param loadingTrigger 加载触发器
     */
    void hasLoadingTriggerI(IBehaviorTrigger loadingTrigger);

    /**
     * 设置加载触发器
     *
     * @param loadingTrigger 加载触发器
     * @param override       是否覆盖既有配置
     */
    void hasLoadingTriggerI(IBehaviorTrigger loadingTrigger, boolean override);

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param method 触发引用加载的方法
     */
    void hasLoadingTriggerI(Method method);

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器
     *
     * @param method   触发引用加载的方法
     * @param override 是否覆盖既有配置
     */
    void hasLoadingTriggerI(Method method, boolean override);

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param property 触发引用加载的属性访问器
     */
    void hasLoadingTriggerI(Property property);

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器
     *
     * @param property 触发引用加载的属性访问器
     * @param override 是否覆盖既有配置
     */
    void hasLoadingTriggerI(Property property, boolean override);

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param property    触发引用加载的属性访问器
     * @param triggerType 要创建的加载触发器的类型
     */
    void hasLoadingTriggerI(Property property, EBehaviorTriggerType triggerType);

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器
     *
     * @param property    触发引用加载的属性访问器
     * @param triggerType 要创建的加载触发器的类型
     * @param override    是否覆盖既有配置
     */
    void hasLoadingTriggerI(Property property, EBehaviorTriggerType triggerType, boolean override);

    /**
     * 使用成员名称和触发类型为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param memberName  成员名册
     * @param triggerType 触发类型
     */
    void hasLoadingTriggerI(String memberName, EBehaviorTriggerType triggerType);

    /**
     * 使用成员名称和触发类型为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param memberName  成员名册
     * @param triggerType 触发类型
     * @param override    是否覆盖既有配置
     */
    void hasLoadingTriggerI(String memberName, EBehaviorTriggerType triggerType, boolean override);

    /**
     * 使用成员名称为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param triggerType 触发类型
     */
    void hasLoadingTriggerI(EBehaviorTriggerType triggerType);

    /**
     * 使用成员名称为引用元素创建加载触发器
     *
     * @param triggerType 触发类型
     * @param override    是否覆盖既有配置
     */
    void hasLoadingTriggerI(EBehaviorTriggerType triggerType, boolean override);

    /**
     * 为引用元素创建加载触发器(覆盖现有配置)
     */
    void hasLoadingTriggerI();

    /**
     * 为引用元素创建加载触发器
     *
     * @param override 是否覆盖既有配置
     */
    void hasLoadingTriggerI(boolean override);
}
