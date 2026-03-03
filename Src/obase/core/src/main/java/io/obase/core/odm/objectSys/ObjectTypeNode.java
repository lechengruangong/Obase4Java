/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树中代表对象类型的节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 16:54:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.ObjectType;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.ReferringType;

/**
 * 关联树中代表对象类型的节点
 */
public class ObjectTypeNode extends AssociationTreeNode {

    /**
     * 节点代表的对象元素的名称
     */
    private String elementName;

    /**
     * 父节点
     */
    private AssociationTreeNode parent;

    /**
     * 创建ObjectTypeNode实例
     *
     * @param objType     节点代表的对象类型
     * @param elementName 节点代表的元素的名称
     */
    public ObjectTypeNode(ObjectType objType, String elementName) {
        super(objType);
        this.elementName = elementName;
    }

    /**
     * 获取关联树节点代表的类型元素
     *
     * @return 获取关联树节点代表的类型元素
     */
    public ReferenceElement getElement() {
        if (this.getParent() != null) {
            ReferringType referringType = this.getParent().getRepresentedType();
            if (referringType != null)
                return referringType.getReferenceElement(this.elementName);
        }
        return null;
    }

    /**
     * 获取节点代表的对象元素的名称
     *
     * @return 获取节点代表的对象元素的名称
     */
    public String getElementName() {
        return this.elementName;
    }

    /**
     * 设置节点代表的对象元素的名称
     *
     * @param elementName 设置节点代表的对象元素的名称
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * 获取获取根节点
     *
     * @return 获取根节点
     */
    @Override
    public AssociationTreeNode getRoot() {
        if (this.parent == null) return this;
        return this.parent.getRoot();
    }

    /**
     * 获取当前节点的父级节点
     *
     * @return 获取当前节点的父级节点
     */
    @Override
    public AssociationTreeNode getParent() {
        return this.parent;
    }

    /**
     * 设值当前节点的父级节点
     *
     * @param parent 设值当前节点的父级节点
     */
    @Override
    public void setParent(AssociationTreeNode parent) {
        this.parent = parent;
    }
}
