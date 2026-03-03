/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：集运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 10:57:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.ObjectType;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.query.ESetOperator;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.*;

/**
 * 集运算执行器
 */
public class SetOpExecutor extends RopExecutor {

    /**
     * 集运算操作符
     */
    private final ESetOperator operator;

    /**
     * 与当前查询源执行集运算的另一个集
     */
    private final ISetOperand other;

    /**
     * 另一个集的包含树
     */
    private AssociationTree otherIncluding;

    /**
     * 构造SetOpExecutor的新实例
     *
     * @param queryOp   查询操作
     * @param other     与当前查询源执行集运算的另一个集
     * @param eOperator 集运算操作符
     * @param next      运算管道中的下一个执行器
     */
    public SetOpExecutor(QueryOp queryOp, ISetOperand other, ESetOperator eOperator,
                         OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.other = other;
        this.operator = eOperator;
    }

    /**
     * 获取另一个集的包含树
     *
     * @return 另一个集的包含树
     */
    public AssociationTree getOtherIncluding() {
        return this.otherIncluding;
    }

    /**
     * 设置另一个集的包含树
     *
     * @param otherIncluding 另一个集的包含树
     */
    public void setOtherIncluding(AssociationTree otherIncluding) {
        this.otherIncluding = otherIncluding;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {

        QuerySet set = new QuerySet(ropContext.getResultSql(), this.other, this.operator);
        String name = ((ObjectType) ropContext.getResultModelType()).getTargetTable();
        SetSource source = new SetSource(set, name);
        ropContext.setResultSql(new QuerySql(source));
        //原投影列会消失 在此处增加一个通配列
        WildcardColumn wildcardColumn = new WildcardColumn();
        wildcardColumn.setSource(source);

        ropContext.getResultSql().getSelectionSet().add(wildcardColumn);
        ropContext.getIncluding().grow(this.otherIncluding);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
