/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：分组聚合运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 16:33:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.ESourceJoinType;
import io.obase.providers.sql.sqlobject.Expression;
import io.obase.providers.sql.sqlobject.GroupBy;

import java.util.ArrayList;

/**
 * 分组聚合运算执行器
 */
public class GroupAggregationExecutor extends RopExecutor {

    /**
     * 作为分组依据的表达式
     */
    private final LambdaExpression expression;

    /**
     * 根据分组依据表达式翻译出的分组依据
     */
    private final Expression groupBy;

    /**
     * 构造GroupAggregationExecutor实例
     *
     * @param queryOp    查询操作
     * @param expression 作为分组依据的表达式
     * @param groupBy    根据分组依据表达式解析出的分组依据
     * @param next       下一节
     */
    public GroupAggregationExecutor(QueryOp queryOp, LambdaExpression expression, Expression groupBy,
                                    OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.groupBy = groupBy;
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
        //     if (resultSql.Top > 0) AcceptResult();
        //     ExpandSource(依据表达式);
        //     SourceAliasRootSetter setter = new SourceAliasRootSetter(aliasRoot);
        //     依据表达式.Accept(setter)
        //     计算 GroupBy group = new Group(分组依据);
        //     然后 resultSql.GroupBy = group;

        if (ropContext.getResultSql().getTakeNumber() > 0)
            ropContext.acceptResult();
        ropContext.expandSource(this.expression, ESourceJoinType.Left, true);
        SourceAliasRootSetter setter = new SourceAliasRootSetter(ropContext.getAliasRoot());
        this.groupBy.accept(setter);
        GroupBy group = new GroupBy(this.groupBy);
        ropContext.getResultSql().setOrders(new ArrayList<>());
        ropContext.getResultSql().setGroupBy(group);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
