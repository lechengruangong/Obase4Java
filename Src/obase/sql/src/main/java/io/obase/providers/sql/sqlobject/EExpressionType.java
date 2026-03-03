/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举表达式运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 11:56:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 枚举表达式运算
 */
public enum EExpressionType {

    /**
     * 算术加法运算
     */
    Add,

    /**
     * 逻辑AND运算
     */
    AndAlso,

    /**
     * 表示常量值
     */
    Constant,

    /**
     * 递减运算（a-1），不应就地修改a
     */
    Decrement,

    /**
     * 算术除法运算
     */
    Divide,

    /**
     * 相等比较运算
     */
    Equal,

    /**
     * 表达关系表的一个字段
     */
    Field,

    /**
     * 调用某一函数的运算
     */
    Function,

    /**
     * “大于”比较运算
     */
    GreaterThan,

    /**
     * “大于或等于”比较运算
     */
    GreaterThanOrEqual,

    /**
     * “IN”运算
     */
    In,

    /**
     * 递增运算（a+1），不应就地修改a
     */
    Increment,

    /**
     * “小于”比较运算
     */
    LessThan,

    /**
     * “小于或等于”比较运算
     */
    LessThanOrEqual,

    /**
     * “LIKE”运算
     */
    Like,

    /**
     * 算术余数运算
     */
    Modulo,

    /**
     * 算术乘法运算
     */
    Multiply,

    /**
     * 算术取反运算（-a），不应就地修改a
     */
    Negate,

    /**
     * 逻辑求反运算
     */
    Not,

    /**
     * 不相等比较运算
     */
    NotEqual,

    /**
     * “NOT IN”运算
     */
    NotIn,

    /**
     * 逻辑OR运算
     */
    OrElse,

    /**
     * 幂运算
     */
    Power,

    /**
     * 算术减法运算
     */
    Subtract,

    /**
     * 一元加法运算（+a），不应就地修改a
     */
    UnaryPlus,

    /**
     * 按位取反运算
     */
    BitAnd,

    /**
     * 按位取反运算
     */
    BitNot,

    /**
     * 按位或运算
     */
    BitOr,

    /**
     * 按位异或运算
     */
    BitXor,

    /**
     * 按位左移运算
     */
    LeftShift,

    /**
     * 按位右移运算
     */
    RightShift
}
