/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：重复插入异常,当插入相同主键的记录时引发此异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 17:09:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 表示重复插入异常。当插入相同主键的记录时引发此异常。
 */
public class RepeatInsertionException extends RuntimeException {

    /**
     * 当前数据源是否不支持此异常的处理模式
     */
    private final boolean isUnSupported;

    /**
     * 不支持的原因
     */
    private String unSupportMessage;

    /**
     * 创建RepeatInsertionException实例
     */
    public RepeatInsertionException(boolean isUnSupported, Exception exception) {
        super("插入了重复的记录", exception);
        this.isUnSupported = isUnSupported;
    }

    /**
     * 当前数据源是否不支持此异常的处理模式
     *
     * @return 是否不支持
     */
    public boolean isUnSupported() {
        return this.isUnSupported;
    }

    /**
     * 获取不支持的原因
     *
     * @return 不支持的原因
     */
    public String getUnSupportMessage() {
        return this.unSupportMessage;
    }

    /**
     * 设置不支持的原因
     *
     * @param unSupportMessage 不支持的原因
     */
    public void setUnSupportMessage(String unSupportMessage) {
        this.unSupportMessage = unSupportMessage;
    }
}
