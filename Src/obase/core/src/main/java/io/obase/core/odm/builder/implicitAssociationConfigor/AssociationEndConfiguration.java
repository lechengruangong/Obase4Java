/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于隐式关联的关联端配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-25 11:23:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.common.ActionWithTwoArg;
import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.EElementType;
import io.obase.core.odm.builder.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * 适用于隐式关联的关联端配置器
 */
public abstract class AssociationEndConfiguration extends TypeElementConfiguration implements IAssociationEndConfigurator {

    /**
     * 关联端映射集合
     */
    protected final List<AssociationEndMapping> mappings = new ArrayList<>();
    /**
     * 反射建模加入的映射
     */
    private final HashSet<String> reflectAddedMapping = new HashSet<>();
    /**
     * 行为触发器
     */
    protected List<IBehaviorTrigger> behaviorTriggers = new ArrayList<>();
    /**
     * 是否启用延迟加载
     */
    protected boolean enableLazyLoading;

    /**
     * 关联端在关联型上的索引号（从1开始计数）
     */
    protected byte endIndex;

    /**
     * 关联端的实体Clr类型
     */
    protected Class<?> entityType;

    /**
     * 指示当前关联端是否为聚合关联端
     */
    protected boolean isAggregated;
    /**
     * 获取该关联端上基于当前关联定义的关联引用
     */
    protected IAssociationReferenceConfigurator referenceConfigurator;
    /**
     * 关联配置器建造器，用于建造隐式关联配置器。
     */
    protected AssociationConfiguratorBuilder associationConfiguratorBuilder;
    /**
     * 指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     */
    protected boolean defaultAsNew;
    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     */
    protected int loadingPriority;
    /**
     * 指示当前关联端是否作为伴随端
     */
    private boolean isCompanionEnd;

    /**
     * 指示当前关联端是否作为伴随端
     *
     * @return 当前关联端是否作为伴随端
     */
    public boolean getIsCompanionEnd() {
        return this.isCompanionEnd;
    }

    /**
     * 关联端在关联型上的索引号（从1开始计数）
     *
     * @return 关联端在关联型上的索引号
     */
    public byte getEndIndex() {
        return this.endIndex;
    }

    /**
     * 获取关联端的实体Clr类型
     *
     * @return 关联端的实体Clr类型
     */
    public Class<?> getEntityType() {
        return this.entityType;
    }

    /**
     * 端的ClrType
     *
     * @return 端的ClrType
     */
    @Override
    public Class<?> getEntityTypeI() {
        return this.getEntityType();
    }

    /**
     * 获取该关联端上基于当前关联定义的关联引用。
     *
     * @return 当前关联定义的关联引用
     */
    @Override
    public IAssociationReferenceConfigurator getReferenceConfiguratorI() {
        return this.referenceConfigurator;
    }

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化(覆盖现有配置)
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     */
    @Override
    public void hasDefaultAsNewI(boolean defaultAsNew) {
        this.hasDefaultAsNewI(defaultAsNew, true);
    }

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     * @param override     是否覆盖既有配置
     */
    @Override
    public void hasDefaultAsNewI(boolean defaultAsNew, boolean override) {
        if (override)
            this.defaultAsNew = defaultAsNew;
    }

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端(覆盖现有配置)
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     */
    @Override
    public void isAggregatedI(boolean isAggregated) {
        this.isAggregatedI(isAggregated, true);
    }

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     * @param override     是否覆盖既有配置
     */
    @Override
    public void isAggregatedI(boolean isAggregated, boolean override) {
        if (override)
            this.isAggregated = isAggregated;
    }

    /**
     * 配置关联端映射(覆盖现有配置)
     *
     * @param keyAttribute 关联端标识属性的名称
     * @param targetField  上述标识属性的映射字段
     */
    @Override
    public void hasMappingI(String keyAttribute, String targetField) {
        this.hasMappingI(keyAttribute, targetField, true);
    }

