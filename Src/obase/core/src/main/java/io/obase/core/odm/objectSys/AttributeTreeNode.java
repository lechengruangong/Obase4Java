/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性树的节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 14:36:45
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm.objectSys;

import io.obase.core.odm.Attribute;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.TypeBase;
import io.obase.core.odm.UnknownTypeException;

import java.util.Objects;

/**
 * 属性树节点
 */
public abstract class AttributeTreeNode {

    /**
     * 节点所代表的属性
     */
    private final Attribute attribute;

    /**
     * 属性树。{ 创建属性树后寄存，避免重复创建。}
     */
    private AttributeTree node;

    /**
     * 父节点
     */
    private ComplexAttributeNode parent;

    /**
     * 构造属性树节点实例
     *
     * @param attribute 节点代表的属性
     */
    protected AttributeTreeNode(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * 获取节点代表的属性
     *
     * @return 节点代表的属性
     */
    public Attribute getAttribute() {
        return this.attribute;
    }

    /**
     * 获取节点代表属性的名称
     *
     * @return 节点代表属性的名称
     */
    public String getAttributeName() {
        return this.attribute.getName();
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    public ComplexAttributeNode getParent() {
        return this.parent;
    }

    /**
     * 设置父节点
     *
     * @param parent 父节点
     */
    void setParent(ComplexAttributeNode parent) {
        this.parent = parent;
    }

    /**
     * 获取节点代表的属性的模型类型
     *
     * @return 节点代表的属性的模型类型
     */
    public TypeBase getAttributeType() {
        ObjectDataModel model = this.attribute.getHostType().getModel();
        if (model == null)
            return null;
        try {
            return model.getType(this.attribute.getDataType());
        } catch (UnknownTypeException e) {
            return null;
        }
    }

    /**
     * 将节点视为一棵属性树
     *
     * @return 属性树
     */
    public AttributeTree asTree() {
        if (this.node == null) {
            this.node = new AttributeTree(this);
        }
        return this.node;
    }

    /**
     * 重写相等比较方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AttributeTreeNode that = (AttributeTreeNode) o;
        return Objects.equals(this.attribute, that.attribute) && Objects.equals(this.node, that.node) && Objects.equals(this.parent, that.parent);
    }

    /**
     * 重写获取哈希码方法
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.attribute, this.node, this.parent);
    }
}
