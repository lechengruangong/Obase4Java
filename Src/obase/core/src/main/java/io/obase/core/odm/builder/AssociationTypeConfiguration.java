/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联型配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 17:19:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.AssociationType;
import io.obase.core.odm.EntityType;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 关联型配置项
 */
public class AssociationTypeConfiguration<TAssociation> extends ObjectTypeConfiguration<TAssociation, AssociationTypeConfiguration<TAssociation>> implements IAssociationTypeConfigurator {

    /**
     * 包含元素配置项
     */
    protected HashMap<String, TypeElementConfiguration> typeElementConfigurations;
    /**
     * 指示是否为显式关联
     */
    private boolean visible = true;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    public AssociationTypeConfiguration(Class<TAssociation> clrType, ModelBuilder modelBuilder) {
        super(clrType, modelBuilder);
    }

    /**
     * 获取所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @return 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    @Override
    protected HashMap<String, TypeElementConfiguration> getElementConfigurations() {
        if (this.typeElementConfigurations == null)
            this.typeElementConfigurations = new HashMap<>();
        return this.typeElementConfigurations;
    }

    /**
     * 设置所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @param typeElementConfigurationList 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    @Override
    protected void setElementConfigurations(HashMap<String, TypeElementConfiguration> typeElementConfigurationList) {
        this.typeElementConfigurations = typeElementConfigurationList;
    }

    /**
     * 标识属性集合
     *
     * @return 标识属性集合
     */
    @Override
    protected List<String> getKeyAttributes() {
        return new ArrayList<>();
    }

    /**
     * 关联型
     *
     * @return 关联型
     */
    @Override
    public Class<?> getAssociationTypeI() {
        return this.clrType;
    }

    /**
     * 关联端集合
     *
     * @return 关联端集合
     */
    @Override
    public IAssociationEndConfigurator[] getAssociationEndsI() {
        return this.getElementConfigurations().values().stream().filter(p -> p instanceof IAssociationEndConfigurator).toArray(IAssociationEndConfigurator[]::new);
    }

    /**
     * 设置是否为显式关联型
     *
     * @param value 是否为显式关联型
     */
    @Override
    public void setIsVisibleI(boolean value) {
        this.setIsVisibleI(value, true);
    }

    /**
     * 设置是否为显式关联型
     *
     * @param value    是否为显式关联型
     * @param override 是否覆盖
     */
    @Override
    public void setIsVisibleI(boolean value, boolean override) {
        if (override) {
            this.visible = value;
        } else {
            //不覆盖的情况下 只有默认值才会被设值
            if (this.visible)
                this.visible = value;
        }
    }

    /**
     * 启动一个关联端配置项，如果要启动的配置项未创建则新建一个
     *
     * @param name       关联端的名称
     * @param entityType 作为关联端的实体类型
     * @return 关联端配置项
     */
    @Override
    public IAssociationEndConfigurator associationEndI(String name, Class<?> entityType) {
        return (IAssociationEndConfigurator) this.associationEnd(name, entityType);
    }

    /**
     * 根据名称获取元素配置器
     *
     * @param name 元素名称
     * @return 类型元素配置器
     */
    @Override
    public ITypeElementConfigurator getElement(String name) {
        return (ITypeElementConfigurator) this.typeElementConfigurations.get(name);
    }

    /**
     * 通过反射从CLR类型中收集元数据，生成类型配置项
     *
     * @param analyticPipeline 类型解析管道
     */
    @Override
    void reflectionModeling(ITypeAnalyzer analyticPipeline) {
        ITypeAnalyzer pipeLine = analyticPipeline;
        while (pipeLine != null) {
            pipeLine.configure(this.clrType, (IStructuralTypeConfigurator) this);
            pipeLine = pipeLine.getNext();
        }
    }

    /**
     * 启动一个关联端配置项，如果要启动的配置项未创建则新建一个。
     *
     * @param name       名称
     * @param entityType 作为关联端的实体类型
     * @return 关联端配置
     */
    public AssociationEndConfiguration<TAssociation> associationEnd(String name, Class<?> entityType) {

        if (Utils.getStringIsEmpty(name))
            throw new IllegalArgumentException("关联端名称不能为空");
        //转换为首字母大写
        name = StringUtils.capitalize(name);

        if (!this.getElementConfigurations().containsKey(name)) {
            //创建关联端配置项
            AssociationEndConfiguration<TAssociation> associationEnd = this.generateAssociationEndConfiguration(name, entityType);
            //添加到元素配置项
            this.getElementConfigurations().put(name, associationEnd);
        }

        return (AssociationEndConfiguration<TAssociation>) this.getElementConfigurations().get(name);
    }

