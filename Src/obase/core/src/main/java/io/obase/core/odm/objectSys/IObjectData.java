/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象数据规范,符合该规范的数据可用于创建一个对象.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:42:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.ObjectKey;

/**
 * 对象数据规范，符合该规范的数据可用于创建一个对象
 */
public interface IObjectData {

    /**
     * 获取指定属性树节点代表的简单属性的值
     *
     * @param attrNode 属性树节点
     * @return 值
     */
    Object getValue(SimpleAttributeNode attrNode);

    /**
     * 获取对象标识
     *
     * @return 对象标识
     */
    ObjectKey getObjectKey();
}

