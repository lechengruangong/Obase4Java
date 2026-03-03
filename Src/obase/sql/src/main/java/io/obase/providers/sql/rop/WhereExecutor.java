/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：筛选运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-7-1 16:15:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.ELogicalOperator;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.query.AggregateOp;
import io.obase.core.query.FilterOp;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.ComplexCriteria;
import io.obase.providers.sql.sqlobject.ESourceJoinType;
import io.obase.providers.sql.sqlobject.ICriteria;

/**
 * 筛选运算执行器
 */
public class WhereExecutor extends RopExecutor {

    /**
     * 根据条件表达式解析出的筛选条件
     */
    private final ICriteria criteria;

    /**
     * 条件表达式
     */
    private final LambdaExpression expression;

    /**
     * 构造WhereExecutor的新实例
     *
     * @param queryOp  查询运算
     * @param criteria 根据条件表达式解析出的筛选条件
     * @param next     运算管道中的下一个执行器
     */
    public WhereExecutor(QueryOp queryOp, ICriteria criteria, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.criteria = criteria;
        if (queryOp instanceof FilterOp) {
            FilterOp op = (FilterOp) queryOp;
            this.expression = op.getPredicate();
        } else if (queryOp instanceof AggregateOp) {
            AggregateOp op = (AggregateOp) queryOp;
            this.expression = op.getPredicate();
        } else {
            this.expression = null;
        }
    }

    /**
     * 构造WhereExecutor的新实例
     *
     * @param expression 条件表达式
     * @param criteria   根据条件表达式解析出的筛选条件
     * @param next       运算管道中的下一个执行器
     */
    public WhereExecutor(LambdaExpression expression, ICriteria criteria, OpExecutorWithContext<RopContext> next) {
        super(null, next);
        this.criteria = criteria;
        this.expression = expression;
    }


    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        //     算法：
        //     if(_expression.Parameters.Count == 2)
        //     AddIndexColumn();
        //     if (resultSql.Top > 0) AcceptResult();
        //     ExpandSource(条件表达式);
        //     SourceAliasRootSetter setter = new SourceAliasRootSetter(aliasRoot);
        //     条件.GuideExpressionVisitor(setter);
        //     resultSql.条件 = resultSql.条件 且 条件;

        if (this.expression != null && this.expression.getParameters() != null && this.expression.getParameters().length == 2)
            ropContext.addIndexColumn();
        if (ropContext.getResultSql().getTakeNumber() > 0)
            ropContext.acceptResult();
        if (this.expression != null)
            ropContext.expandSource(this.expression, ESourceJoinType.Left, true);
        SourceAliasRootSetter setter = new SourceAliasRootSetter(ropContext.getAliasRoot());
        this.criteria.guideExpressionVisitor(setter);

        ropContext.getResultSql().setCriteria(ropContext.getResultSql().getCriteria() == null ?
                this.criteria
                : new ComplexCriteria(ropContext.getResultSql().getCriteria(), this.criteria, ELogicalOperator.And));

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
