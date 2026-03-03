/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：主引类型,可以引用其它对象的类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:28:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.expression.*;
import io.obase.core.odm.objectSys.*;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.query.QueryOp;
import io.obase.core.query.WhereOp;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表示可以引用其它对象的类型，简称为主引类型。
 */
public abstract class ReferringType extends StructuralType {

    /**
     * 根据指定的CLR类型创建引用实例
     *
     * @param clrType      运行时类型
     * @param derivingFrom 基类
     */
    protected ReferringType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType, derivingFrom);
    }

    /**
     * 根据指定的CLR类型创建引用实例
     *
     * @param clrType 运行时类型
     */
    protected ReferringType(Class<?> clrType) {
        super(clrType, null);
    }

    /**
     * 创建引用实例，该实例还没有关联的对象系统类型，有待后续指定。
     */
    protected ReferringType() {
        super();
    }

    /**
     * 获取类型包含的所有引用元素
     *
     * @return 类型包含的所有引用元素
     */
    public ReferenceElement[] getReferenceElements() {
        List<ReferenceElement> referenceElements = new ArrayList<>();

        for (TypeElement ele : this.getElements()) {
            if (ele instanceof ReferenceElement) {
                referenceElements.add((ReferenceElement) ele);
            }
        }

        return referenceElements.toArray(new ReferenceElement[0]);
    }

    /**
     * 添加引用元素
     *
     * @param element 引用元素
     */
    public void addReferenceElement(ReferenceElement element) {
        this.addElement(element);
    }

    /**
     * 根据名称查询引用类型
     *
     * @param name 元素名称
     * @return 引用类型
     */
    public ReferenceElement getReferenceElement(String name) {
        return (ReferenceElement) this.getElement(name);
    }

    /**
     * 实例化主引类型，并初始化实例属性
     *
     * @param attrValueGetter 一个委托，用于为指定属性树节点所代表的简单属性取值
     * @param refArgGetter    一个委托，用于为绑定到引用元素的构造参数取值
     * @return 实例化主引类型
     */
    public Object instantiateWithRefArgs(FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter,
                                         FunctionWithOneArg<Parameter, Object> refArgGetter) {

        //构造取参器
        AttributeValueGetterBasedArgumentGetter argValueGetter = new AttributeValueGetterBasedArgumentGetter(attrValueGetter);

        //包装为局部方法
        //等价于Func<Parameter, object> argGetter = p => p.ElementType == eElementType.Attribute ? argValueGetter.Get(p) : refArgGetter(p);
        FunctionWithOneArg<Parameter, Object> ArgGetter = parameter -> {
            try {
                return parameter.getElementType() == EElementType.Attribute ? argValueGetter.get(parameter) : refArgGetter.invoke(parameter);
            } catch (Exception e) {
                throw new IllegalArgumentException("获取参数值错误" + e.getMessage(), e);
            }
        };

        //返回基类方法值
        return this.instantiateWithParameter(ArgGetter);
    }

    /**
     * 实例化主引类型，并初始化实例属性和引用元素。
     *
     * @param attrValueGetter 一个委托，用于为指定属性树节点所代表的简单属性取值
     * @param refValueGetter  一个委托，用于为引用元素取值
     * @return 实例化主引类型
     */
    public Object instantiateWithRefGetter(FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter,
                                           FunctionWithOneArg<ReferenceElement, Object> refValueGetter, Function<ReferenceElement, Boolean> hasInclude) {

        //包装成局部方法
        FunctionWithOneArg<Parameter, Object> RefArgGetter = parameter -> {
            TypeElement ele = parameter.getElement();
            if (ele instanceof Attribute) {
                Attribute attribute = (Attribute) ele;
                return attrValueGetter.invoke(new SimpleAttributeNode(attribute));
            } else
                return refValueGetter.invoke((ReferenceElement) ele);
        };

        //构造结构类型
        Object resultObj = this.instantiateWithSimpleAttributeNode(RefArgGetter, attrValueGetter);
        //处理引用元素
        ReferenceElement[] referenceElements = this.getReferenceElements();
        if (referenceElements != null && referenceElements.length > 0) {
            for (ReferenceElement referenceElement : referenceElements) {
                //从构造器获取参数
                Parameter para = this.constructor.getParameterByElement(referenceElement.getName());
                //如果已经通过构造函数赋值，则不需要再赋值。
                if (para != null) continue;
                Object values = refValueGetter.invoke(referenceElement);
                if (values == null) continue;
                if (referenceElement.getIsMultiple()) {
                    if (values instanceof Iterable) {
                        Iterable<?> vars = (Iterable<?>) values;
                        if (vars.iterator().hasNext())
                            referenceElement.setValue(resultObj, (Iterable<Object>) values);
                    } else if (values instanceof Object[]) {
                        Object[] vars = (Object[]) values;
                        if (vars.length > 0) {
                            List<Object> list = new ArrayList<>(Arrays.asList(vars));
                            referenceElement.setValue(resultObj, list);
                        } else {
                            if (hasInclude.apply(referenceElement)) {
                                referenceElement.setValue(resultObj, new ArrayList<>());
                            }
                        }
                    }
                } else {
                    if (values instanceof Iterable) {
                        Iterable<Object> vars = (Iterable<Object>) values;
                        for (Object val : vars) {
                            referenceElement.setValue(resultObj, val);
                        }
                    } else if (values instanceof Object[]) {
                        Object[] vars = (Object[]) values;
                        for (Object val : vars) {
                            referenceElement.setValue(resultObj, val);
                        }
                    }

                }
            }
        }

        //返回s
        return resultObj;
    }

    /**
     * 实例化主引类型，并初始化实例属性
     *
     * @param attrValueGetter 属性取值委托，该属性须为类型的直接属性
     * @param refArgGetter    一个委托，用于为绑定到引用元素的构造参数取值
     * @return 初始化后的实例
     */
    public Object instantiateWithRefArgGetter(FunctionWithOneArg<Attribute, Object> attrValueGetter, FunctionWithOneArg<Parameter, Object> refArgGetter) {
        //等同于Func<Parameter, object> argGetter = parameter =>{ XXX }
        FunctionWithOneArg<Parameter, Object> ArgGetter = parameter -> {
            if (parameter.getElementType() == EElementType.Attribute) {
                //类型为属性 强转
                Attribute attr = (Attribute) parameter.getElement();
                return attrValueGetter.invoke(attr);
            }

            return refArgGetter.invoke(parameter);
        };

        return this.instantiate(ArgGetter, attrValueGetter);
    }

    /**
     * 实例化主引类型，并初始化实例属性和引用元素。
     *
     * @param elementValueGetter 元素取值委托，该元素须为类型的直接元素
     * @return 实例化产生的对象
     */
    public Object instantiateWithElementValueGetter(FunctionWithOneArg<TypeElement, Object> elementValueGetter) {

        FunctionWithOneArg<Attribute, Object> attrValueGetter = elementValueGetter::invoke;
        //等同于Func<Parameter, object> refArgGetter = parameter => elementValueGetter(parameter.GetElement());
        FunctionWithOneArg<Parameter, Object> refArgGetter = parameter -> elementValueGetter.invoke(parameter.getElement());

        return this.instantiateWithRefArgGetter(attrValueGetter, refArgGetter);
    }

    /**
     * 获取类型的筛选键。
     * 对于类型的某一个属性或属性序列，如果其值或值序列可以作为该类型实例的标识，该属性或属性序列即可作为该类型的筛选键。
     * 对于实体型，可以用主键作为筛选键。对于关联型，可以用其在各关联端上的外键属性组合成的属性序列作为筛选键。
     *
     * @return 构成筛选键的属性序列
     */
    public abstract Attribute[] getFilterKey();

    /**
     * 获取指定实例的标识
     * 作为标识的IdentityArray实例。
     * 实施说明
     * 首先获取当前类型的筛选键，然后顺序获取各筛选键属性的值，组合成标识
     *
     * @param targetObj 对象
     * @return 标识
     */
    public IdentityArray getIdentity(Object targetObj) {
        //获取属性
        Attribute[] attrs = this.getFilterKey();
        //获取每一个属性值
        List<Object> listIdentity = Arrays.stream(attrs).map(attribute -> {
            try {
                return attribute.getValue(targetObj);
            } catch (Exception e) {
                throw new IllegalArgumentException("无法获取属性值" + e.getMessage(), e);
            }
        }).collect(Collectors.toList());
        //组合成标识
        IdentityArray result = new IdentityArray();
        result.addAll(listIdentity);

        return result;
    }

    /**
     * 生成当前类型的筛选查询。
     * 筛选查询用于从类型实例的集合中筛选出指定实例。
     *
     * @param objects 要从筛选源中筛选出来的实例
     * @param nextOp  查询链中的下一节点
     * @return 筛选查询
     */
    public WhereOp generateFilterQuery(Object[] objects, QueryOp nextOp) {

        //形参绑定
        ParameterExpression parameter = Expression.parameter("", this.getRebuildingType());
        //过滤键属性
        Attribute[] keyAttrs = this.getFilterKey();
        //所有对象一起
        Expression bodyExpression = null;

        for (Object obj : objects) {
            for (Attribute keyAttr : keyAttrs) {
                //左边 成员表达式
                MemberExpression memberExp;
                try {
                    memberExp = Expression.member(parameter, this.getClrType().getMethod("get" + keyAttr.getName()), parameter, parameter.getType());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("无法获取" + this.clrType.getName() + "的键属性" + keyAttr.getName(), e);
                }
                //右边 静态变量表达式
                Object attrValue = keyAttr.getValue(obj);
                ConstantExpression valueExp = Expression.constant(attrValue);
                //组合一下
                BinaryExpression segment = Expression.equal(memberExp, valueExp, null);
                //组合至单个对象 所有属性一起
                bodyExpression = bodyExpression == null
                        ? segment
                        : Expression.and(null, segment, null);
            }
        }

        if (bodyExpression == null) {
            bodyExpression = Expression.constant(true);
        }

        LambdaExpression predicate = Expression.lambda(null, bodyExpression);
        return (WhereOp) QueryOp.where(predicate, this.getModel(), nextOp);
    }

    /**
     * 为当前类型或建立在当前类型上的视图的指定实例集编写字典，该字典以实例的标识为键，以实例本身为值
     *
     * @param objects  作为筛选源的实例集
     * @param typeView 如果筛选源是视图实例，指定视图类型
     * @return 实例集字典
     */
    public Map<IdentityArray, Object> makeDictionary(Object[] objects, TypeView typeView) {
        //返回值
        Map<IdentityArray, Object> result = new HashMap<>();

        if (typeView == null) {
            for (Object obj : objects)
                //直接获取标识
                result.put(this.getIdentity(obj), obj);
        } else {

            //参与构造过滤的属性
            //过滤键
            Attribute[] filterKeys = this.getFilterKey();
            List<Attribute> filterAttrs = new ArrayList<>(Arrays.asList(filterKeys));
            //分解视图
            Stack<TypeView> stack = typeView.getNestingStack();

            while (stack.size() > 0) {
                TypeView currentView = stack.pop();
                //每个再根据过滤键获取一次直观属性
                for (Attribute filterKey : filterKeys) {
                    ViewAttribute attribute = currentView.getIntuitiveAttribute(filterKey, null);
                    filterAttrs.add(attribute);
                }
            }
            //每个对象 按照过滤属性挨个取一边
            for (Object obj : objects) {
                IdentityArray identity = new IdentityArray();
                for (Attribute filterAttr : filterAttrs) {
                    Object filterValue = filterAttr.getValue(obj);
                    identity.add(filterValue);
                }

                //添加标识
                result.put(identity, obj);
            }
        }

        return result;
    }

    /**
     * 为当前类型或建立在当前类型上的视图的指定实例集编写字典，该字典以实例的标识为键，以实例本身为值
     *
     * @param objects 作为筛选源的实例集
     * @return 实例集字典
     */
    public Map<IdentityArray, Object> makeDictionary(Object[] objects) {
        return this.makeDictionary(objects, null);
    }

    /**
     * 根据快照重建对象
     *
     * @param snapshot 对象快照
     * @param attach   附加委托
     * @param asRoot   是否作为根对象
     * @return 重建的对象
     */
    @Override
    public Object rebuild(ObjectSnapshot snapshot, IAttachObject attach, boolean asRoot) {
        //实施说明

        //首先调用ObjectSnapshot.GenerateTree方法生成关联树，附带生成用于重建对象系统的数据集。
        //然后实例化ObjectSystemBuilder，将其作为访问者访问上述关联树，生成对象。

        ObjectReferencePack<IObjectDataSet> dataSet = new ObjectReferencePack<>();
        AssociationTree tree = snapshot.generateTree(dataSet);
        ObjectSystemBuilder builder = new ObjectSystemBuilder(dataSet.realValue, attach, true);
        tree.accept(builder);

        return builder.getResult();
    }
}
