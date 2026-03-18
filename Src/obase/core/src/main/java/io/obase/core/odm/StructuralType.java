/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结构化类型,为实体类、关联型和复杂类型提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:54:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.common.TwoTuple;
import io.obase.core.common.Utils;
import io.obase.core.odm.objectSys.*;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

/**
 * 为实体类、关联型和复杂类型提供基础实现。
 */
public abstract class StructuralType extends TypeBase {

    /**
     * 键为元素名值为元素（属性、引用元素（关联端、关联引用））
     */
    protected final Map<String, TypeElement> elements = new HashMap<>();
    /**
     * 邮戳锁对象
     */
    private final StampedLock stampedLock = new StampedLock();
    /**
     * 当前类型的继承类型
     */
    private final List<StructuralType> derivedTypes = new ArrayList<>();
    /**
     * 当前类型的基类型
     */
    private final StructuralType derivingFrom;
    /***
     * 类型扩展
     */
    private final List<TypeExtension> extensions = new ArrayList<>();
    /**
     * 基类型的构造器
     */
    protected IInstanceConstructor baseTypeConstructor;
    /**
     * 构造器
     */
    protected IInstanceConstructor constructor;
    /**
     * 以类型各属性为根节点生长而成的属性树
     */
    private Map<String, AttributeTree> attributeTrees;
    /**
     * 具体类型判别标志
     */
    private TwoTuple<String, Object> concreteTypeSign;
    /**
     * 对象数据模型
     */
    private ObjectDataModel model;

    /**
     * 新实例构造函数
     */
    private IInstanceConstructor newInstanceConstructor;

    /**
     * 代理类型，如果未生成代理类则为null
     */
    private Class<?> proxyType;

