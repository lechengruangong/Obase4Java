/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联型配置器的建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 17:24:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.core.common.Utils;
import io.obase.core.odm.FieldDescriptor;
import io.obase.core.odm.builder.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 隐式关联型配置器的建造器
 */
public class AssociationConfiguratorBuilder {

    /**
     * 关联端配置项
     */
    private final List<AssociationEndConfiguration> endConfigurations = new ArrayList<>();

    /**
     * 关联的扩展配置器
     */
    private final HashMap<Class<?>, TypeExtensionConfiguration> extensionConfigurations = new HashMap<>();

    /**
     * 建模器
     */
    private final ModelBuilder builder;

    /**
     * 生成的关联类型
     */
    private Class<?> associationType;

    /**
     * 当前配置出的关联型配置
     */
    private StructuralTypeConfiguration<?> associationTypeConfiguration;

    /**
     * 端数量
     */
    private byte endCount;

    /**
     * 关联端标签
     */
    private String endsTag;

    /**
     * 映射表
     */
    private String targetTable;

    /**
     * 初始化AssociationConfiguratorBuilder类的新实例
     *
     * @param modelBuilder 建模器
     */
    public AssociationConfiguratorBuilder(ModelBuilder modelBuilder) {
        this.builder = modelBuilder;
    }

    /**
     * 根据指定的关联端实体型（顺序不敏感）生成关联端标签
     *
     * @param endTypes     作为关联端的实体型，顺序不敏感
     * @param modelBuilder 建模器
     * @return 关联端标签
     */
    public static String generateEndsTag(Class<?>[] endTypes, ModelBuilder modelBuilder) {
        //取限定名
        String[] fullNameList = Arrays.stream(endTypes).map(modelBuilder::findConfiguration).map(p -> p.getClrType().getName()).toArray(String[]::new);

        //排序
        Arrays.sort(fullNameList);
        //组合
        return String.join("/", fullNameList);
    }

    /**
     * 获取生成的关联类型
     *
     * @return 生成的关联类型
     */
    public Class<?> getAssociationType() {
        return this.associationType;
    }

    /**
     * 获取端数量
     *
     * @return 端数量
     */
    public byte getEndCount() {
        return this.endCount;
    }

    /**
     * 获取建模器
     *
     * @return 建模器
     */
    public ModelBuilder getModelBuilder() {
        return this.builder;
    }

    /**
     * 获取关联端配置项
     *
     * @return 关联端配置项
     */
    public List<AssociationEndConfiguration> getEndConfigurations() {
        return this.endConfigurations;
    }

    /**
     * 启动对一个新关联端的配置
     *
     * @param endClass  关联端类型
     * @param <TEntity> 关联端类型
     * @return 关联端配置项
     */
    public <TEntity> AssociationEndConfigurationGeneric<TEntity> associationEnd(Class<TEntity> endClass) {

        this.endsTag = "";

        StructuralTypeConfiguration<?> endModelType = this.builder.findConfiguration(endClass);
        if (endModelType == null)
            throw new IllegalArgumentException(endClass.getName() + "类型还未注册,不能参与构建隐式关联.");
        if (!(endModelType instanceof IEntityTypeConfigurator))
            throw new IllegalArgumentException(endClass.getName() + "类型不是实体型,不能参与构建隐式关联.");
        this.endCount++;
        AssociationEndConfigurationGeneric<TEntity> endConfiguration = new AssociationEndConfigurationGeneric<>(this.endCount, this, endClass);
        this.endConfigurations.add(endConfiguration);

        return endConfiguration;
    }

    /**
     * 为类型配置项设置一个扩展配置器
     *
     * @param extensionConfigurationClass 扩展配置器类型
     * @param <TExtensionConfiguration>   扩展配置器类型
     * @return 扩展配置器
     */
    public <TExtensionConfiguration extends TypeExtensionConfiguration> TExtensionConfiguration hasExtension(Class<TExtensionConfiguration> extensionConfigurationClass) {
        if (this.extensionConfigurations.containsKey(extensionConfigurationClass))
            return (TExtensionConfiguration) this.extensionConfigurations.get(extensionConfigurationClass);

        TypeExtensionConfiguration extensionConfiguration;
        try {
            extensionConfiguration = extensionConfigurationClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException("添加扩展配置器失败," + extensionConfigurationClass.getName() + "没有适合的无参构造函数", e);
        }
        this.extensionConfigurations.put(extensionConfigurationClass, extensionConfiguration);
        return (TExtensionConfiguration) this.extensionConfigurations.get(extensionConfigurationClass);
    }

