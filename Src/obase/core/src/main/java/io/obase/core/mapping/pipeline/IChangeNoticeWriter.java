/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：变更通知编写器接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:44:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.common.FunctionWithOneArg;

/**
 * 变更通知编写器接口
 */
public interface IChangeNoticeWriter {

    /**
     * 编写通知的字符串形式
     *
     * @param serializeFunction 序列化方法
     * @return 通知的字符串形式
     */
    String write(FunctionWithOneArg<Object, String> serializeFunction);

    /**
     * 编写通知
     *
     * @return 通知
     */
    ChangeNotice write();
}
