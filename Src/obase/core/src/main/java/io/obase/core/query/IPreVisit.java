/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代表特定前置访问逻辑的委托.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：22025-12-29 15:22:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;

/**
 * 代表特定前置访问逻辑的委托
 */
@FunctionalInterface
public interface IPreVisit {

    /**
     * 代表特定前置访问逻辑的委托
     *
     * @param queryOp          要访问的查询运算
     * @param previousState    访问前一运算时产生的状态数据
     * @param outPreviousState 返回一个状态数据，在遍历到后一运算时该数据将被视为前序状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    boolean preVisit(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState,
                     ObjectReferencePack<Object> outPreVisitState);
}
