/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：用于将对象附加到对象上下文的委托.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:24:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;

/**
 * 用于将对象附加到对象上下文的委托
 */
public interface IAttachObject {

    /**
     * 用于将对象附加到对象上下文的委托。
     * 如果要附加斩对象在对象上下文中不存在，则附加该对象，否则将该对象合并至已存在的对象，并将参数的引用修改为已存在的对象。
     *
     * @param obj    对要附加的对象的引用
     * @param asRoot 是否作为根对象
     */
    <T> void attachObject(ObjectReferencePack<T> obj, boolean asRoot);
}
