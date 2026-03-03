/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树,关联关系生成的树形结构.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:43:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.odm.*;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 关联树,关联关系生成的树形结构
 */
public class AssociationTree {

    /**
     * 关联树的节点层级结构（根节点为当前子树的根）
     */
    private final AssociationTreeNode node;

    /**
     * 构造AssociationTree的新实例，构造的树将作为子树添加到某一节点。
     *
     * @param objectType  当前节点的对象类型
     * @param elementName 当前节点在父级类型中的元素名称
     */
    public AssociationTree(ObjectType objectType, String elementName) {
        this.node = new ObjectTypeNode(objectType, elementName);
    }

    /**
     * 创建代表指定引用元素的AssociationTree实例，构造的树将作为子树添加到某一节点
     *
     * @param reference 要创建的节点代表的引用元素
     */
    public AssociationTree(ReferenceElement reference) {
        this.node = new ObjectTypeNode(reference.getReferenceType(), null);
    }

    /**
     * 构造AssociationTree的新实例
     *
     * @param representedTyp 当前节点的对象类型
     */
    public AssociationTree(ReferringType representedTyp) {
        if (representedTyp instanceof TypeView) {
            this.node = new TypeViewNode((TypeView) representedTyp);
        } else {
            this.node = new ObjectTypeNode((ObjectType) representedTyp, null);
        }
    }

    /**
     * 使用指定的节点层级结构（根节点为树根）创建AssociationTree实例
     *
     * @param node 节点层级结构
     */
    AssociationTree(AssociationTreeNode node) {
        this.node = node;
    }

    /**
     * 将两个关联树合并。
     * 该方法不会生成新关联树，而是生长其中一个以覆盖另一个。
     *
     * @param assTree1 第一个关联树
     * @param assTree2 第二个关联树
     * @return 生长了的关联树。如果assTree1为null则返回assTree2；如果assTree2为null则返回assTree1。
     */
    public static AssociationTree combine(AssociationTree assTree1, AssociationTree assTree2) {
        //有空的情况下
        if (assTree1 == null && assTree2 == null) return null;
        if (assTree1 == null) return assTree2;
        if (assTree2 == null) return assTree1;

        return assTree1.grow(assTree2);
    }

    /**
     * 获取关联树代表的类型
     *
     * @return 获取关联树代表的类型
     */
    public ReferringType getRepresentedType() {
        return this.getNode().getRepresentedType();
    }

    /**
     * 获取当前节点在父级类型中的元素名称(即关联引用名或关联端名)
     *
     * @return 获取当前节点在父级类型中的元素名称
     */
    public String getElementName() {
        if (this.node instanceof ObjectTypeNode) {
            return ((ObjectTypeNode) this.node).getElementName();
        }
        return null;
    }

    /**
     * 获取当前节点的所有子树
     *
     * @return 获取当前节点的所有子树
     */
    public AssociationTree[] getSubTrees() {
        if (this.node.getChildren() != null) {
            return Arrays.stream(this.node.getChildren()).map(AssociationTreeNode::asTree).toArray(AssociationTree[]::new);
        }
        return new AssociationTree[0];
    }

    /**
     * 获取子树（直接子代）的数量
     *
     * @return 获取子树（直接子代）的数量
     */
    public int getSubCount() {
        if (this.node.getChildren() != null) {
            return this.node.getChildren().length;
        }
        return 0;
    }

    /**
     * 获取顶级树
     *
     * @return 获取顶级树
     */
    public AssociationTree getRoot() {
        return this.node.getRoot().asTree();
    }

    /**
     * 获取一个值，该值指示当前子树是否为顶级树。
     *
     * @return 是否为顶级树
     */
    @Deprecated
    public boolean getIsRoot() {
        return this.node instanceof TypeViewNode || this.node.getParent() == null;
    }

    /**
     * 获取当前子树所属的关联树
     *
     * @return 获取当前子树所属的关联树
     */
    public AssociationTree getParent() {
        if (this.node.getParent() != null) {
            return this.node.getParent().asTree();
        }
        return null;
    }

