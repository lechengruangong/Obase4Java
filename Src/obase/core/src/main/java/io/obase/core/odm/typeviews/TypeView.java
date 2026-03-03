/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型视图.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:28:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithNoArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.NewExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.*;
import io.obase.core.query.StorageHeterogeneityPredicationProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 类型视图。
 * 类型视图是对一个类型及以其为中心的对象系统的局部观察。从形式上看，类型视图是由该类型的元素及其关联（直接或间接）的类型的元素组合而成的临时类型。
 * 该类型称为视图的源，以源类型为中心的对象系统称为源扩展，可以用一个关联树表示，其根节点代表源类型。
 */
public class TypeView extends ReferringType implements IMappable {

    /**
     * 别名生成器
     */
    private final AssociationTreeNodeAliasGenerator aliasGenerator = new AssociationTreeNodeAliasGenerator();

    /**
     * 附加项。
     */
    private final List<TypeViewAttachingItem> attachingItems = new ArrayList<>();

    /**
     * 锁对象
     */
    private final Object lockObject = new Object();

    /**
     * 作为视图源的类型。
     */
    private final StructuralType source;

    /**
     * 在表达式（如视图属性的绑定表达式）中代表视图源的形式参数
     */
    private final ParameterExpression sourceParameter;

    /**
     * 用于存储锚点与视图元素之间关联的字典，其中键为锚点，值为元素集合。
     */
    private Map<AssociationTreeNode, TypeElement[]> anchorElements;

    /**
     * 执行极限分解后的基础视图。
     */
    private TypeView baseView;

    /**
     * 是否已分解
     */
    private boolean decomposed;

    /**
     * 平展鍵
     */
    private ViewAttribute[] flatteningKey;

    /**
     * 平展点。
     * 在源扩展树中，如果某个节点代表的元素具有多重性（IsMultiple == true），如果指定在此节点上平展，那么在最终生成的视图实例集中，该元素的属主对象将被复制多份，分别引用该元素值集合中的一个。该节点称为平展点。
     */
    private List<ViewFlatteningPoint> flatteningPoints;

    /**
     * 视图的标识属性，各属性值的组合可以唯一标识一个视图实例。标识属性是顺序敏感的。
     */
    private String[] keyAttributes;

    /**
     * 标识成员
     */
    private List<String> keyField;

    /**
     * 标识成员的名称序列
     */
    private List<String> keyMemberNames;

    /**
     * 形参绑定，即作为参数值来源的表达式。
     */
    private List<ParameterBinding> parameterBindings;

    /**
     * 源扩展。
     */
    private AssociationTree sourceExtension;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 此视图是否为极限分解的结果
     */
    private boolean isDecomposeExtremelyResult;

    /**
     * 创建TypeView实例。
     *
     * @param source     视图源。
     * @param clrType    视图的CLR类型。
     * @param sourcePara 在表达式中代表视图源的形式参数。
     */
    public TypeView(StructuralType source, Class<?> clrType, ParameterExpression sourcePara) {
        super(clrType, null);
        this.source = source;
        this.sourceParameter = sourcePara;
        if (source instanceof ReferringType) {
            ReferringType referringType = (ReferringType) source;
            this.sourceExtension = new AssociationTree(referringType);
        }
    }

    /**
     * 创建基于指定源扩展树的TypeView实例
     *
     * @param sourceExtension 源拓展
     */
    public TypeView(AssociationTree sourceExtension) {
        super(null, null);
        this.source = sourceExtension.getRoot().getRepresentedType();
        this.sourceParameter = Expression.parameter("", sourceExtension.getRoot().getRepresentedType().getClrType());
        this.sourceExtension = sourceExtension;
    }

    /**
     * 获取所有视图引用
     *
     * @return 获取所有视图引用
     */
    public ViewReference[] getViewReferences() {
        if (this.getElements() != null) {
            return this.getElements().stream().filter(p -> p.getClass().equals(ViewReference.class)).map(p -> (ViewReference) p).toArray(ViewReference[]::new);
        }
        return new ViewReference[0];
    }

    /**
     * 获取视图源
     *
     * @return 视图源
     */
    public StructuralType getSource() {
        return this.source;
    }

    /**
     * 获取视图扩展
     *
     * @return 获取视图扩展
     */
    public AssociationTree getExtension() {
        return this.sourceExtension;
    }

    /**
     * 设置视图扩展
     *
     * @param sourceExtension 视图扩展
     */
    public void setExtension(AssociationTree sourceExtension) {
        this.sourceExtension = sourceExtension;
    }

    /**
     * 获取视图的平展鍵。如果视图没有平展点或者未定义平展属性，均返回null。
     *
     * @return 视图的平展鍵
     */
    public ViewAttribute[] getFlatteningKey() {
        return this.flatteningKey;
    }

    /**
     * 获取所有平展点
     *
     * @return 所有平展点
     */
    public AssociationTreeNode[] getFlatteningPoints() {
        if (this.flatteningPoints != null) {
            return this.flatteningPoints.stream().map(ViewFlatteningPoint::getExtensionNode).toArray(AssociationTreeNode[]::new);
        }
        return new AssociationTreeNode[0];
    }

    /**
     * 获取形参绑定
     *
     * @return 获取形参绑定
     */
    public ParameterBinding[] getParameterBindings() {
        if (this.parameterBindings == null)
            return new ParameterBinding[0];
        return this.parameterBindings.toArray(new ParameterBinding[0]);
    }

    /**
     * 设置形参绑定
     *
     * @param parameterBindings 形参绑定
     */
    public void setParameterBindings(ParameterBinding[] parameterBindings) {
        this.parameterBindings = Arrays.asList(parameterBindings);
    }