    /**
     * 根据Lambda表达式包含的信息启动一个关联端配置项，如果要启动的配置项未创建则新建一个
     *
     * @param get       lambda表达式
     * @param <TResult> lambda表达式的返回值
     * @return 关联端配置
     */
    public <TResult> AssociationEndConfigurationGeneric<TAssociation, TResult> associationEnd(SerializedFunction<TAssociation, TResult> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            Property property = memberExpression.getProperty();

            //获取关联端配置项
            AssociationEndConfiguration<TAssociation> associationEndConfiguration = this.associationEnd(property.getName(), property.getPropertyElementType()[0]);
            //配置取值器和设值器
            Utils.configureValueGetterAndSetter(property, associationEndConfiguration);

            return (AssociationEndConfigurationGeneric<TAssociation, TResult>) associationEndConfiguration;

        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 根据端类型和名称启动一个关联端配置项，如果要启动的配置项未创建则新建一个
     *
     * @param name 端名称
     * @return 关联端配置
     */
    public AssociationEndConfiguration<TAssociation> associationEnd(String name) {
        Property property = Utils.getProperty(this.clrType, name);

        //获取关联端配置项
        AssociationEndConfiguration<TAssociation> associationEndConfiguration = this.associationEnd(property.getName(), property.getPropertyElementType()[0]);
        //配置取值器和设值器
        Utils.configureValueGetterAndSetter(property, associationEndConfiguration);

        return associationEndConfiguration;
    }

    /**
     * 设置是否为显式关联
     *
     * @param visible 是否为显式关联
     * @return 自身
     */
    public AssociationTypeConfiguration<TAssociation> hasVisible(boolean visible) {
        this.visible = visible;
        return this;
    }


    /**
     * 根据类型配置项中的元数据构建模型类型
     * 由派生类实现
     *
     * @param buildingModel 对象数据模型
     * @return 结构化类型
     */
    @Override
    StructuralType createReally(ObjectDataModel buildingModel) {
        //根据配置项数据创建模型对象并设值
        AssociationType associationType;
        if (this.derivingFrom != null) {
            StructuralType derivingFrom = buildingModel.getStructuralType(this.derivingFrom);
            if (derivingFrom == null)
                throw new IllegalArgumentException(String.format("无法找到%s所声明的基类%s,需要先注册基类.", this.clrType.getName(), this.derivingFrom.getName()));

            associationType = new AssociationType(this.clrType, derivingFrom);
        } else {
            associationType = new AssociationType(this.clrType, null);
        }
        if (this.constructor == null) {
            throw new IllegalArgumentException("无法获取" + this.clrType.getName() + "的public或protect,请为其配置构造函数,");
        }
        associationType.setNewInstanceConstructor(this.newInstanceConstructor);

        if (Utils.getStringIsEmpty(this.targetTable)) {
            //如果未设置映射表，检查各关联端，找出第一个伴随端，以其实体型的映射表作为映射表。
            AssociationEndConfigurationGeneric<?, ?> associationEnd = (AssociationEndConfigurationGeneric<?, ?>) this.typeElementConfigurations.values().stream()
                    .filter(p -> p instanceof AssociationEndConfiguration && ((AssociationEndConfiguration<?>) p).isCompanionEnd).findFirst().orElse(null);
            if (associationEnd != null) {
                EntityType entityType =
                        buildingModel.getEntityType(associationEnd.getEntityTypeI());
                this.targetTable = entityType.getTargetTable();
            }

        }

        associationType.setConstructor(this.constructor);
        associationType.setName(this.name);
        associationType.setTargetTable(this.targetTable);
        associationType.setVisible(this.visible);
        associationType.setNamespace(this.namespace);
        associationType.setNoticeAttributes(this.NoticeAttributes);
        associationType.setNotifyCreation(this.NotifyCreation);
        associationType.setNotifyUpdate(this.NotifyUpdate);
        associationType.setNotifyDeletion(this.NotifyDeletion);

        return associationType;
    }

    /**
     * 创建隐式关联型
     */
    @Override
    public void createImplicitAssociationConfiguration() {
        //关联型上无需创建隐式关联型
    }


    /**
     * 创建引用元素
     *
     * @param property 属性
     * @return 引用元素配置
     */
    @Override
    protected ITypeElementConfigurator createReferenceElement(Property property) {
        AssociationEndConfiguration<TAssociation> associationEndConfiguration;

        //获取关联端配置项
        if (this.getElementConfigurations().containsKey(property.getName())) {

            associationEndConfiguration = (AssociationEndConfiguration<TAssociation>) this.getElementConfigurations().get(property.getName());

        } else {

            if (this.getModelBuilder().findConfiguration(property.getPropertyType()) == null)
                throw new IllegalArgumentException(property.getPropertyType() + "未在模型中注册,无法配置为关联端");

            //创建关联端配置项
            associationEndConfiguration
                    = this.generateAssociationEndConfiguration(property.getName(), property.getPropertyType());

            //添加元素配置项
            this.getElementConfigurations().put(property.getName(), associationEndConfiguration);
        }
        return associationEndConfiguration;
    }

    /**
     * 生成AssociationEndConfiguration实例
     *
     * @param name       关联端名称
     * @param entityType 实体类型
     * @return 关联端配置
     */
    private AssociationEndConfiguration<TAssociation> generateAssociationEndConfiguration(String name, Class<?> entityType) {
        return new AssociationEndConfigurationGeneric<>(name, entityType, this.clrType, this);
    }
}
