/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结构化类型,提供结构化配置基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:49:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.TwoTuple;
import io.obase.core.odm.*;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 为实体型配置项、关联型配置项和复杂类型配置项提供基础实现
 *
 * @param <TStructural> 结构化类型的运行时类型
 */
public abstract class StructuralTypeConfiguration<TStructural> {

    /**
     * 过滤属性名集合
     */
    protected final List<String> ignoreList = new ArrayList<>();
    /**
     * 类型扩展配置器
     */
    protected final List<TypeExtensionConfiguration> extensionConfigs = new ArrayList<>();
    /**
     * 建模器
     */
    private final ModelBuilder modelBuilder;
    /**
     * 类型的CLR类型
     */
    protected Class<TStructural> clrType;

    /**
     * 具体类型判别器
     */
    protected IConcreteTypeDiscriminator concreteTypeDiscriminator;

    /**
     * 判别类型标记
     * 即判别字段的名称和判别字段的值
     */
    protected TwoTuple<String, Object> concreteTypeSign;

    /**
     * 根据当前配置信息生成的类型
     */
    protected StructuralType createdType;

    /**
     * 基类型
     */
    protected Class<?> derivingFrom;
    /**
     * 用于判断类型的字段名称
     */
    protected String typeAttributeName;
    /**
     * 类型的实例构造器
     */
    protected IInstanceConstructor constructor;
    /**
     * 类型的名称
     */
    protected String name;
    /**
     * 类型的命名空间
     */
    protected String namespace;
    /**
     * 新实例构造函数
     */
    protected IInstanceConstructor newInstanceConstructor;
    /**
     * 触发器集合
     */
    protected Map<IBehaviorTrigger, List<TypeElementConfiguration>> triggerElements;
    /**
     * 当前的外键定义器
     */
    private ForeignKeyAdder foreignKeyAdder;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    protected StructuralTypeConfiguration(Class<TStructural> clrType, ModelBuilder modelBuilder) {
        this.clrType = clrType;
        this.modelBuilder = modelBuilder;
        this.name = clrType.getSimpleName();
    }

    /**
     * 标识属性集合
     *
     * @return 标识属性集合
     */
    protected abstract List<String> getKeyAttributes();

    /**
     * 建模器
     *
     * @return 建模器
     */
    public ModelBuilder getModelBuilder() {
        return this.modelBuilder;
    }

    /**
     * 获取类型各元素上设置的行为触发器，注：相同的触发器只返回一个实例
     *
     * @return 行为触发器
     */
    public List<IBehaviorTrigger> getBehaviorTriggers() {
        if (this.triggerElements == null)
            this.loadTriggerElements();

        List<IBehaviorTrigger> result = this.triggerElements == null ? new ArrayList<>() : new ArrayList<>(this.triggerElements.keySet());
        if (this.derivingFrom != null) {
            StructuralTypeConfiguration<?> baseTypeConfiguration = this.getModelBuilder().findConfiguration(this.derivingFrom);
            result.addAll(baseTypeConfiguration.getBehaviorTriggers());
        }
        return result;
    }

    /**
     * 获取类型的CLR类型
     *
     * @return 类型的CLR类型
     */
    public Class<TStructural> getClrType() {
        return this.clrType;
    }

    /**
     * 设置类型的CLR类型
     *
     * @param clrType 类型的CLR类型
     */
    void setClrType(Class<TStructural> clrType) {
        this.clrType = clrType;
    }

    /**
     * 获取所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @return 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    protected abstract HashMap<String, TypeElementConfiguration> getElementConfigurations();

    /**
     * 设置所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @param typeElementConfigurationList 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    protected abstract void setElementConfigurations(HashMap<String, TypeElementConfiguration> typeElementConfigurationList);

    /**
     * 获取创建的类型
     *
     * @return 创建的类型
     */
    public StructuralType getCreatedType() {
        return this.createdType;
    }

    /**
     * 获取当前的外键定义器
     *
     * @return 外键定义器
     */
    ForeignKeyAdder getForeignKeyAdder() {
        return this.foreignKeyAdder;
    }

    /**
     * 设置当前的外键定义器
     *
     * @param adder 外键定义器
     */
    public void setForeignKeyAdder(ForeignKeyAdder adder) {
        this.foreignKeyAdder = adder;
    }

