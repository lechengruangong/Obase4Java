/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：建模器,提供配置对象数据模型的配置方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:16:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.ObjectContext;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.MethodChecker;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 建模器，用于存储模型元数据，并可以依据这些元数据生成对象数据模型。
 * 对应于对象数据模型的实体型、复杂类型和关联型，建模器包含实体型配置项、复杂类型配置项和关联型配置项，各类型的元数据即存储于这三种类型配置项中。
 * 各类型配置项又包含相应元素配置项：属性配置项、关联引用配置项、关联端配置项，分别存储属性、关联引用和关联端的元数据。
 * 当Build方法被调用时，建模器根据这些配置项中的元数据信息构建对象数据模型。生成模型前，建模器还利用反映自动从CLR类型中收集元数据信息，因此大多数元数据不需
 * 要手工配置。
 * 此外，建模器会自动生成对象类型的派生代理类型，该派生类型实现IIntervene接口以允许第三方介入者介入对象行为。
 */
public class ModelBuilder {

    /**
     * 隐式关联型配置器的建造器集合
     */
    private final List<AssociationConfiguratorBuilder> associationConfiguratorBuilders = new ArrayList<>();
    /**
     * 所属的上下文类型
     */
    private final Class<? extends ObjectContext> contextType;
    /**
     * 存储从程序集解析类型过程中应忽略的类型
     */
    private final HashSet<Class<?>> ignoredTypes = new HashSet<>();
    /**
     * 代理类型生成管道建造器
     */
    private final ProxyTypeGenerationPipelineBuilder proxyTypeGenerationPipelineBuilder;
    /**
     * 类型解析管道构造器
     */
    private final TypeAnalyticPipelineBuilder typeAnalyticPipelineBuilder;
    /**
     * 类型成员解析管道建造器
     */
    private final TypeMemberAnalyticPipelineBuilder typeMemberAnalyticPipelineBuilder;
    /**
     * 补充配置管道建造器
     */
    private ComplementConfigurationPipelineBuilder complementConfigurationPipelineBuilder;
    /**
     * 补充配置器
     */
    private IComplementConfigurator complementConfigurator;
    /**
     * 模型默认的存储标记
     */
    private StorageSymbol defaultStorageSymbol = StorageSymbols.getCurrent().getDefault();

    /**
     * 指示是否进行完整性检查
     */
    private boolean integrityCheck = true;

    /**
     * 对象数据模型
     */
    private ObjectDataModel objectDataModel;

    /**
     * 代理类型生成器
     */
    private IProxyTypeGenerator proxyTypeGenerator;

    /**
     * 类型解析器
     */
    private ITypeAnalyzer typeAnalyzer;

    /**
     * 值为StructuralTypeConfiguration{TStructural}
     */
    private HashMap<Class<?>, StructuralTypeConfiguration<?>> typeConfigs;

    /**
     * 类型元素生成器
     */
    private ITypeMemberAnalyzer typeMemberAnalyzer;

    /**
     * 初始化ModelBuilder的新实例
     */
    public ModelBuilder(ObjectContext context) {
        //实例化代理类型生成管道建造器，并自动添加默认的生成器。
        this.proxyTypeGenerationPipelineBuilder = new ProxyTypeGenerationPipelineBuilder();
        this.proxyTypeGenerationPipelineBuilder.use(DefaultProxyTypeGenerator::new);
        //实例化类型成员解析管道建造器，并自动添加默认的解析器。
        this.typeMemberAnalyticPipelineBuilder = new TypeMemberAnalyticPipelineBuilder();
        this.typeMemberAnalyticPipelineBuilder.use(next -> new DefaultTypeMemberAnalyzer(this, next));
        //实例化类型解析管道建造器，并自动添加默认的解析器。
        this.typeAnalyticPipelineBuilder = new TypeAnalyticPipelineBuilder();
        this.typeAnalyticPipelineBuilder.use(p -> new DefaultTypeAnalyzer(this.ignoredTypes, this, p));
        //实例化补充管道建造器，并自动添加默认的解析器。
        this.complementConfigurationPipelineBuilder = new ComplementConfigurationPipelineBuilder();
        this.complementConfigurationPipelineBuilder.use(DefaultComplementConfigurator::new);
        //保存上下文类型 和 类加载器
        this.contextType = context.getClass();
        this.hasClassLoader(this.contextType.getClassLoader());
    }

