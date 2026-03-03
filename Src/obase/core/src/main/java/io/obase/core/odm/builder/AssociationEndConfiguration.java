/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联的关联端的配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 15:54:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Utils;
import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 关联端的配置
 */
public abstract class AssociationEndConfiguration<TAssociation>
        extends ReferenceElementConfiguration<TAssociation, AssociationEndConfiguration<TAssociation>> {

    /**
     * 元素类型
     */
    private final EElementType elementType;
    /**
     * 指示当前关联端是否作为伴随端
     */
    protected boolean isCompanionEnd;
    /**
     * 关联端映射集合
     */
    protected List<AssociationEndMapping> mappings;
    /**
     * 端的CLR类型
     */
    protected Class<?> entityType;
    /**
     * 指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化
     */
    private boolean defaultAsNew;
    /**
     * 指示当前关联端是否为聚合关联端
     */
    private boolean isAggregated;

    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param dataType          关联端的实体类型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    protected AssociationEndConfiguration(String name, Class<?> dataType, Class<TAssociation> associationType, AssociationTypeConfiguration<TAssociation> typeConfiguration) {
        super(name, false, typeConfiguration, associationType);

        this.entityType = dataType;
        this.mappings = new ArrayList<>();
        this.elementType = EElementType.AssociationEnd;
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    @Override
    public EElementType getElementType() {
        return this.elementType;
    }

    /**
     * 触发器集合
     *
     * @return 触发器集合
     */
    @Override
    public List<IBehaviorTrigger> getBehaviorTriggers() {
        if (this.LoadingTriggers == null)
            this.LoadingTriggers = new ArrayList<>();
        return this.LoadingTriggers;
    }

    /**
     * 获取端的CLR类型
     *
     * @return 端的CLR类型
     */
    public Class<?> getEntityType() {
        return this.entityType;
    }

    /**
     * 获取是否为伴随关联端
     *
     * @return 是否为伴随关联端
     */
    public boolean getIsCompanionEnd() {
        return this.isCompanionEnd;
    }

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端
     *
     * @param isAggregated 指示当前关联端是否为聚合关联端
     * @return 自身
     */
    public AssociationEndConfiguration<TAssociation> isAggregated(boolean isAggregated) {
        this.isAggregated = isAggregated;
        return this;
    }

    /**
     * 设置一个值地，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     * @return 自身
     */
    public AssociationEndConfiguration<TAssociation> hasDefaultAsNew(boolean defaultAsNew) {
        this.defaultAsNew = defaultAsNew;
        return this;
    }

    /**
     * 设置关联端映射。
     * 每次调用此方法将追加一个关联端映射
     * 其中keyAttribute为此端的键属性
     * targetField为此端的键属性在关联表中映射的字段
     *
     * @param keyAttribute 此端的标志属性
     * @param targetField  此段在关联表内的映射属性
     * @return 自身
     */
    public AssociationEndConfiguration<TAssociation> hasMapping(String keyAttribute, String targetField) {
        if (this.mappings == null)
            this.mappings = new ArrayList<>();

        //检查当前端的映射是否包含键属性
        try {
            Utils.getProperty(this.entityType, keyAttribute);
        } catch (Exception e1) {
            throw new IllegalArgumentException("关联型" + this.structuralType.getName() + "的关联端[" + this.entityType.getName() + "]" + this.getName() + "中不包含键属性" + keyAttribute);
        }

        if (this.mappings.size() == 0 || this.mappings.stream().allMatch(p -> !p.getTargetField().equalsIgnoreCase(targetField) && !p.getKeyAttribute().equalsIgnoreCase(keyAttribute))) {
            AssociationEndMapping mapping = new AssociationEndMapping();
            mapping.setTargetField(targetField);
            mapping.setKeyAttribute(keyAttribute);
            this.mappings.add(mapping);
        }
        return this;
    }

    /**
     * 指示是否将当前关联端作为伴随端。
     * 设置当前端为伴随端会将之前设值的伴随端改设不作为伴随端
     *
     * @param asCompanion 是否为伴随端
     * @return 自身
     */
    public AssociationEndConfiguration<TAssociation> asCompanion(boolean asCompanion) {
        //如果设置为伴随端 则先将其他端都设置为不作为伴随端
        if (asCompanion) {
            List<AssociationEndConfiguration<TAssociation>> endConfigs = new ArrayList<>();

            for (Object configuration : this.typeConfiguration.getElementConfigurations().values()) {
                if (configuration instanceof AssociationEndConfiguration) {
                    AssociationEndConfiguration<TAssociation> endConfiguration = (AssociationEndConfiguration<TAssociation>) configuration;
                    endConfigs.add(endConfiguration);
                }
            }
            for (AssociationEndConfiguration<TAssociation> endConfig : endConfigs)
                endConfig.asCompanion(false);
        }

        this.isCompanionEnd = asCompanion;
        return this;
    }

    /**
     * 根据元素配置项包含的元数据信息创建元素实例
     *
     * @param objectModel 对象模型
     * @return 类型元素
     */
    @Override
    public TypeElement createReally(ObjectDataModel objectModel) {
        EntityType endEntityType = objectModel.getEntityType(this.entityType);

        //检查当前端的实体类型是否在模型中注册
        if (endEntityType == null)
            throw new IllegalArgumentException(this.entityType.getName() + "未在模型中注册.");

        //根据配置项数据创建模型对象并设值
        AssociationEnd end = new AssociationEnd(this.getName(), endEntityType);
        end.setMappings(this.mappings);
        end.setLoadingTriggers(this.getBehaviorTriggers());
        end.setIsMultiple(this.isMultiple);
        end.setValueGetter(this.getValueGetter());
        end.setValueSetter(this.getValueSetter());
        end.setDefaultAsNew(this.defaultAsNew);
        end.setLoadingTriggers(this.LoadingTriggers);
        end.setIsAggregated(this.isAggregated);

        return end;
    }
}
