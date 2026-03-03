/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：介入者接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 16:14:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 介入者接口
 */
public interface IIntervener {

    /**
     * 通知介入者属性已更改。
     *
     * @param obj      发生属性更改的对象
     * @param attrName 发生更改的属性
     */
    void attributeChanged(Object obj, String attrName);

    /**
     * 请求介入者加载关联
     * 对于实体对象，本方法将加载关联引用；对于关联对象则加载关联端
     *
     * @param obj           要加载关联的对象
     * @param referenceName 要加载的关联引用或关联端的名称
     */
    void loadAssociation(Object obj, String referenceName);
}
