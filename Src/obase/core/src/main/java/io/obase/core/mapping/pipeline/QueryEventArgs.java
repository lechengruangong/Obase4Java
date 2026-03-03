/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询事件数据.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:26:32
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.query.QueryContext;

import java.util.EventObject;

/**
 * 查询事件数据
 */
public class QueryEventArgs extends EventObject {

    /**
     * 查询上下文
     */
    private final QueryContext context;

    /**
     * 构造查询事件数据
     *
     * @param source  源
     * @param context 查询上下文
     */
    public QueryEventArgs(Object source, QueryContext context) {
        super(source);
        this.context = context;
    }

    /**
     * 获取查询上下文
     *
     * @return 查询上下文
     */
    public QueryContext getContext() {
        return this.context;
    }
}
