/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：修改通知扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:15:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.ObjectContext;

/**
 * 修改通知扩展
 */
public class ChangeNoticeExtensions {

    /**
     * 启用修改通知
     *
     * @param context 上下文
     */
    public static void enableChangeNotice(ObjectContext context) {
        context.registerModule(new ChangeNoticeModule(context));
    }
}