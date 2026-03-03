/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举关系运算符.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:26:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 枚举关系运算符
 */
public enum ERelationOperator {

    /**
     * 等于
     */
    Equal,

    /**
     * 不等于
     */
    Unequal,

    /**
     * 小于等于
     */
    LessThanOrEqual,

    /**
     * 小于
     */
    LessThan,

    /**
     * 大于
     */
    GreaterThan,

    /**
     * 大于等于
     */
    GreaterThanOrEqual,

    /**
     * LIKE
     */
    Like,

    /**
     * IN
     */
    In,

    /**
     * NOT IN
     */
    NotIn
}