    /**
     * 配置关联端映射
     *
     * @param keyAttribute 关联端标识属性的名称
     * @param targetField  上述标识属性的映射字段
     * @param override     是否覆盖既有配置
     */
    @Override
    public void hasMappingI(String keyAttribute, String targetField, boolean override) {
        if (override)
            this.mappings.clear();

        //检查当前端的映射是否包含键属性
        try {
            Utils.getProperty(this.entityType, keyAttribute);
        } catch (Exception e1) {
            throw new IllegalArgumentException("关联端[" + this.entityType.getName() + "]" + this.getName() + "中不包含键属性" + keyAttribute);
        }

        String keys = keyAttribute.toUpperCase() + "/" + targetField.toUpperCase();
        //没有任何映射 直接加入
        if (this.mappings.size() == 0) {
            AssociationEndMapping mapping = new AssociationEndMapping();
            mapping.setTargetField(targetField);
            mapping.setKeyAttribute(keyAttribute);
            this.mappings.add(mapping);
            //记录一下 是由反射加入的
            this.reflectAddedMapping.add(keys);
        } else {
            //当前Mapping内的所有映射
            String[] exKeys = this.mappings.stream().map(p -> p.getKeyAttribute().toUpperCase() + "/" + p.getTargetField().toUpperCase()).sorted().toArray(String[]::new);
            String[] existKeys = this.reflectAddedMapping.stream().sorted().toArray(String[]::new);
            boolean flag = Utils.sequenceEqual(existKeys, exKeys);
            //如果由反射加入的集合与当前Mapping集合一一对应
            if (flag) {
                //就可以加入
                this.hasMapping(keyAttribute, targetField);
                //记录一下 是由反射加入的
                this.reflectAddedMapping.add(keys);
            }
            //否则 不加入 因为当前Mapping内是由其他方式加入的 不可以覆盖
        }
    }

    /**
     * 指示是否将当前关联端作为伴随端
     * 设置当前端为伴随端会将之前设置的伴随端改设不作为伴随端。
     * 当override为false时，其它端只要任意一端已设置为伴随端，本方法就不再执行设置操作。
     *
     * @param value 是否伴随
     */
    @Override
    public void asCompanionI(boolean value) {
        this.asCompanionI(value, true);
    }

    /**
     * 指示是否将当前关联端作为伴随端
     * 设置当前端为伴随端会将之前设置的伴随端改设不作为伴随端。
     * 当override为false时，其它端只要任意一端已设置为伴随端，本方法就不再执行设置操作。
     *
     * @param value    是否伴随
     * @param override 是否覆盖既有配置
     */
    @Override
    public void asCompanionI(boolean value, boolean override) {
        if (override) {
            this.asCompanion(value);
        } else {
            //如果任意一个端都不是伴随
            List<AssociationEndConfiguration> endConfigs = this.associationConfiguratorBuilder.getEndConfigurations();
            if (endConfigs.stream().noneMatch(AssociationEndConfiguration::getIsCompanionEnd))
                this.asCompanion(value);
        }
    }

    /**
     * 生成基于当前关联定义的关联引用的配置器，如果配置器已存在返回该配置器。
     *
     * @param propInfo 返回关联引用的访问器，如果关联引用没有访问器返回null
     * @return 当前关联定义的关联引用的配置器
     */
    @Override
    public IAssociationReferenceConfigurator associationReferenceI(ObjectReferencePack<Property> propInfo) {
        propInfo.realValue = Utils.getProperty(this.entityType, this.name);
        return this.referenceConfigurator;
    }

