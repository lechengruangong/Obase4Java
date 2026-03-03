/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提供发送变更通知的方法接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:31:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

/**
 * 提供发送变更通知的方法
 */
public interface IChangeNoticeSender {

    /**
     * 发送变更通知
     *
     * @param notice 变更通知
     */
    void send(ChangeNotice notice);
}