    /**
     * 获取关联树节点代表的类型元素
     *
     * @return 节点代表的类型元素
     */
    public ReferenceElement getElement() {
        if (this.getNode() instanceof ObjectTypeNode) {
            return ((ObjectTypeNode) this.getNode()).getElement();
        }
        return null;
    }

    /**
     * 获取关联树的节点层级结构（根节点为当前子树的根
     *
     * @return 节点
     */
    public AssociationTreeNode getNode() {
        return this.node;
    }


    /**
     * 获取指定元素名称（关联引用名或关联端名）对应的子树
     *
     * @param elementName 要获取其对应子树的元素名称
     * @return 子树
     */
    public AssociationTree getSubTree(String elementName) {
        ObjectTypeNode child = this.getNode().getChild(elementName);
        if (child != null)
            return child.asTree();
        return null;
    }

    /**
     * 为当前节点添加子树。
     * 如果已存在同名子树，不执行添加操作
     *
     * @param subTree 子树对应的元素名称
     * @return 返回刚添加的子树；如果存在同名子树，返回已存在的子树
     */
    public AssociationTree addSubTree(AssociationTree subTree) {
        return this.addSubTree(subTree, null);
    }

    /**
     * 为当前节点添加子树。
     * 如果已存在同名子树，不执行添加操作
     *
     * @param subTree     子树对应的元素名称
     * @param elementName 要添加的子树
     * @return 返回刚添加的子树；如果存在同名子树，返回已存在的子树
     */
    public AssociationTree addSubTree(AssociationTree subTree, String elementName) {
        ObjectTypeNode childNode = (ObjectTypeNode) subTree.getNode();
        if (childNode == null) throw new IllegalArgumentException("非顶级节点只能是“对象类型的节点”");
        ObjectTypeNode node = !(Utils.getStringIsEmpty(elementName)) ? this.getNode().addChild(childNode, elementName) : this.getNode().addChild(childNode, subTree.getElementName());
        return node.asTree();
    }

    /**
     * 生长关联树以使其覆盖指定的元素。
     * 如果已存在同名子树，不执行操作。
     *
     * @param elementName 子树对应的元素名称
     * @return 返回刚添加的子树；如果存在同名子树，返回已存在的子树
     */
    public AssociationTree grow(String elementName) {
        AssociationTree subTree = this.getSubTree(elementName);
        ReferenceElement reference = this.node.getRepresentedType().getReferenceElement(elementName);
        if (subTree == null) {

            if (reference == null)
                throw new IllegalArgumentException("子树对应的元素名称" + elementName + "的引用类型不存在。");
            subTree = new AssociationTree(reference);
            this.addSubTree(subTree, reference.getName());

            if (reference instanceof AssociationReference) {
                AssociationReference ar = (AssociationReference) reference;
                if (!ar.getAssociationType().getVisible()) {
                    AssociationTree sub1 = new AssociationTree(ar.getAssociationType().getAssociationEnd(ar.getRightEnd()).getEntityType(),
                            ar.getRightEnd());
                    subTree.addSubTree(sub1, ar.getRightEnd());
                }
            } else if (reference instanceof ViewReference) {
                ViewReference vr = (ViewReference) reference;
                if (vr.getBinding() instanceof AssociationReference) {
                    AssociationReference ar1 = (AssociationReference) vr.getBinding();
                    if (!ar1.getAssociationType().getVisible()) {
                        AssociationTree sub1 = new AssociationTree(ar1.getAssociationType().getAssociationEnd(ar1.getRightEnd()).getEntityType(),
                                ar1.getRightEnd());
                        subTree.addSubTree(sub1, ar1.getRightEnd());
                    }
                }
            }
        }
        return subTree;
    }

    /**
     * 生长关联树以使其覆盖指定的元素。
     * 如果已存在同名子树，不执行操作。
     *
     * @param element 子树代表的元素
     * @return 返回刚添加的子树；如果存在同名子树，返回已存在的子树。
     */
    public AssociationTree grow(ReferenceElement element) {
        return this.grow(element.getName());
    }

