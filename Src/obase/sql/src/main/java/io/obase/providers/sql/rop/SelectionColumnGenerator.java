/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：投影列生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 14:57:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTree;
import io.obase.core.odm.objectSys.IAttributeTreeDownwardVisitor;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.sqlobject.Field;
import io.obase.providers.sql.sqlobject.ISelectionSet;
import io.obase.providers.sql.sqlobject.MonomerSource;
import io.obase.providers.sql.sqlobject.SelectionSet;

/**
 * 投影列生成器，为被其访问的属性树的映射字段生成投影列。
 * 属性树的映射字段是指属性树各简单属性节点所代表属性的映射字段，它是一个字段集合。
 */
public class SelectionColumnGenerator implements IAttributeTreeDownwardVisitor {

    /**
     * 要生成投影列的属性树在关联树上的锚点，即属性属于哪个关联树节点代表的类型。如果不指定，则认为属于根节点。
     */
    private final AssociationTreeNode anchor;

    /**
     * 被访问属性树的锚点的别名。不启用别名时，本属性无效。
     */
    private final String anchorAlias;

    /**
     * 指示是否启用别名
     */
    private final boolean enableAlias;

    /**
     * 属性树映射字段所属的源
     */
    private final MonomerSource source;

    /**
     * 映射字段生成器，用于生成属性对应的映射字段，（将基于该字段生成投影列）
     */
    private final TargetFieldGenerator targetFieldGenerator = new TargetFieldGenerator();

    /**
     * 别名生成器，用于在生成投影列的过程中生成投影列的别名
     */
    private AliasGenerator aliasGenerator;

    /**
     * 收集投影列的投影集
     */
    private ISelectionSet selectionSet;

    /**
     * 创建SelectionColumnGenerator实例，被访问属性树的映射字段属于指定的源，同时指定收集投影列的投影集
     *
     * @param selectionSet 收集投影列的投影集
     * @param source       属性树映射字段所属的源
     * @param anchor       属性树的锚点。不指定表示不启用别名
     */
    public SelectionColumnGenerator(ISelectionSet selectionSet, MonomerSource source,
                                    AssociationTreeNode anchor) {
        this.selectionSet = selectionSet;
        this.source = source;
        if (anchor != null) {
            this.anchor = anchor;
            this.enableAlias = true;
        } else {
            this.anchor = null;
            this.enableAlias = false;
        }
        this.anchorAlias = null;
        this.targetFieldGenerator.setEnableCache(true);
    }

    /**
     * 创建SelectionColumnGenerator实例，被访问属性树的映射字段属于指定的源，同时指定收集投影列的投影集
     *
     * @param selectionSet 收集投影列的投影集
     * @param source       属性树映射字段所属的源
     * @param anchorAlias  属性树锚点的别名。不指定则表示不启用别名
     */
    public SelectionColumnGenerator(ISelectionSet selectionSet, MonomerSource source, String anchorAlias) {
        this.anchor = null;
        this.selectionSet = selectionSet;
        this.source = source;
        this.anchorAlias = anchorAlias;
        this.enableAlias = true;
        this.targetFieldGenerator.setEnableCache(true);
    }

    /**
     * 创建SelectionColumnGenerator实例，被访问属性树的映射字段属于指定的源
     *
     * @param source 属性树映射字段所属的源
     * @param anchor 属性树的锚点。不指定表示不启用别名
     */
    public SelectionColumnGenerator(MonomerSource source, AssociationTreeNode anchor) {
        this.source = source;
        this.anchor = anchor;
        this.enableAlias = true;
        this.anchorAlias = null;
        this.selectionSet = null;
        this.targetFieldGenerator.setEnableCache(true);
    }

    /**
     * 获取别名生成器
     *
     * @return 别名生成器
     */
    public AliasGenerator getAliasGenerator() {
        return this.aliasGenerator;
    }

    /**
     * 设置别名生成器
     *
     * @param aliasGenerator 别名生成器
     */
    public void setAliasGenerator(AliasGenerator aliasGenerator) {
        this.aliasGenerator = aliasGenerator;
    }

    /**
     * 获取投影列的投影集
     *
     * @return 收集投影列的投影集
     */
    public ISelectionSet getSelectionSet() {
        return this.selectionSet;
    }

    /**
     * 设置收集投影列的投影集
     *
     * @param selectionSet 投影列的投影集
     */
    public void setSelectionSet(ISelectionSet selectionSet) {
        this.selectionSet = selectionSet;
    }

    /**
     * 前置访问，即在访问子级前执行操作
     *
     * @param subTree          被访问的子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     */
    @Override
    public void preVisit(AttributeTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        //用不到
        outParentState.realValue = null;
        outPreVisitState.realValue = null;

        //复杂属性 返回
        if (subTree.getIsComplex()) return;

        //目标字段
        String targetField = subTree.accept(this.targetFieldGenerator);
        Field filed = new Field(this.source, targetField);

        //添加
        if (this.selectionSet == null)
            this.selectionSet = new SelectionSet();

        //是否启用别名
        if (this.enableAlias) {
            if (this.anchor != null) {
                String alias = this.anchor.asTree().accept(this.aliasGenerator);
                this.selectionSet.add(filed, alias);

                return;
            }

            if (!Utils.getStringIsEmpty(this.anchorAlias)) {
                String alias = this.anchorAlias + "_" + targetField;
                this.selectionSet.add(filed, alias);

                return;
            }
        }

        //无别名
        this.selectionSet.add(filed);
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AttributeTree subTree, Object parentState, Object preVisitState) {
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
