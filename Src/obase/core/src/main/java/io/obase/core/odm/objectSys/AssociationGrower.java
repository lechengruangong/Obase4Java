/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联生长器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 16:05:32
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.*;
import io.obase.core.odm.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 关联生长器
 */
public class AssociationGrower extends ExpressionVisitor {

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 待生长的关联树
     */
    private AssociationTree associationTree;

    /**
     * 从表达式抽取的属性树
     */
    private AttributeTree attributeTree;

    /**
     * 指示是否抽取属性树
     */
    private boolean extractingAttribute;

    /**
     * 关联树生长后的末节点
     */
    private AssociationTree lastAssociationNode;

    /**
     * 从表达式抽取的属性树的末节点
     */
    private AttributeTree lastAttributeNode;

    /**
     * 形参绑定
     */
    private ParameterBinding[] parameterBindings;

    /**
     * 表达式中的前一部分
     * 目前仅在判断隐式多方关联中使用
     */
    private Expression preExpression;

    /**
     * 创建AssociationGrower实例
     *
     * @param model   对象数据模型
     * @param assTree 待生长的关联树。如果未指定，生长器在解析表达式的过程中将创建一棵新树
     */
    public AssociationGrower(ObjectDataModel model, AssociationTree assTree) {
        this.model = model;
        this.associationTree = assTree;
    }

    /**
     * 获取待生长的关联树
     *
     * @return 获取待生长的关联树
     */
    public AssociationTree getAssociationTree() {
        return this.associationTree;
    }

    /**
     * 获取从表达式抽取的属性树
     *
     * @return 获取从表达式抽取的属性树
     */
    public AttributeTree getAttributeTree() {
        return this.attributeTree;
    }

    /**
     * 获取一个值，该值指示是否抽取属性树。
     *
     * @return 获取一个值，该值指示是否抽取属性树。
     */
    public boolean getExtractingAttribute() {
        return this.extractingAttribute;
    }

    /**
     * 设置一个值，该值指示是否抽取属性树
     *
     * @param extractingAttribute 设置一个值，该值指示是否抽取属性树
     */
    public void setExtractingAttribute(boolean extractingAttribute) {
        this.extractingAttribute = extractingAttribute;
    }

    /**
     * 获取关联树生长后的末节点
     *
     * @return 获取关联树生长后的末节点
     */
    public AssociationTree getLastAssociationNode() {
        return this.lastAssociationNode;
    }

    /**
     * 获取从表达式抽取的属性树的末节点
     *
     * @return 获取从表达式抽取的属性树的末节点
     */
    public AttributeTree getLastAttributeNode() {
        return this.lastAttributeNode;
    }

    /**
     * 获取形参绑定
     *
     * @return 获取形参绑定
     */
    public ParameterBinding[] getParameterBindings() {
        return this.parameterBindings;
    }

    /**
     * 设置形参绑定
     *
     * @param parameterBindings 设置形参绑定
     */
    public void setParameterBindings(ParameterBinding[] parameterBindings) {
        this.parameterBindings = parameterBindings;
    }

    /**
     * 表达式中的前一部分
     *
     * @return 表达式中的前一部分
     */
    public Expression getPreExpression() {
        return this.preExpression;
    }

    /**
     * 表达式中的前一部分
     *
     * @param preExpression 表达式中的前一部分
     */
    public void setPreExpression(Expression preExpression) {
        this.preExpression = preExpression;
    }

    /**
     * 默认的访问常量表达式
     * 直接返回自身
     *
     * @param constantExpression 常量表达式
     * @return 常量表达式自身
     */
    @Override
    protected Expression visitConstant(ConstantExpression constantExpression) {
        return constantExpression;
    }

