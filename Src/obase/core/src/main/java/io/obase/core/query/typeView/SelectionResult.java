/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：投影结果.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:48:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

/**
 * 投影结果
 */
public abstract class SelectionResult {

    /**
     * 元素
     */
    protected Object element;

    /**
     * 键
     */
    protected Object key;

    /**
     * 源
     */
    protected Object source;
}

