/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性树.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 11:16:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ComplexAttribute;
import io.obase.core.odm.TypeBase;
import io.obase.core.odm.typeviews.ViewAttribute;

/**
 * 属性树
 * 属性树是以一个复杂属性为根节点，以其类型的属性为子节点，层层嵌套，生成的树形结构。
 */
public class AttributeTree {

    /**
     * 属性树的节点层级结构（根节点为当前子树的根）。
     */
    private final AttributeTreeNode node;

    /**
     * 父级树
     */
    private final AttributeTree parent;

    /**
     * 创建代表指定属性的AttributeTree实例
     *
     * @param attribute 代表属性
     */
    public AttributeTree(Attribute attribute) {
        //根据是否为复杂属性构造
        if (attribute instanceof ComplexAttribute)
            this.node = new ComplexAttributeNode(attribute);
        else this.node = new SimpleAttributeNode(attribute);

        this.parent = null;
    }

    /**
     * 使用指定的节点层级结构（根节点为树根）创建AttributeTree实例
     *
     * @param treeNode 节点层级结构
     */
    AttributeTree(AttributeTreeNode treeNode) {
        this.node = treeNode;
        if (this.node.getParent() != null)
            this.parent = new AttributeTree(this.node.getParent());
        else
            this.parent = null;
    }

    /**
     * 获取属性树代表的属性
     *
     * @return 属性树代表的属性
     */
    public Attribute getAttribute() {
        return this.node.getAttribute();
    }

    /**
     * 获取代表属性的名称
     *
     * @return 代表属性的名称
     */
    public String getAttributeName() {
        if (this.node.getAttribute() instanceof ViewAttribute) {
            ViewAttribute viewAttribute = (ViewAttribute) this.node.getAttribute();
            if (viewAttribute.getShadow() != null) {
                return ((MemberExpression) viewAttribute.getBinding()).getMemberName();
            }
            return viewAttribute.getName();
        }

        return this.node.getAttribute().getName();
    }

    /**
     * 获取代表属性的模型类型
     *
     * @return 代表属性的模型类型
     */
    public TypeBase getAttributeType() {
        return this.node.getAttributeType();
    }

    /**
     * 获取属性树的节点层级结构（根节点为当前子树的根）
     *
     * @return 属性树的节点层级结构（根节点为当前子树的根）
     */
    public AttributeTreeNode getNode() {
        return this.node;
    }

    /**
     * 获取所有子树
     *
     * @return 所有子树
     */
    public AttributeTree[] getSubTrees() {
        if (this.getIsComplex()) {
            ComplexAttributeNode node = (ComplexAttributeNode) this.node;
            if (node.getChildren() != null) {
                return node.getChildren().stream().map(AttributeTreeNode::asTree).toArray(AttributeTree[]::new);
            }
            return new AttributeTree[0];
        }

        return new AttributeTree[0];
    }

    /**
     * 获取属性树的父级
     *
     * @return 属性树的父级
     */
    public AttributeTree getParent() {
        return this.parent;
    }

    /**
     * 获取一个值，该值指示属性树代表的属性是否为复杂属性。
     *
     * @return 指示属性树代表的属性是否为复杂属性。
     */
    public boolean getIsComplex() {
        return this.node.getAttribute().getIsComplex();
    }

    /**
     * 为当前节点添加子树。如果已存在同名子树，不执行添加操作。
     *
     * @param subTree 要添加的子树
     * @return 返回刚添加的子树；如果存在同名子树，返回已存在的子树
     */
    public AttributeTree addSubTree(AttributeTree subTree) {
        if (!(this.node instanceof ComplexAttributeNode))
            throw new IllegalArgumentException("非代表复杂属性的节点，不能添加子树");
        //查询同名子树
        ComplexAttributeNode complexAttribute = (ComplexAttributeNode) this.node;
        AttributeTreeNode subTreeChild = complexAttribute.getChild(subTree.getAttributeName());
        //判断同名子树是否存在
        if (subTreeChild == null) subTreeChild = complexAttribute.addChild(subTree.getNode());
        return subTreeChild.asTree();
    }

    /**
     * 获取代表指定属性的子树
     *
     * @param attrName 属性名称
     * @return 子树
     */
    public AttributeTree getSubTree(String attrName) {
        if (this.node instanceof ComplexAttributeNode) {
            ComplexAttributeNode complexAttribute = (ComplexAttributeNode) this.node;
            AttributeTreeNode subTreeChild = complexAttribute.getChild(attrName);
            if (subTreeChild != null)
                return subTreeChild.asTree();
        }

        return null;
    }

    /**
     * 移除代表指定属性的子树，并返回该子树。
     *
     * @param attrName 属性名称
     */
    public void removeSubTree(String attrName) {
        if (this.node instanceof ComplexAttributeNode) {
            ComplexAttributeNode complexAttribute = (ComplexAttributeNode) this.node;
            complexAttribute.removeChild(attrName);
        }
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor 访问者
     */
    public void accept(IAttributeTreeDownwardVisitor visitor) {
        this.accept(visitor, null);
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor   访问者
     * @param <TResult> 结果
     * @return 结果
     */
    public <TResult> TResult accept(IAttributeTreeDownwardVisitorWithResult<TResult> visitor) {
        this.accept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向下遍历关联树过程中接受访问者
     *
     * @param visitor     访问者
     * @param parentState 访问父级时产生的状态数据
     */
    private void accept(IAttributeTreeDownwardVisitor visitor, Object parentState) {
        ObjectReferencePack<Object> outParentState = new ObjectReferencePack<>();
        ObjectReferencePack<Object> outPrevisitState = new ObjectReferencePack<>();

        visitor.preVisit(this, parentState, outParentState, outPrevisitState);

        AttributeTree[] subTrees = this.getSubTrees();
        if (subTrees != null) {
            for (AttributeTree subTree : subTrees) {
                subTree.accept(visitor, outParentState.realValue);
            }
        }
        visitor.postVisit(this, outParentState.realValue, outPrevisitState.realValue);
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor 访问者
     */
    public void accept(IAttributeTreeUpwardVisitor visitor) {
        this.accept(visitor, null);
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor   访问者
     * @param <TResult> 结果
     * @return 结果
     */
    public <TResult> TResult accept(IAttributeTreeUpwardVisitorWithResult<TResult> visitor) {
        this.accept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 在向上遍历关联树过程中接受访问者
     *
     * @param visitor    访问者
     * @param childState 访问子级时产生的状态数据
     */
    private void accept(IAttributeTreeUpwardVisitor visitor, Object childState) {
        ObjectReferencePack<Object> outChildState = new ObjectReferencePack<>();
        ObjectReferencePack<Object> outPrevisitState = new ObjectReferencePack<>();

        visitor.preVisit(this, childState, outChildState, outPrevisitState);
        if (this.getParent() != null) this.getParent().accept(visitor, outChildState.realValue);
        visitor.postVisit(this, outChildState.realValue, outPrevisitState.realValue);
    }
}