    /**
     * 类型配置项字典
     *
     * @return 类型配置项字典
     */
    private HashMap<Class<?>, StructuralTypeConfiguration<?>> getTypeConfigs() {
        if (this.typeConfigs == null)
            this.typeConfigs = new LinkedHashMap<>();
        return this.typeConfigs;
    }

    /**
     * 获取上下文类型
     *
     * @return 上下文类型
     */
    public Class<? extends ObjectContext> getContextType() {
        return this.contextType;
    }

    /**
     * 启动一个实体型配置项，如果要启动的实体型配置项未创建则新建一个。
     * 类型参数TEntity指定该实体型对应的CLR类型（即对象系统中的类型），在建模器中它是配置项的键。
     *
     * @param entityClass 对应的CLR类型
     * @return 实体型配置
     */
    public <TEntity> EntityTypeConfiguration<TEntity> entity(Class<TEntity> entityClass) {
        //注册所有的getter和setter
        MethodChecker.registerClassMethod(entityClass);
        if (!this.getTypeConfigs().containsKey(entityClass))
            this.getTypeConfigs().put(entityClass, new EntityTypeConfiguration<>(entityClass, this));
        //如果已配置的不是实体 就新建一个
        if (!(this.getTypeConfigs().get(entityClass) instanceof EntityTypeConfiguration)) {
            this.getTypeConfigs().put(entityClass, new EntityTypeConfiguration<>(entityClass, this));
        }
        return (EntityTypeConfiguration<TEntity>) this.getTypeConfigs().get(entityClass);
    }

    /**
     * 启动一个复杂类型配置项，如果要启动的复杂类型配置项未创建则新建一个。
     * 类型参数TComplex指定该复杂类型对应的CLR类型（即对象系统中的类型），在建模器中它是配置项的键。
     *
     * @param complexClass 对应的CLR类型
     * @return 复杂类型配置
     */
    public <TComplex> ComplexTypeConfiguration<TComplex> complex(Class<TComplex> complexClass) {
        MethodChecker.registerClassMethod(complexClass);
        if (!this.getTypeConfigs().containsKey(complexClass))
            this.getTypeConfigs().put(complexClass, new ComplexTypeConfiguration<>(complexClass, this));
        //如果已配置的不是复杂类型 就新建一个
        if (!(this.getTypeConfigs().get(complexClass) instanceof ComplexTypeConfiguration)) {
            this.getTypeConfigs().put(complexClass, new ComplexTypeConfiguration<>(complexClass, this));
        }
        return (ComplexTypeConfiguration<TComplex>) this.getTypeConfigs().get(complexClass);
    }

    /**
     * 启动一个关联型配置项，如果要启动的关联型配置项未创建则新建一个。
     * 类型参数TAssociation指定该实体型对应的CLR类型（即对象系统中的类型），在建模器中它是配置项的键。
     * 使用此方法启动的关联型配置项将来生成的关联型为显式关联。
     *
     * @param associationType 关联型
     * @return 关联型配置项
     */
    public <TAssociation> AssociationTypeConfiguration<TAssociation> association(Class<TAssociation> associationType) {
        MethodChecker.registerClassMethod(associationType);
        if (!this.getTypeConfigs().containsKey(associationType))
            this.getTypeConfigs().put(associationType, new AssociationTypeConfiguration<>(associationType, this));
        //如果已配置的不是显式关联类型 就新建一个
        if (!(this.getTypeConfigs().get(associationType) instanceof AssociationTypeConfiguration)) {
            this.getTypeConfigs().put(associationType, new AssociationTypeConfiguration<>(associationType, this));
        }
        return (AssociationTypeConfiguration<TAssociation>) this.getTypeConfigs().get(associationType);
    }