    /**
     * 生长关联树从而覆盖另一棵树（目标树），但目标树代表的类型是由当前树代表的类型经过一次或多次退化投影得出的。
     *
     * @param targetTree 要覆盖的目标树
     * @param atrophies  退化投影形成的退化路径序列
     */
    public void grow(AssociationTree targetTree, AssociationTreeNode[] atrophies) {
        //期望类型
        StructuralType expectedType = atrophies.length > 0 ? atrophies[atrophies.length - 1].getRepresentedType() : targetTree.getRepresentedType();

        //拆解挂关联树
        AssociationTree[] trees = this.unPackage(targetTree, expectedType);

        for (AssociationTree tree : trees) {
            AssociationTreeNode rootNode = tree.getNode();
            for (AssociationTreeNode atrophy : atrophies) {
                rootNode.addChild(atrophy.getChildren());
                targetTree.grow(tree);
            }
        }
    }

    /**
     * 对以视图为根的关联树拆包，即将以视图为根的关联树映射为以该视图的源为根的关联树。如果存在视图嵌套，则层层拆包直到所得关联树代表的类型为指定的期望类型。
     *
     * @param targetTree   要拆包的关联树
     * @param expectedType 期望类型
     * @return 拆包后的关联树
     */
    private AssociationTree[] unPackage(AssociationTree targetTree, StructuralType expectedType) {
        if (targetTree.getElement() != null && targetTree.getElement().getReferenceType() == expectedType)
            return new AssociationTree[]{targetTree};
        List<AssociationTree> result = new ArrayList<>();
        for (AssociationTree tree : targetTree.getSubTrees()) {
            AssociationTree[] unpackedResult = this.unPackage(tree, expectedType);
            result.addAll(Arrays.asList(unpackedResult));
        }

        return result.toArray(new AssociationTree[0]);
    }

    /**
     * 移除代表指定元素的子树，然后返回该子树
     *
     * @param elementName 元素
     * @return 子树
     */
    public AssociationTree removeSub(String elementName) {
        ObjectTypeNode subTreeNode = this.getNode().removeChild(elementName);

        return subTreeNode.asTree();
    }

    /**
     * 使关联树生长从而覆盖另一棵树（目标树）。
     *
     * @param other 要覆盖的另一棵树
     * @return 生长后的关联树
     */
    public AssociationTree grow(AssociationTree other) {
        AssociationTreeGrower grower = new AssociationTreeGrower();
        this.accept(grower, other);
        return other;
    }

    /**
     * 对当前关联树圈定的对象系统实施退化投影，得到新的对象系统结构。
     *
     * @param atrophyPath 退化路径
     * @return 表示新的对象系统结构的关联树
     */
    public AssociationTree select(AssociationTreeNode atrophyPath) {
        return this.searchSub(atrophyPath);
    }

    /**
     * 对当前关联树圈定的对象系统实施一般投影，得到新的对象系统结构
     *
     * @param typeView 投影结果视图
     * @return 表示新的对象系统结构的关联树
     */
    public AssociationTree select(TypeView typeView) {
        ReferenceElement[] elements = typeView.getReferenceElements();

        //裁剪包含树
        for (ReferenceElement referenceElement : elements) {
            if (referenceElement instanceof ViewReference) {
                ViewReference viewReference = (ViewReference) referenceElement;

                AssociationTree anchorTree = this.searchSub(viewReference.getAnchor());
                if (anchorTree != null) {
                    AssociationTree bindingTree = this.removeSub(viewReference.getBinding().getName());
                    if (bindingTree != null) this.addSubTree(bindingTree, viewReference.getName());
                }

                this.grow(viewReference.getName());
            }
        }

        return this;
    }

    /**
     * 在关联树中搜索以指定节点为根的子树
     *
     * @param targetNode 指定节点
     * @return 子树
     */
    public AssociationTree searchSub(AssociationTreeNode targetNode) {

        //子树搜索器
        SubTreeSearcher subSearcher = new SubTreeSearcher();
        //遍历 搜索
        return targetNode.asTree().accept(subSearcher, this);
    }

