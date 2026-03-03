/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：实体型配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 16:04:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.EntityType;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.PrimitiveType;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体型配置项
 */
public class EntityTypeConfiguration<TEntity> extends ObjectTypeConfiguration<TEntity, EntityTypeConfiguration<TEntity>> implements IEntityTypeConfigurator {

    /**
     * 显式关联关联引用访问器
     */
    private HashMap<Class<?>, Property> explicitAssociationRefProperties;

    /**
     * 隐式关联关联引用访问器
     */
    private HashMap<String, Property> implicitAssociationRefProperties;

    /**
     * 标识属性组（一般表示为主键
     */
    private List<String> keyAttributes;

    /**
     * 自增是否被指定
     */
    private boolean keyIncreaseHasSet;

    /**
     * 标识属性是否自增
     */
    private boolean keyIsSelfIncreased;

    /**
     * 所有配置类型元素
     */
    private HashMap<String, TypeElementConfiguration> typeElementConfigurations;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    public EntityTypeConfiguration(Class<TEntity> clrType, ModelBuilder modelBuilder) {
        super(clrType, modelBuilder);
    }

    /**
     * 标识属性集合
     *
     * @return 标识属性集合
     */
    @Override
    protected List<String> getKeyAttributes() {
        return this.keyAttributes;
    }

