/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询链访问者.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:05:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义在遍历查询链过程中访问查询运算的规范，并提供基础实现。
 */
public abstract class QueryOpVisitor {

    /**
     * 特定查询运算的后置访问逻辑
     */
    private Map<EQueryOpName, SpecificPostVisitor> specificPostVisitors;

    /**
     * 特定查询运算的前置访问逻辑
     */
    private Map<EQueryOpName, SpecificPreVisitor> specificPreVisitors;

    /**
     * 后置访问，即在访问后续运算后执行操作
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问前一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    public void postVisit(QueryOp queryOp, Object previousState, Object preVisitState) {

        SpecificPostVisitor specific = this.specificPostVisitors != null && this.specificPostVisitors.containsKey(queryOp.getName())
                ? this.specificPostVisitors.get(queryOp.getName())
                : null;

        if (specific == null || specific.getPredicate() == null) {
            this.postVisitGenerally(queryOp, previousState, preVisitState);
        } else {
            ESpecialPredicate predicateRe = specific.getPredicate().invoke(queryOp);
            if (predicateRe == ESpecialPredicate.PreExecute)
                specific.getPostVisit().postVisit(queryOp, previousState, preVisitState);
            if (predicateRe != ESpecialPredicate.Substitute)
                this.postVisitGenerally(queryOp, previousState, preVisitState);
            if (predicateRe == ESpecialPredicate.PostExecute)
                specific.getPostVisit().postVisit(queryOp, previousState, preVisitState);
        }
    }

    /**
     * 执行通用后置访问逻辑
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问前一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     * @return 是否继续访问
     */
    protected abstract boolean postVisitGenerally(QueryOp queryOp, Object previousState, Object preVisitState);

    /**
     * 前置访问，即在访问后续运算前执行操作
     *
     * @param queryOp          要访问的查询运算
     * @param previousState    访问前一运算时产生的状态数据
     * @param outPreviousState 返回一个状态数据，在访问下一运算时该数据将被视为前序状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 如果要继续访问后续运算，返回true；否则返回false
     */
    public boolean preVisit(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState, ObjectReferencePack<Object> outPreVisitState) {
        boolean result = false;
        outPreviousState.realValue = outPreVisitState.realValue = null;
        SpecificPreVisitor specific = this.specificPreVisitors != null && this.specificPreVisitors.containsKey(queryOp.getName())
                ? this.specificPreVisitors.get(queryOp.getName())
                : null;

        if (specific == null || specific.getPredicate() == null) {
            result = this.preVisitGenerally(queryOp, previousState, outPreviousState, outPreVisitState);
        } else {
            ESpecialPredicate predicateRe = specific.getPredicate().invoke(queryOp);
            if (predicateRe == ESpecialPredicate.PreExecute)
                result = specific.getPreVisit().preVisit(queryOp, previousState, outPreviousState, outPreVisitState);
            if (predicateRe != ESpecialPredicate.Substitute)
                result |= this.preVisitGenerally(queryOp, previousState, outPreviousState, outPreVisitState);
            if (predicateRe == ESpecialPredicate.PostExecute || predicateRe == ESpecialPredicate.Substitute)
                result |= specific.getPreVisit().preVisit(queryOp, previousState, outPreviousState, outPreVisitState);
        }

        return result;
    }

    /**
     * 执行通用前置访问逻辑
     *
     * @param queryOp          要访问的查询运算
     * @param previousState    访问前一运算时产生的状态数据
     * @param outPreviousState 返回一个状态数据，在遍历到下一运算时该数据将被视为前序状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    protected abstract boolean preVisitGenerally(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState,
                                                 ObjectReferencePack<Object> outPreVisitState);

    /**
     * 为指定的查询运算设置特定的前置访问逻辑
     *
     * @param name      运算的名称
     * @param preVisit  代表前置访问逻辑的委托
     * @param predicate 断言是否启用特定访问逻辑的函数
     */
    protected void specify(EQueryOpName name, IPreVisit preVisit, FunctionWithOneArg<QueryOp, ESpecialPredicate> predicate) {
        if (this.specificPreVisitors == null) this.specificPreVisitors = new HashMap<>();
        SpecificPreVisitor specificPrevisitor = new SpecificPreVisitor();
        specificPrevisitor.setName(name);
        specificPrevisitor.setPreVisit(preVisit);
        specificPrevisitor.setPredicate(predicate);

        this.specificPreVisitors.put(name, specificPrevisitor);
    }

    /**
     * 为指定的查询运算设置特定的后置访问逻辑
     *
     * @param name      运算名称
     * @param postVisit 代表后置访问逻辑的委托
     * @param predicate 断言是否启用特定访问逻辑的函数
     */
    protected void specify(EQueryOpName name, IPostVisit postVisit, FunctionWithOneArg<QueryOp, ESpecialPredicate> predicate) {

        if (this.specificPostVisitors == null) this.specificPostVisitors = new HashMap<>();
        SpecificPostVisitor specificPostvisitor = new SpecificPostVisitor();
        specificPostvisitor.setName(name);
        specificPostvisitor.setPostVisit(postVisit);
        specificPostvisitor.setPredicate(predicate);

        this.specificPostVisitors.put(name, specificPostvisitor);
    }
}