    /**
     * 访问成员表达式
     *
     * @param memberExpression 成员表达式
     * @return 访问结果
     */
    @Override
    protected Expression visitMember(MemberExpression memberExpression) {
        //读取表达式的值
        Expression host = memberExpression.getExpression();
        String name = memberExpression.getMemberName();
        //访问内部表达式
        this.visit(host);

        //在模型内查找
        TypeBase type = this.model.getTypeOrNull(host.getType());
        if (type == null)
            type = this.model.getTypeOrNull(memberExpression.getHostType());

        if (type == null)
            throw new IllegalArgumentException("无法从表达式中抽取关联树,如果此关联引用是定义在父类上的,请使用include(String includeExpression)方法代替");

        //增加一个是否处理的
        boolean isProcessed = false;

        //查不到 直接退出
        if (type instanceof StructuralType) {
            StructuralType structural = (StructuralType) type;
            //查找Name
            if (structural.getElements() != null) {
                TypeElement element = structural.getElements().stream().filter(p -> Objects.equals(p.getName(), name)).findFirst().orElse(null);
                //如果名字是Field_XXX 是由投影构造的 需要加上下划线再次查找
                if (element == null && name.contains("Field_"))
                    element = structural.getElements().stream().filter(p -> Objects.equals(p.getName(), "_" + StringUtils.uncapitalize(name))).findFirst().orElse(null);
                if (element instanceof Attribute) {
                    Attribute attribute = (Attribute) element;
                    //是否要抽取属性树
                    if (this.extractingAttribute) {
                        //抽取 则在抽取树的末节点上增加一节
                        AttributeTree sub = new AttributeTree(attribute);
                        if (this.lastAttributeNode != null) {
                            this.lastAttributeNode.addSubTree(sub);
                        }
                        this.lastAttributeNode = sub;
                        //无抽取的树 则此节为属性树
                        if (this.attributeTree == null) this.attributeTree = sub;
                    } else {
                        throw new IllegalArgumentException("包含路径错误,找不到为" + name + "的引用元素.");
                    }
                } else if (element instanceof ReferenceElement) {
                    ReferenceElement referenceElement = (ReferenceElement) element;

                    //获取关联树子树
                    AssociationTree sub = null;
                    if (this.lastAssociationNode != null)
                        sub = this.lastAssociationNode.getSubTree(name);
                    if (sub != null) {
                        if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                            ObjectNavigation navi = referenceElement.getNavigation();
                            AssociationTree refTree = sub.getSubTree(navi.getTargetEndName());
                            if (refTree != null)
                                this.lastAssociationNode = refTree;
                        } else {
                            this.lastAssociationNode = sub;
                        }
                    } else {
                        //构造树
                        sub = new AssociationTree(referenceElement.getReferenceType(), name);
                        if (this.lastAssociationNode != null) {
                            this.lastAssociationNode.addSubTree(sub, null);
                        }

                        //如果当前为显式关联型 且表达式不完整 仅指向对端
                        if (referenceElement instanceof AssociationReference) {
                            AssociationReference associationReference = (AssociationReference) referenceElement;
                            if (associationReference.getAssociationType().getVisible()) {
                                Class<?> currentType = memberExpression.getType();
                                if (Iterable.class.isAssignableFrom(currentType)) {
                                    currentType = (Class<?>) ((ParameterizedType) memberExpression.getMemberMethod().getGenericReturnType()).getActualTypeArguments()[0];
                                }

                                if (currentType != associationReference.getAssociationType().getClrType()) {
                                    Class<?> finalCurrentType = currentType;
                                    AssociationEnd end = associationReference.getAssociationType().getAssociationEnds().stream().filter(p ->
                                            p.getEntityType().getClrType() == finalCurrentType).findFirst().orElse(null);

                                    if (end != null) {
                                        //下移 补一层
                                        this.lastAssociationNode = sub;
                                        sub = new AssociationTree(end.getReferenceType(), end.getName());
                                        if (this.lastAssociationNode != null) {
                                            this.lastAssociationNode.addSubTree(sub, null);
                                        }
                                    }
                                }
                            }
                        }

                        if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                            this.lastAssociationNode = sub;
                            //根据导航构造树
                            ObjectNavigation navi = referenceElement.getNavigation();
                            sub = new AssociationTree(navi.getTargetEnd().getEntityType(),
                                    navi.getTargetEndName());
                            if (this.lastAssociationNode != null) {
                                this.lastAssociationNode.addSubTree(sub, null);
                            }
                            this.lastAssociationNode = sub;
                        } else {
                            this.lastAssociationNode = sub;
                        }
                    }
                }
                //都不是 保底
                else {
                    throw new IllegalArgumentException("包含路径错误,找不到为" + name + "的引用元素.");
                }
                isProcessed = true;
            }

        }

        if (!isProcessed)
            throw new IllegalArgumentException("包含路径错误," + memberExpression.getType() + "不是已注册的Obase类型.");

        return memberExpression;
    }

    /**
     * 访问参数表达式
     *
     * @param parameterExpression 参数表达式
     * @return 参数表达式访问能结果
     */
    @Override
    protected Expression visitParameter(ParameterExpression parameterExpression) {
        Optional<ParameterBinding> binding = Optional.empty();
        if (this.parameterBindings != null)
            binding = Arrays.stream(this.parameterBindings).filter(p -> p.getParameter() == parameterExpression).findFirst();

        if (binding.isPresent()) {
            this.visit(binding.get().getParameter());
        } else {
            if (this.associationTree == null) {
                //获取引用类型
                ReferringType referringType = this.model.getReferringType(parameterExpression.getType());
                //关联树
                this.associationTree = new AssociationTree(referringType);
            }

            //末节点即为待
            this.lastAssociationNode = this.associationTree;
        }

        return parameterExpression;
    }

    /**
     * 访问调用表达式
     *
     * @param methodCallExpression Lambda表达式
     * @return 调用表达式访问那就诶过
     */
    @Override
    protected Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        //解析map方法 实际无用处 因为Lambda表达式无法解析此方法
        if (!methodCallExpression.getMethod().getName().equals("map") && !methodCallExpression.getMethod().getName().equals("flatMap"))
            return super.visitMethodCall(methodCallExpression);

        Expression obj, arg;
        if (methodCallExpression.getObject() == null) {
            obj = methodCallExpression.getArgument()[0];
            arg = methodCallExpression.getArgument()[1];
        } else {
            obj = methodCallExpression.getObject();
            arg = methodCallExpression.getArgument()[0];
        }

        this.visitMember((MemberExpression) obj);

        ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();
        this.lastAssociationNode = arg.growAssociationTree(this.lastAssociationNode, this.model, attrTail, this.parameterBindings).asTree();
        if (attrTail.realValue != null) {
            this.attributeTree = attrTail.realValue.asTree();
        }
        return methodCallExpression;
    }
}
