/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：缺少键属性异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 14:58:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 缺少键属性异常
 */
public class KeyAttributeLackException extends RuntimeException {

    /**
     * 构造缺少键属性异常
     *
     * @param message 异常消息
     */
    public KeyAttributeLackException(String message) {
        super(message);
    }
}