    /**
     * 是否已启用延迟加载
     *
     * @return 已启用延迟加载
     */
    @Override
    public boolean getEnableLazyLoadingI() {
        return this.enableLazyLoading;
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
        }
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), memberName);
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
        }
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), this.name);
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
            if (this.behaviorTriggers == null)
                this.behaviorTriggers = new ArrayList<>();
            this.behaviorTriggers.clear();
        }
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), this.name);
        this.hasLoadingTrigger(property, EBehaviorTriggerType.PropertyGet);
    }

    /**
     * 为元素配置项设置一个扩展配置器
     *
     * @param configType 扩展配置器的类型，须继承自ElementExtensionConfiguration
     * @return 扩展配置器
     */
    @Override
    public <TExtensionConfiguration extends ElementExtensionConfiguration> ElementExtensionConfiguration hasExtensionI(Class<TExtensionConfiguration> configType) {
        return this.hasExtension(configType);
    }

    /**
     * 为类型元素设置取值器(覆盖现有配置)
     *
     * @param valueGetter 取值器
     */
    @Override
    public void hasValueGetterI(IValueGetter valueGetter) {
        this.hasValueGetterI(valueGetter, true);
    }

    /**
     * 为类型元素设置取值器
     *
     * @param valueGetter 取值器
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(IValueGetter valueGetter, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(valueGetter);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(valueGetter);
        }
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器(覆盖现有配置)
     *
     * @param method 获取元素值的方法
     */
    @Override
    public void hasValueGetterI(Method method) {
        this.hasValueGetterI(method, true);
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器
     *
     * @param method   获取元素值的方法
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Method method, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(method);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(method);
        }
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器(覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    @Override
    public void hasValueGetterI(Property property) {
        this.hasValueGetterI(property, true);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Property property, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(property);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(property);
        }
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    @Override
    public void hasValueGetterI(Field field) {
        this.hasValueGetterI(field, true);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(Field field, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(field);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(field);
        }
    }

    /**
     * 使用指定的类成员为类型元素创建取值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    @Override
    public void hasValueGetterI(String memberName) {
        this.hasValueGetterI(memberName, true);
    }

    /**
     * 使用指定的类成员为类型元素创建取值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(String memberName, boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(memberName);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(memberName);
        }
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器(覆盖现有配置)
     */
    @Override
    public void hasValueGetterI() {
        this.hasValueGetterI(true);
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建取值器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueGetterI(boolean override) {
        //如果覆盖了既有配置，则直接设置取值器
        if (override)
            this.hasValueGetter(this.name);
        else {
            //不覆盖的情形 如果没有设置过取值器，则设置取值器
            if (this.getValueGetter() == null)
                this.hasValueGetter(this.name);
        }
    }

    /**
     * 为类型元素设置设值器(覆盖现有配置)
     *
     * @param valueSetter 设值器
     */
    @Override
    public void hasValueSetterI(IValueSetter valueSetter) {
        this.hasValueSetterI(valueSetter, true);
    }

    /**
     * 为类型元素设置设值器
     *
     * @param valueSetter 设值器
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(IValueSetter valueSetter, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(valueSetter);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(valueSetter);
        }
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器(覆盖现有配置)
     *
     * @param method 为类型元素设值的方法
     * @param mode   设值模式
     */
    @Override
    public void hasValueSetterI(Method method, EValueSettingMode mode) {
        this.hasValueSetterI(method, mode, true);
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器
     *
     * @param method   为类型元素设值的方法
     * @param mode     设值模式
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Method method, EValueSettingMode mode, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(method, mode);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(method, mode);
        }
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器覆盖现有配置)
     *
     * @param property 获取元素值的属性访问器
     */
    @Override
    public void hasValueSetterI(Property property) {
        this.hasValueSetterI(property, true);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建设值器
     *
     * @param property 获取元素值的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Property property, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(property);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(property);
        }
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器(覆盖现有配置)
     *
     * @param field 表示类型元素的字段
     */
    @Override
    public void hasValueSetterI(Field field) {
        this.hasValueSetterI(field, true);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field    表示类型元素的字段
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(Field field, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(field);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(field);
        }
    }

    /**
     * 使用指定的类成员为类型元素创建设值器(覆盖现有配置)
     *
     * @param memberName 成员的名称
     */
    @Override
    public void hasValueSetterI(String memberName) {
        this.hasValueSetterI(memberName, true);
    }

    /**
     * 使用指定的类成员为类型元素创建设值器
     *
     * @param memberName 成员的名称
     * @param override   是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(String memberName, boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(memberName);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(memberName);
        }
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器(覆盖现有配置)
     */
    @Override
    public void hasValueSetterI() {
        this.hasValueSetterI(true);
    }

    /**
     * 使用与类型元素同名的属性访问器为类型元素创建设值器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasValueSetterI(boolean override) {
        //如果覆盖了既有配置，则直接设置设值器
        if (override)
            this.hasValueSetter(this.name);
        else {
            //不覆盖的情形 如果没有设置过设值器，则设置取值器
            if (this.getValueSetter() == null)
                this.hasValueSetter(this.name);
        }
    }

    /**
     * 进入当前元素所属类型的配置项
     *
     * @return 元素所属类型的配置项
     */
    @Override
    public IStructuralTypeConfigurator upwardI() {
        return (IStructuralTypeConfigurator) this.typeConfiguration;
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    @Override
    public EElementType getElementType() {
        return EElementType.AssociationEnd;
    }

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     * @return 自身
     */
    public AssociationEndConfiguration hasDefaultAsNew(boolean defaultAsNew) {
        this.defaultAsNew = defaultAsNew;
        return this;
    }

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端。
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     * @return 自身
     */
    public AssociationEndConfiguration isAggregated(boolean isAggregated) {
        this.isAggregated = isAggregated;
        return this;
    }

    /**
     * 配置关联端映射
     *
     * @param keyAttribute 关联端标识属性的名称
     * @param targetField  上述标识属性的映射字段
     * @return 自身
     */
    public AssociationEndConfiguration hasMapping(String keyAttribute, String targetField) {

        //检查当前端的映射是否包含键属性
        try {
            Utils.getProperty(this.entityType, keyAttribute);
        } catch (Exception e1) {
            throw new IllegalArgumentException("关联端[" + this.entityType.getName() + "]" + this.getName() + "中不包含键属性" + keyAttribute);
        }

        if (this.mappings.size() == 0 || this.mappings.stream().allMatch(p -> !p.getTargetField().equalsIgnoreCase(targetField) && !p.getKeyAttribute().equalsIgnoreCase(keyAttribute))) {
            AssociationEndMapping mapping = new AssociationEndMapping();
            mapping.setKeyAttribute(keyAttribute);
            mapping.setTargetField(targetField);
            this.mappings.add(mapping);
        }
        return this;
    }

    /**
     * 指示是否将当前关联端作为伴随端
     * 设置当前端为伴随端会将之前设置的伴随端改设不作为伴随端。
     *
     * @param value 指示是否作为伴随端
     * @return 自身
     */
    public AssociationEndConfiguration asCompanion(boolean value) {
        //如果设置为伴随端 则先将其他端都设置为不作为伴随端
        if (value) {
            List<AssociationEndConfiguration> endConfigs = this.associationConfiguratorBuilder.getEndConfigurations();
            for (AssociationEndConfiguration endConfig : endConfigs) {
                endConfig.asCompanion(false);
            }
        }

        this.isCompanionEnd = value;

        return this;
    }

    /**
     * 为类型元素设置取值器
     *
     * @param valueGetter 取值器
     * @return 自身
     */
    public AssociationEndConfiguration hasValueGetter(IValueGetter valueGetter) {
        this.setValueGetter(valueGetter);
        return this;
    }

    /**
     * 使用一个能够获取类型元素值的方法为类型元素创建取值器。
     * 如果该方法的返回值类型与元素的IsMultiple属性不匹配，则引发异常
     *
     * @param method 获取元素值的方法
     * @return 自身
     */
    public AssociationEndConfiguration hasValueGetter(Method method) {
        if (!this.isMultiple) {
            if (Iterable.class.isAssignableFrom(method.getReturnType()))
                throw new IllegalArgumentException(String.format("%s方法与目标多重性不一致.", method.getName()));
        } else {
            if (!Iterable.class.isAssignableFrom(method.getReturnType()))
                throw new IllegalArgumentException(String.format("%s方法与目标多重性不一致.", method.getName()));
        }

        IValueGetter getter = Utils.makeDelegateValueGetter(method);
        return this.hasValueGetter(getter);
    }

    /**
     * 使用一个能够获取类型元素值的属性访问器为类型元素创建取值器
     *
     * @param property 获取元素值的属性访问器
     * @return 自身
     */
    public AssociationEndConfiguration hasValueGetter(Property property) {
        return this.hasValueGetter(property.getGetterMethod());
    }

    /**
     * 使用表示类型元素的字段为类型元素创建取值器
     *
     * @param field 表示类型元素的字段
     * @return 自身
     */
    public AssociationEndConfiguration hasValueGetter(Field field) {
        //是Iterable并且不是string 则认为是多重的
        if ((Iterable.class.isAssignableFrom((Class<?>) field.getGenericType()) && !field.getGenericType().equals(String.class)) != this.isMultiple)
            throw new IllegalArgumentException(String.format("%s与目标的多重性不一致.", field.getName()));

        FieldValueGetter filedGetter = new FieldValueGetter(field);

        return this.hasValueGetter(filedGetter);
    }

    /**
     * 使用指定的类成员为类型元素创建取值器。
     *
     * @param memberName 成员的名称
     * @return 自身
     */
    public AssociationEndConfiguration hasValueGetter(String memberName) {
        try {
            Field field = this.associationConfiguratorBuilder.getAssociationType().getField(memberName);
            return this.hasValueGetter(field);
        } catch (NoSuchFieldException e) {
            try {
                Method method = this.associationConfiguratorBuilder.getAssociationType().getMethod(memberName);
                return this.hasValueGetter(method);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException(String.format("%s无法获取到成员.", memberName));
            }
        }
    }

    /**
     * 使用一个能够获取类型元素的值且返回值为单个对象的委托为不具备多重性的类型元素创建取值器
     *
     * @param getValue    获取元素值的委托
     * @param <TProperty> 表示元素的类型。对于属性，它表示属性值类型；对于关联引用，它表示关联类型；对于关联端，它表示关联端的类型。
     * @return 自身
     */
    public <TStructural, TProperty> AssociationEndConfiguration hasValueGetter(FunctionWithOneArg<TStructural, TProperty> getValue) {
        if (this.isMultiple)
            throw new IllegalArgumentException(String.format("%s类型的设值器为多重性,不能设置单一设值器.", this.getName()));
        DelegateValueGetter<TStructural, TProperty> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasValueGetter(valueGetter);
    }

    /**
     * 使用一个能够获取类型元素的值且返回值为对象序列的委托为具备多重性的类型元素创建取值器。
     *
     * @param getValue    获取元素值的委托
     * @param <TProperty> 表示元素的类型。对于属性，它表示属性值类型；对于关联引用，它表示关联类型；对于关联端，它表示关联端的类型。
     * @return 自身
     */
    public <TStructural, TProperty> AssociationEndConfiguration hasValueGetterMultiple(FunctionWithOneArg<TStructural, Iterable<TProperty>> getValue) {
        if (!this.isMultiple)
            throw new IllegalArgumentException(String.format("%s类型的设值器为单一性,不能设置多重设值器.", this.getName()));
        DelegateValueGetter<TStructural, Iterable<TProperty>> valueGetter = new DelegateValueGetter<>(getValue);
        return this.hasValueGetter(valueGetter);
    }


    /**
     * 设置设值器
     *
     * @param valueSetter 对象设值器接口
     * @return 自身
     */
    public AssociationEndConfiguration hasValueSetter(IValueSetter valueSetter) {
        this.setValueSetter(valueSetter);
        return this;
    }

    /**
     * 使用一个能够为类型元素设值的方法为类型元素创建设值器
     *
     * @param method 为类型元素设值的方法
     * @param mode   设值模式
     * @return 自身
     */
    public AssociationEndConfiguration hasValueSetter(Method method, EValueSettingMode mode) {
        if (method.getParameterCount() != 1)
            throw new IllegalArgumentException("设值器方法只能有一个参数");

        this.setValueSetter(ValueSetter.create(method, mode));
        return this;
    }

    /**
     * 使用一个能够为类型元素设值的Property为类型元素创建设值器
     *
     * @param property 为类型元素设值的属性访问器
     * @return 自身
     */
    public AssociationEndConfiguration hasValueSetter(Property property) {
        Method setMethod = property.getSetterMethod();
        return this.hasValueSetter(setMethod, EValueSettingMode.Assignment);
    }

    /**
     * 使用表示类型元素的字段为类型元素创建设值器
     *
     * @param field 字段
     * @return 自身
     */
    public AssociationEndConfiguration hasValueSetter(Field field) {
        return this.hasValueSetter(ValueSetter.create(field));
    }

    /**
     * 使用指定的类成员为类型元素创建设值器
     *
     * @param memberName 成员的名称
     * @return 自身
     */
    public AssociationEndConfiguration hasValueSetter(String memberName) {

        try {
            Field field = this.associationConfiguratorBuilder.getAssociationType().getField(memberName);
            return this.hasValueSetter(field);
        } catch (NoSuchFieldException e) {
            try {
                Method method = this.associationConfiguratorBuilder.getAssociationType().getMethod(memberName);
                //此处无法确定eValueSettingMode
                throw new IllegalArgumentException(String.format("%s暂不支持用Method构造设值器", method.getName()));
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException(String.format("%s无法获取到成员.", memberName));
            }
        }
    }

    /**
     * 为lambda表达式指示的元素创建设值器，该lambda表达式的主体须为MemberExpression，其访问的成员代表要设值的元素
     *
     * @param propertyExp  表示属性访问器的Lambda表达式
     * @param valueCreator 元素创建委托
     * @param <TProperty>  作为lambda表达式主体的MemberExpression的类型
     * @param <TElement>   值序列项的类型
     * @return 自身
     */
    public <TStructural, TProperty extends Iterable<TElement>, TElement> AssociationEndConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TProperty> propertyExp, Function<Iterable<TElement>, TProperty> valueCreator) {

        return this.hasValueSetter(new DelegateEnumerableValueSetterWithThreeArgs<>(propertyExp, valueCreator));
    }

    /**
     * 为lambda表达式指示的元素创建设值器，该lambda表达式的主体须为MemberExpression，其访问的成员代表要设值的元素
     *
     * @param propertyExp 表达式
     * @param <TProperty> 作为lambda表达式主体的MemberExpression的类型，亦即元素值的类型
     * @return 自身
     */
    public <TStructural, TProperty> AssociationEndConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TProperty> propertyExp) {
        return this.hasValueSetter(propertyExp, EValueSettingMode.Assignment);
    }

    /**
     * 使用能够修改元素值的委托为类型元素创建设值器
     *
     * @param setValue 表示属性访问器的Lambda表达式
     * @param mode     设值模式
     * @param <TValue> Assignment模式下为元素值的类型，Appending模式下为元素值序列项的类型
     * @return 自身
     */
    public <TStructural, TValue> AssociationEndConfiguration hasValueSetter(ActionWithTwoArg<TStructural, TValue> setValue, EValueSettingMode mode) {
        return this.hasValueSetter(ValueSetter.create(setValue, mode));
    }

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     *
     * @param loadingPriority 加载优先级
     * @return 自身
     */
    public AssociationEndConfiguration hasLoadingPriority(int loadingPriority) {
        this.loadingPriority = loadingPriority;
        return this;
    }

    /**
     * 设置加载触发器。
     * 每次调用本方法将追加一个加载触发器。
     *
     * @param loadingTrigger 加载触发器
     * @return 自身
     */
    public AssociationEndConfiguration hasLoadingTrigger(IBehaviorTrigger loadingTrigger) {
        if (this.behaviorTriggers == null)
            this.behaviorTriggers = new ArrayList<>();
        this.behaviorTriggers.add(loadingTrigger);
        return this;
    }

    /**
     * 使用一个能触发引用加载的方法为引用元素创建加载触发器。
     * 每次调用本方法将追加一个触发器。
     *
     * @param method 方法
     * @return 自身
     */
    public AssociationEndConfiguration hasLoadingTrigger(Method method) {
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
    public AssociationEndConfiguration hasLoadingTrigger(Property property, EBehaviorTriggerType triggerType) {
        Method method;
        switch (triggerType) {
            case PropertyGet:
                method = property.getGetterMethod();
                break;
            case PropertySet:
                method = property.getSetterMethod();
                break;
            case Method:
                throw new IllegalArgumentException("引用加载触发器不能用PropertyInfo构造");
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
    public AssociationEndConfiguration hasLoadingTrigger(String memberName, EBehaviorTriggerType triggerType) {
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), memberName);
        return this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 使用成员名称为引用元素创建加载触发器
     *
     * @param triggerType 触发类型
     * @return 自身
     */
    public AssociationEndConfiguration hasLoadingTrigger(EBehaviorTriggerType triggerType) {
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), this.name);
        return this.hasLoadingTrigger(property, triggerType);
    }

    /**
     * 为引用元素创建加载触发器
     *
     * @return 自身
     */
    public AssociationEndConfiguration hasLoadingTrigger() {
        Property property = Utils.getProperty(this.associationConfiguratorBuilder.getAssociationType(), this.name);
        return this.hasLoadingTrigger(property, EBehaviorTriggerType.PropertyGet);
    }

    /**
     * 设置是否支持延迟加载
     *
     * @param enableLazyLoading 是否支持延迟加载
     * @return 自身
     */
    public AssociationEndConfiguration hasEnableLazyLoading(boolean enableLazyLoading) {
        this.enableLazyLoading = enableLazyLoading;
        return this;
    }

    /**
     * 设置关联型
     *
     * @param structuralTypeConfiguration 关联型
     */
    public void setAssociationType(StructuralTypeConfiguration<?> structuralTypeConfiguration) {
        this.typeConfiguration = structuralTypeConfiguration;
    }
}
