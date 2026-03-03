/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示特定于条件拼合器的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:41:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 表示特定于条件拼合器的异常
 */
public class PredicateCombinerException extends RuntimeException {

    /**
     * 初始化特定于条件拼合器的异常
     *
     * @param message 异常消息
     */
    public PredicateCombinerException(String message) {
        super(message);
    }
}