    /**
     * 在关联树中搜索指定表达式对应的子树
     *
     * @param expression 作为搜索依据的表达式。
     * @param model      对象数据模型。
     * @return 子树
     */
    public AssociationTree searchSub(Expression expression, ObjectDataModel model) {
        ObjectReferencePack<AssociationTreeNode> nodePack = new ObjectReferencePack<>();

        //提取关联树
        expression.extractAssociation(model, nodePack, null);
        //子树搜索器
        SubTreeSearcher subSearcher = new SubTreeSearcher();
        AssociationTree targetTree = nodePack.realValue.asTree();
        //遍历 搜索
        return targetTree.accept(subSearcher, this);
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor 向上访问者
     */
    public void accept(IAssociationTreeUpwardVisitor visitor) {
        visitor.reset();
        this.realUpWardAccept(visitor, null);
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor  向上访问者
     * @param argument 遍历操作参数
     * @param <TArg>   遍历操作参数类型
     */
    public <TArg> void accept(IParameterizedAssociationTreeUpwardVisitor<TArg> visitor, TArg argument) {
        visitor.reset();
        //设置参数
        visitor.setArgument(argument);
        this.realUpWardAccept(visitor, argument);
    }

    /**
     * 在向上遍历关联树过程中接受访问者。
     *
     * @param visitor   向上访问者
     * @param <TResult> 遍历结构类型
     * @return 访问结果
     */
    public <TResult> TResult accept(IAssociationTreeUpwardVisitorWithResult<TResult> visitor) {
        visitor.reset();
        this.realUpWardAccept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor   向上访问者
     * @param argument  遍历操作参数
     * @param <TArg>    遍历操作参数
     * @param <TResult> 遍历操作结果
     * @return 访问结果
     */
    public <TArg, TResult> TResult accept(IParameterizedAssociationTreeUpwardVisitorWithResult<TArg, TResult> visitor, TArg argument) {
        visitor.reset();
        //设置参数
        visitor.setArgument(argument);
        this.realUpWardAccept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor    向上访问者
     * @param childState 子级访问产生的状态数据1
     */
    private void realUpWardAccept(IAssociationTreeUpwardVisitor visitor, Object childState) {
        ObjectReferencePack<Object> outChildState = new ObjectReferencePack<>();
        ObjectReferencePack<Object> outRevisitState = new ObjectReferencePack<>();
        boolean res = visitor.preVisit(this, childState, outChildState, outRevisitState);
        if (res)
            if (this.getNode() instanceof ObjectTypeNode) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) this.getNode();
                if (objectTypeNode.getParent() != null) {
                    AssociationTree parent = objectTypeNode.getParent().asTree();
                    parent.realUpWardAccept(visitor, outChildState.realValue);
                }
            }
        visitor.postVisit(this, childState, outRevisitState.realValue);
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor 向下访问者
     */
    public void accept(IAssociationTreeDownwardVisitor visitor) {
        visitor.reset();
        this.realDownWardAccept(visitor, null);
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor  向下访问者
     * @param argument 遍历操作参数
     * @param <TArg>   参数类型
     */
    public <TArg> void accept(IParameterizedAssociationTreeDownwardVisitorWithArg<TArg> visitor, TArg argument) {
        visitor.reset();
        //设置参数
        visitor.setArgument(argument);
        this.realDownWardAccept(visitor, argument);
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor   向下访问者
     * @param <TResult> 结果
     * @return 访问结果
     */
    public <TResult> TResult accept(IAssociationTreeDownwardVisitorWithResult<TResult> visitor) {
        visitor.reset();
        this.realDownWardAccept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向下遍历关联树过程中接受访问者。
     *
     * @param visitor   向下访问者
     * @param argument  遍历操作参数
     * @param <TArg>    参数
     * @param <TResult> 结果
     * @return 访问结果
     */
    public <TArg, TResult> TResult accept(IParameterizedAssociationTreeDownwardVisitorWithArgAndResult<TArg, TResult> visitor, TArg argument) {
        visitor.reset();
        //设置参数
        visitor.setArgument(argument);
        this.realDownWardAccept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor   向下访问者
     * @param outArg    以输出参数的形式返回访问结果
     * @param <TResult> 遍历操作返回结果的类型
     * @param <TOut>    输出参数的类型
     * @return 访问结果
     */
    public <TResult, TOut> TResult accept(IAssociationTreeDownwardVisitorWithOutArg<TResult, TOut> visitor, ObjectReferencePack<TOut> outArg) {
        visitor.reset();
        this.realDownWardAccept(visitor, null);
        outArg.realValue = visitor.getOutArgument();
        return visitor.getResult();
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor   向下访问者
     * @param argument  访问者参数
     * @param outArg    以输出参数的形式返回访问结果
     * @param <TArg>    访问者参数类型
     * @param <TResult> 访问结果类型
     * @param <TOut>    以输出参数的形式返回访问结果类型
     * @return 访问结果
     */
    public <TArg, TResult, TOut> TResult accept(IParameterizedAssociationTreeDownwardVisitorWithArgOutArgAndResult<TArg, TResult, TOut> visitor, TArg argument, ObjectReferencePack<TOut> outArg) {
        visitor.reset();
        //设置参数
        visitor.setArgument(argument);
        this.realDownWardAccept(visitor, null);
        ObjectReferencePack<Object> outParentState = new ObjectReferencePack<>();
        ObjectReferencePack<Object> outRevisitState = new ObjectReferencePack<>();
        visitor.preVisit(this, null, outParentState, outRevisitState);
        this.realDownWardAccept(visitor, outParentState.realValue);
        visitor.postVisit(this, outParentState.realValue, outRevisitState.realValue);
        outArg.realValue = visitor.getOutArgument();
        return visitor.getResult();
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor     向下访问者
     * @param parentState 父级访问产生的状态数据
     */
    private void realDownWardAccept(IAssociationTreeDownwardVisitor visitor, Object parentState) {
        ObjectReferencePack<Object> outParentState = new ObjectReferencePack<>();
        ObjectReferencePack<Object> outRevisitState = new ObjectReferencePack<>();
        //前置访问
        boolean revisitResult = visitor.preVisit(this, parentState, outParentState, outRevisitState);
        if (revisitResult)
            //子树遍历
            for (AssociationTree subTree : this.getSubTrees()) {
                subTree.realDownWardAccept(visitor, outParentState.realValue);
            }
        //后置访问
        visitor.postVisit(this, parentState, outRevisitState.realValue);
    }

    /**
     * 子树搜索器
     */
    private static class SubTreeSearcher implements IParameterizedAssociationTreeUpwardVisitorWithResult<AssociationTree, AssociationTree> {
        /**
         * 作为搜索源的关联树
         */
        private AssociationTree sourceTree;

        /**
         * 获取遍历关联树的结果
         *
         * @return 遍历操作返回结果的类型
         */
        @Override
        public AssociationTree getResult() {
            return this.sourceTree;
        }

        /**
         * 前置访问，即在访问父级前执行操作
         *
         * @param subTree          被访问的子树
         * @param childState       访问子级时产生的状态数据
         * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
         * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
         * @return 是否继续访问父级
         */
        @Override
        public boolean preVisit(AssociationTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
            //Nothing To Do
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            return true;
        }

        /**
         * 后置访问，即在访问父级后执行操作
         *
         * @param subTree       被访问的子树
         * @param childState    访问子级时产生的状态数据
         * @param preVisitState 前置访问产生的状态数据
         */
        @Override
        public void postVisit(AssociationTree subTree, Object childState, Object preVisitState) {
            if (subTree == null) return;

            //如果已经是根节点
            if (subTree.getIsRoot()) {
                StructuralType sourceType = this.sourceTree.getRepresentedType();
                StructuralType treeType = subTree.getRepresentedType();
                //相等 直接返回
                if (sourceType.equals(treeType)) return;

                //否则 结果赋空
                this.sourceTree = null;
            } else {
                //找到子树 赋值子树 否则赋空
                if (this.sourceTree == null)
                    return;
                this.sourceTree = this.sourceTree.getSubTree(subTree.getElementName());
            }
        }

        /**
         * 重置访问者
         */
        @Override
        public void reset() {
            this.sourceTree = null;
        }

        /**
         * 为即将开始的遍历操作设置参数
         *
         * @param argument 参数值
         */
        @Override
        public void setArgument(AssociationTree argument) {
            this.sourceTree = argument;
        }
    }
}
