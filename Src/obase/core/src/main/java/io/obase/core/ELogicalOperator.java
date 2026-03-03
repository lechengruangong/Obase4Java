/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举逻辑运算符.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:36:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

/**
 * 枚举逻辑运算符
 */
public enum ELogicalOperator {

    /**
     * 与
     */
    And(0),

    /**
     * 或
     */
    Or(1),

    /**
     * 非
     */
    Not(2);


    /**
     * 运算符
     */
    private final int operator;

    /**
     * 枚举逻辑运算符
     *
     * @param operator 运算符
     */
    ELogicalOperator(int operator) {
        this.operator = operator;
    }

    /**
     * 获取运算符
     *
     * @return 运算符
     */
    public int getOperator() {
        return this.operator;
    }
}
