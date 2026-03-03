/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代表特定后置访问逻辑的委托.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:08:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 代表特定后置访问逻辑的委托
 */
@FunctionalInterface
public interface IPostVisit {

    /**
     * 代表特定后置访问逻辑的委托
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问上一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    void postVisit(QueryOp queryOp, Object previousState, Object preVisitState);
}
