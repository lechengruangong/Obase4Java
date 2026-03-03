/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：管道终结执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 14:52:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AttributeTree;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.SourceJoiner;
import io.obase.providers.sql.common.SqlUtils;
import io.obase.providers.sql.sqlobject.Field;
import io.obase.providers.sql.sqlobject.ISelectionSet;
import io.obase.providers.sql.sqlobject.SimpleSource;

/**
 * 管道终结执行器
 */
public class RopTerminator extends RopExecutor {

    /**
     * 构造OpExecutor的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    public RopTerminator(QueryOp queryOp, OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
    }

    /**
     * 执行挂起的包含操作。
     *
     * @param assocTree         包含挂起的包含操作的关联树
     * @param aliasRoot         别名根
     * @param parentSourceAlias 父节点源的别名
     * @param selectionSet      投影列集合
     * @param aliasPrefix       字段别名前缀
     */
    private void executeIncluding(AssociationTree assocTree, String aliasRoot, String parentSourceAlias, ISelectionSet selectionSet, String aliasPrefix) {
        ReferringType currentType = assocTree.getRepresentedType();
        if (currentType == null)
            return;
        SourceJoiner sourceJoiner = new SourceJoiner(currentType, null, null, null);

        for (AssociationTree subTree : assocTree.getSubTrees()) {
            String eleName = subTree.getElementName();
            boolean shouldJoin = sourceJoiner.shouldJoin(eleName);
            String source = shouldJoin ? aliasPrefix + "_" + eleName : parentSourceAlias;
            String aliasPrefixThisRef = aliasPrefix + "_" + eleName;

            ObjectType objType = (ObjectType) subTree.getRepresentedType();
            if (aliasRoot == null)
                aliasRoot = "";
            if (source == null)
                source = "";
            this.generateColumn(objType, aliasRoot.equals(source) ? source : (aliasRoot + source), aliasPrefixThisRef, selectionSet);
            this.executeIncluding(subTree, aliasRoot, source, selectionSet, aliasPrefixThisRef);
        }
    }

    /**
     * 根据指定的对象类型生成投影列
     *
     * @param objectType   对象类型
     * @param source       属性映射字段所属源的名称
     * @param aliasPrefix  投影列别名的前缀
     * @param selectionSet 生成的投影列所属的投影集
     */
    private void generateColumn(ObjectType objectType, String source, String aliasPrefix, ISelectionSet selectionSet) {

        if (Utils.getStringIsEmpty(source)) {
            source = objectType.getTargetTable();
        }

        SelectionColumnGenerator generator = new SelectionColumnGenerator(selectionSet, new SimpleSource(source), aliasPrefix);
        if (objectType.enumerateAttributeTree() != null)
            for (AttributeTree attrTree : objectType.enumerateAttributeTree()) {
                attrTree.accept(generator);
            }

        if (objectType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) objectType;
            for (AssociationEnd end : associationType.getAssociationEnds()) {
                for (AssociationEndMapping map : end.getMappings()) {
                    String fieldName = map.getTargetField();
                    Field field = new Field(source, fieldName);
                    selectionSet.add(field, aliasPrefix + "_" + fieldName);
                }
            }
        }
    }

    /**
     * 执行运算
     *
     * @param ropContext 运算上下文
     */
    @Override
    public void execute(RopContext ropContext) {
        AssociationTree including = ropContext.getIncluding();
        if (ropContext.getResultIncluding() != null) {
            ropContext.getResultIncluding().grow(ropContext.getIncluding());
        }

        if (including != null && including.getSubTrees().length > 0) {
            if (ropContext.getResultSql().getTakeNumber() > 0) ropContext.acceptResult();
            ropContext.expandSource(false);
            this.executeIncluding(including, ropContext.getAliasRoot(), ropContext.getAliasRoot(),
                    ropContext.getResultSql().getSelectionSet(), "");
        }

        //如果存在同一个Field的仅保留一个
        ropContext.getResultSql().setOrders(SqlUtils.distinctOrders(ropContext.getResultSql().getOrders()));
    }
}
