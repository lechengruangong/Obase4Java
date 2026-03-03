/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于隐式关联的关联端配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-25 11:23:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.EntityTypeConfiguration;

import java.util.List;

/**
 * 适用于隐式关联的关联端配置器
 *
 * @param <TEntity> 作为关联端的实体类型
 */
public class AssociationEndConfigurationGeneric<TEntity> extends AssociationEndConfiguration {

    /**
     * 基于当前关联定义的关联引用的配置器
     */
    private io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> associationReferenceConfiguration;

    /**
     * 初始化适用于隐式关联的关联端配置器
     *
     * @param endIndex           关联端在关联型上的索引号（从1开始计数
     * @param assocConfigBuilder 关联配置器建造器
     * @param entityType         端的实体型
     */
    public AssociationEndConfigurationGeneric(byte endIndex, AssociationConfiguratorBuilder assocConfigBuilder, Class<TEntity> entityType) {
        this.entityType = entityType;
        this.endIndex = endIndex;
        this.isMultiple = false;
        this.name = "End" + endIndex;
        this.associationConfiguratorBuilder = assocConfigBuilder;
    }

    /**
     * 获取行为触发器，对于属性是指修改触发器，对于关联引用和关联端是加载触发器
     *
     * @return 行为触发器
     */
    @Override
    public List<IBehaviorTrigger> getBehaviorTriggers() {
        return this.behaviorTriggers;
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置；如果相应的配置项未创建则新建一个。
     *
     * @param name        关联引用名称，它将作为关联引用的键
     * @param isMultiple  关联引用是否具有多重性
     * @param <TReferred> 被引对象组成的元组的类型。被引对象是指关联引用指向的对象，如果关联引用是多重性的，它是指其中的一个。
     * @return 关联引用的配置
     */
    public <TReferred> io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> associationReference(String name, boolean isMultiple) {
        if (this.associationReferenceConfiguration == null) {

            //首先 查找端的配置
            EntityTypeConfiguration<TEntity> entityTypeConfiguration =
                    (EntityTypeConfiguration<TEntity>) this.associationConfiguratorBuilder.getModelBuilder().findConfiguration(this.entityType);

            AssociationReferenceConfiguration<TEntity, TReferred> configuration =
                    new AssociationReferenceConfiguration<>(name, isMultiple, this.endIndex, (Class<TEntity>) this.entityType, this.associationConfiguratorBuilder);

            //保存
            this.associationReferenceConfiguration = configuration;
            this.referenceConfigurator = configuration;
            //加入实体型的配置
            entityTypeConfiguration.addAssociationReference(this.associationReferenceConfiguration);
            //配置左端
            this.referenceConfigurator.hasLeftEndI(this.name);
        }

        return this.associationReferenceConfiguration;
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置；如果相应的配置项未创建则新建一个。
     *
     * @param get       表达式
     * @param <TResult> 引用类型
     * @return 关联引用配置
     */
    public <TResult> io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> associationReference(SerializedFunction<TEntity, TResult> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            Property property = Utils.getProperty(this.entityType, memberName);

            return this.associationReference(property);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 启动对关联端上基于当前关联定义的关联引用的配置，如果相应的配置项未创建则新建一个。
     *
     * @param propInfo 关联引用的访问器
     * @return 关联引用配置
     */
    private io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> associationReference(Property propInfo) {
        if (this.associationReferenceConfiguration == null) {
            //名称
            String name = propInfo.getName();

            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
            //是否集合属性
            boolean isMultiple = Utils.getIsMultiple(propInfo, type);

            //首先 查找端的配置
            EntityTypeConfiguration<TEntity> entityTypeConfiguration =
                    (EntityTypeConfiguration<TEntity>) this.associationConfiguratorBuilder.getModelBuilder().findConfiguration(this.entityType);

            io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> configuration = new AssociationReferenceConfiguration<>(name, isMultiple, this.endIndex, (Class<TEntity>) this.entityType, this.associationConfiguratorBuilder);

            //取值器设值器
            Utils.configureValueGetterAndSetter(propInfo, configuration);

            //保存
            this.associationReferenceConfiguration = configuration;
            this.referenceConfigurator = configuration;
            //加入实体型的配置
            entityTypeConfiguration.addAssociationReference(this.associationReferenceConfiguration);
            //配置左端
            this.referenceConfigurator.hasLeftEndI(this.name);
        }

        return this.associationReferenceConfiguration;
    }

    /**
     * 根据元素配置项包含的元数据信息创建元素实例
     * 本方法由派生类实现
     *
     * @param model 对象数据模型
     * @return 类型元素
     */
    @Override
    public TypeElement createReally(ObjectDataModel model) {
        EntityType endEntityType = model.getEntityType(this.entityType);

        if (endEntityType == null)
            throw new IllegalArgumentException(this.entityType.getName() + "未在模型中注册.");

        //根据配置项数据创建模型对象并设值
        AssociationEnd end = new AssociationEnd(this.getName(), endEntityType);
        end.setValueGetter(this.getValueGetter());
        end.setValueSetter(this.getValueSetter());
        end.setMappings(this.mappings);
        end.setLoadingTriggers(this.getBehaviorTriggers());
        end.setIsMultiple(this.isMultiple);
        end.setDefaultAsNew(this.defaultAsNew);
        end.setLoadingTriggers(this.getBehaviorTriggers());
        end.setIsAggregated(this.isAggregated);

        return end;
    }
}