    /**
     * 标识属性是否自增
     *
     * @return 标识属性是否自增
     */
    public boolean getKeyIsSelfIncreased() {
        return this.keyIsSelfIncreased;
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
     * 设置标识属性(覆盖现有配置)
     *
     * @param attrName 属性名称
     */
    @Override
    public void hasKeyAttributeI(String attrName) {
        this.hasKeyAttributeI(attrName, true);
    }

    /**
     * 设置标识属性
     *
     * @param attrName 属性名称
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasKeyAttributeI(String attrName, boolean override) {
        //每调用一次本方法，如果override为false，追加一个标识属性，如果为true清空之前的所有设置。
        if (!override) {
            //如果是非覆盖 只有在没有设置过标识属性时才生效
            if (this.keyAttributes == null || this.keyAttributes.size() == 0)
                this.hasKeyAttribute(attrName);
        } else {
            if (this.keyAttributes == null)
                this.keyAttributes = new ArrayList<>();
            this.keyAttributes.clear();
            this.hasKeyAttribute(attrName);
        }
    }

    /**
     * 设置一个值，该值指示标识属性是否为自增(覆盖现有配置)
     *
     * @param keyIsSelfIncreased 是否为自增
     */
    @Override
    public void hasKeyIsSelfIncreasedI(boolean keyIsSelfIncreased) {
        this.hasKeyIsSelfIncreasedI(keyIsSelfIncreased, true);
    }

    /**
     * 设置一个值，该值指示标识属性是否为自增
     *
     * @param keyIsSelfIncreased 是否为自增
     * @param override           是否覆盖既有配置
     */
    @Override
    public void hasKeyIsSelfIncreasedI(boolean keyIsSelfIncreased, boolean override) {
        if (override)
            this.hasKeyIsSelfIncreased(keyIsSelfIncreased);
        else {
            if (!this.keyIsSelfIncreased) {
                this.hasKeyIsSelfIncreased(keyIsSelfIncreased);
            }
        }
    }

    /**
     * 获取标识属性集合
     *
     * @return 标识属性集合
     */
    @Override
    public String[] getKeyAttributesFiledI() {
        if (this.keyAttributes == null)
            this.keyAttributes = new ArrayList<>();

        List<String> result = new ArrayList<>();

        for (String keyAttribute : this.keyAttributes) {
            if (this.getElementConfigurations().get(keyAttribute) instanceof AttributeConfiguration) {
                AttributeConfiguration<TEntity> attrConfig = (AttributeConfiguration<TEntity>) this.typeElementConfigurations.get(keyAttribute);
                result.add(attrConfig.targetField);
            }

        }

        return result.toArray(new String[0]);
    }

    /**
     * 添加关联引用配置项
     *
     * @param associationReference 关联引用配置项
     */
    public void addAssociationReference(AssociationReferenceConfiguration<TEntity> associationReference) {
        this.getElementConfigurations().put(associationReference.getName(), associationReference);
    }

    /**
     * 查找一个关联引用访问器，该关联引用基于指定显式关联定义。
     *
     * @param associationType 关联类型
     * @return 属性
     */
    public Property findProperty(Class<?> associationType) {
        //定义一个字典（explicitAssociationRefProperties）用于寄存当前类型上定义的所有显式关联引用，其键为关联类型。
        //首次调用本方法时生成上述字典和implicitAssociationRefProperties字典(参见本方法另一重载)，后续调用时应避免重复生成。
        if (this.explicitAssociationRefProperties == null)
            this.explicitAssociationRefProperties = new HashMap<>();

        if (this.explicitAssociationRefProperties.containsKey(associationType))
            return this.explicitAssociationRefProperties.get(associationType);
        return null;
    }

    /**
     * 查找一个关联引用访问器，该关联引用基于指定关联端标签代表的隐式关联定义，如果指定的关联端标签代表的关联不只一个，返回符合此标签的第一个关联引用。
     *
     * @param endsTag 关联端标签
     * @return 属性
     */
    public Property findProperty(String endsTag) {
        //定义一个字典（implicitAssociationRefProperties）用于寄存当前类型上定义的所有隐式关联引用，其键为关联端标签。
        //首次调用本方法时生成上述字典和explicitAssociationRefProperties字典(参见本方法另一重载)，后续调用时应避免重复生成。
        if (this.implicitAssociationRefProperties == null)
            this.implicitAssociationRefProperties = new HashMap<>();
        if (this.implicitAssociationRefProperties.containsKey(endsTag))
            return this.implicitAssociationRefProperties.get(endsTag);
        return null;
    }

    /**
     * 设置标识属性。
     *
     * @param attrName 标识属性
     * @return 自身
     */
    public EntityTypeConfiguration<TEntity> hasKeyAttribute(String attrName) {
        if (this.keyAttributes == null)
            this.keyAttributes = new ArrayList<>();
        Property property = Utils.getProperty(this.clrType, attrName);
        if (!this.keyAttributes.contains(property.getName()))
            this.keyAttributes.add(property.getName());
        return this;
    }

    /**
     * 根据Lambda表达式包含的信息设置标识属性
     *
     * @param get          表达式
     * @param <TAttribute> 标识属性
     * @return 自身
     */
    public <TAttribute> EntityTypeConfiguration<TEntity> hasKeyAttribute(SerializedFunction<TEntity, TAttribute> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.hasKeyAttribute(memberName);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 设置一个值，该值指示标识属性是否为自增
     *
     * @param keyIsSelfIncreased 是否主键自增
     * @return 自身
     */
    public EntityTypeConfiguration<TEntity> hasKeyIsSelfIncreased(boolean keyIsSelfIncreased) {
        this.keyIsSelfIncreased = keyIsSelfIncreased;
        this.keyIncreaseHasSet = true;
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
        EntityType entityType;
        if (this.derivingFrom != null) {
            StructuralType derivingFrom = buildingModel.getStructuralType(this.derivingFrom);
            if (derivingFrom == null)
                throw new IllegalArgumentException(String.format("无法找到%s所声明的基类%s,需要先注册基类.", this.clrType.getName(), this.derivingFrom.getName()));
            entityType = new EntityType(this.clrType, derivingFrom);
        } else {
            entityType = new EntityType(this.clrType, null);
        }
        if (this.constructor == null) {
            throw new IllegalArgumentException("无法获取" + this.clrType.getName() + "的public或protect的构造函数,请为其配置构造函数,");
        }

        entityType.setNewInstanceConstructor(this.newInstanceConstructor);
        entityType.setConstructor(this.constructor);

        //如果只有一个标识属性 并且此属性为long或int 且未进行指定是否自增 则自增
        if (this.keyAttributes != null && this.keyAttributes.size() == 1 && !this.keyIncreaseHasSet) {
            Class<?> keyAttr;
            Property property = ObaseIntrospector.getObaseBeanProperties(this.clrType).stream().filter(p -> p.getName().equalsIgnoreCase(this.keyAttributes.get(0))).findFirst().orElse(null);
            if (property != null) {
                keyAttr = property.getPropertyType();
                if (keyAttr == long.class || keyAttr == int.class) this.keyIsSelfIncreased = true;
            }
        }

        entityType.setKeyIsSelfIncreased(this.keyIsSelfIncreased);
        entityType.setKeyAttributes(this.keyAttributes);
        entityType.setName(this.name);
        entityType.setNamespace(this.namespace);
        entityType.setTargetTable(this.targetTable);
        entityType.setNoticeAttributes(this.NoticeAttributes);
        entityType.setNotifyCreation(this.NotifyCreation);
        entityType.setNotifyDeletion(this.NotifyDeletion);
        entityType.setNotifyUpdate(this.NotifyUpdate);

        entityType.setVersionAttributes(this.versionAttributes);
        entityType.setConcurrentConflictHandlingStrategy(this.concurrentConflictHandlingStrategy);
        return entityType;
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
     * 创建引用元素
     *
     * @param property 属性
     * @return 引用元素配置
     */
    @Override
    protected ITypeElementConfigurator createReferenceElement(Property property) {
        ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
        //关联重数（表示是否是集合属性）
        boolean isMultiplicity = Utils.getIsMultiple(property, type);

        //配置的关联引用
        ITypeElementConfigurator associationReferenceConfig = null;

        //尝试按照显式进行查询
        StructuralTypeConfiguration<?> obvious = this.getModelBuilder().findConfiguration(type.realValue);

        //不为空 则查询是否为关联型配置
        if (obvious instanceof AssociationTypeConfiguration) {
            //此显式关联引用未被配置
            if (this.findProperty(type.realValue) == null) {
                //配置一个显式关联
                associationReferenceConfig = this.createAssociationReference(property.getName(), type.realValue, isMultiplicity);
                //将此显式关联型加入访问器存储
                this.explicitAssociationRefProperties.put(type.realValue, property);
            }
        }

        //是否是元组
        boolean isTuple = Utils.isTuple(type.realValue);

        //没找到显示关联型
        //按照隐式关联型查询 引用的类型是否被配置为实体型
        //不是元组 按照普通的两方关联处理
        Class<?>[] endTypes = new Class<?>[0];
        if (!isTuple) {
            //查询属性类型模型配置项
            StructuralTypeConfiguration<?> implicitEntityConfig = this.getModelBuilder().findConfiguration(type.realValue);
            if (implicitEntityConfig instanceof IEntityTypeConfigurator) {
                //提取关联端
                endTypes = new Class<?>[]{this.clrType, type.realValue};
            }
        }
        //是元组 要分拆为多方关联
        else {
            //如果是元组 取出所有类型参数判断
            Class<?>[] tupleTypeList = property.getPropertyElementType();
            List<StructuralTypeConfiguration<?>> configs = Arrays.stream(tupleTypeList).map(p -> this.getModelBuilder().findConfiguration(p)).collect(Collectors.toList());
            //都是实体型 才进入推断
            if (configs.stream().allMatch(p -> p instanceof IEntityTypeConfigurator)) {
                //加入自己这一端的类型
                List<Class<?>> endTypesList = new ArrayList<>();
                endTypesList.add(this.clrType);
                //取出另外的端类型
                endTypesList.addAll(Arrays.asList(tupleTypeList));
                //提取关联端
                endTypes = endTypesList.toArray(new Class<?>[0]);
            }
        }

        String endTags = AssociationConfiguratorBuilder.generateEndsTag(endTypes, this.getModelBuilder());
        //Tag不是空 且 没有配置过
        if (!Utils.getStringIsEmpty(endTags) && this.findProperty(endTags) == null) {
            //配置一个隐式关联关联引用
            //只有配置过隐式关联的才能配置引用
            AssociationConfiguratorBuilder builder = this.getModelBuilder().findImplicitAssociationConfigurationBuilder(endTags);
            if (builder != null) {
                io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration end = builder.getEndConfigurations().stream().filter(p -> p.getEntityType().equals(this.clrType)).findFirst().orElse(null);
                if (end != null)
                    associationReferenceConfig = this.createAssociationReference(property.getName(), this.clrType, isMultiplicity, end.getEndIndex(), builder);
            }

            //加入隐式关联访问器存储
            this.implicitAssociationRefProperties.put(endTags, property);
        }


        //没有创建出来
        if (associationReferenceConfig == null)
            throw new IllegalArgumentException("无法为" + this.getClrType().getName() + "的属性" + property.getName() + "配置关联引用,请检查是否为此属性关联引用对应的关联型(如已存在相同关联端的其他引用或此属性的类型未被配置为显式关联型).");

        return associationReferenceConfig;
    }

    /**
     * 创建隐式关联型
     */
    @Override
    public void createImplicitAssociationConfiguration() {
        //此类声明了继承类 则只查找到继承类为止
        List<Property> properties = this.derivingFrom != null ? ObaseIntrospector.getObaseBeanProperties(this.clrType, this.derivingFrom) : ObaseIntrospector.getObaseBeanProperties(this.clrType);

        for (Property propInfo : properties) {
            //class忽略掉
            if (propInfo.getName().equals("class")) continue;

            //过滤属性不参与隐式关联推断
            if (this.ignoreList.contains(propInfo.getName()))
                continue;

            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
            //关联重数（表示是否是集合属性）
            Utils.getIsMultiple(propInfo, type);

            //基元类型 不参与
            if (PrimitiveType.isObasePrimitive(type.realValue))
                continue;

            //是否是元组
            boolean isTuple = Utils.isTuple(type.realValue);

            //是否元组
            if (isTuple) {
                //如果是元组 取出所有类型参数判断
                Class<?>[] tupleTypeList = propInfo.getPropertyElementType();
                List<StructuralTypeConfiguration<?>> configs = Arrays.stream(tupleTypeList).map(this.getModelBuilder()::findConfiguration).collect(Collectors.toList());
                //任意一个不是实体型 不参与推断
                if (configs.stream().anyMatch(p -> !(p instanceof IEntityTypeConfigurator)))
                    continue;
            } else {
                StructuralTypeConfiguration<?> config = this.getModelBuilder().findConfiguration(type.realValue);
                //类型没配置 不参与
                if (config == null)
                    continue;

                //类型不是实体型 不参与
                if (!(config instanceof IEntityTypeConfigurator))
                    continue;
            }


            //是元组 要分拆为多方关联
            Class<?>[] endTypes;
            if (isTuple) {
                //加入自己这一端的类型
                List<Class<?>> endTypesList = new ArrayList<>();
                endTypesList.add(this.clrType);
                Class<?>[] tupleTypeList = propInfo.getPropertyElementType();
                //取出另外的端类型
                endTypesList.addAll(Arrays.asList(tupleTypeList));
                endTypes = endTypesList.toArray(new Class<?>[0]);
            } else {
                //不是元组 按照普通的两方关联处理
                //提取关联端
                endTypes = new Class<?>[]{this.clrType, type.realValue};
            }

            String endTags = AssociationConfiguratorBuilder.generateEndsTag(endTypes, this.getModelBuilder());
            //已配置为隐式关联的 不参与
            if (this.getModelBuilder().findImplicitAssociationConfigurationBuilder(endTags) != null)
                continue;

            //创建建造器
            AssociationConfiguratorBuilder builder = this.getModelBuilder().association();
            //每个端
            for (Class<?> endType : endTypes)
                builder.associationEnd(endType);
        }
    }

    /**
     * 创建显式关联引用
     *
     * @param name            名称
     * @param associationType 关联型
     * @param isMultiple      是否多重
     * @return 显式关联的关联引用
     */
    private AssociationReferenceConfigurationGeneric<TEntity, EntityTypeConfiguration<TEntity>> createAssociationReference(String name, Class<?> associationType, boolean isMultiple) {
        if (!this.getElementConfigurations().containsKey(name)) {
            //创建关联应用配置类型
            AssociationReferenceConfigurationGeneric<?, ?> assRefCfgInstance = new AssociationReferenceConfigurationGeneric<>(name, associationType, isMultiple, this.clrType, this);
            //添加元素项集合
            this.getElementConfigurations().put(name, assRefCfgInstance);
        }

        //返回当前配置项
        return (AssociationReferenceConfigurationGeneric<TEntity, EntityTypeConfiguration<TEntity>>) this.getElementConfigurations().get(name);
    }

    /**
     * 创建隐式关联引用
     *
     * @param name                     名称
     * @param endType                  端类型
     * @param isMultiple               是否多重
     * @param endIndex                 端序号
     * @param associationConfigBuilder 隐式关联型建造器
     * @return 隐式关联的关联引用
     */
    private AssociationReferenceConfiguration<TEntity> createAssociationReference(String name, Class<?> endType, boolean isMultiple, byte endIndex, AssociationConfiguratorBuilder associationConfigBuilder) {
        if (!this.getElementConfigurations().containsKey(name)) {
            //创建关联应用配置类型
            io.obase.core.odm.builder.implicitAssociationConfigor.AssociationReferenceConfiguration<?, ?> assRefCfgInstance = new io.obase.core.odm.builder.implicitAssociationConfigor.AssociationReferenceConfiguration<>(name, isMultiple, endIndex, endType, associationConfigBuilder);
            //添加元素项集合
            this.getElementConfigurations().put(name, assRefCfgInstance);
        }

        //返回当前配置项
        return (AssociationReferenceConfiguration<TEntity>) this.getElementConfigurations().get(name);
    }
}
