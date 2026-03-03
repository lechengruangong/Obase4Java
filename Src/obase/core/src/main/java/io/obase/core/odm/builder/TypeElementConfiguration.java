/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型元素配置,提供类型元素配置项提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 10:34:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 为属性配置项、关联引用配置项、关联端配置项提供基础实现。
 */
public abstract class TypeElementConfiguration {

    /**
     * 元素扩展配置器
     */
    protected final List<ElementExtensionConfiguration> extensionConfigs = new ArrayList<>();

    /**
     * 名称
     */
    protected String name;

    /**
     * 创建当前元素配置项的类型配置项
     */
    protected StructuralTypeConfiguration<?> typeConfiguration;
    /**
     * 指示元素是否具有多重性，即其值是否为集合
     */
    protected Boolean isMultiple;
    /**
     * 取值器
     */
    private IValueGetter valueGetter;
    /**
     * 设值器
     */
    private IValueSetter valueSetter;

    /**
     * 名称访问器
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    public abstract EElementType getElementType();

    /**
     * 获取行为触发器，对于属性是指修改触发器，对于关联引用和关联端是加载触发器
     *
     * @return 行为触发器
     */
    public abstract List<IBehaviorTrigger> getBehaviorTriggers();

    /**
     * 获取取值器
     *
     * @return 取值器
     */
    protected IValueGetter getValueGetter() {
        return this.valueGetter;
    }

    /**
     * 设置取值器
     *
     * @param valueGetter 取值器
     */
    protected void setValueGetter(IValueGetter valueGetter) {
        this.valueGetter = valueGetter;
    }

    /**
     * 获取设值器
     *
     * @return 设值器
     */
    protected IValueSetter getValueSetter() {
        return this.valueSetter;
    }

    /**
     * 设置设值器
     *
     * @param valueSetter 设值器
     */
    protected void setValueSetter(IValueSetter valueSetter) {
        this.valueSetter = valueSetter;
    }

    /**
     * 根据元素配置项包含的元数据信息创建元素实例
     *
     * @param model 对象数据模型
     * @return 类型元素
     */
    TypeElement create(ObjectDataModel model) {
        TypeElement typeElement = this.createReally(model);
        for (ElementExtensionConfiguration typeExtensionConfiguration : this.extensionConfigs) {
            typeElement.addExtension(typeExtensionConfiguration.makeExtension());
        }

        return typeElement;
    }

    /**
     * 为元素配置项设置一个指定类型的扩展配置器，如果指定类型的配置器已存在，返回该配置器。
     *
     * @param configType 扩展配置器的类型，须继承自ElementExtensionConfiguration。
     * @return 扩展配置器
     */
    public ElementExtensionConfiguration hasExtension(Class<? extends ElementExtensionConfiguration> configType) {
        try {
            ElementExtensionConfiguration extensionConfiguration = configType.newInstance();
            this.extensionConfigs.add(extensionConfiguration);
            return extensionConfiguration;
        } catch (Exception e) {
            throw new IllegalArgumentException("添加扩展配置器失败," + configType.getName() + "没有适合的无参构造函数", e);
        }
    }

    /**
     * 根据元素配置项包含的元数据信息创建元素实例
     * 本方法由派生类实现
     *
     * @param model 对象数据模型
     * @return 类型元素
     */
    public abstract TypeElement createReally(ObjectDataModel model);
}
