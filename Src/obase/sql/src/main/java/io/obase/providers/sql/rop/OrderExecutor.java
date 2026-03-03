/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 10:29:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.EOrderDirection;
import io.obase.providers.sql.sqlobject.ESourceJoinType;
import io.obase.providers.sql.sqlobject.Order;

/**
 * 排序运算执行器
 */
public class OrderExecutor extends RopExecutor {

    /**
     * 指示是否清除以前的排序
     */
    private final boolean clearPrevious;

    /**
     * 作为排序依据的表达式
     */
    private final LambdaExpression expression;

    /**
     * 根据排序依据表达式翻译出的排序依据
     */
    private final io.obase.providers.sql.sqlobject.Expression orderBy;

    /**
     * 指示是否倒序
     */
    private final boolean reverted;

    /**
     * 构造OrderExecutor的新实例
     *
     * @param queryOp       查询运算
     * @param expression    排序依据表达式
     * @param orderBy       根据排序依据表达式翻译出的排序依据
     * @param reverted      指示是否倒序
     * @param clearPrevious 指示是否清除以前的排序
     * @param next          运算管道中的下一个执行器
     */
    public OrderExecutor(QueryOp queryOp, LambdaExpression expression, io.obase.providers.sql.sqlobject.Expression orderBy, boolean reverted,
                         boolean clearPrevious,
                         OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);

        this.expression = expression;
        this.orderBy = orderBy;
        this.reverted = reverted;
        this.clearPrevious = clearPrevious;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {
        //如果_clearPrevious == true且RopContext.HasOrdered == true时，清理resultSql.Orders，否则不清理。
        //清理后须将RopContext.HasOrdered设置为false。
        //执行排序后将RopContext.HasOrdered设置为true。

        if (ropContext.getResultSql().getTakeNumber() > 0)
            ropContext.acceptResult();
        ropContext.expandSource(this.expression, ESourceJoinType.Left, true);
        SourceAliasRootSetter setter = new SourceAliasRootSetter(ropContext.getAliasRoot());
        this.orderBy.accept(setter);

        //清理时要只清理由执行器添加的排序
        //因为在构造RopContext时添加的主键排序是为了保证在执行排序的字段值相同的时候仍可以让主键相同的记录相邻 所以不能清理掉

        //查找哪些是由执行器添加的排序
        long clearCount = ropContext.getResultSql().getOrders().stream().filter(Order::getIsAddByExecutor).count();

        //要清理的情况
        if (this.clearPrevious && ropContext.getHasOrdered()) {
            //因为是使用Insert插入的 所有的由执行器插入的排序都在Count这个索引前面 所以可以直接删除前面添加的排序
            for (int i = 0; i < clearCount; i++)
                ropContext.getResultSql().getOrders().remove(i);
            //如果清理了 需要将HasOrdered设置为false
            ropContext.setHasOrdered(false);
            //此处的值肯定为0 因为都清理掉了
            clearCount = 0;
        }

        //添加排序 并设置IsAddByExecutor为true 以供后续的执行器判断
        Order order = new Order(this.orderBy, this.reverted ? EOrderDirection.Desc : EOrderDirection.Asc);
        order.setIsAddByExecutor(true);
        //插入到所有由执行器添加的排序后面
        ropContext.getResultSql().getOrders().add((int) clearCount, order);
        ropContext.setHasOrdered(true);
        //如果存在同一个Field的 在终结点中处理
        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
