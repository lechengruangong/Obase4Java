/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元素扩展,提供类型元素的扩展配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:49:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 元素扩展
 */
public class ElementExtension {

    /**
     * 被扩展元素
     */
    private TypeElement extendedElement;

    /**
     * 获取被扩展元素
     *
     * @return 被扩展元素
     */
    public TypeElement getExtendedElement() {
        return this.extendedElement;
    }

    /**
     * 设置被扩展的元素
     *
     * @param extendedElement 被扩展元素
     */
    void SetExtendedElement(TypeElement extendedElement) {
        this.extendedElement = extendedElement;
    }
}
