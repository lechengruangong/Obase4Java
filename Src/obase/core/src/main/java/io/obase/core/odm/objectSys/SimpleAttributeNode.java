/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代表简单属性的节点.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 14:51:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.Attribute;

/**
 * 代表简单属性的节点
 */
public class SimpleAttributeNode extends AttributeTreeNode {

    /**
     * 创建SimpleAttributeNode实例
     *
     * @param attribute 节点代表的属性
     */
    public SimpleAttributeNode(Attribute attribute) {
        super(attribute);
    }
}
