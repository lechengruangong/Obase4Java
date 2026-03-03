/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定查询运算的前置访问逻辑.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:21:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.FunctionWithOneArg;

/**
 * 特定查询运算的前置访问逻辑
 */
public class SpecificPreVisitor {

    /**
     * 特定查询运算的名称
     */
    private EQueryOpName name;

    /**
     * 代表前置访问逻辑的委托
     */
    private IPreVisit preVisit;

    /**
     * 是否启用特定访问逻辑的断言函数
     */
    private FunctionWithOneArg<QueryOp, ESpecialPredicate> predicate;

    /**
     * 获取特定查询运算的名称
     *
     * @return 特定查询运算的名称
     */
    public EQueryOpName getName() {
        return this.name;
    }

    /**
     * 设置特定查询运算的名称
     *
     * @param name 特定查询运算的名称
     */
    public void setName(EQueryOpName name) {
        this.name = name;
    }

    /**
     * 获取代表前置访问逻辑的委托
     *
     * @return 代表前置访问逻辑的委托
     */
    public IPreVisit getPreVisit() {
        return this.preVisit;
    }

    /**
     * 设置代表前置访问逻辑的委托
     *
     * @param preVisit 代表前置访问逻辑的委托
     */
    public void setPreVisit(IPreVisit preVisit) {
        this.preVisit = preVisit;
    }

    /**
     * 获取是否启用特定访问逻辑的断言函数
     *
     * @return 是否启用特定访问逻辑的断言函数
     */
    public FunctionWithOneArg<QueryOp, ESpecialPredicate> getPredicate() {
        return this.predicate;
    }

    /**
     * 设置是否启用特定访问逻辑的断言函数
     *
     * @param predicate 是否启用特定访问逻辑的断言函数
     */
    public void setPredicate(FunctionWithOneArg<QueryOp, ESpecialPredicate> predicate) {
        this.predicate = predicate;
    }


}
