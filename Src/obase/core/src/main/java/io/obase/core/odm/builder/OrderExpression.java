/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序依据的成员表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 12:01:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.expression.MemberExpression;

/**
 * 描述作为排序依据的成员表达式
 */
public class OrderExpression {

    /**
     * 作为排序依据的成员表达式。
     */
    public MemberExpression Expression;

    /**
     * 指示是否倒序（即降序）排列。
     */
    public boolean Inverted;
}