    /**
     * 获取具体类型判别器
     *
     * @return 具体类型判别器
     */
    public IConcreteTypeDiscriminator getConcreteTypeDiscriminator() {
        return this.concreteTypeDiscriminator;
    }

    /**
     * 获取判别类型标记
     *
     * @return 判别类型标记
     */
    public String getTypeAttributeName() {
        return this.typeAttributeName;
    }

    /**
     * 获取用于判断类型的字段名称
     *
     * @return 用于判断类型的字段名称
     */
    public TwoTuple<String, Object> getConcreteTypeSign() {
        return this.concreteTypeSign;
    }

    /**
     * 获取基类型
     *
     * @return 基类型
     */
    public Class<?> getDerivedFrom() {
        return this.derivingFrom;
    }

    /**
     * 获取忽略列表
     *
     * @return 忽略列表
     */
    public List<String> getIgnoreList() {
        return this.ignoreList;
    }

    /**
     * 设置类型的命名空间
     *
     * @param namespace 命名空间
     * @return 自身
     */
    public StructuralTypeConfiguration<TStructural> hasNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     *
     * @return 构建的类型
     */
    StructuralType create(ObjectDataModel buildingModel) {
        //调用实现类的CreateReally方法构建模型类型
        StructuralType structuralType = this.createReally(buildingModel);
        //获取当前的类型扩展 并设置到模型类型中
        for (TypeExtensionConfiguration typeExtensionConfiguration : this.extensionConfigs) {
            structuralType.addExtension(typeExtensionConfiguration.makeExtension());
        }
        //设置判别标识
        structuralType.setConcreteTypeSign(this.getConcreteTypeSign());
        //为根据当前配置信息生成的类型赋值
        this.createdType = structuralType;
        return structuralType;
    }

    /**
     * 遍历元素配置项，根据配置项中的元数据生成元素实例，并添加到指定的模型类型实例中
     *
     * @param objectDataModel 模型类型实例，是即将生成的元素实例的宿主
     */
    void createElements(ObjectDataModel objectDataModel) {
        StructuralType modelType = objectDataModel.getStructuralType(this.clrType);
        //遍历类型的元素配置项
        for (TypeElementConfiguration item : this.getElementConfigurations().values()) {
            //创建元素模型对象
            TypeElement typeElement = item.create(objectDataModel);
            modelType.addElement(typeElement);
        }
    }

    /**
     * 加载触发器
     */
    private void loadTriggerElements() {
        this.triggerElements = new HashMap<>();
        for (TypeElementConfiguration element : this.getElementConfigurations().values()) {
            for (IBehaviorTrigger tri : element.getBehaviorTriggers()) {
                if (!this.triggerElements.containsKey(tri)) {
                    ArrayList<TypeElementConfiguration> elements = new ArrayList<>();
                    elements.add(element);
                    this.triggerElements.put(tri, elements);
                } else {
                    this.triggerElements.get(tri).add(element);
                }
            }
        }
    }

    /**
     * 作补充管道的操作
     *
     * @param complementConfigurator 补充配置管道
     */
    void configureComplement(IComplementConfigurator complementConfigurator) {
        //有补充 先做补充
        IComplementConfigurator pipeLine = complementConfigurator;
        while (pipeLine != null) {
            pipeLine.configure(this.createdType, this);
            pipeLine = pipeLine.getNext();
        }
    }

    /**
     * 获取行为触发器触发的对象行为所涉及到的元素。（有触发器的元素配置项）
     *
     * @param trigger 指定的触发器实例
     * @return 行为触发器触发的对象行为所涉及到的元素
     */
    protected List<TypeElementConfiguration> getBehaviorElements(IBehaviorTrigger trigger) {
        Map<IBehaviorTrigger, List<TypeElementConfiguration>> triggerElems = this.getTriggerElems();

        if (triggerElems != null && triggerElems.containsKey(trigger)) {

            List<TypeElementConfiguration> typeElementConfigurationList = triggerElems.get(trigger);
            typeElementConfigurationList.sort((o1, o2) -> {

                int loadingPriorityO1 = 99999;
                int loadingPriorityO2 = 99999;

                if (o1 instanceof ILazyLoadingConfiguration) {
                    ILazyLoadingConfiguration lazyLoadingConfigurationO1 = (ILazyLoadingConfiguration) o1;
                    loadingPriorityO1 = lazyLoadingConfigurationO1.getLoadingPriority();
                }

                if (o2 instanceof ILazyLoadingConfiguration) {
                    ILazyLoadingConfiguration lazyLoadingConfigurationO2 = (ILazyLoadingConfiguration) o2;
                    loadingPriorityO2 = lazyLoadingConfigurationO2.getLoadingPriority();
                }

                return loadingPriorityO1 - loadingPriorityO2;
            });

            return triggerElems.get(trigger);
        }

        return new ArrayList<>();
    }