    /**
     * 此视图是否为极限分解的结果
     *
     * @return 极限分解的结果
     */
    public boolean getIsDecomposeExtremelyResult() {
        return this.isDecomposeExtremelyResult;
    }

    /**
     * 此视图是否为极限分解的结果
     *
     * @param value 极限分解的结果
     */
    public void setIsDecomposeExtremelyResult(boolean value) {
        this.isDecomposeExtremelyResult = value;
    }

    /**
     * 获取标识属性
     *
     * @return 获取标识属性
     */
    public String[] getKeyAttributes() {
        return this.keyAttributes;
    }

    /**
     * 设置标识属性
     *
     * @param keyAttributes 标识属性
     */
    public void setKeyAttributes(String[] keyAttributes) {
        this.keyField = null;
        this.keyMemberNames = null;
        this.keyAttributes = keyAttributes;
    }

    /**
     * 获取在表达式（如视图属性的绑定表达式）中代表视图源的形式参数
     *
     * @return 获取在表达式（如视图属性的绑定表达式）中代表视图源的形式参数
     */
    public ParameterExpression getSourceParameter() {
        return this.sourceParameter;
    }

    /**
     * 获取标识成员的映射目标序列
     *
     * @return 标识成员的映射目标序列
     */
    @Override
    public List<String> getKeyFields() {

        synchronized (this.lockObject) {
            if (this.keyField != null && this.keyField.size() > 0)//已设置/已生成标识成员 则返回值
                return this.keyField;

            this.generateKey(new ObjectReferencePack<>());
            return this.keyField;
        }
    }

    /**
     * 设置标识成员的映射目标序列
     *
     * @param keyFields 标识成员的映射目标序列
     */
    @Override
    public void setKeyFields(List<String> keyFields) {
        this.keyField = keyFields;
    }

    /**
     * 获取映射目标名称
     *
     * @return 映射目标名称
     */
    @Override
    public String getTargetName() {
        if (Utils.getStringIsEmpty(this.targetName)) {
            return this.source.getName();
        }
        return this.targetName;
    }

    /**
     * 设置映射目标名称
     *
     * @param targetName 映射目标名称
     */
    @Override
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * 获取标识成员的名称序列
     *
     * @return 标识成员的名称序列
     */
    @Override
    public String[] getKeyMemberNames() {
        synchronized (this.lockObject) {
            //是否已生成
            if (this.keyMemberNames != null && this.keyMemberNames.size() > 0)
                return this.keyMemberNames.toArray(new String[0]);
            this.generateKey(new ObjectReferencePack<>()); //生成标识和标识成员
            return this.keyMemberNames.toArray(new String[0]);
        }
    }

    /**
     * 获取对视图实施极限分解得到的基础视图。
     * 警告
     * 不会检测视图是否为异构，对于同构视图，将生成其副本作为基础视图。强烈建议调用前实施异构性检测。
     *
     * @param predicationProvider 异构断言器
     * @return 分解得到的基础视图
     */
    public TypeView getBaseView(HeterogeneityPredicationProvider predicationProvider) {
        synchronized (this.lockObject) {
            if (this.baseView == null) {
                this.decomposeExtremely(predicationProvider);
            }
            return this.baseView;
        }
    }

    /**
     * 获取对视图实施极限分解得到的基础视图。
     * 警告
     * 不会检测视图是否为异构，对于同构视图，将生成其副本作为基础视图。强烈建议调用前实施异构性检测。
     *
     * @return 分解得到的基础视图
     */
    public TypeView getBaseView() {
        return this.getBaseView(null);
    }

