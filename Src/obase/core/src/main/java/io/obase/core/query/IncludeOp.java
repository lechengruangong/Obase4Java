/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Include运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 17:30:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.common.Tuple;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 表示Include运算
 */
public class IncludeOp extends QueryOp {

    /**
     * 包含树。
     * 包含树指示随同对象加载的引用元素。在任一子树中，根节点表示要加载的对象，其子节点指示要加载的引用。
     */
    private AssociationTree includingTree;

    /**
     * 包含表达式，用于指示包含路径
     */
    private LambdaExpression[] selectors;

    /**
     * 包含操作的目标类型
     */
    private Class<?> targetType;

    /**
     * 创建IncludeOp实例
     *
     * @param selector 包含表达式
     */
    IncludeOp(LambdaExpression selector, ObjectDataModel model) {
        super(EQueryOpName.Include, QueryOp.getParameterHostType(selector));

        this.selectors = new LambdaExpression[1];
        this.selectors[0] = selector;
        this.targetType = selector.getBody().getType();
        this.model = model;
    }

    /**
     * 创建IncludeOp实例
     *
     * @param includingPath 包含路径
     * @param sourceType    源类型
     * @param model         对象数据模型
     */
    IncludeOp(String includingPath, Class<?> sourceType, ObjectDataModel model) {
        super(EQueryOpName.Include, sourceType);
        //推进类型 获取返回类型
        String[] subPaths = includingPath.split("\\.");
        this.model = model;

        this.generateIncludingTreeByPath(subPaths, sourceType);
    }

    /**
     * 创建IncludeOp实例
     *
     * @param includingTree 包含树
     */
    public IncludeOp(AssociationTree includingTree, ObjectDataModel model) {
        super(EQueryOpName.Include, includingTree.getRepresentedType().getClrType());

        this.includingTree = includingTree;
        this.model = model;
    }

    /**
     * 创建IncludeOp实例
     *
     * @param includingTree 包含树
     * @param nextOp        查询链中的下一个运算
     * @return IncludeOp实例
     */
    public static IncludeOp create(AssociationTree includingTree, ObjectDataModel model, QueryOp nextOp) {
        ParameterExpression sourcePar = Expression.parameter("", includingTree.getRoot().getRepresentedType().getClrType());
        AssociationExpressionGenerator generator = new AssociationExpressionGenerator(sourcePar, null);
        includingTree.accept(generator);
        return (IncludeOp) QueryOp.include(generator.getResult(), model, nextOp);
    }

    /**
     * 获取包含树
     *
     * @return 包含树
     */
    public AssociationTree getIncludingTree() {
        if (this.selectors != null && Arrays.stream(this.selectors).findFirst().isPresent()) {
            this.includingTree = Arrays.stream(this.selectors).findFirst().get().getBody().onlyExtractAssociation(this.model, null);
        }
        return this.includingTree;
    }

    /**
     * 获取包含表达式
     *
     * @return 包含表达式
     */
    public LambdaExpression[] getSelectors() {
        if (this.selectors != null) return this.selectors;
        List<LambdaExpression> exps = new ArrayList<>();
        AssociationLeafNodeCollector collector = new AssociationLeafNodeCollector();
        this.includingTree.accept(collector);
        for (AssociationTreeNode item : collector.getResult()) {
            AssociationExpressionGenerator generator = new AssociationExpressionGenerator(Expression.parameter("", this.getSourceType()), null);
            item.asTree().accept(generator);
            exps.add(generator.getResult());
        }

        return this.selectors = exps.toArray(new LambdaExpression[0]);
    }

    /**
     * 获取目标类型
     *
     * @return 目标类型
     */
    public Class<?> getTargetType() {
        return this.targetType;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getSourceType();
    }

    /**
     * 获取一个值，该值指示查询运算是否为异构的
     *
     * @return 指示查询运算是否为异构的
     */
    @Override
    public final Boolean getHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        if (this.includingTree != null) {
            AssociationTreeHeterogeneityPredicater predicater = new AssociationTreeHeterogeneityPredicater(predicationProvider);
            this.includingTree.accept(predicater);
            return predicater.getResult();
        }

