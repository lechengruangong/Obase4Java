/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：略过运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:02:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.*;

/**
 * 略过运算执行器
 */
public class SkipExecutor extends RopExecutor {

    /**
     * 提取的数量
     */
    private final int count;

    /**
     * 构造SkipExecutor的新实例
     *
     * @param queryOp 查询运算
     * @param count   要提取的数量
     * @param next    运算管道中的下一个执行器
     */
    public SkipExecutor(QueryOp queryOp, int count, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.count = count;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        switch (ropContext.getSourceType()) {

            case SqlServer: {
                if (ropContext.getResultSql().getDistinct())
                    ropContext.acceptResult();
                if (ropContext.getResultSql().getOrders().size() == 0) {
                    ropContext.getResultSql().bubbleOrder();
                }
                FunctionExpression index = Expression.function("row_number");
                OverClause over = new OverClause(ropContext.getResultSql().getOrders().toArray(new Order[0]));
                index.setOver(over);
                String alias = ropContext.getResultModelType().getName() + "_rownumber";
                if (ropContext.getResultSql().getSelectionSet() == null)
                    ropContext.getResultSql().setSelectionSet(new SelectionSet());
                ropContext.getResultSql().getSelectionSet().add(index, alias);
                ropContext.acceptResult();
                ropContext.getResultSql().setCriteria(new NumericCriteria<>(alias, ERelationOperator.GreaterThan, this.count));
                break;
            }
            case Oracle:
                break;
            case MySql:
            case PostgreSql:
            case Sqlite: {
                ropContext.getResultSql().setSkipNumber(this.count);
                break;
            }
        }

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
