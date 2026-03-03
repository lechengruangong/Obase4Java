/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：无法定义属性异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 16:17:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

public class CannotDefiningAttributeException extends RuntimeException {

    /**
     * 构造无法定义属性异常实例
     *
     * @param message        消息
     * @param innerException 内部异常
     */
    public CannotDefiningAttributeException(String message, Exception innerException) {
        super(message, innerException);
    }
}