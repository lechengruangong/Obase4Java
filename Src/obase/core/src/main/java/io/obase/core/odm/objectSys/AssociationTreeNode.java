/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:33:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.common.Utils;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;

import java.util.HashMap;
import java.util.Map;

/**
 * 关联树节点
 */
public abstract class AssociationTreeNode {

    /**
     * 子节点
     */
    private final Map<String, ObjectTypeNode> children;

    /**
     * 节点所代表的类型
     */
    private final ReferringType representedType;

    /**
     * 关联树
     */
    private AssociationTree tree;

    /**
     * 创建AssociationTreeNode实例
     *
     * @param representedType 节点所代表的类型
     */
    protected AssociationTreeNode(ReferringType representedType) {
        this.children = new HashMap<>();
        this.representedType = representedType;
    }

    /**
     * 获取当前节点所代表的类型
     *
     * @return 当前节点所代表的类型
     */
    public ReferringType getRepresentedType() {
        return this.representedType;
    }

    /**
     * 获取当前节点的所有子节点
     *
     * @return 当前节点的所有子节点
     */
    public ObjectTypeNode[] getChildren() {
        return this.children.values().toArray(new ObjectTypeNode[0]);
    }

    /**
     * 获取获取根节点
     *
     * @return 获取根节点
     */
    public abstract AssociationTreeNode getRoot();

    /**
     * 获取当前节点的父级节点
     *
     * @return 获取当前节点的父级节点
     */
    public abstract AssociationTreeNode getParent();

    /**
     * 设值当前节点的父级节点
     *
     * @param parent 设值当前节点的父级节点
     */
    public abstract void setParent(AssociationTreeNode parent);

    /**
     * 获取一个值，该值指示当前节点是否为根节点
     *
     * @return 当前节点是否为根节点
     */
    public boolean getIsRoot() {
        return this.getParent() == null;
    }

    /**
     * 获取代表指定元素的子节点
     *
     * @param elementName 要获取其对应子树的元素名称
     * @return 子节点
     */
    public ObjectTypeNode getChild(String elementName) {
        if (this.children.containsKey(elementName)) return this.children.get(elementName);
        return null;
    }

    /**
     * 为当前节点添加子节点，如果指定元素名称，则将子节点的元素名称强制更改为指定的名称。
     * 说明：如果子节点的ElementName属性为空且未指定新名称，引发异常。
     * 如果已存在同名子节点（以新名称为准），不执行添加操作。
     *
     * @param child       要添加的子节点
     * @param elementName 子节点所代表的元素的名称
     * @return 返回刚添加的子节点；如果存在同名子节点，返回已存在的子节点
     * @throws IllegalArgumentException 被添加的关联树只能作为根节点
     */
    public ObjectTypeNode addChild(ObjectTypeNode child, String elementName) {
        String key = elementName;
        if (Utils.getStringIsEmpty(key)) {
            key = child.getElementName();
        }
        if (key == null) throw new IllegalArgumentException("被添加的关联树只能作为根节点。");
        if (elementName != null)
            child.setElementName(elementName);

        if (this.children.containsKey(key)) {
            child = this.children.get(key);
        } else {
            //如果是投影到某一个已有的关联树节点而出现的同类型但不同名的
            ObjectTypeNode finalChild = child;
            ObjectTypeNode refferingTypeRepeated =
                    this.children.values().stream().filter(p -> p.getRepresentedType() == finalChild.getRepresentedType() && p.getElement() instanceof ViewReference).findFirst().orElse(null);
            if (refferingTypeRepeated != null) {
                child = this.children.get(refferingTypeRepeated.getElementName());
            } else {
                this.children.put(key, child);
                //设置子节点的Parent
                child.setParent(this);
            }

        }

        return child;
    }

    /**
     * 为当前节点批量添加子节点
     * 如果子节点的ElementName属性为空，引发异常。如果已存在同名子节点，不执行添加操作。
     *
     * @param children 要添加的子节点
     * @throws IllegalArgumentException 被添加的关联树只能作为根节点
     */
    public void addChild(ObjectTypeNode[] children) {
        for (ObjectTypeNode child : children) {
            if (child.getElementName().isEmpty())
                throw new IllegalArgumentException("子节点的ElementName属性为空且未指定新名称。");
            if (!this.children.containsKey(child.getElementName())) {
                this.children.put(child.getElementName(), child);
                //设置子节点的Parent
                child.setParent(this);
            }
        }
    }

    /**
     * 移除代表指定元素的子节点，然后返回该节点。
     *
     * @param elementName 要获取其对应子树的元素名称
     * @return 指定元素的子节点
     */
    public ObjectTypeNode removeChild(String elementName) {
        ObjectTypeNode node = null;
        if (this.children.containsKey(elementName)) {
            node = this.children.get(elementName);
            this.children.remove(elementName);
        }

        return node;
    }

    /**
     * 将节点视为一棵关联树
     *
     * @return 关联树
     */
    public AssociationTree asTree() {
        if (this.tree == null)
            this.tree = new AssociationTree(this);
        return this.tree;
    }

    /**
     * 克隆关联树节点得到一个孤立节点，即不引用父节点和子节点
     *
     * @return 孤立节点
     */
    public AssociationTreeNode cloneAlone() {
        //克隆
        AssociationTreeNode clone = null;
        //分类
        if (this instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) this;
            clone = new ObjectTypeNode((ObjectType) objectTypeNode.getRepresentedType(), objectTypeNode.getElementName());
        } else if (this instanceof TypeViewNode) {
            TypeViewNode typeViewNode = (TypeViewNode) this;
            clone = new TypeViewNode((TypeView) typeViewNode.getRepresentedType());
        }

        return clone;
    }

    /**
     * 检测当前节点是否有代表指定元素的子节点
     *
     * @param elementName 子节点代表元素的名称
     * @return 是否有子节点
     */
    public boolean hasChild(String elementName) {
        return this.children.containsKey(elementName);
    }

}
