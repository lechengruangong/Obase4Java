/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元素不存在时引发的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:33:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 元素（简单属性、复杂属性、引用元素）不存在时引发的异常。
 */
public class ElementNotFoundException extends RuntimeException {

    /**
     * 元素名称
     */
    private final String elementName;

    /**
     * 创建ElementNotFoundException实例
     *
     * @param elementName 元素名称
     */
    public ElementNotFoundException(String elementName) {
        this.elementName = elementName;
    }

    /**
     * 元素名称
     *
     * @return 元素名称
     */
    public String getElementName() {
        return this.elementName;
    }

    /**
     * 返回异常消息
     *
     * @return 异常消息
     */
    @Override
    public String getMessage() {
        return "无法找到元素" + this.elementName;
    }
}
