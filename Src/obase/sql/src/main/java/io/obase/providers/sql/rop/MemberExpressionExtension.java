/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：扩展成员表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 12:40:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.ComplexType;
import io.obase.core.odm.IMappable;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.objectSys.*;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.SourceJoiner;
import io.obase.providers.sql.sqlobject.*;

/**
 * 扩展成员表达式
 * 此处用静态工具类实现
 */
public class MemberExpressionExtension {

    /**
     * 生成表达式表示的关联树节点或属性树节点的别名
     *
     * @param memberExp 成员表达式
     * @param model     对象数据模型
     * @return 别名
     */
    public static String generateAlias(MemberExpression memberExp, ObjectDataModel model) {
        ObjectReferencePack<AttributeTree> attrTree = new ObjectReferencePack<>();
        AssociationTree lastAssNode = memberExp.extractAssociation(model, new ObjectReferencePack<>(), new ObjectReferencePack<>(), attrTree, null);
        String alias = "";
        AttributeTree lastAttrNode = attrTree.realValue;

        if (lastAssNode != null) {
            //生成别名
            AliasGenerator aliasGen = new AliasGenerator();
            if (lastAttrNode != null) {
                //生成字段
                TargetFieldGenerator filedGen = new TargetFieldGenerator();
                String filed = lastAttrNode.accept(filedGen);
                alias = lastAssNode.accept(aliasGen, filed);
            } else {
                alias = lastAssNode.accept(aliasGen);
            }
        }

        return alias;
    }

