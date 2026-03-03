/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标识列生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 16:39:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.IMappable;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAssociationTreeDownwardVisitor;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.sqlobject.Field;
import io.obase.providers.sql.sqlobject.ISelectionSet;
import io.obase.providers.sql.sqlobject.MonomerSource;

import java.util.List;

/**
 * 标识列生成器
 */
public class IdColumnGenerator implements IAssociationTreeDownwardVisitor {

    /**
     * 节点别名生成器，用于生成源扩展树各节点的别名
     */
    private final AliasGenerator aliasGenerator;

    /**
     * 源联接备忘录
     */
    private final JoinMemo joinMemo;

    /**
     * 投影集，作为收集所生成的标识列的容器
     */
    private final ISelectionSet selectionSet;

    /**
     * 创建IDColumnGenerator实例
     *
     * @param aliasGenerator 别名生成器
     * @param joinMemo       源联接备忘录
     * @param selectionSet   收集投影列的投影集
     */
    public IdColumnGenerator(AliasGenerator aliasGenerator, JoinMemo joinMemo, ISelectionSet selectionSet) {

        this.aliasGenerator = aliasGenerator;
        this.joinMemo = joinMemo;
        this.selectionSet = selectionSet;
    }

    /**
     * 前置访问，即在访问子级前执行操作。
     *
     * @param subTree          被访问的关联树子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        String nodeAlias = subTree.accept(this.aliasGenerator);
        MonomerSource source = this.joinMemo.getSource(nodeAlias);
        if (source == null)
            source = this.joinMemo.getSource(null);

        if (subTree.getRepresentedType() instanceof IMappable) {
            IMappable mappable = (IMappable) subTree.getRepresentedType();

            List<String> filedNames = mappable.getKeyFields();
            if (filedNames != null)
                for (String keyName : filedNames) {
                    String columnName = subTree.accept(this.aliasGenerator, keyName);
                    this.selectionSet.add(new Field(source, keyName), columnName);
                }
        }

        outParentState.realValue = null;
        outPreVisitState.realValue = null;

        return true;
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的关联树子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object parentState, Object preVisitState) {
        //Nothing to do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to do
    }
}
