/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象运算管道构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:20:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.query.*;

/**
 * 对象运算管道构造器
 */
public class OopPipelineBuilder extends QueryOpVisitorWithResult<OopExecutor> {

    /**
     * 初始化OopPipelineBuilder类的新实例
     */
    public OopPipelineBuilder() {
        // 累加运算。
        // Accumulate
        this.specify(EQueryOpName.Accumulate, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 算术聚合运算。
        // ArithAggregate
        this.specify(EQueryOpName.ArithAggregate, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // All测定运算。
        // All
        this.specify(EQueryOpName.All, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // Any测定运算。
        // Any
        this.specify(EQueryOpName.Any, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 类型转换运算。
        // Cast
        this.specify(EQueryOpName.Cast, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // Contains测定运算。
        // Contains
        this.specify(EQueryOpName.Contains, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 计数运算。
        // Count
        this.specify(EQueryOpName.Count, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 取默认值运算。
        // DefaultIfEmpty
        this.specify(EQueryOpName.DefaultIfEmpty, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 去重运算。
        // Distinct
        this.specify(EQueryOpName.Distinct, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 索引运算。
        // ElementAt
        this.specify(EQueryOpName.ElementAt, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // First索引运算。
        // First
        this.specify(EQueryOpName.First, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 分组运算。
        // Group
        this.specify(EQueryOpName.Group, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 包含运算。
        // Include
        this.specify(EQueryOpName.Include, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 联接运算。
        // Join
        this.specify(EQueryOpName.Join, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // Last索引运算。
        // Last
        this.specify(EQueryOpName.Last, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 类型筛选运算。
        // OfType
        this.specify(EQueryOpName.OfType, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 排序运算。
        // Order
        this.specify(EQueryOpName.Order, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 反序运算。
        // Reverse
        this.specify(EQueryOpName.Reverse, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 投影运算。
        // Select
        this.specify(EQueryOpName.Select, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 顺序相等比较运算。
        // SequenceEqual
        this.specify(EQueryOpName.SequenceEqual, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 集运算。
        // Set
        this.specify(EQueryOpName.Set, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 单值索引运算。
        // Single
        this.specify(EQueryOpName.Single, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 略过运算。
        // Skip
        this.specify(EQueryOpName.Skip, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 条件略过运算。
        // SkipWhile
        this.specify(EQueryOpName.SkipWhile, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 提取运算。
        // Take
        this.specify(EQueryOpName.Take, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 条件提取运算。
        // TakeWhile
        this.specify(EQueryOpName.TakeWhile, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 筛选运算。
        // Where
        //Where,
        this.specify(EQueryOpName.Where, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
        // 合并运算。
        // Zip
        this.specify(EQueryOpName.Zip, this::postVisitFunc, predicate -> ESpecialPredicate.PreExecute);
    }

    /**
     * 执行通用后置访问逻辑
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问前一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     * @return 是否继续访问
     */
    @Override
    protected boolean postVisitGenerally(QueryOp queryOp, Object previousState, Object preVisitState) {
        return true;
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
    @Override
    protected boolean preVisitGenerally(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState, ObjectReferencePack<Object> outPreVisitState) {
        outPreviousState.realValue = outPreVisitState.realValue = null;
        return true;
    }

    /**
     * 获取生成的对象运算管道
     *
     * @return 对象运算管道
     */
    public OopExecutor getPipeline() {
        return this.result;
    }

    /**
     * 为指定运算添加特定的后置访问逻辑
     *
     * @param queryOp       查询运算
     * @param previousState 前序状态数据
     * @param preVisitState 前序访问数据
     */
    private void postVisitFunc(QueryOp queryOp, Object previousState, Object preVisitState) {
        switch (queryOp.getName()) {
            case Where:
                this.result = OopExecutor.create((WhereOp) queryOp, this.result);
                break;
            case Select:
                this.result = OopExecutor.create((SelectOp) queryOp, this.result);
                break;
            case Distinct:
                this.result = OopExecutor.create((DistinctOp) queryOp, this.result);
                break;
            case Skip:
                this.result = OopExecutor.create((SkipOp) queryOp, this.result);
                break;
            case Take:
                this.result = OopExecutor.create((TakeOp) queryOp, this.result);
                break;
            case Any:
                this.result = OopExecutor.create((AnyOp) queryOp, this.result);
                break;
            case All:
                this.result = OopExecutor.create((AllOp) queryOp, this.result);
                break;
            case First:
                this.result = OopExecutor.create((FirstOp) queryOp, this.result);
                break;
            case Last:
                this.result = OopExecutor.create((LastOp) queryOp, this.result);
                break;
            case ElementAt:
                this.result = OopExecutor.create((ElementAtOp) queryOp, this.result);
                break;
            case Group:
                if (queryOp instanceof GroupAggregationOp) {
                    GroupAggregationOp groupAggregationOp = (GroupAggregationOp) queryOp;
                    this.result = OopExecutor.create(groupAggregationOp, this.result);
                } else {
                    this.result = OopExecutor.create((GroupOp) queryOp, this.result);
                }
                break;
        }

    }
}