    /**
     * 构造StructuralType实例
     *
     * @param clrType      运行时类型
     * @param derivingFrom 基类型
     */
    protected StructuralType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType);
        this.derivingFrom = derivingFrom;
        //为当前类型的基类注册继承类
        if (this.derivingFrom != null) {
            this.derivingFrom.registerDerivedType(this);
        }
    }

    /**
     * 构造StructuralType实例
     *
     * @param clrType 运行时类型
     */
    protected StructuralType(Class<?> clrType) {
        this(clrType, null);
    }

    /**
     * 创建类型实例，该实例还没有关联的对象系统类型，有待后续指定。
     */
    protected StructuralType() {
        this.derivingFrom = null;
    }

    /**
     * 获取该类型对象的构造器
     *
     * @return 该类型对象的构造器
     */
    public IInstanceConstructor getConstructor() {
        return this.constructor;
    }

    /**
     * 设置该类型对象的构造器
     *
     * @param constructor 该类型对象的构造器
     */
    public void setConstructor(IInstanceConstructor constructor) {
        this.constructor = constructor;
        this.constructor.setInstanceType(this);
    }

    /**
     * 获取基类型的构造器
     *
     * @return 基类型的构造器
     */
    public IInstanceConstructor getBaseTypeConstructor() {
        return this.baseTypeConstructor;
    }

    /**
     * 获取类型包含的属性的集合
     *
     * @return 属性的集合
     */
    public List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();

        for (TypeElement ele : this.getElements()) {
            if (ele instanceof Attribute) {
                attributes.add((Attribute) ele);
            }
        }
        return attributes;
    }

    /**
     * 获取类型包含的所有元素
     *
     * @return 所有元素
     */
    public List<TypeElement> getElements() {
        //获取继承链
        List<StructuralType> derivingList = Utils.getDerivingChain(this);
        //用字典存储元素 同名的子级覆盖
        Map<String, TypeElement> result = new HashMap<>();
        //处理继承链上的每个类型
        for (StructuralType derivingType : derivingList) {
            //加入当前类型的元素
            for (TypeElement element : derivingType.elements.values()) {
                result.put(element.getName(), element);
            }
        }
        return new ArrayList<>(result.values());
    }

    /**
     * 获取类型对应的对象系统类型的代理类型
     *
     * @return 对应的对象系统类型的代理类型
     */
    public Class<?> getProxyType() {
        return this.proxyType;
    }

    /**
     * 设置类型对应的对象系统类型的代理类型
     * 如果类型已放入模型，要模型中创建一条代理映射。
     *
     * @param proxyType 代理类型
     */
    public void setProxyType(Class<?> proxyType) {
        this.model.createProxyMapping(this.clrType, proxyType);
        this.proxyType = proxyType;
    }

    /**
     * 获取结构化类型所属的对象数据模型
     *
     * @return 结构化类型所属的对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 设置结构化类型所属的对象数据模型
     *
     * @param model 类型所属的模型
     */
    public void setModel(ObjectDataModel model) {
        this.model = model;
    }

    /**
     * 获取重建对象时实际使用的程序语言类型。如果已生成代理类型，则使用代理类型，否则使用原始类型
     *
     * @return 程序语言类型
     */
    public Class<?> getRebuildingType() {
        if (this.proxyType != null)
            return this.proxyType;
        else
            return this.clrType;
    }

    /**
     * 当前类型的基类型
     *
     * @return 当前类型的基类型
     */
    public StructuralType getDerivingFrom() {
        //返回基类
        return this.derivingFrom;
    }

    /**
     * 继承类的集合
     *
     * @return 继承类的集合
     */
    public List<StructuralType> getDerivedTypes() {
        return this.derivedTypes;
    }

    /**
     * 新实例构造函数
     *
     * @return 新实例构造函数
     */
    public IInstanceConstructor getNewInstanceConstructor() {
        return this.newInstanceConstructor;
    }

    /**
     * 设置新实例构造函数
     *
     * @param newInstanceConstructor 新实例构造函数
     */
    public void setNewInstanceConstructor(IInstanceConstructor newInstanceConstructor) {
        this.newInstanceConstructor = newInstanceConstructor;
    }

    /**
     * 获取具体类型判别标志
     *
     * @return 具体类型判别标志
     */
    public TwoTuple<String, Object> getConcreteTypeSign() {
        return this.concreteTypeSign;
    }

    /**
     * 设置具体类型判别标志
     *
     * @param concreteTypeSign 具体类型判别标志
     */
    public void setConcreteTypeSign(TwoTuple<String, Object> concreteTypeSign) {
        this.concreteTypeSign = concreteTypeSign;
    }

    /**
     * 向类型（实体型、关联型、复杂类型）添加属性
     *
     * @param attribute 要添加的属性
     */
    public void addAttribute(Attribute attribute) {
        this.addElement(attribute);
    }

    /**
     * 向类型（实体型、关联型、复杂类型）添加元素（属性、关联引用或关联端）
     *
     * @param element 要添加的元素
     */
    public void addElement(TypeElement element) {
        long stamp = this.stampedLock.writeLock();
        element.setHostType(this);
        this.elements.put(element.getName(), element);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 为当前类型添加扩展
     *
     * @param extension 要添加的类型扩展
     */
    public void addExtension(TypeExtension extension) {
        extension.setExtendedType(this);
        this.extensions.add(extension);
    }

    /**
     * 为当前类型添加扩展
     *
     * @param extensionType 扩展类型，它是一个继承自TypeExtension的类型
     * @return 新创建的类型扩展实例
     */
    public TypeExtension addExtension(Class<?> extensionType) {
        if (!(TypeExtension.class.isAssignableFrom(extensionType)))
            throw new IllegalArgumentException("添加扩展失败," + extensionType.getName() + "不是TypeExtension类型");
        try {
            TypeExtension extension = (TypeExtension) extensionType.getConstructor().newInstance();
            extension.setExtendedType(this);
            this.extensions.add(extension);
            return extension;
        } catch (Exception e) {
            throw new IllegalArgumentException("添加扩展失败," + extensionType.getName() + "没有适合的无参构造函数", e);
        }
    }

    /**
     * 枚举以各属性为根生成的属性树（包含继承自基类的
     *
     * @return 各属性为根生成的属性树
     */
    public Iterable<AttributeTree> enumerateAttributeTree() {

        long stamp = this.stampedLock.readLock();
        try {
            while (this.attributeTrees == null) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    //属性
                    List<Attribute> attrs = this.getAttributes();
                    //生长器
                    AttributeTreeGrower grower = new AttributeTreeGrower();

                    this.attributeTrees = new HashMap<>();

                    for (Attribute attribute : attrs) {
                        AttributeTree attrTree = new AttributeTree(attribute);
                        attrTree.accept(grower);
                        this.attributeTrees.put(attribute.getName(), attrTree);
                    }
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.attributeTrees.values();
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }

    /**
     * 获取以指定属性为根生成的属性树
     *
     * @param attrName 属性名称
     * @return 属性树
     */
    public AttributeTree getAttributeTree(String attrName) {
        Iterable<AttributeTree> trees = this.enumerateAttributeTree();
        for (AttributeTree tree : trees) {
            if (Objects.equals(tree.getAttributeName(), attrName)) {
                return tree;
            }
        }
        return null;
    }

    /**
     * 根据名称查询类型包含的元素（属性、关联引用或关联端）
     *
     * @param name 元素名称
     * @return 元素
     */
    public TypeElement getElement(String name) {
        return this.getElements().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * 根据名称查询属性
     *
     * @param name 属性名称
     * @return 属性
     */
    public Attribute getAttribute(String name) {
        TypeElement result = this.getElement(name);
        if (result instanceof Attribute) {
            return (Attribute) result;
        }
        return null;
    }

    /**
     * 获取类型扩展
     *
     * @param extensionType 扩展类型，即派生自TypeExtension的具体类型
     * @return 返回类型扩展实例；如果指定的扩展类型不存在，返回null。
     */
    public TypeExtension getExtension(Class<? extends TypeExtension> extensionType) {
        Optional<TypeExtension> typeExtension = this.extensions.stream().filter(p -> p.getClass() == extensionType).findFirst();
        return typeExtension.orElse(null);
    }

    /**
     * 根据映射字段查找属性，未找到则返回null。
     *
     * @param field 映射字段
     * @return 属性
     */
    public Attribute findAttributeByTargetField(String field) {
        for (Attribute item : this.getAttributes()) {
            if (item.getTargetField().equalsIgnoreCase(field))
                return item;
        }
        return null;
    }

    /**
     * 实例化结构类型，但不初始化实例属性
     *
     * @param argGetter 一个委托，用于为构造参数取值
     * @return 实例化结构类型
     */
    public Object instantiateWithParameter(FunctionWithOneArg<Parameter, Object> argGetter) {
        return this.instantiateWithSimpleAttributeNode(argGetter, null);
    }

    /**
     * 实例化结构类型，并初始化实例属性。仅支持构造参数均为属性参数的类型。
     *
     * @param attrValueGetter 一个委托，用于为属性树节点代表的简单属性取值
     * @return 实例化结构类型
     */
    public Object instantiate(FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter) {
        //构造取参器
        AttributeValueGetterBasedArgumentGetter argValueGetter = new AttributeValueGetterBasedArgumentGetter(attrValueGetter);

        FunctionWithOneArg<Parameter, Object> ArgGetter = parameter -> {
            try {
                return argValueGetter.get(parameter);
            } catch (Exception e) {
                throw new IllegalArgumentException("无法获取参数值" + e.getMessage(), e);
            }
        };

        return this.instantiateWithSimpleAttributeNode(ArgGetter, attrValueGetter);
    }

    /**
     * 实例化结构类型，并初始化实例属性
     *
     * @param argGetter           一个委托，用于为构造参数取值。
     * @param attrNodeValueGetter 一个委托，用于为属性树节点代表的简单属性取值
     * @return 实例化结构类型
     */
    protected Object instantiateWithSimpleAttributeNode(FunctionWithOneArg<Parameter, Object> argGetter,
                                                        FunctionWithOneArg<SimpleAttributeNode, Object> attrNodeValueGetter) {
        //构造Func<Attribute, object> 委托
        //此处用本地方法代替
        FunctionWithOneArg<Attribute, Object> attrValueGetter = attribute -> {
            try {
                AttributeTree attrTree = new AttributeTree(attribute);
                AttributeTreeGrower grower = new AttributeTreeGrower();
                attrTree.accept(grower);
                AttributeValueGenerator generator = new AttributeValueGenerator(attrNodeValueGetter);
                attrTree.accept(generator);
                return generator.getResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("无法获取属性值" + e.getMessage(), e);
            }

        };

        return this.instantiate(argGetter, attrValueGetter);
    }

    /**
     * 实例化结构类型，并初始化实例属性。
     *
     * @param argGetter       一个委托，用于为构造参数取值
     * @param attrValueGetter 属性取值委托，属性须为类型的直接属性。
     * @return 实例化结构类型
     */
    protected Object instantiate(FunctionWithOneArg<Parameter, Object> argGetter, FunctionWithOneArg<Attribute, Object> attrValueGetter) {
        List<Parameter> paras = this.constructor.getParameters();
        //取出所有的值
        Object[] paraValues = paras == null ? new Object[0] : paras.stream().map(argGetter::invoke).toArray();

        Object resultObj = this.constructor.construct(paraValues);

        List<Attribute> attrs = this.getAttributes();
        if (this.constructor instanceof AbstractConstructor) {
            AbstractConstructor abstractConstructor = (AbstractConstructor) this.constructor;
            attrs = abstractConstructor.getDiscriminateType(paraValues).getAttributes();
        }


        for (Attribute attribute : attrs) {
            String attrName = attribute.getName();
            Parameter parameter = this.constructor.getParameterByElement(attrName);
            if (parameter != null) {
                //如果是生成的类型判断参数 则跳过
                if (!parameter.getName().equalsIgnoreCase("obase_gen_typeCode"))
                    continue;
            }

            Object value = attrValueGetter.invoke(attribute);
            if (value != null)
                attribute.setValue(resultObj, value);
        }

        return resultObj;
    }

    /**
     * 完整性检查
     * 继承类需要检查则重写此方法
     *
     * @param errDictionary 错误信息字典
     */
    public abstract void integrityCheck(Map<String, List<String>> errDictionary);

    /**
     * 根据快照重建对象
     *
     * @param snapshot  快照对象
     * @param attachObj 附加对象委托
     * @param asRoot    是否作为根对象
     * @return 重建的对象
     */
    public Object rebuild(ObjectSnapshot snapshot, IAttachObject attachObj, boolean asRoot) {
        Map<ObjectKey, ObjectSnapshot> references = snapshot.getAllReferences();
        Map<ObjectKey, Object> rebuiltObjs = new HashMap<>();
        return this.rebuild(snapshot, attachObj, asRoot, references, rebuiltObjs);
    }

    /**
     * 根据快照重建对象
     *
     * @param snapshot    对象快照
     * @param attachObj   用于将对象附加到对象上下文的委托
     * @param asRoot      对象是否为根对象
     * @param references  在重建过程中存储被引用对象的容器，它将沿递归路径逐级传递
     * @param rebuiltObjs 在重建过程中存储已重建对象的容器，它将沿递归路径逐级传递
     * @return 重建的对象
     */
    public Object rebuild(ObjectSnapshot snapshot, IAttachObject attachObj, boolean asRoot,
                          Map<ObjectKey, ObjectSnapshot> references, Map<ObjectKey, Object> rebuiltObjs) {
        //创建基础对象
        Object resultObj = this.constructor.construct(null);
        // 添加到rebuiltObjs
        if (this instanceof ObjectType)
            rebuiltObjs.put(snapshot.getKey(), resultObj);
        //循环类型元素
        for (TypeElement element : this.getElements()) {
            //获取元素的值
            String eleName = element.getName();
            Object eleValue;
            try {
                eleValue = snapshot.getElement(eleName);
            } catch (ElementNotFoundException ex) {
                continue;
            }

            //为属性
            if (element instanceof Attribute) {
                Attribute attribute = (Attribute) element;
                //复杂属性 获取下一层
                if (attribute instanceof ComplexAttribute) {
                    //重建复杂属性对象
                    eleValue = this.rebuild((ObjectSnapshot) eleValue, attachObj, false, references, rebuiltObjs);
                }
                if (attribute.getDataType().isEnum()) {
                    Enum<?>[] cons = (Enum<?>[]) attribute.getDataType().getEnumConstants();
                    for (Enum<?> con : cons) {
                        if (Objects.equals(con.ordinal(), eleValue)) {
                            eleValue = con;
                        }
                    }
                }
                element.setValue(resultObj, eleValue);
            }
            //为引用
            else if (element instanceof ReferenceElement) {
                ReferenceElement associationReference = (ReferenceElement) element;

                //引用的类型
                StructuralType subType = (StructuralType) element.getValueType();
                //引用值为集合类型并且设值模式为“赋值”
                if (associationReference.getIsMultiple() && associationReference.getValueSetter().getMode() == EValueSettingMode.Assignment) {
                    List<Object> values = new ArrayList<>();
                    if (!(eleValue instanceof List))
                        continue;
                    List<ObjectKey> eleValueList = (List<ObjectKey>) eleValue;
                    for (ObjectKey eleKey : eleValueList) {
                        //重建引用对象
                        Object refObj;
                        if (rebuiltObjs.containsKey(eleKey))
                            refObj = rebuiltObjs.get(eleKey);
                        else
                            refObj = subType.rebuild(references.get(eleKey), attachObj, false, references, rebuiltObjs);
                        values.add(refObj);
                    }

                    element.setValue(resultObj, values);
                }
                //引用值为非集合类型或引用值为集合类型并且设值模式为“追加”
                else {
                    if (!(eleValue instanceof List))
                        continue;
                    List<ObjectKey> eleValueList = (List<ObjectKey>) eleValue;
                    for (ObjectKey eleKey : eleValueList) {
                        //重建引用对象
                        Object refObj;
                        if (rebuiltObjs.containsKey(eleKey))
                            refObj = rebuiltObjs.get(eleKey);
                        else
                            refObj = subType.rebuild(references.get(eleKey), attachObj, false, references, rebuiltObjs);
                        element.setValue(resultObj, refObj);
                    }
                }

            }
        }

        if (this instanceof ObjectType && attachObj != null) {
            ObjectReferencePack<Object> objectReferencePack = new ObjectReferencePack<>();
            objectReferencePack.realValue = resultObj;
            attachObj.attachObject(objectReferencePack, asRoot);
        }

        return resultObj;
    }

    /**
     * 为指定对象生成快照
     *
     * @param targetObj 被快照的对象
     * @return 对象快照
     */
    public ObjectSnapshot snapshot(Object targetObj) {
        Map<ObjectKey, ObjectSnapshot> references = new HashMap<>();
        ObjectSnapshot snapshot = this.snapshot(targetObj, references);
        if (this instanceof ObjectType) {
            ObjectType objectType = (ObjectType) this;
            references.remove(objectType.getObjectKey(targetObj));
        }
        snapshot.setAllReferences(references);
        return snapshot;
    }

    /**
     * 为指定对象生成快照
     *
     * @param targetObj  被快照的对象
     * @param references 在快照过程中存储被引用对象的容器，它将沿递归路径逐级传递
     * @return 对象快照
     */
    ObjectSnapshot snapshot(Object targetObj, Map<ObjectKey, ObjectSnapshot> references) {
        ObjectSnapshot snapshot = new ObjectSnapshot(this);
        if (this instanceof ObjectType) {
            ObjectType objectType = (ObjectType) this;
            ObjectKey key = objectType.getObjectKey(targetObj);
            references.put(key, snapshot);
        }

        //循环类型元素
        for (TypeElement element : this.getElements()) {
            //获取值
            Object eleValue = element.getValue(targetObj);
            //为属性
            if (element instanceof Attribute) {
                Attribute attribute = (Attribute) element;
                //如果为复合属性 则继续向下一层快照
                if (attribute instanceof ComplexAttribute)
                    eleValue = this.snapshot(eleValue, references);

                //Java中的枚举 不用特别的处理
                snapshot.setAttribute(element.getName(), eleValue);
            }
            //为引用
            else {
                if (eleValue != null && element instanceof ReferenceElement) {
                    ReferenceElement referenceElement = (ReferenceElement) element;

                    ObjectType refType = (ObjectType) element.getValueType();

                    if (referenceElement.getIsMultiple() && eleValue instanceof Iterable) {
                        Iterable<Object> valueEnumerable = (Iterable<Object>) eleValue;
                        for (Object refObj : valueEnumerable) {
                            ObjectKey key = refType.getObjectKey(refObj);
                            //为引用建立快照
                            if (!references.containsKey(key))
                                refType.snapshot(refObj, references);
                            //添加到引用
                            snapshot.addReference(element.getName(), key);
                        }
                    } else {
                        ObjectKey key = refType.getObjectKey(eleValue);
                        //为引用建立快照
                        if (!references.containsKey(key))
                            refType.snapshot(eleValue, references);
                        //添加到引用
                        snapshot.addReference(element.getName(), key);
                    }
                }
            }
        }

        return snapshot;
    }

    /**
     * 为新元素命名。
     * 实施说明
     * 默认使用建议名；如果该名称已被占用，在建议名后追加数字“1”，如果仍然被占用，则将追加数字值加1，直到得到一个未被占用的名称。
     * 注意，实施同名校验时，不仅要检查类型的已有元素，还要检查预定义元素。
     *
     * @param proposedName 推荐使用的名称
     * @param predefined   预定义的元素
     * @return 命名
     */
    public String nameNew(String proposedName, TypeElement[] predefined) {

        boolean independent = false;
        if (this instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this;
            if (associationType.getIndependent())
                independent = true;
        }

        boolean exits = true;
        if (!independent) {
            //是否已被占用
            String finalProposedName = proposedName;
            exits = this.getElements().stream().anyMatch(elementsValue -> elementsValue.getName().equals(finalProposedName));

            if (predefined != null)
                if (Arrays.stream(predefined).anyMatch(preElement -> preElement.getName().equals(finalProposedName)))
                    exits = true;

            //不重名
            if (!exits)
                return proposedName;
        }

        //尾部附加
        int ext = 0;
        while (exits) {
            ext++;
            proposedName = "proposedName" + ext;
            String finalProposedName1 = proposedName;
            exits = this.getElements().stream().anyMatch(elementsValue -> elementsValue.getName().equals(finalProposedName1));

            if (predefined != null)
                if (Arrays.stream(predefined).anyMatch(preElement -> preElement.getName().equals(finalProposedName1)))
                    exits = true;
        }

        return proposedName;
    }

    /**
     * 注册派生类型
     *
     * @param derivedType 派生类型
     */
    private void registerDerivedType(StructuralType derivedType) {
        //如果配置相应的判别标志值
        if (derivedType.getConcreteTypeSign() != null) {
            for (StructuralType derived : this.derivedTypes) {
                if (derived.getConcreteTypeSign() != null && !derived.getConcreteTypeSign().getItem2().getClass().equals(derivedType.getConcreteTypeSign().getItem2().getClass()))
                    throw new IllegalArgumentException("" + derivedType.getName() + "与" + derived.getName() + "均为" + this.derivingFrom.getName() + "的继承类,但判别字段类型不相符.");
            }
        }

        //加入派生类型集合
        if (!this.derivedTypes.contains(derivedType))
            this.derivedTypes.add(derivedType);
    }

    /**
     * 设置具体类型判别器
     * 本方法将自动生成一个抽象构造器作为当前类型的构造器。
     * 如果在此之前已显式设置了构造器（通过Constructor属性），自动将该构造器作为基类实例构造器。
     * 调用此方法前应将当前类型指定为基类（通过派生类型的DerivingFrom属性），否则无法生成抽象构造器。
     *
     * @param discriminator     判别器实例
     * @param typeAttributeName 类型的一个属性,用于指示具体类型
     */
    public void setConcreteTypeDiscriminator(IConcreteTypeDiscriminator discriminator, String typeAttributeName) {
        if (this.derivedTypes.size() == 0)
            throw new IllegalArgumentException("只有基类类型可以设置具体类型判别器.");

        //当前的构造器保存至_baseTypeConstructor
        this.baseTypeConstructor = this.constructor;

        //_constructor改为AbstractConstructor
        AbstractConstructor constructor = new AbstractConstructor(this.constructor.getParameters(), discriminator, typeAttributeName);
        constructor.setInstanceType(this);

        this.constructor = constructor;
    }

    /**
     * 基于属性取值委托的取参器。
     * 包含一个简单属性取值委托，通过调用该委托为绑定到属性的构造参数取值。
     */
    protected static class AttributeValueGetterBasedArgumentGetter extends StructuralType {

        /**
         * 简单属性取值委托
         */
        private final FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter;

        /**
         * 创建AttributeValueGetterBasedArgumentGetter实例
         *
         * @param attrValueGetter 简单属性取值委托
         */
        public AttributeValueGetterBasedArgumentGetter(FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter) {
            super(SimpleAttributeNode.class);
            this.attrValueGetter = attrValueGetter;
        }

        /**
         * 获取指定构造参数的值
         *
         * @param parameter 要取值的构造参数。只能是属性参数
         * @return 值
         */
        public Object get(Parameter parameter) {
            String attrName = parameter.getElementName();
            TypeElement element = parameter.getElement();
            //没有对应的元素
            if (element == null) {
                Attribute attr = new Attribute(parameter.getExpression().getType(), attrName);
                attr.setTargetField(attrName);
                Object result =
                        this.attrValueGetter.invoke(new SimpleAttributeNode(attr));

                return parameter.getValueConverter() == null ? result : parameter.getValueConverter().invoke(result);
            }
            if (element instanceof ReferenceElement)
                throw new IllegalArgumentException("类型的构造函数不能具有引用型参数。");
            AttributeTree attributeTree = element.getHostType().getAttributeTree(attrName);
            AttributeValueGenerator generator = new AttributeValueGenerator(this.attrValueGetter);
            attributeTree.accept(generator);
            Object value = generator.getResult();
            FunctionWithOneArg<Object, Object> converter = parameter.getValueConverter();
            return converter == null ? value : converter.invoke(value);
        }

        /**
         * 完整性检查
         * 继承类需要检查则重写此方法
         *
         * @param errDictionary 错误信息字典
         */
        @Override
        public void integrityCheck(Map<String, List<String>> errDictionary) {
            //Nothing To Do
        }
    }
}
