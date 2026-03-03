/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举算术聚合运算符.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:38:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 枚举算术聚合运算符
 */
public enum EAggregationOperator {

    /**
     * 求和
     */
    Sum((byte) 0),

    /**
     * 求平均数
     */
    Average((byte) 1),

    /**
     * 取最大值
     */
    Max((byte) 2),

    /**
     * 取最小值
     */
    Min((byte) 3);

    /**
     * 算术聚合运算符
     */
    private final byte operator;

    /**
     * 枚举算术聚合运算符
     *
     * @param operator 算术聚合运算符
     */
    EAggregationOperator(byte operator) {
        this.operator = operator;
    }

    /**
     * 获取算术聚合运算符
     *
     * @return 算术聚合运算符
     */
    public byte getOperator() {
        return this.operator;
    }
}