    /**
     * 生成表达式表示的属性树节点的映射字段
     *
     * @param memberExp         成员表达式
     * @param model             对象数据模型
     * @param source            映射字段所属的源
     * @param parameterBindings 形参绑定
     * @return 字段
     */
    public static Field generateField(MemberExpression memberExp, ObjectDataModel model, MonomerSource source, ParameterBinding[] parameterBindings) {
        ObjectReferencePack<AssociationTreeNode> assocTail = new ObjectReferencePack<>();
        ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();

        //提取关联树
        memberExp.extractAssociation(model, assocTail, attrTail, parameterBindings);

        //所属源不存在 构造源
        if (source == null && assocTail.realValue != null) {
            if (assocTail.realValue instanceof ObjectTypeNode) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) assocTail.realValue;
                if (objectTypeNode.getParent() != null) {
                    AssociationTreeNode assocTailParent = objectTypeNode.getParent();
                    AliasGenerator aliasGen = new AliasGenerator();
                    String parentAlias = assocTailParent.asTree().accept(aliasGen);
                    SourceJoiner sourceJoiner = new SourceJoiner(assocTailParent.getRepresentedType(), null, parentAlias, null);
                    ObjectReferencePack<MonomerSource> monomerSources = new ObjectReferencePack<>();
                    sourceJoiner.join(objectTypeNode.getElementName(), monomerSources, new ObjectReferencePack<>(), ESourceJoinType.Left);
                    source = monomerSources.realValue;
                } else {
                    ReferringType tailType = assocTail.realValue.getRepresentedType();
                    //源为简单源
                    if (tailType != null)
                        source = new SimpleSource(((IMappable) tailType).getTargetName());

                }
            }
        }

        //处理属性树
        if (attrTail.realValue != null) {
            //生成字段
            TargetFieldGenerator filedGen = new TargetFieldGenerator();
            String filed = attrTail.realValue.asTree().accept(filedGen);
            return new Field(source, filed);
        }

        throw new IllegalArgumentException("表达式未指向一个简单属性,无法转换成字段");
    }

    /**
     * 生成表达式表示的属性树节点的映射字段
     *
     * @param memberExp    成员表达式
     * @param model        对象数据模型
     * @param paraBindings 形参绑定
     * @return 映射字段
     */
    public static Field generateField(MemberExpression memberExp, ObjectDataModel model, ParameterBinding[] paraBindings) {
        return generateField(memberExp, model, null, paraBindings);
    }

    /**
     * 生成投影列
     *
     * @param memberExp         成员表达式
     * @param model             对象数据模型
     * @param selectionSet      收集所创建的投影列的投影集
     * @param parameterBindings 形参绑定
     * @param assocResult       返回在关联树上的投影结果
     * @param attrResult        返回在属性树上的投影结果
     * @param source            投影列源字段所属的源
     */
    public static void generateSelectionColumn(MemberExpression memberExp, ObjectDataModel model,
                                               ISelectionSet selectionSet, ParameterBinding[] parameterBindings, ObjectReferencePack<AssociationTreeNode> assocResult,
                                               ObjectReferencePack<AttributeTreeNode> attrResult, MonomerSource source) {
        ObjectReferencePack<AssociationTreeNode> assocTail = new ObjectReferencePack<>();
        ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();
        ObjectReferencePack<AttributeTree> attrTree = new ObjectReferencePack<>();

        //提取关联树
        memberExp.extractAssociation(model, assocTail, attrTail, attrTree, parameterBindings);

        //未指定源 构造源
        if (source == null && assocTail.realValue != null) {
            if (assocTail.realValue instanceof ObjectTypeNode) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) assocTail.realValue;
                if (objectTypeNode.getParent() != null) {
                    AliasGenerator aliasGen = new AliasGenerator();
                    AssociationTreeNode assocTailParent = objectTypeNode.getParent();
                    String parentAlias = assocTailParent.asTree().accept(aliasGen);
                    SourceJoiner sourceJoiner = new SourceJoiner(assocTailParent.getRepresentedType(), null, parentAlias, null);
                    ObjectReferencePack<MonomerSource> monomerSources = new ObjectReferencePack<>();
                    monomerSources.realValue = null;
                    sourceJoiner.join(objectTypeNode.getElementName(), monomerSources, new ObjectReferencePack<>(), ESourceJoinType.Left);
                    source = monomerSources.realValue;
                } else {
                    ReferringType tailType = assocTail.realValue.getRepresentedType();
                    source = new SimpleSource(((IMappable) tailType).getTargetName());
                }
            }
        }

        if (attrTree.realValue != null) {
            SelectionColumnGenerator columnGen = new SelectionColumnGenerator(source, null);
            ISelectionSet tempSet = new SelectionSet();

            if (attrTail.realValue.getAttribute().getIsComplex()) {
                columnGen.setSelectionSet(tempSet);
                attrTree.realValue.accept(new AttributeTreeGrower());
                attrTree.realValue.accept(columnGen);
                //必为ComplexType
                ComplexType comType = (ComplexType) attrTail.realValue.getAttributeType();
                Iterable<AttributeTree> comAttributeTrees = comType.enumerateAttributeTree();
                tempSet = columnGen.getSelectionSet();

                for (AttributeTree tree : comAttributeTrees) {
                    //枚举节点
                    Iterable<AttributeTreeNode> leafNodes = tree.accept(new AttributeTreeNodeEnumerator());
                    for (AttributeTreeNode node : leafNodes) {
                        String targetFiled = node.asTree().accept(new TargetFieldGenerator());
                        if (tempSet != null && tempSet.getColumns() != null && tempSet.getColumns().size() > 0) {
                            for (SelectionColumn column : tempSet.getColumns()) {
                                if (column instanceof ExpressionColumn) {
                                    ExpressionColumn expressionColumn = (ExpressionColumn) column;
                                    expressionColumn.setAlias(targetFiled);
                                    selectionSet.add(column);
                                }
                            }
                        }
                    }
                }
            } else {
                columnGen.setSelectionSet(selectionSet);
                attrTree.realValue.accept(columnGen);
            }
        } else {
            //构造通配列
            WildcardColumn wildColumn = new WildcardColumn();
            wildColumn.setSource(source);
            selectionSet.add(wildColumn);
        }

        assocResult.realValue = assocTail.realValue;
        attrResult.realValue = attrTail.realValue;
    }

}
