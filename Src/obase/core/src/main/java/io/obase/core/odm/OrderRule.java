/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序规则.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 11:18:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 描述排序规则
 */
public class OrderRule {

    /**
     * 是否倒序排列。注：默认为正序（即升序）排序。
     */
    private boolean inverted = false;

    /**
     * 排序依据
     */
    private IOrderBy orderBy;

    /**
     * 获取排序依据
     *
     * @return 排序依据
     */
    public IOrderBy getOrderBy() {
        return this.orderBy;
    }

    /**
     * 设置排序依据
     *
     * @param orderBy 排序依据
     */
    public void setOrderBy(IOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * 获取是否倒序排列。注：默认为正序（即升序）排序。
     *
     * @return 是否倒序排列。
     */
    public boolean getInverted() {
        return this.inverted;
    }

    /**
     * 设置是否倒序排列
     *
     * @param inverted 是否倒序排列
     */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
