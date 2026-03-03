/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象数据集中的数据项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:41:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.ObjectKey;

/**
 * 表示对象数据集中的数据项。
 */
public class ObjectDataSetItem {

    /**
     * 对象数据。
     */
    public IObjectData ObjectData;

    /**
     * 数据项创建的对象的父标识。
     */
    public ObjectKey ParentKey;
}
