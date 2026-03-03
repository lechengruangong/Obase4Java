/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：包含运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 16:41:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Objects;

/**
 * 包含运算执行器
 */
public class IncludeExecutor extends RopExecutor {

    /**
     * 指示包含目标的表达式
     */
    private final LambdaExpression expression;

    /**
     * 指示包含路径的起点
     */
    private final Class<?> includeFromType;

    /**
     * 指示包含目标的路径字符串
     */
    private final String includePath;

    /**
     * 构造IncludeExecutor的新实例
     *
     * @param queryOp         查询运算
     * @param expression      指示包含目标的表达式
     * @param includePath     指示包含目标的路径字符串
     * @param includeFromType 包含起始类型
     * @param next            运算管道中的下一个执行器
     */
    public IncludeExecutor(QueryOp queryOp, LambdaExpression expression, String includePath, Class<?> includeFromType,
                           OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.expression = expression;
        this.includePath = includePath;
        this.includeFromType = includeFromType;
    }

    /**
     * 分析表示包含目标的表达式，执行必要的强制包含操作
     * 如果包含了一个指向显式关联的关联引用，检查该关联的各端（该关联引用的左端除外）是否启用了延迟加载，如果未启用则强制包含该端。
     *
     * @param expression 表示包含目标的表达式
     * @param context    关系运算上下文
     * @param lastNode   返回上述表达式指向的包含树节点
     */
    private void executeForciblyIncluding(MemberExpression expression, RopContext context,
                                          ObjectReferencePack<AssociationTree> lastNode) {
        //取包含对象
        Expression hostObj = expression.getExpression();

        ObjectReferencePack<AssociationTree> previousNode = new ObjectReferencePack<>();
        //向下寻找包含
        if (hostObj instanceof MemberExpression) {
            MemberExpression hostMember = (MemberExpression) hostObj;
            this.executeForciblyIncluding(hostMember, context, previousNode);
        } else {
            previousNode.realValue = context.getIncluding();
        }

        //取字段
        Member member = expression.getMemberMethod();

        ObjectType previousType = (ObjectType) previousNode.realValue.getRepresentedType();
        ReferenceElement reference = null;
        if (previousType != null) {
            reference = previousType.getReferenceElement(member.getName());
        }

        //是否在子树内
        if (reference != null) {
            lastNode.realValue = previousNode.realValue.getSubTree(member.getName());
        } else {
            lastNode.realValue = null;
            return;
        }

        //隐式关联型 关联树生长至对端
        if (reference instanceof AssociationReference) {
            AssociationReference associationRef = (AssociationReference) reference;
            if (!associationRef.getAssociationType().getVisible()) {
                lastNode.realValue = lastNode.realValue.getSubTree(associationRef.getRightEnd());
            }
        }

        if (lastNode.realValue.getRepresentedType() instanceof ObjectType) {
            ObjectType lastType = (ObjectType) lastNode.realValue.getRepresentedType();
            if (lastType instanceof AssociationType) {
                AssociationType associationTypeLast = (AssociationType) lastType;
                if (associationTypeLast.getVisible()) {
                    for (AssociationEnd end : associationTypeLast.getAssociationEnds()) {
                        //关联树生长将不是本端 并且不进行延迟加载的端生长至树内
                        String endName = end.getName();
                        if (reference instanceof AssociationReference) {
                            AssociationReference associanRef = (AssociationReference) reference;
                            if (!end.getEnableLazyLoading() && !Objects.equals(endName, associanRef.getLeftEnd())) {
                                AssociationTree endTree = new AssociationTree(end.getEntityType(), endName);
                                lastNode.realValue.addSubTree(endTree, endName);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 分析表示包含目标的表达式，执行必要的强制包含操作。
     * 如果包含了一个指向显式关联的关联引用，检查该关联的各端（该关联引用的左端除外）是否启用了延迟加载，如果未启用则强制包含该端。
     *
     * @param path    包含路径
     * @param context 关系运算上下文
     */
    private void executeForciblyIncluding(String path, RopContext context) {
        //拆解包含路径
        String[] currentPath = path.split("[.]");

        //从根节点找起
        AssociationTree currentNode = context.getIncluding();

        for (String node : currentPath) {
            if (currentNode.getRepresentedType() instanceof ObjectType) {
                ObjectType previousType = (ObjectType) currentNode.getRepresentedType();
                ReferenceElement reference = previousType.getReferenceElement(node);
                //是否在子树内
                if (reference != null)
                    currentNode = currentNode.getSubTree(node);
                else
                    break;

                if (reference instanceof AssociationReference) {
                    AssociationReference associanRef = (AssociationReference) reference;
                    if (!associanRef.getAssociationType().getVisible()) {
                        currentNode = currentNode.getSubTree(associanRef.getRightEnd());
                    }
                }

                if (currentNode.getRepresentedType() instanceof ObjectType) {
                    ObjectType lastType = (ObjectType) currentNode.getRepresentedType();
                    if (lastType instanceof AssociationType) {
                        AssociationType associanTypeLast = (AssociationType) lastType;
                        if (associanTypeLast.getVisible()) {
                            for (AssociationEnd end : associanTypeLast.getAssociationEnds()) {
                                //关联树生长将不是本端 并且不进行延迟加载的端生长至树内
                                String endName = end.getName();
                                if (reference instanceof AssociationReference) {
                                    AssociationReference associanRef = (AssociationReference) reference;
                                    if (!end.getEnableLazyLoading() && !Objects.equals(endName, associanRef.getLeftEnd())) {
                                        AssociationTree endTree = new AssociationTree(end.getEntityType(), endName);
                                        currentNode.addSubTree(endTree, endName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 仿照AssociationGrower根据字符串生成包含树
     *
     * @param assocTree 包含树
     * @param model     对象数据模型
     */
    private void growIncludingByIncludingPath(AssociationTree assocTree, ObjectDataModel model) {

        //拆解包含路径
        String[] currentPaths = this.includePath.split("[.]");
        //关联树
        AssociationTree lastAssociationNode = assocTree;
        //当前类型 从sourceType开始
        Class<?> currentType = this.includeFromType;
        //前一个类型
        Class<?> preType = this.includeFromType;
        //前一个路径
        String prePath = "";

        for (String subPath : currentPaths) {

            List<Property> properties = ObaseIntrospector.getObaseBeanProperties(currentType);
            Property property = properties.stream().filter(p -> p.getName().equalsIgnoreCase(subPath)).findFirst().orElse(null);
            if (property == null)
                throw new IllegalArgumentException("无法从" + currentType.getName() + "中获取属性" + subPath + ",请检查Include的参数.");

            //在模型内查找
            TypeBase structuralType = model.getTypeOrNull(currentType);
            //增加一个是否处理的
            boolean isProcessed = false;
            //找不到 可能是元组
            if (structuralType == null && Utils.isTuple(property.getGetterMethod().getDeclaringClass())) {
                StructuralType preStructuralType = (StructuralType) model.getTypeOrNull(preType);
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
                            Class<?> type = finalProperty1.getPropertyElementType()[Integer.parseInt(property.getName().substring(property.getName().length() - 1)) - 1];
                            currentEnd = reference.getAssociationType().getAssociationEnds().stream().filter(p -> p.getEntityType().getClrType().equals(type)).findFirst().orElse(null);
                        }
                    }

                    //获取关联树子树
                    if (currentEnd != null) {
                        AssociationTree sub = lastAssociationNode.getSubTree(currentEnd.getName());
                        if (sub != null) {
                            if (currentEnd.getNavigationUse() == ENavigationUse.DirectlyReference) {
                                AssociationTree refTree = sub.getSubTree(currentEnd.getNavigation().getTargetEndName());
                                if (refTree != null)
                                    lastAssociationNode = refTree;
                            } else {
                                lastAssociationNode = sub;
                            }
                        } else {
                            //构造树
                            sub = new AssociationTree(currentEnd.getReferenceType(), currentEnd.getName());
                            lastAssociationNode.addSubTree(sub);

                            if (currentEnd.getNavigationUse() == ENavigationUse.DirectlyReference) {
                                lastAssociationNode = sub;

                            } else {
                                lastAssociationNode = sub;
                                //根据导航构造树
                                sub = new AssociationTree(currentEnd.getNavigation().getTargetEnd().getEntityType(),
                                        currentEnd.getNavigation().getTargetEndName());
                                lastAssociationNode.addSubTree(sub, null);
                                lastAssociationNode = sub;
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("包含路径错误,找不到为" + prePath + "的引用元素.");
                }
                //处理过了
                isProcessed = true;
            }

            //找得到 直接处理
            if (structuralType instanceof StructuralType) {
                StructuralType structural = (StructuralType) structuralType;
                //查找Name
                TypeElement element = structural.getElements().stream().filter(p -> p.getName().equalsIgnoreCase(subPath)).findFirst().orElse(null);
                //为关联引用或关联端
                if (element instanceof ReferenceElement) {
                    ReferenceElement referenceElement = (ReferenceElement) element;
                    //获取关联树子树
                    AssociationTree sub = lastAssociationNode.getSubTree(subPath);
                    if (sub != null) {
                        if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                            AssociationTree refTree = sub.getSubTree(referenceElement.getNavigation().getTargetEndName());
                            if (refTree != null)
                                lastAssociationNode = refTree;
                        } else {
                            lastAssociationNode = sub;
                        }
                    } else {
                        //构造树
                        sub = new AssociationTree(referenceElement.getReferenceType(), subPath);
                        lastAssociationNode.addSubTree(sub, null);

                        //如果当前为显式关联型 且表达式不完整 仅指向对端
                        if (referenceElement instanceof AssociationReference) {
                            AssociationReference associationReference = (AssociationReference) referenceElement;
                            if (associationReference.getAssociationType().getVisible()) {
                                ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
                                Utils.getIsMultiple(property, type);
                                Class<?> propType = type.realValue;

                                if (currentType != associationReference.getAssociationType().getClrType()) {
                                    AssociationEnd end = associationReference.getAssociationType().getAssociationEnds().stream().filter(p ->
                                            p.getEntityType().getClrType().equals(propType)).findFirst().orElse(null);

                                    if (end != null) {
                                        //下移 补一层
                                        lastAssociationNode = sub;
                                        sub = new AssociationTree(end.getReferenceType(), end.getName());
                                        lastAssociationNode.addSubTree(sub, null);
                                    }
                                }
                            }
                        }
                        if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                            lastAssociationNode = sub;
                            //根据导航构造树
                            ObjectNavigation navi = referenceElement.getNavigation();
                            sub = new AssociationTree(navi.getTargetEnd().getEntityType(),
                                    referenceElement.getNavigation().getTargetEndName());
                            lastAssociationNode.addSubTree(sub, null);
                            lastAssociationNode = sub;
                        } else {
                            lastAssociationNode = sub;
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

            //记录前一个类型
            preType = currentType;
            //记录前一个类型
            prePath = subPath;

            //是否集合属性
            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
            Utils.getIsMultiple(property, type);
            //推进下一个类型
            currentType = type.realValue;
        }
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        //抽取对应的关联树
        if (this.expression != null) {
            List<MemberExpression> members =
                    new MemberExpressionExtractor(new SubTreeEvaluator(this.expression)).extractMember(this.expression);

            for (MemberExpression member : members) {
                member.onlyGrowAssociationTree(ropContext.getIncluding(), ropContext.getModel(), null, null);
            }
            this.executeForciblyIncluding((MemberExpression) this.expression.getBody(), ropContext, new ObjectReferencePack<>());
        } else {
            this.growIncludingByIncludingPath(ropContext.getIncluding(), ropContext.getModel());
            this.executeForciblyIncluding(this.includePath, ropContext);
        }

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