    /**
     * 获取所有的触发器
     *
     * @return 获取所有的触发器
     */
    private Map<IBehaviorTrigger, List<TypeElementConfiguration>> getTriggerElems() {
        if (this.triggerElements == null)
            this.loadTriggerElements();

        Map<IBehaviorTrigger, List<TypeElementConfiguration>> triggerElems = this.triggerElements;
        if (this.triggerElements == null)
            triggerElems = new HashMap<>();
        if (this.derivingFrom != null) {
            StructuralTypeConfiguration<?> baseTypeConfiguration = this.getModelBuilder().findConfiguration(this.derivingFrom);
            Map<IBehaviorTrigger, List<TypeElementConfiguration>> baseMap = baseTypeConfiguration.getTriggerElems();
            for (IBehaviorTrigger elem : baseMap.keySet()) {
                if (!triggerElems.containsKey(elem))
                    triggerElems.put(elem, baseMap.get(elem));
            }
        }
        return triggerElems;
    }

    /**
     * 创建指定对象类型的代理类型
     *
     * @return 指定对象类型的代理类型
     */
    Class<?> createProxyType(IProxyTypeGenerator generationPipeline) {

        //如何具体的构造代理类
        FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers = builder -> {
            IProxyTypeGenerator pipeLine = generationPipeline;
            while (pipeLine != null) {
                //外部已经检测过类型 此处强转即可
                builder = pipeLine.defineMembers(builder, (ObjectType) this.createdType, (IObjectTypeConfigurator) this);
                pipeLine = pipeLine.getNext();
            }
            //创建
            builder = this.defineProxyTypeAbstractMethod(builder);

            return builder;
        };

        Class<?>[] classes = new Class[1];
        classes[0] = IIntervene.class;

        Constructor<?> constructor = this.defineProxyTypeConstructor();

        return //生成一个Type
                ImpliedTypeManager.getCurrent().applyType(this.createdType.getClrType(), classes, defineMembers, constructor);
    }

    /**
     * 定义代理类型的构造函数
     *
     * @return 代理类型的构造函数
     */
    private Constructor<?> defineProxyTypeConstructor() {
        //定义反序列化构造函数 处理参数
        List<Parameter> parameterList = this.createdType.getConstructor().getParameters();
        Constructor<?> constructor;
        try {
            if (parameterList != null && parameterList.size() > 0) {
                Class<?>[] types = parameterList.stream().map(Parameter::getType).toArray(Class<?>[]::new);
                constructor = this.createdType.getClrType().getDeclaredConstructor(types);
            } else {
                constructor = this.createdType.getClrType().getDeclaredConstructor();
            }
        } catch (NoSuchMethodException ex) {
            Constructor<?>[] constructors = this.createdType.getClrType().getDeclaredConstructors();
            constructor = Arrays.stream(constructors).filter(p -> p.getParameterCount() == 0).findFirst().orElse(null);
            if (constructor == null)
                throw new IllegalArgumentException("无法根据" + this.createdType.getClrType().getName() + "的构造器参数获取类型的原始构造函数,此类型也没有public或protected的无参构造函数.");
        }
        return constructor;
    }