        return super.getHeterogeneous(predicationProvider);
    }

    /**
     * 从查询运算中提取隐含包含
     *
     * @return 提取隐含包含
     */
    @Override
    protected final AssociationTree takeImpliedIncluding() {
        return null;
    }

    /**
     * 根据包含路径构造包含树
     *
     * @param subPaths   包含路径切分后的集合
     * @param sourceType 源类型
     */
    private void generateIncludingTreeByPath(String[] subPaths, Class<?> sourceType) {
        //获取引用类型
        ReferringType referringType = this.model.getReferringType(sourceType);
        //构造初始关联树
        this.includingTree = new AssociationTree(referringType);

        AssociationTree lastNode = this.includingTree;

        //返回类型
        Class<?> returnType = null;
        //当前类型 从sourceType开始
        Class<?> currentType = sourceType;

        //前一个类型
        Class<?> preType = sourceType;
        //前一个路径
        String prePath = "";

        for (String subPath : subPaths) {
            try {
                List<Property> properties = ObaseIntrospector.getObaseBeanProperties(currentType);
                Property property = properties.stream().filter(p -> p.getName().equalsIgnoreCase(subPath)).findFirst().orElse(null);
                if (property == null)
                    throw new IllegalArgumentException("无法从" + currentType.getName() + "中获取属性" + subPath + ",请检查Include的参数.");

                //增加一个是否处理的
                boolean isProcessed = false;

                //在模型内查找
                TypeBase structuralType = this.model.getTypeOrNull(currentType);

                //找不到 可能是元组
                //如果是元组
                if (structuralType == null && Utils.isTuple(property.getGetterMethod().getDeclaringClass())) {
                    StructuralType preStructuralType = (StructuralType) this.model.getTypeOrNull(preType);
                    TypeElement refElement = null;
                    if (preStructuralType != null && preStructuralType.getElements() != null) {
                        String finalPrePath = prePath;
                        refElement = preStructuralType.getElements().stream().filter(p -> p.getName().equalsIgnoreCase(finalPrePath)).findFirst().orElse(null);
                    }

                    //为关联引用
                    if (refElement instanceof AssociationReference) {
                        AssociationReference reference = (AssociationReference) refElement;
                        AssociationEnd currentEnd = reference.getAssociationType().getAssociationEnds().stream().filter(p -> p.getEntityType().getClrType().equals(property.getPropertyType())).findFirst().orElse(null);

                        if (currentEnd == null) {
                            String finalPrePath = prePath;
                            Property finalProperty1 = ObaseIntrospector.getObaseBeanProperties(preStructuralType.getClrType()).stream().filter(p -> p.getName().equalsIgnoreCase(finalPrePath)).findFirst().orElse(null);
                            if (finalProperty1 != null) {
                                Class<?>[] types = finalProperty1.getPropertyElementType();
                                //如果此处是元组 在此处取出元组的类型参数
                                if (Tuple.class.isAssignableFrom(types[0])) {
                                    types = Utils.getTupleGenericTypeArguments(finalProperty1.getField());
                                }
                                Class<?> type = types[Integer.parseInt(property.getName().substring(property.getName().length() - 1)) - 1];
                                currentEnd = reference.getAssociationType().getAssociationEnds().stream().filter(p -> p.getEntityType().getClrType().equals(type)).findFirst().orElse(null);
                            }
                        }

                        //获取关联树子树
                        if (currentEnd != null) {
                            AssociationTree sub = lastNode.getSubTree(currentEnd.getName());
                            if (sub != null) {
                                if (currentEnd.getNavigationUse() == ENavigationUse.DirectlyReference) {
                                    ObjectNavigation navi = currentEnd.getNavigation();
                                    AssociationTree refTree = sub.getSubTree(navi.getTargetEndName());
                                    if (refTree != null)
                                        lastNode = refTree;
                                } else {
                                    lastNode = sub;
                                }
                            } else {
                                //构造树
                                sub = new AssociationTree(currentEnd.getReferenceType(), currentEnd.getName());
                                lastNode.addSubTree(sub, null);

                                if (currentEnd.getNavigationUse() != ENavigationUse.DirectlyReference) {
                                    lastNode = sub;

                                } else {
                                    lastNode = sub;
                                    //根据导航构造树
                                    ObjectNavigation navi = currentEnd.getNavigation();
                                    sub = new AssociationTree(navi.getTargetEnd().getEntityType(),
                                            navi.getTargetEndName());
                                    lastNode.addSubTree(sub, null);
                                    lastNode = sub;
                                }
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("包含路径错误,找不到为" + prePath + "的引用元素.");
                    }

                    //处理过了
                    isProcessed = true;
                }

                if (structuralType instanceof StructuralType) {
                    StructuralType structural = (StructuralType) structuralType;
                    //查找Name
                    TypeElement element = structural.getElements().stream().filter(p -> p.getName().equalsIgnoreCase(subPath)).findFirst().orElse(null);
                    //为关联引用或关联端
                    if (element instanceof ReferenceElement) {
                        ReferenceElement referenceElement = (ReferenceElement) element;
                        //获取关联树子树
                        AssociationTree sub = lastNode.getSubTree(subPath);
                        if (sub != null) {
                            if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                                ObjectNavigation navi = referenceElement.getNavigation();
                                AssociationTree refTree = sub.getSubTree(navi.getTargetEndName());
                                if (refTree != null)
                                    lastNode = refTree;
                            } else {
                                lastNode = sub;
                            }
                        } else {
                            //构造树
                            sub = new AssociationTree(referenceElement.getReferenceType(), subPath);
                            lastNode.addSubTree(sub, null);

                            //如果当前为显式关联型 且表达式不完整 仅指向对端
                            if (referenceElement instanceof AssociationReference) {
                                AssociationReference associationReference = (AssociationReference) referenceElement;
                                if (associationReference.getAssociationType().getVisible()) {
                                    ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
                                    Utils.getIsMultiple(property, type);
                                    Class<?> propType = type.realValue;

                                    if (currentType != associationReference.getAssociationType().getClrType()) {
                                        AssociationEnd end = associationReference.getAssociationType().getAssociationEnds().stream().filter(p ->
                                                p.getEntityType().getClrType() == propType).findFirst().orElse(null);

                                        if (end != null) {
                                            //下移 补一层
                                            lastNode = sub;
                                            sub = new AssociationTree(end.getReferenceType(), end.getName());
                                            lastNode.addSubTree(sub, null);
                                        }
                                    }
                                }
                            }
                            if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                                lastNode = sub;
                                //根据导航构造树
                                ObjectNavigation navi = referenceElement.getNavigation();
                                sub = new AssociationTree(navi.getTargetEnd().getEntityType(),
                                        navi.getTargetEndName());
                                lastNode.addSubTree(sub, null);
                                lastNode = sub;
                            } else {
                                lastNode = sub;
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("包含路径错误,找不到为" + subPath + "的引用元素.");
                    }

                    //处理过了
                    isProcessed = true;
                }

                if (!isProcessed)
                    throw new IllegalArgumentException("包含路径错误," + currentType + "不是已注册的Obase类型.");

                ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
                Utils.getIsMultiple(property, type);

                //记录前一个类型
                preType = currentType;
                //记录前一个类型
                prePath = subPath;

                currentType = type.realValue;

                //推进下一个类型
                returnType = currentType;

            } catch (Exception e) {
                if (e instanceof IllegalArgumentException)
                    throw e;
                throw new IllegalArgumentException("无法从" + currentType.getName() + "中获取属性" + subPath + ",请检查Include的参数.");
            }
        }

        this.targetType = returnType;
    }
}
