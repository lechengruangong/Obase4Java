/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：用于条件拼合的谓词类型枚举.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 12:06:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 用于条件拼合的谓词类型
 */
public enum EPredicateType {

    /**
     * 相等
     */
    Equal,

    /**
     * 不相等
     */
    NotEqual,

    /**
     * 小于
     */
    LessThan,

    /**
     * 大于
     */
    GreaterThan,

    /**
     * 小于等于
     */
    LessThanOrEqual,

    /**
     * 大于等于
     */
    GreaterThanOrEqual,

    /**
     * 包含
     */
    Contains,

    /**
     * 以**开头
     */
    StartWith,

    /**
     * 以**结尾
     */
    EndWith
}
