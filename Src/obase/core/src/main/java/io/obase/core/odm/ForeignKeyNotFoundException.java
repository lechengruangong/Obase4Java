/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：没有找到外键异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-25 11:21:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 没有找到外键异常
 */
public class ForeignKeyNotFoundException extends RuntimeException {

    /**
     * 构造没有找到外键异常实例
     *
     * @param message 消息
     */
    public ForeignKeyNotFoundException(String message) {
        super(message);
    }
}
