/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：补充运算的对象运算管道构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:11:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.EQueryOpName;
import io.obase.core.query.ESpecialPredicate;
import io.obase.core.query.QueryOp;
import io.obase.core.query.oop.OopPipelineBuilder;

/**
 * 支持补充运算的对象运算管道构造器
 */
public class ComplementaryPipelineBuilder extends OopPipelineBuilder {

    /**
     * 支持补充运算的对象运算管道构造器
     */
    public ComplementaryPipelineBuilder() {
        // All
        this.specify(EQueryOpName.All, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        // Any
        this.specify(EQueryOpName.Any, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        //Contains
        this.specify(EQueryOpName.Contains, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        //Single
        this.specify(EQueryOpName.Single, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        //First
        this.specify(EQueryOpName.First, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        //Last
        this.specify(EQueryOpName.Last, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
        //ElementAt
        this.specify(EQueryOpName.ElementAt, this::postVisitFunc, predicate -> ESpecialPredicate.PostExecute);
    }


    /**
     * 为指定运算添加特定的后置访问逻辑
     *
     * @param queryOp       查询运算
     * @param previousState 前序结果
     * @param preVisitState 前序状态数据
     */
    private void postVisitFunc(QueryOp queryOp, Object previousState, Object preVisitState) {
        switch (queryOp.getName()) {
            /*测定类运算（AllOp, AnyOp, ContainsOp, SingleOp）的补充运算*/
            case All:
            case Any:
            case Contains:
            case Single:
                this.result = new DetectionComplementaryOpExecutor((DetectionComplementaryOp) queryOp, this.result);
                break;
            /*选择类运算（FirstOp, LastOp）的补充运算*/
            case First:
            case Last:
                this.result = new FilteringComplementaryOpExecutor((FilteringComplementaryOp) queryOp, this.result);
                break;
            /*索引运算（ElementAtOp）的补充运算*/
            case ElementAt:
                this.result = new IndexComplementaryOpExecutor((IndexComplementaryOp) queryOp, this.result);
                break;
        }
    }
}
