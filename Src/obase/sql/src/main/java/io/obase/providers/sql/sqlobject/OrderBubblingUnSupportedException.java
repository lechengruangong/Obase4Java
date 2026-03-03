/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序冒泡不支持异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:06:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 排序冒泡不支持异常。引发该异常表明源不支持排序冒泡操作。
 */
public class OrderBubblingUnSupportedException extends RuntimeException {

    /**
     * 要实施排序冒泡的源
     */
    private final ISource source;

    /**
     * 构造OrderBubblingUnSupportedException的新实例
     *
     * @param source 要实施顺序冒泡的源
     */
    public OrderBubblingUnSupportedException(ISource source) {
        this.source = source;
    }

    /**
     * 获取要实施排序冒泡的源
     *
     * @return 要实施排序冒泡的源
     */
    public ISource getSource() {
        return this.source;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        if (this.source != null) {
            return this.source.getClass().getName() + "类型的源不支持排序冒泡";
        }
        return "类型的源不支持排序冒泡";
    }
}
