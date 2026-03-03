/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联的关联端的配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 16:29:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.AssociationEndMapping;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 关联端配置
 */
public class AssociationEndConfigurationGeneric<TAssociation, TEntity> extends AssociationEndConfiguration<TAssociation> implements IAssociationEndConfigurator {

    /**
     * 反射建模加入的映射
     */
    protected final HashSet<String> reflectAddedMapping = new HashSet<>();
    /**
     * 基于当前关联定义的关联引用的配置器
     */
    private AssociationReferenceConfiguration<TEntity> associationReferenceConfiguration;

    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param dataType          关联端的实体类型
     * @param associationType   关联型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    public AssociationEndConfigurationGeneric(String name, Class<TEntity> dataType, Class<TAssociation> associationType, AssociationTypeConfiguration<TAssociation> typeConfiguration) {
        super(name, dataType, associationType, typeConfiguration);
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
        return this.associationReferenceConfiguration;
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
            this.hasDefaultAsNew(defaultAsNew);
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
            this.isAggregated(isAggregated);
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
        if (this.mappings == null)
            this.mappings = new ArrayList<>();
        if (override)
            this.mappings.clear();

        //检查当前端的映射是否包含键属性
        try {
            Utils.getProperty(this.entityType, keyAttribute);
        } catch (Exception e1) {
            throw new IllegalArgumentException("关联型" + this.structuralType.getName() + "的关联端[" + this.entityType.getName() + "]" + this.getName() + "中不包含键属性" + keyAttribute);
        }

        String keys = keyAttribute.toLowerCase() + "/" + targetField.toLowerCase();
        //没有任何映射 直接加入
        if (this.mappings.size() == 0) {
            AssociationEndMapping mapping = new AssociationEndMapping();
            mapping.setKeyAttribute(keyAttribute);
            mapping.setTargetField(targetField);
            this.mappings.add(mapping);
            //记录一下 是由反射加入的
            this.reflectAddedMapping.add(keys);
        } else {
            //当前Mapping内的所有映射
            String[] exKeys = this.mappings.stream().map(p -> p.getKeyAttribute().toLowerCase() + "/" + p.getTargetField().toLowerCase()).sorted().toArray(String[]::new);
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
            List<AssociationEndConfiguration<TAssociation>> endConfigs = new ArrayList<>();

            for (Object configuration : this.typeConfiguration.getElementConfigurations().values()) {
                if (configuration instanceof AssociationEndConfiguration) {
                    endConfigs.add((AssociationEndConfiguration<TAssociation>) configuration);
                }
            }
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
        return this.associationReferenceConfiguration;
    }

    /**
     * 配置关联端映射
     *
     * @param get         代表关联端实体型的标识属性的表达式
     * @param targetField 上述标识属性的映射字段
     * @param <TProperty> 字段的类型
     * @return 自身
     */
    public <TProperty> AssociationEndConfigurationGeneric<TAssociation, TEntity> hasMapping(SerializedFunction<TEntity, TProperty> get, String targetField) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            this.hasMapping(memberName, targetField);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }

        return this;
    }

    /**
     * 进入当前元素所属类型的配置项
     *
     * @return 所属类型的配置项
     */
    public StructuralTypeConfiguration<TEntity> upward() {
        return (StructuralTypeConfiguration<TEntity>) this.typeConfiguration;
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置，如果相应的配置项未创建则新建一个。
     *
     * @param get lambda表达式
     * @return 关联引用配置
     */
    public AssociationReferenceConfiguration<TEntity> associationReference(SerializedFunction<TEntity, TAssociation> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            return this.associationReference(memberExpression.getProperty());
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置，如果相应的配置项未创建则新建一个。
     *
     * @param name       名称
     * @param isMultiple 是否为多重的
     * @return 关联引用配置
     */
    public AssociationReferenceConfiguration<TEntity> associationReference(String name, boolean isMultiple) {
        Property property = Utils.getProperty(this.entityType, name);
        return this.associationReference(property, isMultiple);
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置，如果相应的配置项未创建则新建一个。
     *
     * @param propInfo 属性
     * @return 关联引用配置
     */
    private AssociationReferenceConfiguration<TEntity> associationReference(Property propInfo) {
        return this.associationReference(propInfo, null);
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置，如果相应的配置项未创建则新建一个。
     *
     * @param propInfo 属性
     * @param multi    是否为多重的
     * @return 关联引用配置
     */
    private AssociationReferenceConfiguration<TEntity> associationReference(Property propInfo, Boolean multi) {
        //没有配置的情况下 才创建新的配置
        if (this.associationReferenceConfiguration == null) {
            //名称
            String name = propInfo.getName();

            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();

            //是否集合属性
            boolean isMultiple = Utils.getIsMultiple(propInfo, type);
            if (multi != null) {
                if (isMultiple != multi)
                    isMultiple = multi;
            }


            //首先 查找端的配置
            EntityTypeConfiguration<TEntity> entityTypeConfiguration = (EntityTypeConfiguration<TEntity>) this.typeConfiguration.getModelBuilder().findConfiguration(this.entityType);

            if (entityTypeConfiguration == null)
                throw new IllegalArgumentException("类型为" + this.entityType + "的实体型未注册");

            //创建关联应用配置类型
            AssociationReferenceConfigurationGeneric<?, EntityTypeConfiguration<TEntity>> configuration = new AssociationReferenceConfigurationGeneric<>(name, this.structuralType, isMultiple, (Class<TEntity>) this.entityType, entityTypeConfiguration);

            //取值器设值器
            Utils.configureValueGetterAndSetter(propInfo, configuration);

            //配置
            configuration.hasLeftEnd(StringUtils.capitalize(this.name));
            //保存
            this.associationReferenceConfiguration = (AssociationReferenceConfiguration<TEntity>) configuration;
            //加入实体型的配置
            entityTypeConfiguration.addAssociationReference(this.associationReferenceConfiguration);
        }

        return this.associationReferenceConfiguration;
    }
}
