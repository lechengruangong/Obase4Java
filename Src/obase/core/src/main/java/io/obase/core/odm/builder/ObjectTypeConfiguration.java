/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象类型配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 12:02:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 为实体型配置项、关联型配置项提供基础实现
 *
 * @param <TConfiguration> 具体的配置项类型
 */
public abstract class ObjectTypeConfiguration<TObject, TConfiguration extends ObjectTypeConfiguration<TObject, TConfiguration>>
        extends StructuralTypeConfigurationGeneric<TObject, TConfiguration> implements IObjectTypeConfigurator {

    /**
     * 映射表
     */
    protected String targetTable;

    /**
     * 并发冲突处理策略
     */
    protected EConcurrentConflictHandlingStrategy concurrentConflictHandlingStrategy = EConcurrentConflictHandlingStrategy.ThrowException;

    /**
     * 版本标识属性集（版本键）
     */
    protected List<String> versionAttributes;

    /**
     * 设置对象变更通知包含的属性
     */
    protected List<String> NoticeAttributes;

    /**
     * 指示是否发送对象创建通知
     */
    protected boolean NotifyCreation;

    /**
     * 指示是否发送对象删除通知
     */
    protected boolean NotifyDeletion;

    /**
     * 指示是否发送对象更新通知
     */
    protected boolean NotifyUpdate;

    /**
     * 对象在关系数据库中的存储顺序（排序规则）
     */
    protected List<OrderExpression> StoringOrder;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    protected ObjectTypeConfiguration(Class<TObject> clrType, ModelBuilder modelBuilder) {
        super(clrType, modelBuilder);
    }

    /**
     * 获取类型各元素上设置的行为触发器，注：相同的触发器只返回一个实例。
     *
     * @return 行为触发器
     */
    @Override
    public List<IBehaviorTrigger> getBehaviorTriggersI() {
        return this.getBehaviorTriggers();
    }

    /**
     * 获取映射表
     *
     * @return 映射表
     */
    @Override
    public String getTargetTableI() {
        return this.targetTable;
    }

    /**
     * 获取行为触发器触发的对象行为所涉及到的元素
     *
     * @param trigger 指定的触发器实例
     * @return 触发的对象行为所涉及到的元素
     */
    @Override
    public ITypeElementConfigurator[] getBehaviorElementsI(IBehaviorTrigger trigger) {
        return this.getBehaviorElements(trigger).stream().map(p -> (ITypeElementConfigurator) p).toArray(ITypeElementConfigurator[]::new);
    }

    /**
     * 设置并发冲突处理策略(覆盖现有配置)
     *
     * @param strategy 冲突处理策略
     */
    @Override
    public void hasConcurrentConflictHandlingStrategyI(EConcurrentConflictHandlingStrategy strategy) {
        this.hasConcurrentConflictHandlingStrategyI(strategy, true);
    }

    /**
     * 设置并发冲突处理策略
     *
     * @param strategy 冲突处理策略
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasConcurrentConflictHandlingStrategyI(EConcurrentConflictHandlingStrategy strategy, boolean override) {
        if (override)
            this.hasConcurrentConflictHandlingStrategy(strategy);
        else {
            if (this.concurrentConflictHandlingStrategy == EConcurrentConflictHandlingStrategy.ThrowException)
                this.hasConcurrentConflictHandlingStrategy(strategy);
        }
    }

    /**
     * 设置要包含在对象变更通知中的属性(覆盖现有配置)
     *
     * @param noticeAttributes 要包含的属性的名称的集合
     */
    @Override
    public void hasNoticeAttributesI(String[] noticeAttributes) {
        this.hasNoticeAttributesI(noticeAttributes, true);
    }

    /**
     * 设置要包含在对象变更通知中的属性
     *
     * @param noticeAttributes 要包含的属性的名称的集合
     * @param override         是否覆盖既有配置
     */
    @Override
    public void hasNoticeAttributesI(String[] noticeAttributes, boolean override) {
        if (override)
            this.hasNoticeAttributes(Arrays.asList(noticeAttributes));
        else {
            for (String noticeAttribute : noticeAttributes) {
                try {
                    this.clrType.getMethod("get" + noticeAttribute);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(this.clrType.getName() + "内找不到属性get" + noticeAttribute + "无法配置变更通知属性.");
                }
            }
            if (this.NoticeAttributes == null)
                this.NoticeAttributes = new ArrayList<>();
            for (String noticeAttribute : noticeAttributes) {
                if (!this.NoticeAttributes.contains(noticeAttribute))
                    this.NoticeAttributes.add(noticeAttribute);
            }
        }
    }

    /**
     * 设置一个值，该值指示对象创建时是否发送通知(覆盖现有配置)
     *
     * @param notifyCreation 指示是否发送对象创建通知
     */
    @Override
    public void hasNotifyCreationI(boolean notifyCreation) {
        this.hasNotifyCreationI(notifyCreation, true);
    }

    /**
     * 设置一个值，该值指示对象创建时是否发送通知
     *
     * @param notifyCreation 指示是否发送对象创建通知
     * @param override       是否覆盖既有配置
     */
    @Override
    public void hasNotifyCreationI(boolean notifyCreation, boolean override) {
        if (override)
            this.hasNotifyCreation(notifyCreation);
    }

    /**
     * 设置一个值，该值指示对象删除时是否发送通知(覆盖现有配置)
     *
     * @param notifyDeletion 指示是否发送对象删除通知
     */
    @Override
    public void hasNotifyDeletionI(boolean notifyDeletion) {
        this.hasNotifyDeletionI(notifyDeletion, true);
    }

    /**
     * 设置一个值，该值指示对象删除时是否发送通知
     *
     * @param notifyDeletion 指示是否发送对象删除通知
     * @param override       是否覆盖既有配置
     */
    @Override
    public void hasNotifyDeletionI(boolean notifyDeletion, boolean override) {
        if (override)
            this.hasNotifyDeletionI(notifyDeletion);
    }

    /**
     * 设置一个值，该值指示对象更新时是否发送通知(覆盖现有配置)
     *
     * @param notifyUpdate 指示是否发送对象更新通知
     */
    @Override
    public void hasNotifyUpdateI(boolean notifyUpdate) {
        this.hasNotifyUpdateI(notifyUpdate, true);
    }

    /**
     * 设置一个值，该值指示对象更新时是否发送通知
     *
     * @param notifyUpdate 指示是否发送对象更新通知
     * @param override     是否覆盖既有配置
     */
    @Override
    public void hasNotifyUpdateI(boolean notifyUpdate, boolean override) {
        if (override)
            this.hasNotifyUpdate(notifyUpdate);
    }

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性。(覆盖现有配置)
     *
     * @param attribute 属性的名称
     */
    @Override
    public void hasVersionAttributeI(String attribute) {
        this.hasVersionAttributeI(attribute, true);
    }

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性。
     *
     * @param attribute 属性的名称
     * @param override  是否覆盖既有配置
     */
    @Override
    public void hasVersionAttributeI(String attribute, boolean override) {
        if (override) {
            if (this.versionAttributes == null)
                this.versionAttributes = new ArrayList<>();
            this.versionAttributes.clear();
        }
        this.hasVersionAttribute(attribute);
    }

    /**
     * 设置映射表(覆盖现有配置)
     *
     * @param table 映射表的名称
     */
    @Override
    public void toTableI(String table) {
        this.toTableI(table, true);
    }

    /**
     * 设置映射表
     *
     * @param table    映射表的名称
     * @param override 是否覆盖既有配置
     */
    @Override
    public void toTableI(String table, boolean override) {
        if (override)
            this.toTable(table);
        else {
            if (Utils.getStringIsEmpty(this.targetTable))
                this.toTable(table);
        }
    }

    /**
     * 设置要包含在对象变更通知中的属性
     *
     * @param noticeAttributes 要包含的属性的名称的集合
     * @return 自身
     */
    public TConfiguration hasNoticeAttributes(List<String> noticeAttributes) {
        for (String noticeAttribute : noticeAttributes) {
            try {
                this.clrType.getMethod("get" + noticeAttribute);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(this.clrType.getName() + "内找不到属性get" + noticeAttribute + "无法配置变更通知属性.");
            }
        }
        this.NoticeAttributes = noticeAttributes;
        return (TConfiguration) this;
    }

    /**
     * 设置所有属性为要包含在对象变更通知中的属性
     * 注意此方法会覆盖HasNoticeAttributes设置的属性
     *
     * @return 自身
     */
    public TConfiguration hasNoticeAttributes() {
        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(this.clrType);
        this.NoticeAttributes = properties.stream().map(Property::getName).collect(Collectors.toList());
        return (TConfiguration) this;
    }

    /**
     * 设置一个值，该值指示对象创建时是否发送通知
     *
     * @param notifyCreation 指示是否发送对象创建通知
     * @return 自身
     */
    public TConfiguration hasNotifyCreation(boolean notifyCreation) {
        this.NotifyCreation = notifyCreation;
        return (TConfiguration) this;
    }

    /**
     * 设置一个值，该值指示对象删除时是否发送通知。
     *
     * @param notifyDeletion 指示是否发送对象删除通知
     * @return 自身
     */
    public TConfiguration hasNotifyDeletion(boolean notifyDeletion) {
        this.NotifyDeletion = notifyDeletion;
        return (TConfiguration) this;
    }

    /**
     * 设置一个值，该值指示对象更新时是否发送通知。
     *
     * @param notifyUpdate 指示是否发送对象更新通知
     * @return 自身
     */
    public TConfiguration hasNotifyUpdate(boolean notifyUpdate) {
        this.NotifyUpdate = notifyUpdate;
        return (TConfiguration) this;
    }

    /**
     * 设置映射表
     *
     * @param table 映射表的名称
     * @return 自身
     */
    public TConfiguration toTable(String table) {
        this.targetTable = table;
        return (TConfiguration) this;
    }

    /**
     * 根据lambda表达式包含的信息设置对象在关系数据库中的存储顺序
     *
     * @param get       一个lambda表达式，用于指定要作为排序依据的属性
     * @param <TResult> 属性类型
     * @return 自身
     */
    public <T, TResult> TConfiguration hasStoringOrder(SerializedFunction<T, TResult> get) {
        if (this.StoringOrder == null)
            this.StoringOrder = new ArrayList<>();

        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            OrderExpression order = new OrderExpression();
            order.Expression = memberExpression;
            order.Inverted = true;
            this.StoringOrder.add(order);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }

        return (TConfiguration) this;
    }

    /**
     * 根据lambda表达式包含的信息设置对象在关系数据库中的存储顺序，同时指定是否倒序排列
     *
     * @param get       表达式
     * @param inverted  指示是否倒序
     * @param <TResult> 属性类型
     * @return 自身
     */
    public <T, TResult> TConfiguration hasStoringOrder(SerializedFunction<T, TResult> get, boolean inverted) {
        if (this.StoringOrder == null)
            this.StoringOrder = new ArrayList<>();

        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            OrderExpression order = new OrderExpression();
            order.Expression = memberExpression;
            order.Inverted = inverted;
            this.StoringOrder.add(order);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }

        return (TConfiguration) this;
    }

    /**
     * 设置并发冲突处理策略
     *
     * @param strategy 冲突处理策略
     * @return 自身
     */
    public TConfiguration hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy strategy) {
        this.concurrentConflictHandlingStrategy = strategy;
        return (TConfiguration) this;
    }

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性
     *
     * @param attribute 属性的名称
     * @return 自身
     */
    public TConfiguration hasVersionAttribute(String attribute) {
        if (this.versionAttributes == null) this.versionAttributes = new ArrayList<>();
        if (!this.versionAttributes.contains(attribute)) this.versionAttributes.add(attribute);
        return (TConfiguration) this;
    }

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性
     *
     * @param get       表示属性的Lambda表达式
     * @param <TResult> 版本键类型
     * @return 自身
     */
    public <TResult> TConfiguration hasVersionAttribute(SerializedFunction<TObject, TResult> get) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            return this.hasVersionAttribute(memberExpression.getMemberName());
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 根据类型配置项中的元数据配置模型类型，被配置的模型类型已根据当前类型配置项实例生成并已注册到指定的模型中。
     * 注：调用方调用Create方法创建模型类型时，由于类型的元素还未创建，因此某些属性可能无法当场配置，可以等到类型元素创建（CreateElement被调用）完成
     * 时，调用本方法完成类型配置。
     *
     * @param model 对象数据模型
     */
    @Override
    void configure(ObjectDataModel model) {
        //获取当前类型
        StructuralType modelType = model.getStructuralType(this.clrType);

        modelType.setNamespace(this.clrType.getName());

        //关联端映射
        if (modelType instanceof AssociationType) {
            AssociationType ass = (AssociationType) modelType;

            //隐式关联型 默认关闭关联端的延迟加载
            if (!ass.getVisible()) ass.getAssociationEnds().forEach(s -> s.setEnableLazyLoading(false));

            //配置版本键和冲突处理策略
            ass.setVersionAttributes(this.versionAttributes);
            ass.setConcurrentConflictHandlingStrategy(this.concurrentConflictHandlingStrategy);
        } else if (modelType instanceof EntityType) {
            EntityType ent = (EntityType) modelType;

            //主要为了走一遍Set方法
            ent.setKeyIsSelfIncreased(ent.getKeyIsSelfIncreased());
            ent.setKeyAttributes(ent.getKeyAttributes());

            //配置版本键和冲突处理策略
            ent.setVersionAttributes(this.versionAttributes);
            ent.setConcurrentConflictHandlingStrategy(this.concurrentConflictHandlingStrategy);
        }
    }
}
