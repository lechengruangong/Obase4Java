/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联元素配置项,为关联引用配置项和关联端配置项提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-22 17:54:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.IBehaviorTrigger;
import io.obase.core.odm.MethodTrigger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 为关联引用配置项和关联端配置项提供基础实现
 *
 * @param <TConfiguration> 具体的配置项类型
 */
public abstract class ReferenceElementConfiguration<TObject, TConfiguration extends ReferenceElementConfiguration<TObject, TConfiguration>>
        extends TypeElementConfigurationGeneric<TObject, TConfiguration>
        implements ILazyLoadingConfiguration, IReferenceElementConfigurator {

    /**
     * 是否启用延迟加载
     */
    protected boolean enableLazyLoading = false;

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     */
    protected int loadingPriority;

    /**
     * 加载触发器
     */
    protected List<IBehaviorTrigger> LoadingTriggers;

    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param isMultiple        指示元素是否具有多重性，即其值是否为集合
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     * @param structuralType    所属的元素类型 保存类型参数的具体类型
     */
    protected ReferenceElementConfiguration(String name, Boolean isMultiple, StructuralTypeConfiguration<TObject> typeConfiguration, Class<TObject> structuralType) {
        super(name, isMultiple, typeConfiguration, structuralType);
    }

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载
     *
     * @return 加载优先级
     */
    @Override
    public int getLoadingPriority() {
        return this.loadingPriority;
    }

    /**
     * 是否启用延迟加载
     *
     * @return 是否启用延迟加载
     */
    @Override
    public boolean getEnableLazyLoading() {
        return this.enableLazyLoading;
    }

    /**
     * 是否已启用延迟加载
     *
     * @return 已启用延迟加载
     */
    @Override
    public boolean getEnableLazyLoadingI() {
        return this.getEnableLazyLoading();
    }

    /**
     * 设置是否支持延迟加载(覆盖现有配置)
     *
     * @param enableLazyLoading 是否支持延迟加载
     */
    @Override
    public void hasEnableLazyLoadingI(boolean enableLazyLoading) {
        this.hasEnableLazyLoadingI(enableLazyLoading, true);
    }

    /**
     * 设置是否支持延迟加载
     *
     * @param enableLazyLoading 是否支持延迟加载
     * @param override          是否覆盖既有配置
     */
    @Override
    public void hasEnableLazyLoadingI(boolean enableLazyLoading, boolean override) {
        if (override)
            this.hasEnableLazyLoading(enableLazyLoading);
    }

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载(覆盖现有配置)
     *
     * @param loadingPriority 加载优先级
     */
    @Override
    public void hasLoadingPriorityI(int loadingPriority) {
        this.hasLoadingPriorityI(loadingPriority, true);
    }

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载
     *
     * @param loadingPriority 加载优先级
     * @param override        是否覆盖既有配置
     */
    @Override
    public void hasLoadingPriorityI(int loadingPriority, boolean override) {
        if (override)
            this.hasLoadingPriority(loadingPriority);
    }

    /**
     * 设置加载触发器(覆盖现有配置)
     *
     * @param loadingTrigger 加载触发器
     */
    @Override
    public void hasLoadingTriggerI(IBehaviorTrigger loadingTrigger) {
        this.hasLoadingTriggerI(loadingTrigger, true);
    }

    /**
     * 设置加载触发器
     *
     * @param loadingTrigger 加载触发器
     * @param override       是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(IBehaviorTrigger loadingTrigger, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        this.hasLoadingTrigger(loadingTrigger);
    }

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param method 触发引用加载的方法
     */
    @Override
    public void hasLoadingTriggerI(Method method) {
        this.hasLoadingTriggerI(method, true);
    }

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器
     *
     * @param method   触发引用加载的方法
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(Method method, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        this.hasLoadingTrigger(method);
    }

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param property 触发引用加载的属性访问器
     */
    @Override
    public void hasLoadingTriggerI(Property property) {
        this.hasLoadingTriggerI(property, true);
    }

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器
     *
     * @param property 触发引用加载的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(Property property, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        this.hasLoadingTrigger(property, EBehaviorTriggerType.PropertyGet);
    }

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param property    触发引用加载的属性访问器
     * @param triggerType 要创建的加载触发器的类型
     */
    @Override
    public void hasLoadingTriggerI(Property property, EBehaviorTriggerType triggerType) {
        this.hasLoadingTriggerI(property, triggerType, true);
    }

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器
     *
     * @param property    触发引用加载的属性访问器
     * @param triggerType 要创建的加载触发器的类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(Property property, EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 使用成员名称和触发类型为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param memberName  成员名册
     * @param triggerType 触发类型
     */
    @Override
    public void hasLoadingTriggerI(String memberName, EBehaviorTriggerType triggerType) {
        this.hasLoadingTriggerI(memberName, triggerType, true);
    }

    /**
     * 使用成员名称和触发类型为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param memberName  成员名册
     * @param triggerType 触发类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(String memberName, EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        Property property = Utils.getProperty(this.structuralType, memberName);
        this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 使用成员名称为引用元素创建加载触发器(覆盖现有配置)
     *
     * @param triggerType 触发类型
     */
    @Override
    public void hasLoadingTriggerI(EBehaviorTriggerType triggerType) {
        this.hasLoadingTriggerI(triggerType, true);
    }

    /**
     * 使用成员名称为引用元素创建加载触发器
     *
     * @param triggerType 触发类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        Property property = Utils.getProperty(this.structuralType, this.name);
        this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 为引用元素创建加载触发器(覆盖现有配置)
     */
    @Override
    public void hasLoadingTriggerI() {
        this.hasLoadingTriggerI(true);
    }

    /**
     * 为引用元素创建加载触发器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasLoadingTriggerI(boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override) {
            if (this.LoadingTriggers == null)
                this.LoadingTriggers = new ArrayList<>();
            this.LoadingTriggers.clear();
        }
        Property property = Utils.getProperty(this.structuralType, this.name);
        this.hasLoadingTrigger(property, EBehaviorTriggerType.PropertyGet);
    }

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     *
     * @param loadingPriority 加载优先级
     * @return 自身
     */
    public TConfiguration hasLoadingPriority(int loadingPriority) {
        this.loadingPriority = loadingPriority;
        return (TConfiguration) this;
    }

    /**
     * 设置加载触发器。
     * 每次调用本方法将追加一个加载触发器。
     *
     * @param loadingTrigger 加载触发器
     * @return 自身
     */
    public TConfiguration hasLoadingTrigger(IBehaviorTrigger loadingTrigger) {
        if (this.LoadingTriggers == null)
            this.LoadingTriggers = new ArrayList<>();
        this.LoadingTriggers.add(loadingTrigger);
        return (TConfiguration) this;
    }

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器。
     * 每次调用本方法将追加一个触发器。
     *
     * @param method 方法
     * @return 自身
     */
    public TConfiguration hasLoadingTrigger(Method method) {
        MethodTrigger methodTrigger = new MethodTrigger(method);
        return this.hasLoadingTrigger(methodTrigger);
    }

    /**
     * 使用一个能触发引用加载的属性访问器为引用元素创建加载触发器
     *
     * @param property    触发引用加载的属性访问器
     * @param triggerType 要创建的加载触发器的类型
     * @return 引用加载触发器
     */
    public TConfiguration hasLoadingTrigger(Property property, EBehaviorTriggerType triggerType) {
        Method method;
        switch (triggerType) {
            case Method:
                throw new IllegalArgumentException("引用加载触发器不能用PropertyInfo构造");
            case PropertyGet:
                method = property.getGetterMethod();
                break;
            case PropertySet:
                method = property.getSetterMethod();
                break;
            default:
                throw new IllegalArgumentException("未知的行为触发器的类型");
        }
        return this.hasLoadingTrigger(method);
    }

    /**
     * 使用成员名称和触发类型为引用元素创建加载触发器
     *
     * @param memberName  成员名册
     * @param triggerType 触发类型
     * @return 自身
     */
    public TConfiguration hasLoadingTrigger(String memberName, EBehaviorTriggerType triggerType) {
        Property property = Utils.getProperty(this.structuralType, memberName);
        return this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 使用成员名称为引用元素创建加载触发器
     *
     * @param triggerType 触发类型
     * @return 自身
     */
    public TConfiguration hasLoadingTrigger(EBehaviorTriggerType triggerType) {
        Property property = Utils.getProperty(this.structuralType, this.name);
        return this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 为引用元素创建加载触发器
     *
     * @return 自身
     */
    public TConfiguration hasLoadingTrigger() {
        Property property = Utils.getProperty(this.structuralType, this.name);
        return this.hasLoadingTrigger(property, EBehaviorTriggerType.PropertyGet);
    }

    /**
     * 设置是否支持延迟加载
     *
     * @param enableLazyLoading 是否支持延迟加载
     * @return 自身
     */
    public TConfiguration hasEnableLazyLoading(boolean enableLazyLoading) {
        this.enableLazyLoading = enableLazyLoading;
        return (TConfiguration) this;
    }
}
