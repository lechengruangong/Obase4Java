/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 16:38:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ActionWithTwoArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ITextSerializer;
import io.obase.core.common.Property;
import io.obase.core.common.TextSerializer;
import io.obase.core.common.Utils;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.*;
import io.obase.core.odm.serialization.SerializationDataTransferObjectWrapper;
import io.obase.core.odm.serialization.SerializationObjectDataModel;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 属性配置项
 */
public class AttributeConfiguration<TStructural>
        extends TypeElementConfigurationGeneric<TStructural, AttributeConfiguration<TStructural>>
        implements IAttributeConfigurator {

    /**
     * 数据类型（对应数据库类型，取值器取出的数据类型要和数据库字段兼容）
     */
    private final Class<?> dataType;
    /**
     * 元素类型
     */
    private final EElementType elementType;
    /**
     * 映射字段（数据库字段名，用以从sql读取器取值）
     */
    String targetField;
    /**
     * 属性的合并处理器
     */
    private IAttributeCombinationHandler attributeCombinationHandler = new OverwriteCombinationHandler();
    /**
     * 修改触发器集合
     */
    private List<IBehaviorTrigger> changeTriggers;
    /**
     * 指示是否为复杂属性
     */
    private boolean isComplex;
    /**
     * 映射连接符
     */
    private char mappingConnectionChar = (char) -1;
    /**
     * 字符串的最大长度（字符数），仅当属性类型为字符串时有效。
     */
    private int maxCharNumber;
    /**
     * 指示字段值是否可空
     */
    private boolean nullable = true;
    /**
     * 值的精度，以小数位数表示，0表示不限制。
     * 仅限于映射类型为decimal的使用
     * 这个精度指的是小数点后的长度
     */
    private byte precision;

    /**
     * 序列化模型
     */
    private SerializationObjectDataModel serializationModel;

    /**
     * 是否使用序列化模型
     */
    private boolean useSerializationModel;

    /**
     * 对属性值实施序列化和反序列化的程序
     */
    private ITextSerializer serializer;
    /**
     * 类型的原始类型
     */
    private Class<?> valueType;

    /**
     * 创建类型元素配置项实例
     *
     * @param name              元素（属性、关联引用、关联端）名称
     * @param dataType          数据类型
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    protected AttributeConfiguration(String name, Class<?> dataType, Class<TStructural> structuralType, StructuralTypeConfiguration<TStructural> typeConfiguration) {
        super(name, false, typeConfiguration, structuralType);

        this.dataType = dataType;
        this.elementType = EElementType.Attribute;

        //不是字符串 日期 基元类型 枚举类型时 是复杂属性
        this.isComplex = !PrimitiveType.isObasePrimitive(dataType);
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
     * 获取行为触发器，对于属性是指修改触发器，对于关联引用和关联端是加载触发器
     *
     * @return 行为触发器
     */
    @Override
    public List<IBehaviorTrigger> getBehaviorTriggers() {
        if (this.changeTriggers == null)
            this.changeTriggers = new ArrayList<>();
        return this.changeTriggers;
    }

    /**
     * 获取取值器
     *
     * @return 取值器
     */
    @Override
    protected IValueGetter getValueGetter() {

        //有模型的序列化
        if (this.useSerializationModel) {
            if (super.getValueGetter() == null)
                throw new IllegalArgumentException("启用了序列化模型的属性前必须先设置取值器.");
            if (this.serializer == null)
                throw new IllegalArgumentException("启用了序列化模型的属性前必须先设置序列化器.");
            return new SerializedModelValueGetter(super.getValueGetter(), this.serializer, this.serializationModel);
        }

        //如果已启用序列化且传入的取值器不为SerializedValueGetter，使用传入的取值器构造SerializedValueGetter，并将其作为实际取值
        // 器。如果传入的取值器是SerializedValueGetter，直接使用该取值器。
        if (this.serializer != null && !(super.getValueGetter() instanceof SerializedValueGetter)) {
            if (super.getValueGetter() == null)
                throw new IllegalArgumentException("设置属性的序列化器的前必须先设置取值器.");
            return new SerializedValueGetter(super.getValueGetter(), this.serializer);
        }
        return super.getValueGetter();
    }

    /**
     * 获取设值器
     *
     * @return 设值器
     */
    @Override
    protected IValueSetter getValueSetter() {

        //有模型的序列化
        if (this.useSerializationModel) {
            if (super.getValueGetter() == null)
                throw new IllegalArgumentException("启用了序列化模型的属性前必须先设置设值器.");
            if (this.serializer == null)
                throw new IllegalArgumentException("启用了序列化模型的属性前必须先设置序列化器.");
            Property property = Utils.getProperty(this.getTypeConfiguration().getClrType(), this.getName());
            boolean isMulti = Utils.getIsMultiple(property, new ObjectReferencePack<>()) || property.getPropertyType().isArray();
            return new SerializedModelValueSetter(super.getValueSetter(), isMulti,
                    this.serializationModel, this.serializer, SerializationDataTransferObjectWrapper.class);
        }

        // 如果已启用序列化且传入的设值器不为SerializedValueSetter，使用传入的设值器构造SerializedValueSetter，并将其作为实际设值
        // 器。如果传入的设值器是SerializedValueSetter，直接使用该设值器。
        if (this.serializer != null && !(super.getValueSetter() instanceof SerializedValueSetter)) {
            if (super.getValueGetter() == null)
                throw new IllegalArgumentException("设置属性的序列化器前必须先设置设值器.");
            return new SerializedValueSetter(super.getValueSetter(), this.serializer, this.valueType);
        }
        return super.getValueSetter();
    }

    /**
     * 设置修改触发器(覆盖现有配置)
     *
     * @param changeTrigger 修改触发器
     */
    @Override
    public void hasChangeTriggerI(IBehaviorTrigger changeTrigger) {
        this.hasChangeTriggerI(changeTrigger, true);
    }

    /**
     * 设置修改触发器
     *
     * @param changeTrigger 修改触发器
     * @param override      是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(IBehaviorTrigger changeTrigger, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(changeTrigger);
    }

    /**
     * 使用一个能触发属性修改的方法为属性创建修改触发器(覆盖现有配置)
     *
     * @param method 触发属性修改的方法
     */
    @Override
    public void hasChangeTriggerI(Method method) {
        this.hasChangeTriggerI(method, true);
    }

    /**
     * 使用一个能触发属性修改的方法为属性创建修改触发器
     *
     * @param method   触发属性修改的方法
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(Method method, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(method);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建Property-Set型修改触发器(覆盖现有配置)
     *
     * @param property 触发属性修改的属性访问器
     */
    @Override
    public void hasChangeTriggerI(Property property) {
        this.hasChangeTriggerI(property, true);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建Property-Set型修改触发器
     *
     * @param property 触发属性修改的属性访问器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(Property property, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(property, EBehaviorTriggerType.PropertySet);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器(覆盖现有配置)
     *
     * @param property    触发属性修改的属性访问器
     * @param triggerType 要创建的触发器类型
     */
    @Override
    public void hasChangeTriggerI(Property property, EBehaviorTriggerType triggerType) {
        this.hasChangeTriggerI(property, triggerType, true);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器
     *
     * @param property    触发属性修改的属性访问器
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(Property property, EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(property, triggerType);
    }

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器(覆盖现有配置)
     *
     * @param memberName  成员的名称
     * @param triggerType 要创建的触发器类型
     */
    @Override
    public void hasChangeTriggerI(String memberName, EBehaviorTriggerType triggerType) {
        this.hasChangeTriggerI(memberName, triggerType, true);
    }

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器
     *
     * @param memberName  成员的名称
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(String memberName, EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(memberName, triggerType);
    }

    /**
     * 使用与属性同名的成员为属性创建修改触发器(覆盖现有配置)
     *
     * @param triggerType 要创建的触发器类型
     */
    @Override
    public void hasChangeTriggerI(EBehaviorTriggerType triggerType) {
        this.hasChangeTriggerI(triggerType, true);
    }

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器
     *
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(EBehaviorTriggerType triggerType, boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(triggerType);
    }

    /**
     * 使用与属性同名的属性访问器为属性创建Property-Set型修改触发器(覆盖现有配置)
     */
    @Override
    public void hasChangeTriggerI() {
        this.hasChangeTriggerI(true);
    }

    /**
     * 使用与属性同名的属性访问器为属性创建Property-Set型修改触发器
     *
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasChangeTriggerI(boolean override) {
        //每次调用本方法，如果override为false将追加一个触发器，为true将清空之前的所有设置。
        if (override)
            this.getBehaviorTriggers().clear();
        this.hasChangeTrigger(this.name, EBehaviorTriggerType.PropertySet);
    }

    /**
     * 设置属性的合并处理器(覆盖现有配置)
     *
     * @param combiner 属性的合并处理器
     */
    @Override
    public void hasCombinationHandlerI(IAttributeCombinationHandler combiner) {
        this.hasCombinationHandlerI(combiner, true);
    }

    /**
     * 设置属性的合并处理器
     *
     * @param combiner 属性的合并处理器
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasCombinationHandlerI(IAttributeCombinationHandler combiner, boolean override) {
        if (override)
            this.hasCombinationHandler(combiner);
        else {
            if (this.attributeCombinationHandler instanceof OverwriteCombinationHandler)
                this.hasCombinationHandler(combiner);
        }
    }

    /**
     * 设置与指定的属性合并处理策略对应的合并处理器(覆盖现有配置)
     *
     * @param strategy 属性的合并处理策略
     */
    @Override
    public void hasCombinationHandlerI(EAttributeCombinationHandlingStrategy strategy) {
        this.hasCombinationHandlerI(strategy, true);
    }

    /**
     * 设置与指定的属性合并处理策略对应的合并处理器
     *
     * @param strategy 属性的合并处理策略
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasCombinationHandlerI(EAttributeCombinationHandlingStrategy strategy, boolean override) {
        if (override)
            this.hasCombinationHandler(strategy);
        else {
            if (this.attributeCombinationHandler instanceof OverwriteCombinationHandler)
                this.hasCombinationHandler(strategy);
        }
    }

    /**
     * 设置映射连接符(覆盖现有配置)
     *
     * @param value 映射连接符
     */
    @Override
    public void hasMappingConnectionCharI(char value) {
        this.hasMappingConnectionCharI(value, true);
    }

    /**
     * 设置映射连接符
     *
     * @param value    映射连接符
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasMappingConnectionCharI(char value, boolean override) {
        if (override)
            this.hasMappingConnectionChar(value);
        else {
            if (this.mappingConnectionChar == (char) -1)
                this.hasMappingConnectionChar(value);
        }
    }

    /**
     * 设置映射字段(覆盖现有配置)
     *
     * @param field 映射字段
     */
    @Override
    public void toFieldI(String field) {
        this.toFieldI(field, true);
    }

    /**
     * 设置映射字段
     *
     * @param field    映射字段
     * @param override 是否覆盖既有配置
     */
    @Override
    public void toFieldI(String field, boolean override) {
        if (override)
            this.toField(field);
        else {
            if (Utils.getStringIsEmpty(this.targetField))
                this.toField(field);
        }
    }

    /**
     * 设置最大字符数
     *
     * @param maxCharNumber 最大字符数 只有1到255是有效的 如果设置为0 会被设置为255 如果超过255 会被设置为Text字段
     */
    @Override
    public void hasMaxCharNumberI(int maxCharNumber) {
        this.hasMaxCharNumberI(maxCharNumber, true);
    }

    /**
     * 设置最大字符数
     *
     * @param maxCharNumber 最大字符数 只有1到255是有效的 如果设置为0 会被设置为255 如果超过255 会被设置为Text字段
     * @param override      是否覆盖既有配置
     */
    @Override
    public void hasMaxCharNumberI(int maxCharNumber, boolean override) {
        if (override) {
            this.hasMaxCharNumber(maxCharNumber);
        } else {
            //等于0 未设置
            if (this.maxCharNumber == 0)
                this.hasMaxCharNumber(maxCharNumber);
        }
    }

    /**
     * 设置精度
     * 只支持为映射类型decimal设置精度
     *
     * @param precision 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     */
    @Override
    public void hasPrecisionI(byte precision) {
        this.hasPrecisionI(precision, true);
    }

    /**
     * 设置精度
     * 只支持为映射类型decimal设置精度
     *
     * @param precision 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     * @param override  是否覆盖既有配置
     */
    @Override
    public void hasPrecisionI(byte precision, boolean override) {
        if (override) {
            this.hasPrecision(precision);
        } else {
            //等于0 未设置
            if (this.precision == 0)
                this.hasPrecision(precision);
        }
    }

    /**
     * 设置是否可空
     *
     * @param value 指示是否可空。对于主键设置为可空是无效的
     */
    @Override
    public void hasNullableI(boolean value) {
        this.hasNullableI(value, true);
    }

    /**
     * 设置是否可空
     *
     * @param value    指示是否可空。对于主键设置为可空是无效的
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasNullableI(boolean value, boolean override) {
        if (override) {
            this.hasNullable(value);
        } else {
            //等于true 未设置
            if (this.nullable)
                this.hasNullable(value);
        }
    }

    /**
     * 设置修改触发器
     * 每次调用本方法将追加一个触发器
     *
     * @param changeTrigger 触发器
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasChangeTrigger(
            IBehaviorTrigger changeTrigger) {
        if (!this.getBehaviorTriggers().contains(changeTrigger))
            this.getBehaviorTriggers().add(changeTrigger);
        return this;
    }

    /**
     * 使用一个能触发属性修改的方法为属性创建修改触发器
     *
     * @param method 触发属性修改的方法
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasChangeTrigger(Method method) {
        MethodTrigger methodTrigger = new MethodTrigger(method);
        return this.hasChangeTrigger(methodTrigger);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器
     *
     * @param property    触发属性修改的属性访问器
     * @param triggerType 要创建的触发器类型
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasChangeTrigger(Property property, EBehaviorTriggerType triggerType) {
        Method method;
        switch (triggerType) {
            case Method:
                throw new IllegalArgumentException("方法型变更触发器不能用PropertyInfo构造");
            case PropertyGet:
                method = property.getGetterMethod();
                break;
            case PropertySet:
                method = property.getSetterMethod();
                break;
            default:
                throw new IllegalArgumentException("未知的行为触发器的类型");
        }
        return this.hasChangeTrigger(method);
    }

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器
     *
     * @param memberName  成员的名称
     * @param triggerType 要创建的触发器类型
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasChangeTrigger(String memberName, EBehaviorTriggerType triggerType) {
        return this.hasChangeTrigger(Utils.getProperty(this.structuralType, memberName), triggerType);
    }

    /**
     * 使用与属性同名的成员为属性创建修改触发器
     *
     * @param triggerType 要创建的触发器类型
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasChangeTrigger(EBehaviorTriggerType triggerType) {
        return this.hasChangeTrigger(this.name, triggerType);
    }

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器
     *
     * @param get         表示属性访问器的Lambda表达式
     * @param triggerType 要创建的触发器的类型
     * @param <TProperty> 属性访问器的类型
     * @return 自身
     */
    public <TProperty> AttributeConfiguration<TStructural> hasChangeTrigger(SerializedFunction<TStructural, TProperty> get, EBehaviorTriggerType triggerType) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(get);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();
            String memberName = memberExpression.getMemberName();
            return this.hasChangeTrigger(memberName, triggerType);
        } else {
            throw new IllegalArgumentException("传入的表达式无法解析为get方法的MemberExpression");
        }
    }

    /**
     * 设置属性的合并处理器
     *
     * @param combiner 属性的合并处理器
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasCombinationHandler(IAttributeCombinationHandler combiner) {
        this.attributeCombinationHandler = combiner;
        return this;
    }

    /**
     * 设置与指定的属性合并处理策略对应的合并处理器
     *
     * @param strategy 属性的合并处理策略
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasCombinationHandler(EAttributeCombinationHandlingStrategy strategy) {
        switch (strategy) {
            case Overwrite:
                this.attributeCombinationHandler = new OverwriteCombinationHandler();
                break;
            case Ignore:
                this.attributeCombinationHandler = new IgnoreCombinationHandler();
                break;
            case Accumulate:
                this.attributeCombinationHandler = new AccumulateCombinationHandler();
                break;
        }

        return this;
    }

    /**
     * 设置映射连接符
     *
     * @param value 映射连接符
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasMappingConnectionChar(char value) {
        this.mappingConnectionChar = value;
        return this;
    }

    /**
     * 设置映射字段
     *
     * @param field 映射字段
     * @return 自身
     */
    public AttributeConfiguration<TStructural> toField(String field) {
        this.targetField = field;
        return this;
    }

    /**
     * 最大字符数
     * 仅限字符串类型
     *
     * @param maxCharNumber 最大字符数 只有1到255是有效的 如果设置为0 会被设置为255 如果超过255 会被设置为Text字段
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasMaxCharNumber(int maxCharNumber) {
        if (!this.dataType.equals(String.class))
            throw new IllegalArgumentException("只支持为映射类型string设置最大字符数");

        if (maxCharNumber < 0)
            throw new IllegalArgumentException("最大字符数必须为正数");

        this.maxCharNumber = maxCharNumber * 8;
        return this;
    }

    /**
     * 设置精度
     * 只支持为映射类型decimal设置精度
     *
     * @param precision 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasPrecision(byte precision) {
        if (!this.dataType.equals(BigDecimal.class))
            throw new IllegalArgumentException("只支持为映射类型decimal设置精度");

        if (precision < 0)
            throw new IllegalArgumentException("映射类型decimal设置精度不可为负值");

        if (precision > 28)
            throw new IllegalArgumentException("映射类型decimal设置精度最大值28");

        this.precision = precision;
        return this;
    }

    /**
     * 设置是否可空
     *
     * @param value 指示是否可空。对于主键设置为可空是无效的
     * @return 自身
     */
    public AttributeConfiguration<TStructural> hasNullable(boolean value) {
        this.nullable = value;
        return this;
    }


    /**
     * 使用一个能够为类型元素设值的委托为类型元素创建设值器
     *
     * @param setValue    为类型元素设值的委托
     * @param <TProperty> 属性的数据类型
     * @return 自身
     */
    public <TProperty> AttributeConfiguration<TStructural> hasValueSetter(ActionWithTwoArg<TStructural, TProperty> setValue) {
        return this.hasValueSetter(ValueSetter.create(setValue, EValueSettingMode.Assignment));
    }

    /**
     * 使用自定义的的序列化方案，对当前属性启用序列化。
     *
     * @param serializer 自定义的序列化器
     * @param valueType  要序列化的原始类型
     * @return 当前属性配置
     */
    public AttributeConfiguration<TStructural> useSerializer(ITextSerializer serializer, Class<?> valueType) {
        this.valueType = valueType;
        this.serializer = serializer;
        return this;
    }

    /**
     * 使用预制的序列化方案基类，对当前属性启用序列化。
     *
     * @param serializer 实现序列化方案基类的序列化方案
     * @param valueType  要序列化的原始类型
     * @return 当前属性配置
     */
    public AttributeConfiguration<TStructural> useSerializer(TextSerializer serializer, Class<?> valueType) {
        this.valueType = valueType;
        this.serializer = serializer;
        return this;
    }

    /**
     * 设置是否使用预制的序列化方案基类进行序列化
     * 如果设置为true 则根据进行配置的序列化模型进行序列化 否则 只将原始值进行序列化
     *
     * @param use 是否使用序列化模型进行序列化
     * @return 当前属性配置
     */
    public AttributeConfiguration<TStructural> useSerializationModel(boolean use) {
        this.useSerializationModel = use;
        return this;
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
        //根据配置项数据创建模型对象并设值
        if (this.getName().isEmpty() || this.dataType == null) return null;

        ComplexType complexType = model.getComplexType(this.dataType);

        //补充一次判断
        this.isComplex = complexType != null;
        Attribute attribute;
        if (complexType != null) {
            attribute = new ComplexAttribute(this.dataType, this.getName(), complexType);
        } else {
            attribute = new Attribute(this.dataType, this.getName());
        }

        //先从objectModel取出序列化模型
        this.serializationModel = model.getSerializationModel();

        if (Utils.getStringIsEmpty(this.targetField))
            this.targetField = this.getName();

        attribute.setTargetField(this.targetField);
        attribute.setChangeTriggers(this.changeTriggers);
        attribute.setValueGetter(this.getValueGetter());
        attribute.setValueSetter(this.getValueSetter());
        //属性合并处理器
        attribute.setCombinationHandler(this.attributeCombinationHandler);
        attribute.setIsComplex(this.isComplex);
        //映射连接字符
        if (attribute instanceof ComplexAttribute) {
            ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
            complexAttribute.setMappingConnectionChar(this.mappingConnectionChar);
        }
        //字段精度和是否可空
        attribute.setValueLength(this.maxCharNumber);
        attribute.setNullable(this.nullable);
        attribute.setPrecision(this.precision);

        return attribute;
    }

}