    /**
     * 启动一个隐式关联型配置器的建造器
     * 注意:每次调用此方法都会返回一个新的建造器
     *
     * @return 隐式关联型配置的建造器
     */
    public AssociationConfiguratorBuilder association() {
        AssociationConfiguratorBuilder builder = new AssociationConfiguratorBuilder(this);
        this.associationConfiguratorBuilders.add(builder);
        return builder;
    }

    /**
     * 生成对象数据模型。
     * 第一步，遍历类型配置项构建类型实例（实体型、关联型、复杂类型）放入模型，（这个过程中会自动生成代理类型）；
     * 第二步，再次遍历类型配置项，通过反射从CLR类型收集元素元数据，然后遍历元素配置项，构建元素实例。
     *
     * @return 对象数据模型
     */
    public ObjectDataModel build() {
        return this.build(null);
    }

    /**
     * 生成对象数据模型。
     * 第一步，遍历类型配置项构建类型实例（实体型、关联型、复杂类型）放入模型，（这个过程中会自动生成代理类型）；
     * 第二步，再次遍历类型配置项，通过反射从CLR类型收集元素元数据，然后遍历元素配置项，构建元素实例。
     *
     * @return 对象数据模型
     */
    public ObjectDataModel build(IStructMappingExecutor executor) {
        if (this.objectDataModel == null) {
            this.objectDataModel = new ObjectDataModel();

            //忽略被忽略的类
            for (Class<?> ignored : this.ignoredTypes) {
                this.typeConfigs.remove(ignored);
            }

            //生成管道
            this.generatePipeLine();

            //遍历配置项 创建隐式关联配置器
            for (StructuralTypeConfiguration<?> item : this.getTypeConfigs().values())
                item.createImplicitAssociationConfiguration();

            //生成隐式关联
            for (AssociationConfiguratorBuilder builder : this.associationConfiguratorBuilders) {
                StructuralTypeConfiguration<?> structuralTypeConfiguration = builder.build();
                //加入配置
                if (structuralTypeConfiguration != null)
                    this.addConfiguration(structuralTypeConfiguration);
                //设置关联型
                for (io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration endConfiguration : builder.getEndConfigurations()) {
                    endConfiguration.setAssociationType(structuralTypeConfiguration);
                }
            }

            //排序 将实体型放置于关联型之前
            HashMap<Class<?>, StructuralTypeConfiguration<?>> typeConfigs = this.getTypeConfigs().entrySet().stream().sorted((p1, p2) -> this.comparison(p1.getValue(), p2.getValue()))
                    .collect(LinkedHashMap::new, (m, i) -> m.put(i.getKey(), i.getValue()), Map::putAll);

            //遍历配置项 创建结构化类型配置
            for (StructuralTypeConfiguration<?> item : typeConfigs.values()) {
                //处理类型解析管道
                item.reflectionModeling(this.typeAnalyzer);
                //创建模型
                StructuralType structuralType = item.create(this.objectDataModel);
                //添加到对象数据模型
                this.objectDataModel.addType(structuralType);
            }
            //遍历配置项 创建类型元素配置
            for (StructuralTypeConfiguration<?> item : typeConfigs.values()) {
                //反射生成元素配置项
                item.reflectionModeling(this.typeMemberAnalyzer);
                //创建类型元素
                item.createElements(this.objectDataModel);
                //配置元素
                item.configure(this.objectDataModel);
            }

            HashMap<ObjectType, StructuralTypeConfiguration<?>> deriving = new HashMap<>();
            //处理继承关系
            for (Class<?> item : typeConfigs.keySet()) {
                //取出对象类型模型
                ObjectType objectType = this.objectDataModel.getObjectType(item);

                //检查一下是否配置了继承 如果配置了 把基类存下来
                if (objectType != null && objectType.getDerivingFrom() != null) {

                    //取出基类
                    ObjectType derivingFrom = (ObjectType) objectType.getDerivingFrom();
                    //检查基类是否配置了类型判别器
                    if (this.typeConfigs.get(derivingFrom.getClrType()).getConcreteTypeDiscriminator() == null) {
                        //没有 则使用内置的判别器
                        HashMap<String, StructuralType> chainCodes = this.getDerivingConcreteTypeValue(derivingFrom);
                        typeConfigs.get(derivingFrom.getClrType()).setConcreteTypeDiscriminator(new ConcreteTypeDiscriminator(chainCodes));
                    }

                    //存下来 之后设置具体类型判别器
                    if (!deriving.containsKey(derivingFrom))
                        deriving.put(derivingFrom, typeConfigs.get(derivingFrom.getClrType()));
                }
            }

            //遍历配置项  处理代理类型
            for (Class<?> item : typeConfigs.keySet()) {
                //取出对象类型模型
                ObjectType objectType = this.objectDataModel.getObjectType(item);

                //如果此配置项为IObjectTypeConfigurator
                if (this.getTypeConfigs().get(item) instanceof IObjectTypeConfigurator) {
                    IObjectTypeConfigurator objectTypeConfigurator = (IObjectTypeConfigurator) this.getTypeConfigs().get(item);
                    //是否配置触发器
                    if (this.shouldCreateProxyType(this.proxyTypeGenerator, objectType, objectTypeConfigurator)) {
                        //生成代理类对象
                        Class<?> proxyType = this.getTypeConfigs().get(item).createProxyType(this.proxyTypeGenerator);
                        IInstanceConstructor ctorObj = this.createConstructor(proxyType, objectType.getConstructor());
                        //反持久化对象构造器
                        objectType.setConstructor(ctorObj);
                        //新对象构造器
                        if (objectType.getNewInstanceConstructor() != null) {
                            //设置要构造的对象类型
                            objectType.getNewInstanceConstructor().setInstanceType(objectType);
                            //新对象构造器
                            IInstanceConstructor newCtorObj = this.CreateNewInstanceConstructor(proxyType, objectType.getNewInstanceConstructor());
                            //对象构造器
                            objectType.setNewInstanceConstructor(newCtorObj);
                        }
                        //生成代理类对象
                        objectType.setProxyType(proxyType);
                    }
                }

                //添加到对象数据模型
                if (objectType != null) {
                    this.objectDataModel.addType(objectType);
                    //检查一下构造器
                    if (objectType.getConstructor().getInstanceType() == null)
                        objectType.getConstructor().setInstanceType(objectType);
                    //处理一下外键保证机制
                    if (this.getTypeConfigs().get(item).getForeignKeyAdder() != null)
                        this.getTypeConfigs().get(item).getForeignKeyAdder().defineValueGetterAndSetter();
                    else
                        this.checkForeignKeyGuarantee(objectType);
                }
            }

            //处理具体类型判别器
            for (ObjectType objectType : deriving.keySet()) {
                objectType.setConcreteTypeDiscriminator(deriving.get(objectType).getConcreteTypeDiscriminator(), deriving.get(objectType).getTypeAttributeName());
            }

            //补充操作
            for (Class<?> item : typeConfigs.keySet()) {
                //执行补充操作
                this.getTypeConfigs().get(item).configureComplement(this.complementConfigurator);
            }

            //完整性检查
            if (this.integrityCheck) {
                Map<String, List<String>> errDictionary = new HashMap<>();
                for (StructuralType structuralType : this.objectDataModel.getTypes())
                    structuralType.integrityCheck(errDictionary);
                if (errDictionary.values().size() > 0)
                    throw new IntegrityCheckFailException(errDictionary);
            }
        }

        //注册类型至表达式解析器 防止有漏掉的
        for (Class<?> clazz : this.objectDataModel.getStructuralTypes().keySet()) {
            try {
                MethodChecker.registerClassMethod(clazz);
                //探测所有get对象的返回值 加入枚举值
                List<Property> properties = ObaseIntrospector.getObaseBeanProperties(clazz);
                for (Property prop : properties) {
                    if (prop.getGetterMethod() != null && prop.getGetterMethod().getReturnType().isEnum()) {
                        MethodChecker.registerEnum(prop.getGetterMethod().getReturnType());
                    }
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("注册表达式解析宿主错误" + ex.getMessage(), ex);
            }
        }

        boolean hasFlag = false;
        //如果有任意类型未配置存储标记
        for (StructuralType structuralType : this.objectDataModel.getTypes().stream().filter(p -> p.getExtension(HeterogStorageExtension.class) == null).collect(Collectors.toList())) {
            hasFlag = true;
            HeterogStorageExtension extension = (HeterogStorageExtension) structuralType.addExtension(HeterogStorageExtension.class);
            extension.setStorageSymbol(this.defaultStorageSymbol);
        }

        if (hasFlag)
            this.objectDataModel.setStorageSymbol(this.defaultStorageSymbol);

        //如果有模型结构映射执行器 执行映射
        if (executor != null)
            executor.execute(this.objectDataModel);

        return this.objectDataModel;
    }

    /**
     * 获取某个结构化类型及其所有派生类的具体类型标记值与结构化类型的字典
     *
     * @param structuralType 根类型
     * @return 具体类型标记值与结构化类型的字典
     */
    private HashMap<String, StructuralType> getDerivingConcreteTypeValue(StructuralType structuralType) {
        //加入自己的区分标记
        HashMap<String, StructuralType> result = new HashMap<>();
        result.put(structuralType.getConcreteTypeSign().getItem2().toString(), structuralType);
        for (StructuralType derivedType : structuralType.getDerivedTypes()) {
            //加入自己继承类的区分标记值
            HashMap<String, StructuralType> derivedResult = this.getDerivingConcreteTypeValue(derivedType);
            for (String key : derivedResult.keySet()) {
                if (!result.containsKey(key))
                    result.put(key, derivedResult.get(key));
            }
        }
        return result;
    }

    /**
     * 比较委托
     *
     * @param x 一个结构化配置
     * @param y 另一个结构化配置
     * @return 表示顺序的值
     */
    private int comparison(StructuralTypeConfiguration<?> x, StructuralTypeConfiguration<?> y) {
        //根据具体的Code进行比较
        int xCode = this.getStructuralTypeConfigurationType(x);
        int yCode = this.getStructuralTypeConfigurationType(y);

        return xCode - yCode;
    }

    /**
     * 具体的比较方法
     *
     * @param configuration 结构化配置
     * @return 结构化配置在继承链里的位置
     */
    private int getStructuralTypeConfigurationType(StructuralTypeConfiguration<?> configuration) {
        //复杂类型 返回0 实体型 返回继承链的Index 关联型返回50
        //可以处理最多50层继承
        if (configuration instanceof IEntityTypeConfigurator) {
            //获取配置的继承链
            List<StructuralTypeConfiguration<?>> chain = Utils.getDerivingConfigChain(configuration, this);
            //有继承的排在没有继承的后面
            return chain.indexOf(configuration) + 1;
        }
        if (configuration instanceof IAssociationTypeConfigurator) {
            return 50;
        }
        return 0;
    }

    /**
     * 生成管道
     */
    private void generatePipeLine() {
        //此三项有默认值 直接Build
        this.proxyTypeGenerator = this.proxyTypeGenerationPipelineBuilder.build();
        this.typeMemberAnalyzer = this.typeMemberAnalyticPipelineBuilder.build();
        this.typeAnalyzer = this.typeAnalyticPipelineBuilder.build();
        //此项无默认值 需判断空值
        if (this.complementConfigurationPipelineBuilder != null)
            this.complementConfigurator = this.complementConfigurationPipelineBuilder.build();

        //检查官方管道个数
        Map<String, Integer> checkDic = new HashMap<>();
        checkDic.put("io.obase.odm.annotation.AnnotatedMemberAnalyzer", 0);
        checkDic.put("io.obase.odm.annotation.AnnotatedTypeAnalyzer", 0);
        checkDic.put("io.obase.logical.deletion.ComplementConfigurator", 0);
        checkDic.put("io.obase.logical.deletion.TypeAnalyzer", 0);
        checkDic.put("io.obase.logical.deletion.ProxyTypeGenerator", 0);
        checkDic.put("io.obase.multi.tenant.ComplementConfigurator", 0);
        checkDic.put("io.obase.multi.tenant.TypeAnalyzer", 0);
        checkDic.put("io.obase.multi.tenant.ProxyTypeGenerator", 0);


        IProxyTypeGenerator currentProxy = this.proxyTypeGenerator;
        while (currentProxy != null) {
            String fullName = currentProxy.getClass().getName();
            if (checkDic.containsKey(fullName)) {
                checkDic.put(fullName, checkDic.get(fullName) + 1);
            }
            currentProxy = currentProxy.getNext();
        }

        ITypeMemberAnalyzer currentTypeMember = this.typeMemberAnalyzer;
        while (currentTypeMember != null) {
            String fullName = currentTypeMember.getClass().getName();
            if (checkDic.containsKey(fullName)) {
                checkDic.put(fullName, checkDic.get(fullName) + 1);
            }
            currentTypeMember = currentTypeMember.getNext();
        }

        ITypeAnalyzer currentType = this.typeAnalyzer;
        while (currentType != null) {
            String fullName = currentType.getClass().getName();
            if (checkDic.containsKey(fullName)) {
                checkDic.put(fullName, checkDic.get(fullName) + 1);
            }
            currentType = currentType.getNext();
        }

        IComplementConfigurator currentComplement = this.complementConfigurator;
        while (currentComplement != null) {
            String fullName = currentComplement.getClass().getName();
            if (checkDic.containsKey(fullName)) {
                checkDic.put(fullName, checkDic.get(fullName) + 1);
            }
            currentComplement = currentComplement.getNext();
        }
        //重复注册则抛出异常
        for (String key : checkDic.keySet()) {
            if (checkDic.get(key) > 1)
                throw new IllegalArgumentException("不能在管道中多次注册" + key);
        }
    }

    /**
     * 创建代理类型的构造器
     *
     * @param proxyType   代理类型
     * @param constructor 原构造器
     * @return 代理类型的构造器
     */
    private IInstanceConstructor createConstructor(Class<?> proxyType, IInstanceConstructor constructor) {

        //用原有的类型参数查找构造信息
        List<Parameter> paraObjs = constructor.getParameters();
        //构造信息
        Constructor<?> ctorInfo;
        try {
            //构造信息
            ctorInfo = (paraObjs == null || paraObjs.size() == 0)
                    ? proxyType.getDeclaredConstructor()
                    : proxyType.getDeclaredConstructor(paraObjs.stream().map(Parameter::getType).toArray(Class<?>[]::new));

            //找到了 构造新的代理类型的构造器
            ReflectionConstructor ctorObj = new ReflectionConstructor(ctorInfo);
            if (paraObjs != null) {
                //设置参数
                for (Parameter p : paraObjs) {
                    ctorObj.setParameter(p.getName(), p.getElementName(), p.getValueConverter(), p.getExpression());
                }
            }

            return ctorObj;

        } catch (NoSuchMethodException ex) {
            //找不到 构造器可能是自定义的 此时继续使用其原有的构造器
            return constructor;
        }
    }

    /**
     * 创建新对象构造器
     *
     * @param proxyType   代理类型
     * @param constructor 构造器
     * @return 新对象构造器
     */
    private IInstanceConstructor CreateNewInstanceConstructor(Class<?> proxyType, IInstanceConstructor constructor) {
        //参数
        Class<?>[] paraObjs = ((InstanceConstructor) constructor).getParameterTypes().toArray(new Class<?>[0]);
        try {
            //构造信息
            Constructor<?> ctorInfo = paraObjs.length == 0
                    ? proxyType.getDeclaredConstructor()
                    : proxyType.getDeclaredConstructor(paraObjs);

            //构造器
            return new ReflectionConstructor(ctorInfo);
        } catch (NoSuchMethodException ex) {
            //找不到 构造器可能是自定义的 此时继续使用其原有的构造器
            return constructor;
        }
    }

    /**
     * 调用管道判断是否需要创建代理对象
     *
     * @param generator    代理创建器
     * @param objType      当前的对象类型
     * @param configurator 对象类型配置器
     * @return 是否需要创建代理对象
     */
    private boolean shouldCreateProxyType(IProxyTypeGenerator generator, ObjectType objType, IObjectTypeConfigurator configurator) {
        boolean should = false;

        //后续管道的判定
        IProxyTypeGenerator pipeLine = generator;
        while (pipeLine != null) {
            should |= pipeLine.should(objType, configurator);
            pipeLine = pipeLine.getNext();
        }

        return should;
    }

    /**
     * 检查外键保证机制是否正确执行
     * 如果因为之前注册过此代理类型 此时进行增补
     *
     * @param objType 对象类型
     */
    private void checkForeignKeyGuarantee(ObjectType objType) {
        //获取定义的外键
        List<Attribute> attrs = Utils.getDefinedForeignAttributes(objType, null, new ObjectReferencePack<>());

        //检查构造的新属性
        if (attrs.size() > 0) {
            for (Attribute attribute : attrs) {
                Field field;
                try {
                    field = objType.getRebuildingType().getField(attribute.getName());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("无法定义外键," + attribute.getName() + "字段未定义", e);
                }
                //构造FieldValueGetter
                FieldValueGetter valueGetter = new FieldValueGetter(field);
                attribute.setValueGetter(valueGetter);
                //构造FieldValueSetter
                ValueSetter setter = ValueSetter.create(field);
                attribute.setValueSetter(setter);

                objType.addAttribute(attribute);
            }
        }
    }

    /**
     * 从模型查找类型配置项。如果未找到返回null。
     *
     * @param structuralType 目标类型的对象系统类型
     * @return 配置项
     */
    public StructuralTypeConfiguration<?> findConfiguration(Class<?> structuralType) {
        return this.getTypeConfigs().getOrDefault(structuralType, null);
    }

    /**
     * 从模型查找隐式关联配置项建造器
     *
     * @param endsTag 如果未找到返回null
     * @return 配置项建造器
     */
    public AssociationConfiguratorBuilder findImplicitAssociationConfigurationBuilder(String endsTag) {
        return this.associationConfiguratorBuilders.stream().filter(p -> p.generateEndsTag().equals(endsTag)).findFirst().orElse(null);
    }

    /**
     * 设置模型是否进行完整性检查
     *
     * @param integrityCheck 是否进行完整性检查
     */
    public void hasIntegrityCheck(boolean integrityCheck) {
        this.integrityCheck = integrityCheck;
    }

    /**
     * 为模型设置默认的存储标记
     *
     * @param defaultStorageSymbol 默认的存储标记
     * @return 自身
     */
    public ModelBuilder hasDefaultStorageSymbol(StorageSymbol defaultStorageSymbol) {
        this.defaultStorageSymbol = defaultStorageSymbol;
        return this;
    }

    /**
     * 启动一个关联型配置项，如果要启动的关联型配置项未创建则新建一个。
     *
     * @param assocType 要配置的关联的类型
     * @return 关联型配置项
     */
    public IAssociationTypeConfigurator associationI(Class<?> assocType) {
        return this.association(assocType);
    }

    /**
     * 启动一个复杂类型配置项，如果要启动的复杂类型配置项未创建则新建一个
     *
     * @param complexType 要配置的复杂类型
     * @return 复杂类型配置项，
     */
    public IStructuralTypeConfigurator complexI(Class<?> complexType) {
        return this.complex(complexType);
    }

    /**
     * 启动一个实体型配置项，如果要启动的实体型配置项未创建则新建一个。
     *
     * @param entityType 要配置的实体类型
     * @return 实体型配置项
     */
    public IEntityTypeConfigurator entityI(Class<?> entityType) {
        return this.entity(entityType);
    }

    /**
     * 从指定的程序集中提取类型并注册到模型
     *
     * @param assembly 类型所在的程序集
     * @param analyzer 程序集解析器，负责从程序集中发现类型
     */
    public void registerType(String assembly, IAssemblyAnalyzer analyzer) {
        analyzer.analyze(assembly, this);
    }

    /**
     * 指定的程序集字符串加载程序集，按照推断约定提取类型并注册到模型。
     *
     * @param assembly 类型所在的程序集
     */
    public void registerType(String assembly) {
        DefaultAssemblyAnalyzer analyzer = new DefaultAssemblyAnalyzer(this.ignoredTypes);
        analyzer.analyze(assembly, this);
    }

    /***
     * 从指定的类型集合中提取类型并注册到模型
     * @param types 类型集合
     */
    public void registerType(Class<?>... types) {
        DefaultAssemblyAnalyzer analyzer = new DefaultAssemblyAnalyzer(this.ignoredTypes);
        analyzer.analyze(types, this);
    }

    /***
     * 从指定的类型集合中提取类型并注册到模型
     * @param analyzer 程序集解析器，负责从程序集中发现类型
     * @param types 类型集合
     */
    public void registerType(IAssemblyAnalyzer analyzer, Class<?>... types) {
        analyzer.analyze(types, this);
    }

    /**
     * 向类型成员解析管道注册中间件，该管道用于在反射建模过程中解析类型成员。
     *
     * @param middlewareDelegate 中间件委托
     * @return 类型成员解析管道建造器
     */
    public TypeMemberAnalyticPipelineBuilder useMemberAnalyzer(FunctionWithOneArg<ITypeMemberAnalyzer, ITypeMemberAnalyzer> middlewareDelegate) {
        return this.typeMemberAnalyticPipelineBuilder.use(middlewareDelegate);
    }

    /**
     * 向代理类型生成管道注册中间件，该管道用于为模型中注册的类型生成代理类。
     *
     * @param middlewareDelegate 中间件委托
     * @return 代理类型生成管道建造器
     */
    public ProxyTypeGenerationPipelineBuilder useProxyTypeGenerator(FunctionWithOneArg<IProxyTypeGenerator, IProxyTypeGenerator> middlewareDelegate) {
        return this.proxyTypeGenerationPipelineBuilder.use(middlewareDelegate);
    }

    /**
     * 向类型解析管道注册中间件，该管道用于在反射建模过程中解析类型
     *
     * @param middlewareDelegate 中间件委托
     * @return 类型解析管道建造器
     */
    public TypeAnalyticPipelineBuilder useTypeAnalyzer(FunctionWithOneArg<ITypeAnalyzer, ITypeAnalyzer> middlewareDelegate) {
        return this.typeAnalyticPipelineBuilder.use(middlewareDelegate);
    }

    /**
     * 向补充配置管道注册中间件，该管道用于在生成模型过程中执行补充配置。
     *
     * @param middlewareDelegate 中间件委托
     * @return 补充配置管道建造器
     */
    public ComplementConfigurationPipelineBuilder useComplementConfigurator(FunctionWithOneArg<IComplementConfigurator, IComplementConfigurator> middlewareDelegate) {
        if (this.complementConfigurationPipelineBuilder == null)
            this.complementConfigurationPipelineBuilder = new ComplementConfigurationPipelineBuilder();
        return this.complementConfigurationPipelineBuilder.use(middlewareDelegate);
    }

    /**
     * 向建模器添加类型配置项
     *
     * @param configuration 类型配置项
     */
    public void addConfiguration(StructuralTypeConfiguration<?> configuration) {
        this.getTypeConfigs().put(configuration.getClrType(), configuration);
    }

    /**
     * 指定从程序集解析类型过程中应忽略的类型；如果类型已创建配置器，应删除该配置器。
     *
     * @param ignored 要忽略的类型
     * @return 自身
     */
    public ModelBuilder ignore(Class<?> ignored) {
        //加入忽略
        this.ignoredTypes.add(ignored);
        //删除已有
        this.getTypeConfigs().remove(ignored);
        return this;
    }

    /**
     * 为建模设置类加载器
     * 在SpringBoot等框架中 存在多个类加载 此时需要将Obase的类加载器设置为主加载器而不是依赖加载器
     * 通常情况下 Obase会自动将上下文类型的类加载器设置为自己的类加载器 如果需要更改 使用此方法在注册模型时修改
     *
     * @param classLoader 类加载器
     */
    public void hasClassLoader(ClassLoader classLoader) {
        GlobalClassLoaderCache.getInstance().setClassLoader(classLoader);
    }
}
