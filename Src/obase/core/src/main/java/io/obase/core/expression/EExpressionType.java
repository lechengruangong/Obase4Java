/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式类型枚举.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 16:13:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 表达式类型枚举
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
     * 递减运算（a-1）不应就地修改a
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
     * 递增运算（a+1） 不应就地修改a
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
    Modula,

    /**
     * 算术乘法运算
     */
    Multiply,

    /**
     * 算术取反运算（-a），不应就地修改a
     */
    Negate,

    /**
     * 算术取反运算（-a），不应就地修改a
     */
    NegateChecked,

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
     * 按位与运算
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
    RightShift,

    /**
     * 成员访问
     */
    MemberAccess,

    /**
     * Lambda
     */
    Lambda,

    /**
     * 参数表达式
     */
    Parameter,

    /**
     * 调用表达式
     */
    Call,

    /**
     * 构造函数表达式
     */
    New,

    /**
     * 引用
     */
    Quote,

    /**
     * 转换
     */
    Convert,

    /**
     * 按位与
     */
    And,

    /**
     * 按位或
     */
    Or,

    /**
     * +=运算
     */
    AddAssign,

    /**
     * +=运算
     */
    AddAssignChecked,

    /**
     * 相加运算
     */
    AddChecked,

    /**
     * &= 运算
     */
    AndAssign,

    /**
     * /= 运算
     */
    DivideAssign,

    /**
     * ^= 运算
     */
    ExclusiveOrAssign,

    /**
     * <<=运算
     */
    LeftShiftAssign,

    /**
     * %= 运算
     */
    ModuloAssign,

    /**
     * *= 运算
     */
    MultiplyAssign,

    /**
     * *= 运算
     */
    MultiplyAssignChecked,

    /**
     * 相乘运算
     */
    MultiplyChecked,

    /**
     * |=运算
     */
    OrAssign,

    /**
     * -- 运算
     */
    PostDecrementAssign,

    /**
     * ++ 运算
     */
    PostIncrementAssign,

    /**
     * ^= 运算
     */
    PowerAssign,

    /**
     * >>= 运算
     */
    RightShiftAssign,

    /**
     * -= 运算
     */
    SubtractAssign,

    /**
     * -= 运算
     */
    SubtractAssignChecked,

    /**
     * 相减运算
     */
    SubtractChecked,
    /**
     * 异或
     */
    ExclusiveOr,

    /**
     * 成员初始化
     * 此类型的表达式在C#中为构造对象时同时为访问器赋值 在JAVA中无法解析此种逻辑
     */
    MemberInit
}