    /**
     * 获取一个值，该值指示视图是否是异构的
     *
     * @return 图是否是异构的
     */
    public boolean getHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        synchronized (this.lockObject) {
            if (this.getIsDecomposeExtremelyResult())
                return false;
            if (predicationProvider == null)
                predicationProvider = new StorageHeterogeneityPredicationProvider();
            AssociationTreeHeterogeneityPredicater predicater = new AssociationTreeHeterogeneityPredicater(predicationProvider);
            this.sourceExtension.accept(predicater);

            return predicater.getResult();
        }
    }

    /**
     * 为视图添加元素
     *
     * @param element 要添加的元素
     */
    @Override
    public void addElement(TypeElement element) {
        synchronized (this.lockObject) {
            super.addElement(element);
            if (this.anchorElements == null) this.anchorElements = new HashMap<>();
            if (element instanceof ViewAttribute) {
                ViewAttribute viewAttribute = (ViewAttribute) element;
                for (ViewAttributeSource source : viewAttribute.getSources()) {
                    AssociationTreeNode anchor = source.getExtensionNode();
                    this.addAnchor(element, anchor);
                }
            } else if (element instanceof ViewComplexAttribute) {
                ViewComplexAttribute viewComplexAttribute = (ViewComplexAttribute) element;
                AssociationTreeNode anchor = viewComplexAttribute.getAnchor();
                this.addAnchor(element, anchor);

            } else if (element instanceof ViewReference) {
                ViewReference viewReference = (ViewReference) element;
                AssociationTreeNode anchor = viewReference.getAnchor();
                this.addAnchor(element, anchor);
            }
        }
    }

    /**
     * 增加锚点
     *
     * @param element 类型元素
     * @param anchor  锚点
     */
    private void addAnchor(TypeElement element, AssociationTreeNode anchor) {
        List<TypeElement> elements = new ArrayList<>();
        if (anchor != null && this.anchorElements.containsKey(anchor))
            elements = Arrays.stream(this.anchorElements.get(anchor)).collect(Collectors.toList());
        elements.add(element);
        if (anchor != null)
            this.anchorElements.put(anchor, elements.toArray(new TypeElement[0]));
    }

    /**
     * 为视图添加元素
     *
     * @param elements 要添加的视图元素
     */
    public void addElement(TypeElement[] elements) {
        for (TypeElement element : elements) {
            this.addElement(element);
        }
    }

    /**
     * 添加平展点
     *
     * @param extensionNode 源扩展树上的节点，在此节点上实施扩展
     * @param ensureKey     指示是否确保定义平展鍵
     */
    public void addFlatteningPoint(AssociationTreeNode extensionNode, boolean ensureKey) {
        ParameterExpression flatteningPara = Expression.parameter("", extensionNode.getRepresentedType().getRebuildingType());
        AssociationExpressionGenerator generator = new AssociationExpressionGenerator(this.sourceParameter, null);
        LambdaExpression lambda = extensionNode.asTree().accept(generator);
        if (lambda != null)
            this.addParameterBinding(flatteningPara, lambda, EParameterReferring.Single);
        this.addFlatteningPoint(extensionNode, flatteningPara, ensureKey);
    }

    /**
     * 添加平展点
     *
     * @param extensionNode 源扩展树上的节点，在此节点上实施扩展
     */
    public void addFlatteningPoint(AssociationTreeNode extensionNode) {
        this.addFlatteningPoint(extensionNode, false);
    }

    /**
     * 添加平展点
     *
     * @param extensionNode  源扩展树上的节点，在此节点上实施扩展
     * @param flatteningPara 平展形参
     * @param ensureKey      指示是否确保定义平展鍵
     */
    public void addFlatteningPoint(AssociationTreeNode extensionNode, ParameterExpression flatteningPara, boolean ensureKey) {
        synchronized (this.lockObject) {
            //前置条件：
            //1.根节点不能作为平展点；
            //2.节点代表的引用不是多重引用的，不能作为平展点
            AssociationTree tree = extensionNode.asTree();
            if (tree.getIsRoot()) return; //根节点不能作为平展点；

            if (extensionNode instanceof ObjectTypeNode) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) extensionNode;
                if (objectTypeNode.getElement() == null) {
                    return;
                }
                if (!objectTypeNode.getElement().getIsMultiple()) {
                    return;
                }
                //节点代表的引用不是多重引用的，不能作为平展点
            }

            if (this.flatteningPoints == null) this.flatteningPoints = new ArrayList<>();
            if (this.flatteningPoints.stream().anyMatch(p -> p.getExtensionNode().equals(extensionNode))) return; //已存在。
            ViewFlatteningPoint flatteningPoint = new ViewFlatteningPoint(extensionNode, flatteningPara);
            this.flatteningPoints.add(flatteningPoint);
            if (!ensureKey) return; //不确保定义平展键，则直接结束。
            ReferringType nodeType = extensionNode.getRepresentedType();
            Attribute[] keyAttrs = new Attribute[0];
            if (nodeType instanceof EntityType) {
                EntityType entityType = (EntityType) nodeType;
                keyAttrs = entityType.getKey();
            } else if (nodeType instanceof AssociationType) {
                AssociationType associationType = (AssociationType) nodeType;

                List<Attribute> keys = new ArrayList<>();
                for (AssociationEnd end : associationType.getAssociationEnds()) {
                    keys.addAll(Arrays.asList(end.getForeignKey()));
                }
                keyAttrs = keys.toArray(new Attribute[0]);
            }

            ViewAttribute[] items = this.ensureIntuitive(keyAttrs, extensionNode);
            List<ViewAttribute> tp = this.flatteningKey != null ? Arrays.asList(this.flatteningKey) : new ArrayList<>();
            tp.addAll(Arrays.asList(items));
            this.flatteningKey = tp.toArray(new ViewAttribute[0]);

            /*
             * 20210915obase设计修改，执行以下操作时清空寄存器：
             * （1）设置标识属性（KeyAttributes）。
             * （2）添加平展点（AddFlatteningPoint）。
             */
            this.keyField = null;
            this.keyMemberNames = null;
        }
    }

    /**
     * 添加平展点
     *
     * @param extensionNode  源扩展树上的节点，在此节点上实施扩展
     * @param flatteningPara 平展形参
     */
    public void addFlatteningPoint(AssociationTreeNode extensionNode, ParameterExpression flatteningPara) {
        this.addFlatteningPoint(extensionNode, flatteningPara, false);
    }

    /**
     * 添加形参绑定
     *
     * @param parameter  形参
     * @param expression 绑定目标
     * @param referring  形参指代
     */
    public void addParameterBinding(ParameterExpression parameter, Expression expression, EParameterReferring referring) {
        synchronized (this.lockObject) {
            if (this.parameterBindings == null)
                this.parameterBindings = new ArrayList<>();
            this.parameterBindings.add(new ParameterBinding(parameter, referring, expression));
        }
    }

    /**
     * 添加形参绑定
     *
     * @param paraBindings 待添加的形参绑定集
     */
    public void addParameterBinding(ParameterBinding[] paraBindings) {
        synchronized (this.lockObject) {
            if (this.parameterBindings == null)
                this.parameterBindings = new ArrayList<>();
            this.parameterBindings.addAll(Arrays.asList(paraBindings));
        }
    }

    /**
     * 添加形参绑定
     *
     * @param paraBinding 待添加的形参绑定实例
     */
    public void addParameterBinding(ParameterBinding paraBinding) {
        synchronized (this.lockObject) {
            if (this.parameterBindings == null)
                this.parameterBindings = new ArrayList<>();
            this.parameterBindings.add(paraBinding);
        }
    }

    /**
     * 获取形参绑定
     *
     * @param parameter 要获取其绑定的形式参数
     * @param referring 形参指代
     * @return 形参绑定
     */
    public Expression getParameterBinding(ParameterExpression parameter, ObjectReferencePack<EParameterReferring> referring) {
        synchronized (this.lockObject) {
            if (this.parameterBindings == null)
                this.parameterBindings = new ArrayList<>();
            //查找值
            ParameterBinding binding = this.parameterBindings.stream().filter(p -> p.getParameter() == parameter).findFirst().orElse(null);
            //有值赋值 否则返回single
            if (binding != null && binding.getReferring() != null)
                referring.realValue = binding.getReferring();
            else {
                referring.realValue = EParameterReferring.Single;
            }
            return binding == null ? null : binding.getExpression();
        }
    }

    /**
     * 生成视图的标识成员，同时生成标识成员对应的映射目标。
     *
     * @param keyFields 标志字段
     * @return 标识成员列表
     */
    private String[] generateKey(ObjectReferencePack<String[]> keyFields) {
        if (this.keyField != null && this.keyField.size() > 0 && this.keyMemberNames != null && this.keyMemberNames.size() > 0) {
            keyFields.realValue = this.keyField.toArray(new String[0]);
            return this.keyMemberNames.toArray(new String[0]);
        }

        if (this.source instanceof IMappable) {
            IMappable mappableSource = (IMappable) this.source;
            //键成员名称序列
            List<String> keyMemberNameList = Arrays.asList(mappableSource.getKeyMemberNames());
            //映射目标序列
            List<String> keyFieldList = mappableSource.getKeyFields();

            //平展点转成关联树
            AssociationTree[] trees = new AssociationTree[0];
            if (this.flatteningPoints != null) {
                trees = this.flatteningPoints.stream().map(p -> p.getExtensionNode().asTree()).toArray(AssociationTree[]::new);
            }
            for (AssociationTree tree : trees) {
                tree.accept(this.aliasGenerator);
                //节点别名
                String nodeAlias = this.aliasGenerator.getResult();
                //分别加入
                for (String member : mappableSource.getKeyMemberNames()) {
                    keyMemberNameList.add(nodeAlias + "_" + member);
                }
                for (String field : mappableSource.getKeyFields()) {
                    keyFieldList.add(nodeAlias + "_" + field);
                }
            }
            this.keyField = keyFieldList; //寄存
            this.keyMemberNames = keyMemberNameList;//寄存

            keyFields.realValue = keyFieldList.toArray(new String[0]);
            return keyMemberNameList.toArray(new String[0]);
        }

        this.keyMemberNames = new ArrayList<>();
        this.keyField = new ArrayList<>();
        return new String[0];
    }

    /**
     * 完整性检查
     * 继承类需要检查则重写此方法
     *
     * @param errDictionary 错误信息字典
     */
    @Override
    public void integrityCheck(Map<String, List<String>> errDictionary) {
        //Nothing
    }

    /**
     * 生成视图的嵌套堆栈，最外层视图位于堆栈底部，最内层视图位于顶部。
     *
     * @return 嵌套堆栈
     */
    public Stack<TypeView> getNestingStack() {
        TypeView currentView = this;
        Stack<TypeView> nestingView = new Stack<>();
        while (currentView != null) {
            nestingView.push(this);
            currentView = (TypeView) currentView.getSource();
        }

        return nestingView;
    }

    /**
     * 生成视图的CLR类型，并为视图绑定实例构造器，为视图元素绑定取值器和设值器
     */
    public void generateType() {
        /*生成隐含类型。*/
        List<FieldDescriptor> fields = new ArrayList<>();
        List<TypeElement> elements = this.anchorElements.values().stream().flatMap(p -> Arrays.stream(p).distinct()).collect(Collectors.toList());
        for (TypeElement element : elements) {
            Class<?> type;
            if (element instanceof ViewAttribute) {
                ViewAttribute viewAttribute = (ViewAttribute) element;
                type = viewAttribute.getDataType();
            } else if (element instanceof ViewComplexAttribute) {
                ViewComplexAttribute viewComplexAttribute = (ViewComplexAttribute) element;
                type = viewComplexAttribute.getDataType();
            } else {
                ViewReference viewReference = (ViewReference) element;
                type = viewReference.getReferenceType().getClrType();
            }

            fields.add(new FieldDescriptor(type, element.getName()));
        }

        IdentityArray subIdentity = new IdentityArray(this.source.getFullName(), new Date());

        if (this.clrType == null)
            this.clrType = ImpliedTypeManager.getCurrent().applyType(fields.toArray(new FieldDescriptor[0]), subIdentity, null);

        /*为视图绑定实例构造器*/
        this.constructor = new DelegateConstructor<>((FunctionWithNoArg<Object>) () -> {
            try {
                return this.clrType.getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new IllegalArgumentException("无法创建视图实例" + e.getMessage(), e);
            }
        });

        /*为视图元素绑定设值器和取值器*/
        for (FieldDescriptor field : fields) {
            if (!this.elements.containsKey(field.getName())) continue;
            TypeElement element = this.elements.get(field.getName());//元素

            Property property = ObaseIntrospector.getObaseBeanProperties(this.clrType).stream().filter(p -> p.getName().equals(field.getName())).findFirst().orElse(null);

            if (property != null) {
                IValueGetter getter = Utils.makeDelegateValueGetter(property.getGetterMethod());
                element.setValueGetter(getter);
                element.setValueSetter(ValueSetter.create(property.getSetterMethod(), EValueSettingMode.Assignment));
            }
        }
    }

    /**
     * 获取对异构视图实施极限分解后得到的附加视图及相应的附加引用和附加点
     *
     * @param heterogeneityPredicationProvider 异构断言
     * @return 对异构视图实施极限分解后得到的附加视图及相应的附加引用和附加点
     */
    public TypeViewAttachingItem[] getAttachedViews(HeterogeneityPredicationProvider heterogeneityPredicationProvider) {
        if (heterogeneityPredicationProvider == null)
            heterogeneityPredicationProvider = new StorageHeterogeneityPredicationProvider();
        if (!this.decomposed)
            this.decomposeExtremely(heterogeneityPredicationProvider);
        if (this.attachingItems == null)
            return null;
        return this.attachingItems.toArray(new TypeViewAttachingItem[0]);
    }

    /**
     * 获取对异构视图实施极限分解后得到的附加视图及相应的附加引用和附加点
     *
     * @return 对异构视图实施极限分解后得到的附加视图及相应的附加引用和附加点
     */
    public TypeViewAttachingItem[] getAttachedViews() {
        return this.getAttachedViews(null);
    }

    /**
     * 获取锚定于指定扩展节点的元素，返回的元素中不包含非直观属性。
     *
     * @param anchor 锚点
     * @return 锚定于指定扩展节点的元素
     */
    public TypeElement[] getElements(AssociationTreeNode anchor) {
        if (this.anchorElements == null)
            this.anchorElements = new HashMap<>();
        return this.anchorElements.containsKey(anchor) ? this.anchorElements.get(anchor) : new TypeElement[0];
    }

    /**
     * 统计锚定于指定扩展节点的元素个数，不计算非直观属性。
     *
     * @param anchor 锚点
     * @return 锚定于指定扩展节点的元素个数
     */
    public int countElements(AssociationTreeNode anchor) {
        if (this.anchorElements == null)
            this.anchorElements = new HashMap<>();
        return this.anchorElements.containsKey(anchor) ? this.anchorElements.get(anchor).length : 0;
    }

    /**
     * 根据指定的属性源搜索直观属性。
     *
     * @param attribute     构成属性源的属性，须为顶级属性，不接受子属性
     * @param extensionNode 构成属性源的扩展树节点，未指定表示根节点
     * @return 直观属性
     */
    public ViewAttribute getIntuitiveAttribute(Attribute attribute, AssociationTreeNode extensionNode) {
        TypeElement[] elements;
        if (extensionNode == null) {
            elements = this.elements.values().toArray(new TypeElement[0]);
        } else {
            elements = this.getElements(extensionNode);
        }
        for (TypeElement element : elements) {
            if (element instanceof ViewAttribute) {
                ViewAttribute viewAttribute = (ViewAttribute) element;

                if (!viewAttribute.getIsIntuitive()) continue;
                SimpleAttributeNode attrNode = viewAttribute.getSources()[0].getAttributeNode();
                if (attrNode.getParent() != null || !attribute.equals(attrNode.getAttribute())) continue;
                return viewAttribute;
            } else if (element instanceof ViewReference) {
                for (TypeElement typeElement : this.elements.values()) {
                    if (typeElement instanceof ViewAttribute) {
                        ViewAttribute viewAttr = (ViewAttribute) typeElement;
                        if (viewAttr.getName().equals(attribute.getName()))
                            return viewAttr;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 根据指定的属性源搜索直观属性。
     *
     * @param attribute 构成属性源的属性，须为顶级属性，不接受子属性
     * @return 直观属性
     */
    public ViewAttribute getIntuitiveAttribute(Attribute attribute) {
        return this.getIntuitiveAttribute(attribute, null);
    }

    /**
     * 对视图实施极限分解
     *
     * @param heterogeneityPredicationProvider 异构断言
     */
    private void decomposeExtremely(HeterogeneityPredicationProvider heterogeneityPredicationProvider) {
        if (this.decomposed) return;
        if (heterogeneityPredicationProvider == null)
            heterogeneityPredicationProvider = new StorageHeterogeneityPredicationProvider();
        AssociationTreeDecomposer visitor = new AssociationTreeDecomposer(heterogeneityPredicationProvider);
        AssociationTree baseTree = this.sourceExtension.accept(visitor, false);

        TypeView baseView;
        if (visitor.getOutArgument() == null) {
            baseView = this;
        } else {
            baseView = new TypeView(baseTree);
            baseView.setIsDecomposeExtremelyResult(true);

            for (AssociationTreeAttachingItem item : visitor.getOutArgument()) {
                //创建附加视图
                TypeView attachingView = new TypeView(item.getAttachingTree());
                attachingView.setIsDecomposeExtremelyResult(true);
                //确保附加引用
                ViewReference vr = baseView.ensureReference(item.getAttachingReference(), item.getAttachingNode(), null);
                //确保绑定到引用属性的直观属性
                Attribute[] refKeys; //获取附加引用的引用键
                //确保参考键
                baseView.ensureReferredKey(vr);
                //获取附加引用的引用键
                refKeys = item.getAttachingReference().getReferringKey(true);
                //确保绑定到引用属性的直观属性
                attachingView.ensureIntuitive(refKeys, null);
                this.attachingItems.add(new TypeViewAttachingItem(attachingView, item.getAttachingNode(), vr));
            }

            ElementAdder ea = new ElementAdder(this, baseView, this.attachingItems.toArray(new TypeViewAttachingItem[0]));
            //为基础视图和附加视图定义元素
            this.sourceExtension.accept(ea);
            for (TypeElement typElementsValue : this.elements.values()) {
                if (typElementsValue instanceof ViewAttribute) {
                    ViewAttribute viewAttribute = (ViewAttribute) typElementsValue;
                    if (viewAttribute.getShadow() == null) {
                        baseView.addElement(viewAttribute);
                    }
                }
            }
            //生成基础视图的CLR类型
            baseView.generateType();
            this.getModel().addType(baseView);
            this.attachingItems.forEach(item -> {
                try {
                    //生成附加视图的CLR类型并添加
                    item.getAttachingView().generateType();
                    this.getModel().addType(item.getAttachingView());
                } catch (Exception e) {
                    throw new IllegalArgumentException("无法为附加视图生成Clr类型" + e.getMessage(), e);
                }
            });
        }

        this.baseView = baseView;
        this.decomposed = true;
    }

    /**
     * 确保视图已在指定属性源上定义了直观属性
     *
     * @param attribute     构成属性源的属性，须为顶级属性，不接受子属性
     * @param extensionNode 构成属性源的扩展树节点，未指定表示根节点
     * @param proposedName  定义属性时推荐使用的名称
     * @return 返回以该属性为源的直观属性，可能是新定义的，也可能是已存在的
     */
    public ViewAttribute ensureIntuitive(Attribute attribute, AssociationTreeNode extensionNode, String proposedName) {
        return this.ensureIntuitive(new SimpleAttributeNode(attribute), extensionNode, proposedName);
    }

    /**
     * 确保视图已在指定属性源上定义了直观属性
     *
     * @param attribute     构成属性源的属性，须为顶级属性，不接受子属性
     * @param extensionNode 构成属性源的扩展树节点，未指定表示根节点
     * @return 视图已在指定属性源上定义了直观属性
     */
    public ViewAttribute[] ensureIntuitive(Attribute[] attribute, AssociationTreeNode extensionNode) {
        List<ViewAttribute> attributes = new ArrayList<>();
        for (Attribute item : attribute) {
            ViewAttribute attr = this.ensureIntuitive(item, extensionNode, null);
            attributes.add(attr);
        }

        return attributes.toArray(new ViewAttribute[0]);
    }

    /**
     * 确保视图已在指定属性源上定义了直观属性
     *
     * @param attributeNode 构成属性源的属性树节点
     * @param extensionNode 构成属性源的扩展树节点
     * @param proposedName  定义属性时推荐使用的名称
     * @return 视图已在指定属性源上定义了直观属性
     */
    public ViewAttribute ensureIntuitive(AttributeTreeNode attributeNode, AssociationTreeNode extensionNode,
                                         String proposedName) {
        if (extensionNode != null) {
            TypeElement[] elements = this.getElements(extensionNode);
            for (TypeElement element : elements) {
                if (element instanceof ViewAttribute) {
                    ViewAttribute viewAttribute = (ViewAttribute) element;
                    if (!viewAttribute.getIsIntuitive()) continue;
                    if (attributeNode.equals(viewAttribute.getSources()[0].getAttributeNode()))
                        return viewAttribute; //返回已存在的
                }
            }
        }


        //新定义
        if (proposedName == null) proposedName = attributeNode.getAttributeName();
        String name = this.nameNew(proposedName, null); //解决名称冲突
        ViewAttribute newViewAttribute = new ViewAttribute(name, attributeNode, extensionNode);
        this.addElement(newViewAttribute);
        return newViewAttribute;
    }

    /**
     * 确保视图已在指定源上定义了视图引用
     *
     * @param binding      构成视图引用源的引用元素，也称为视图引用的绑定
     * @param anchor       构成视图引用源的扩展树节点，也称为视图引用的锚点，未指定表示根节点
     * @param proposedName 定义视图引用时推荐使用的名称
     * @return 返回定义在指定源上的视图引用，可能是新定义的，也可能是已存在的
     */
    public ViewReference ensureReference(ReferenceElement binding, AssociationTreeNode anchor, String proposedName) {

        TypeElement[] elements = this.getElements(anchor);
        for (TypeElement element : elements) {
            if (element instanceof ViewReference) {
                ViewReference viewReference = (ViewReference) element;
                if (binding.equals(viewReference.getBinding()))
                    return viewReference; //返回已存在的
            }
        }
        //新定义
        if (proposedName == null) proposedName = binding.getName();
        String name = this.nameNew(proposedName, null); //解决名称冲突
        ViewReference newViewReference = new ViewReference(binding, name, anchor);
        this.addElement(newViewReference);
        return newViewReference;
    }

    /**
     * 确保已在视图上定义了指定视图引用的参考鍵
     *
     * @param viewRef 视图引用
     * @return 确保的直观属性
     */
    public ViewAttribute[] ensureReferredKey(ViewReference viewRef) {
        ReferenceElement binding = viewRef.getBinding();
        Attribute[] attrs;
        AssociationTreeNode treeNode = null;
        if (binding.getElementType() == EElementType.ViewReference) {
            AssociationTreeNode anchor = viewRef.getAnchor();
            attrs = ((TypeView) anchor.getRepresentedType()).ensureReferredKey(viewRef);
            treeNode = anchor;
        } else {
            attrs = viewRef.getReferringKey(true);
        }

        return this.ensureIntuitive(attrs, treeNode);
    }

    /**
     * 根据指定的基础视图实例和附加视图实例生成异构视图实例
     *
     * @param baseInstances         基础视图实例的序列，每一个基础视图实例对应生成一个异构视图实例
     * @param attachingInstanceSets 附加视图实例集的集合，每一个实例集与一个附加视图对应；实例集内部包含一个或多个视图实例，具体取决于附加
     *                              引用的重数。
     * @return 生成的异构视图实例序列
     */
    public Object[] instantiate(Object[] baseInstances, AttachingInstanceSet... attachingInstanceSets) {
        List<Object> resultObjs = new ArrayList<>();

        List<InstantiationUnit> units = Arrays.stream(baseInstances).map(p -> new InstantiationUnit(p, this.baseView)).collect(Collectors.toList());
        //遍历附加实例集
        if (attachingInstanceSets != null && attachingInstanceSets.length > 0)
            for (AttachingInstanceSet item : attachingInstanceSets) {
                List<InstantiationUnit> newUnits = new ArrayList<>();
                //根据平展键分组。
                AttachingInstanceSet[] groupSets = item.groupByFlatteningKey();
                //遍历实例单元
                for (InstantiationUnit unit : units) {
                    //克隆实例化单元
                    InstantiationUnit[] unitClones = unit.clone(groupSets.length);
                    //遍历分组
                    for (int i = 0; i < groupSets.length; i++) {
                        InstantiationUnit unitClone = unitClones[i];
                        //向实例化单元添加附加实例。
                        unitClone.addAttachingInstance(groupSets[i]);
                        newUnits.add(unitClone);
                    }
                }

                units = newUnits;
            }

        //遍历实例化
        for (InstantiationUnit unit : units) {
            resultObjs.add(this.instantiateWithElementValueGetter(element -> {
                Object result;
                if (element instanceof ViewAttribute) {
                    ViewAttribute viewAttribute = (ViewAttribute) element;
                    if (!viewAttribute.getIsIntuitive()) {
                        List<Object> sourceValues = new ArrayList<>();
                        for (ViewAttributeSource item : viewAttribute.getSources()) {
                            Object agentValue = unit.getValue(item.getAgent()); //获取代理属性值
                            sourceValues.add(agentValue);
                        }
                        result = viewAttribute.getEvaluator().evaluate(sourceValues.toArray()); //属性求值
                    } else {
                        TypeElement shadow = (TypeElement) ((ITypeViewElement) element).getShadow();
                        result = unit.getValue(shadow);
                    }
                } else {
                    TypeElement shadow = (TypeElement) ((ITypeViewElement) element).getShadow();
                    if (shadow == null) {
                        result = unit.getValue(element);
                    } else {
                        result = unit.getValue(shadow);
                    }
                }

                return result;
            }));
        }

        return resultObjs.toArray();
    }

    /**
     * 生成视图表达式
     *
     * @param flatteningExpressions 返回平展表达式，无平展点返回null
     * @return 视图表达式
     */
    public LambdaExpression generateExpression(ObjectReferencePack<LambdaExpression[]> flatteningExpressions) {
        List<Parameter> parameters = new ArrayList<>();
        if (this.constructor != null && this.constructor.getParameters() != null)
            parameters = this.constructor.getParameters();

        List<Expression> arguments = new ArrayList<>();

        for (Parameter parameter : parameters) {
            TypeElement ele = parameter.getElement();
            //生成元素的绑定表达式
            if (ele instanceof ITypeViewElement) {
                ITypeViewElement element = (ITypeViewElement) ele;
                Expression exp = element.generateExpression(this.sourceParameter, associationTreeNode -> {
                    ViewFlatteningPoint point = this.flatteningPoints.stream().filter(p -> p.getExtensionNode() == associationTreeNode).findFirst().orElse(null);
                    return point == null ? null : point.getFlatteningParameter();
                });
                if (exp instanceof LambdaExpression) {
                    LambdaExpression lambdaExpression = (LambdaExpression) exp;
                    arguments.add(lambdaExpression.getBody());
                } else {
                    arguments.add(exp);
                }
            }
        }


        Constructor<?> constructor;
        try {
            constructor = this.getRebuildingType().getConstructor((parameters.stream().map(Parameter::getType).toArray(Class<?>[]::new)));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取类型视图的构造函数", e);
        }
        NewExpression expression = Expression.news(this.getRebuildingType());
        expression.setConstructor(constructor);
        expression.setArgument(arguments.toArray(new Expression[0]));

        //此处无MemberInit
        //Nothing to do

        List<LambdaExpression> flattening = new ArrayList<>();
        if (this.flatteningPoints != null && this.flatteningPoints.size() > 0) {
            for (ViewFlatteningPoint item : this.flatteningPoints) {
                Optional<ParameterBinding> exp = this.parameterBindings.stream().filter(p -> p.getParameter() == item.getFlatteningParameter()).findFirst();
                if (!exp.isPresent()) throw new IllegalArgumentException("平展点,没有对应的形参绑定。");
                ParameterExpression[] parameterExpressions = new ParameterExpression[1];
                parameterExpressions[0] = exp.get().getParameter();
                flattening.add(Expression.lambda(parameterExpressions, exp.get().getExpression()));
            }
        }

        flatteningExpressions.realValue = flattening.toArray(new LambdaExpression[0]);
        List<ParameterExpression> allParams = new ArrayList<>();
        allParams.add(this.sourceParameter);
        if (this.flatteningPoints != null) {
            allParams.addAll(this.flatteningPoints.stream().map(ViewFlatteningPoint::getFlatteningParameter).collect(Collectors.toList()));
        }
        return Expression.lambda(allParams.toArray(new ParameterExpression[0]), expression);
    }

    /**
     * 获取类型的筛选键。
     * 对于类型的某一个属性或属性序列，如果其值或值序列可以作为该类型实例的标识，该属性或属性序列即可作为该类型的筛选键。
     * 对于实体型，可以用主键作为筛选键。对于关联型，可以用其在各关联端上的外键属性组合成的属性序列作为筛选键。
     *
     * @return 构成筛选键的属性序列
     */
    @Override
    public Attribute[] getFilterKey() {
        return this.flatteningKey;
    }

    /**
     * 为基础视图和附加视图添加元素，这些元素将作为被分解视图的元素的影子元素或属性源代理。
     */
    private static class ElementAdder implements IAssociationTreeDownwardVisitor {

        /**
         * 实施极限分解得到的附加视图及其附加节点和附加引用
         */
        private final TypeViewAttachingItem[] attachingItems;

        /**
         * 实施极限分解得到的基础视图
         */
        private final TypeView baseView;

        /**
         * 被分解的视图
         */
        private final TypeView decomposedView;

        /**
         * 创建ElementAdder实例
         *
         * @param decomposedView 被分解的视图
         * @param baseView       实施极限分解得到的基础视图
         * @param attachingItems 实施极限分解得到的附加视图及其附加节点、附加引用
         */
        public ElementAdder(TypeView decomposedView, TypeView baseView, TypeViewAttachingItem[] attachingItems) {
            this.decomposedView = decomposedView;
            this.baseView = baseView;
            this.attachingItems = attachingItems;
        }

        /**
         * 前置访问，即在访问子级前执行操作。
         *
         * @param subTree          被访问的关联树子树
         * @param parentState      访问父级时产生的状态数据
         * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
         * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
         * @return 是否继续访问
         */
        @Override
        public boolean preVisit(AssociationTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
            Object[] parentStateUnboxing = parentState == null ? new Object[0] : (Object[]) parentState;
            TypeView targetView =
                    parentStateUnboxing.length > 0 ? (TypeView) parentStateUnboxing[1] : this.baseView; //从状态参数中取出目标视图

            //在目标视图（基础视图或附加视图）中定位节点
            AssociationTreeNode anchor;

            if (parentState != null) {
                AssociationTreeNode parentNode = (AssociationTreeNode) parentStateUnboxing[0];
                ObjectTypeNode currentNode = parentNode.getChild(subTree.getElementName()); //获取子节点。
                if (currentNode != null) {
                    anchor = currentNode;
                } else {
                    ReferenceElement ref = parentNode.getRepresentedType().getReferenceElement(subTree.getElementName()); //查找引用元素。
                    TypeViewAttachingItem attachingItem = Arrays.stream(this.attachingItems).filter(p -> p.getAttachingNode() == parentNode && p.getAttachingReference().equals(ref))
                            .findFirst().orElse(null);
                    if (attachingItem == null)
                        throw new RuntimeException("无法找到符合条件的附加视图项");
                    targetView = attachingItem.getAttachingView();
                    anchor = attachingItem.getAttachingView().getExtension().getNode();
                }
            } else {
                anchor = this.baseView.getExtension().getNode();
            }

            //将原视图中锚定于当前节点的元素克隆到目标视图
            TypeElement[] elements = this.decomposedView.getElements(anchor); //获取锚定于当前节点的元素。
            for (TypeElement element : elements) {
                if (element instanceof SelfReference) {
                    SelfReference selfReference = (SelfReference) element;
                    SelfReference attr = new SelfReference(selfReference.getName());
                    targetView.addElement(element);
                    selfReference.setShadow(attr);
                } else if (element instanceof ViewComplexAttribute) {
                    ViewComplexAttribute complexAttribute = (ViewComplexAttribute) element;
                    ViewComplexAttribute attr = new ViewComplexAttribute(element.getName(), anchor, complexAttribute.getBinding());
                    targetView.addElement(element);
                    complexAttribute.setShadow(attr);
                } else if (element instanceof ViewReference) {
                    ViewReference viewReference = (ViewReference) element;
                    ViewReference rf = targetView.ensureReference(viewReference.getBinding(), anchor, null);
                    viewReference.setShadow(rf);
                } else if (element instanceof ViewAttribute) {
                    ViewAttribute viewAttribute = (ViewAttribute) element;
                    if (viewAttribute.getIsIntuitive()) //直观属性.
                    {
                        ViewAttribute attr = targetView.ensureIntuitive(viewAttribute, anchor, null); //确保定义直观属性。
                        viewAttribute.setShadow(attr);
                    } else //非直观属性
                    {
                        List<ViewAttributeSource> sources = Arrays.stream(viewAttribute.getSources()).filter(item -> item.getExtensionNode() == subTree.getNode())
                                .collect(Collectors.toList());
                        for (ViewAttributeSource item : sources) {
                            ViewAttribute attr = targetView.ensureIntuitive(item.getAttributeNode(), anchor, null); //确保定义直观属性。
                            item.setAgent(attr);
                        }
                    }
                }
            }

            outParentState.realValue = new Object[2];
            ((Object[]) outParentState.realValue)[0] = anchor;
            ((Object[]) outParentState.realValue)[1] = targetView;
            outPreVisitState.realValue = null;

            if (Arrays.asList(this.decomposedView.getFlatteningPoints()).contains(subTree.getNode()))
                targetView.addFlatteningPoint(anchor, true); //添加平展点。

            return true;
        }

        /**
         * 后置访问，即在访问子级后执行操作
         *
         * @param subTree       被访问的关联树子树
         * @param parentState   访问父级时产生的状态数据
         * @param preVisitState 前置访问产生的状态数据
         */
        @Override
        public void postVisit(AssociationTree subTree, Object parentState, Object preVisitState) {
            //Nothing to do
        }

        /**
         * 重置访问者
         */
        @Override
        public void reset() {
            //Nothing to do
        }
    }
}