    /**
     * 生成关联端标签。
     * 关联端标签是以关联端类型（实体型）的完全限定名（即以命名空间限定的名称）串联而成的字符串。同一组类型（顺序无关）建立的多个隐式关联具有相同的关联端标签。
     *
     * @return 关联端标签
     */
    public String generateEndsTag() {
        if (this.endCount < 2)
            throw new IllegalArgumentException("隐式关联端的数量不能少于2,当前仅有关联端" + this.endCount + "个.");

        if (!Utils.getStringIsEmpty(this.endsTag))
            return this.endsTag;

        Class<?>[] endTypes = this.endConfigurations.stream().map(AssociationEndConfiguration::getEntityType).toArray(Class[]::new);
        this.endsTag = generateEndsTag(endTypes, this.builder);
        return this.endsTag;
    }

    /**
     * 设置映射表
     *
     * @param table 映射表
     * @return 自身
     */
    public AssociationConfiguratorBuilder toTable(String table) {
        this.targetTable = table;
        return this;
    }

    /**
     * 建造关联型配置器
     *
     * @return 隐式关联型配置
     */
    public StructuralTypeConfiguration<?> build() {
        //已生成 直接返回
        if (this.associationTypeConfiguration != null)
            return this.associationTypeConfiguration;

        if (this.endCount < 2)
            throw new IllegalArgumentException("隐式关联端的数量不能少于2,当前仅有关联端" + this.endCount + "个.");

        //生成隐式关联型
        //以ImplicitAssociation为基类 定义若干个关联端字段
        List<FieldDescriptor> fields = new ArrayList<>();
        List<String> subNames = new ArrayList<>();
        this.endConfigurations.sort((p1, p2) -> p1.getEntityType().getName().compareToIgnoreCase(p2.getEntityType().getName()));
        for (AssociationEndConfiguration endConfiguration : this.endConfigurations) {
            FieldDescriptor fieldDescriptor = new FieldDescriptor(endConfiguration.getEntityType(), endConfiguration.getName());
            fieldDescriptor.setHasGetter(true);
            fieldDescriptor.setHasSetter(true);
            fields.add(fieldDescriptor);

            subNames.add(endConfiguration.getEntityType().getSimpleName());
        }

        //动态创建的关联型完全限定名
        String fullName = "ImplicitAssociation_" + String.join("_", subNames);

        //定义一个隐式关联型的Clr类型
        this.associationType = ImplicitAssociationManager.getCurrent().applyType(fields.toArray(new FieldDescriptor[0]), fullName);

        String companionEndTargetTable = "";
        //查找伴随端
        AssociationEndConfiguration endConfig = this.endConfigurations.stream().filter(AssociationEndConfiguration::getIsCompanionEnd).findFirst().orElse(null);
        if (endConfig != null) {
            companionEndTargetTable = this.getEntityTargetTable(endConfig.getEntityType());
        }

        //如果根据关联端伴随推断了表 且 表和设置的不同
        if (!Utils.getStringIsEmpty(companionEndTargetTable) && !Utils.getStringIsEmpty(this.targetTable) &&
                !companionEndTargetTable.equals(this.targetTable)) {
            throw new IllegalArgumentException(
                    this.generateEndsTag() + "间的关联,设置的映射表 " + this.targetTable + "与设置的伴随端映射表 " + companionEndTargetTable + "不相同.");
        }

        //推断成功赋值 否则等到处理类型管道时赋值
        if (!Utils.getStringIsEmpty(companionEndTargetTable))
            this.targetTable = companionEndTargetTable;

        //构造关联型配置
        IAssociationTypeConfigurator associationTypeConfig = new AssociationTypeConfiguration<>(this.endConfigurations.toArray(new AssociationEndConfiguration[0]), this.extensionConfigurations.values().toArray(new TypeExtensionConfiguration[0]), this.generateEndsTag(), this.builder, this.associationType);
        //配置 如果关联端等于2 就设为普通的隐式关联
        associationTypeConfig.setIsVisibleI(this.endCount != 2);
        associationTypeConfig.toTableI(this.targetTable);
        //保存
        this.associationTypeConfiguration = (StructuralTypeConfiguration<?>) associationTypeConfig;

        return this.associationTypeConfiguration;
    }

    /**
     * 获取实体型的映射表
     *
     * @param entityType 实体型
     * @return 映射表
     */
    private String getEntityTargetTable(Class<?> entityType) {
        StructuralTypeConfiguration<?> structuralTypeConfiguration = this.builder.findConfiguration(entityType);
        if (structuralTypeConfiguration instanceof IEntityTypeConfigurator) {
            IEntityTypeConfigurator configurator = (IEntityTypeConfigurator) structuralTypeConfiguration;
            return configurator.getTargetTableI();
        }
        throw new IllegalArgumentException(entityType.getName() + "没有被配置为实体型.");
    }
}