    /**
     * 定义抽象方法的实现
     */
    private DynamicType.Builder<?> defineProxyTypeAbstractMethod(DynamicType.Builder<?> builder) {
        //查找所有的抽象方法
        List<Method> methods = Arrays.stream(this.createdType.getClrType().getDeclaredMethods()).filter(p -> Modifier.isAbstract(p.getModifiers())).collect(Collectors.toList());
        //处理这些抽象方法
        for (Method method : methods) {
            //重写抽象方法 内部实际上是抛出一个异常
            builder = builder.method(ElementMatchers.named(method.getName())).intercept(ExceptionMethod.throwing(RuntimeException.class, "此方法由Obase定义代理类型时实现,不应该被调用."));
        }

        return builder;
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     * 由派生类实现
     *
     * @param buildingModel 对象数据模型
     * @return 结构化类型
     */
    abstract StructuralType createReally(ObjectDataModel buildingModel);

    /**
     * 创建隐式关联型
     */
    public abstract void createImplicitAssociationConfiguration();

    /**
     * 反射建模
     *
     * @param analyticPipeline 类型成员解析管道
     */
    abstract void reflectionModeling(ITypeMemberAnalyzer analyticPipeline);

    /**
     * 根据类型配置项中的元数据配置模型类型，被配置的模型类型已根据当前类型配置项实例生成并已注册到指定的模型中。
     * 注：调用方调用Create方法创建模型类型时，由于类型的元素还未创建，因此某些属性可能无法当场配置，可以等到类型元素创建（CreateElement被调用）完成
     * 时，调用本方法完成类型配置。
     *
     * @param model 对象数据模型
     */
    abstract void configure(ObjectDataModel model);

    /**
     * 根据名称获取元素配置器
     *
     * @param name 元素名称
     * @return 类型元素配置器
     */
    public abstract ITypeElementConfigurator getElement(String name);

    /**
     * 通过反射从CLR类型中收集元数据，生成类型配置项
     *
     * @param analyticPipeline 类型解析管道
     */
    abstract void reflectionModeling(ITypeAnalyzer analyticPipeline);

    /**
     * 将基类型缺失的外键属性定义到代理类型。
     * 实施说明
     * 为每一属性（Attribute）定义一个公有字段，字段名称为属性名。
     * 为每一属性（Attribute）设置取值器和设置器，使用委托取/设值器。委托可基于访问上述字段的MemberExpression生成。
     */
    public static class ForeignKeyAdder extends ForeignKeyGuarantor {

        /**
         * 基类型的模型类型
         */
        private final ObjectType objType;

        /**
         * 代理类型的建造器
         */
        private DynamicType.Builder<?> proxyTypeBuilder;

        /**
         * 被定义的属性
         */
        private Attribute[] definedAttrs;

        /**
         * 创建ForeignKeyAdder实例
         *
         * @param objType          基类型的模型类型
         * @param proxyTypeBuilder 代理类型的建造器
         */
        public ForeignKeyAdder(ObjectType objType, DynamicType.Builder<?> proxyTypeBuilder) {
            this.objType = objType;
            this.proxyTypeBuilder = proxyTypeBuilder;
        }

        /**
         * 在外键属性缺失的情况下定义所缺的属性
         *
         * @param attrs   要定义的外键属性
         * @param objType 要定义属性的类型
         */
        @Override
        protected void defineMissing(Attribute[] attrs, ObjectType objType) {
            //处理每个属性
            for (Attribute attribute : attrs) {
                this.proxyTypeBuilder = this.proxyTypeBuilder.defineField(attribute.getName(), attribute.getDataType(), Visibility.PUBLIC);
                this.proxyTypeBuilder = this.proxyTypeBuilder.defineMethod("get" + attribute.getName(), attribute.getDataType(), Visibility.PUBLIC).intercept(FieldAccessor.ofField(attribute.getName()));
                //用空的占位
                attribute.setValueSetter(new IValueSetter() {
                    @Override
                    public EValueSettingMode getMode() {
                        return null;
                    }

                    @Override
                    public void setValue(Object obj, Object value) {
                    }
                });
                attribute.setValueGetter(obj -> null);
            }
            this.definedAttrs = attrs;
        }

        /**
         * 在定义了字段后覆盖定义取值和设值器
         */
        public void defineValueGetterAndSetter() {
            if (this.definedAttrs == null)
                return;
            for (Attribute attribute : this.definedAttrs) {

                Field field;
                try {
                    field = this.objType.getRebuildingType().getField(attribute.getName());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("无法定义外键,代理类型" + this.objType.getRebuildingType().getName() + "无法获取到属性" + attribute.getName() + "的字段.", e);
                }
                //构造FieldValueGetter
                FieldValueGetter valueGetter = new FieldValueGetter(field);
                attribute.setValueGetter(valueGetter);
                //构造FieldValueSetter
                ValueSetter setter = ValueSetter.create(field);
                attribute.setValueSetter(setter);
                this.objType.addAttribute(attribute);
            }
        }

        /**
         * 获取代理类型Builder
         *
         * @return 代理类型建造器
         */
        public DynamicType.Builder<?> getProxyTypeBuilder() {
            return this.proxyTypeBuilder;
        }
    }
}
