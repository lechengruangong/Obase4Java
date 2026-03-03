/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代表复杂属性的节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:38:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexAttributeNode extends AttributeTreeNode {

    /**
     * 子节点
     */
    private final Map<String, AttributeTreeNode> children;

    /**
     * 创建ComplexAttributeNode实例
     *
     * @param attribute 节点代表的属性
     */
    public ComplexAttributeNode(Attribute attribute) {
        super(attribute);
        this.children = new HashMap<>();
    }

    /**
     * 获取所有子节点
     *
     * @return 所有子节点
     */
    public List<AttributeTreeNode> getChildren() {
        return new ArrayList<>(this.children.values());
    }

    /**
     * 添加子节点
     *
     * @param child 要添加的子节点
     * @return 自身
     */
    public AttributeTreeNode addChild(AttributeTreeNode child) {
        child.setParent(this);
        this.children.put(child.getAttribute().getName(), child);
        return this;
    }


    /**
     * 获取代表指定属性的子节点
     *
     * @param attrName 属性名称
     * @return 指定属性的子节点
     */
    public AttributeTreeNode getChild(String attrName) {
        return this.children.getOrDefault(attrName, null);
    }

    /**
     * 移除代表指定属性的子节点，并返回该节点
     *
     * @param attrName 属性名称
     * @return 被移除的节点
     */
    public AttributeTreeNode removeChild(String attrName) {
        AttributeTreeNode node = null;
        if (this.children.containsKey(attrName)) {
            node = this.children.get(attrName);
            this.children.remove(attrName);
        }
        return node;
    }
}
