/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：退化投影运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:11:59
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.rop;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.PrimitiveType;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.sqlobject.ESourceJoinType;
import io.obase.providers.sql.sqlobject.ISelectionSet;
import io.obase.providers.sql.sqlobject.QuerySql;

/**
 * 退化投影运算执行器
 */
public class AtrophySelectExecutor extends RopExecutor {

    /**
     * 在关联树上的投影结果
     */
    private final AssociationTreeNode associationResult;

    /**
     * 在属性树上的投影结果
     */
    private final AttributeTreeNode attributeResult;

    /**
     * 集合选择器
     */
    private final LambdaExpression collectionSelector;

    /**
     * 投影表达式
     */
    private final LambdaExpression expression;

    /**
     * 投影结果对应的别名
     */
    private final String resultAlias;

    /**
     * 根据投影表达式解析出的投影集
     */
    private final ISelectionSet selectionSet;

    /**
     * 构造SelectExecutor的新实例
     *
     * @param queryOp            查询运算
     * @param expression         投影表达式
     * @param collectionSelector 收集元素选择器
     * @param selectionSet       根据投影表达式解析出的投影集
     * @param assocResult        关联树
     * @param attrResult         属性树
     * @param resultAlias        投影结果对应的别名
     * @param next               运算管道中的下一个执行器
     */
    public AtrophySelectExecutor(QueryOp queryOp, LambdaExpression expression, LambdaExpression collectionSelector,
                                 ISelectionSet selectionSet, AssociationTreeNode assocResult, AttributeTreeNode attrResult,
                                 String resultAlias,
                                 OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.expression = expression;
        this.collectionSelector = collectionSelector;
        this.selectionSet = selectionSet;
        this.associationResult = assocResult;
        this.attributeResult = attrResult;
        this.resultAlias = resultAlias;
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {
        if (this.collectionSelector == null && this.expression.getParameters().length == 2)
            ropContext.addIndexColumn();

        QuerySql resultSql = ropContext.getResultSql();
        //是否提取或去重
        if (resultSql.getTakeNumber() > 0 || resultSql.getDistinct())
            ropContext.acceptResult();

        ropContext.expandSource(this.expression, ESourceJoinType.Inner, false);
        String aliasRoot = ropContext.getAliasRoot();
        if (aliasRoot != null && this.selectionSet != null) this.selectionSet.setSourceAliasPrefix(aliasRoot);
        ropContext.getResultSql().setSelectionSet(this.selectionSet);

        //根据是否有投影表达式，设置投影结果类型
        if (this.associationResult != null)
            ropContext.setResultType(this.associationResult, this.attributeResult, this.next instanceof RopTerminator);
        else
            ropContext.setResultType(PrimitiveType.fromType(this.expression.getBody().getType()), true,
                    this.next instanceof RopTerminator || this.next instanceof AggregateExecutor);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
