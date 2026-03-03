/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：一般投影运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 10:40:32
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.Attribute;
import io.obase.core.odm.IMappable;
import io.obase.core.odm.Parameter;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.*;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.odm.typeviews.ViewAttributeSource;
import io.obase.core.odm.typeviews.ViewComplexAttribute;
import io.obase.core.query.OpExecutorWithContext;
import io.obase.core.query.QueryOp;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.sqlobject.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 一般投影运算执行器
 * 一般投影运算是指以查询源为基础源生成一个类型视图
 */
public class SelectExecutor extends RopExecutor {

    /**
     * 由视图属性绑定解析出的SQL表达式
     */
    private final Map<String, Expression> sqlExpressions;

    /**
     * 作为投影目标的类型视图
     */
    private final TypeView typeView;

    /**
     * 节点别名生成器，用于生成扩展树各节点的别名
     */
    private AliasGenerator aliasGenerator;

    /**
     * 属性树生长器，用于将指定的复杂属性生长至叶子节点
     */
    private AttributeTreeGrower attributeTreeGrower;

    /**
     * 简单属性枚举器，用于枚举属性树上的简单属性节点（即叶子节点）
     */
    private AttributeTreeNodeEnumerator simpleAttributeEnumerator;

    /**
     * 映射字段生成器，用于为复杂视图属性所含的简单属性生成映射字段
     */
    private TargetFieldGenerator targetFieldGenerator;

    /**
     * 创建SelectExecutor实例
     *
     * @param queryOp        查询运算
     * @param typeView       作为投影目录的类型视图
     * @param sqlExpressions 由视图属性绑定翻译的Sql表达式
     * @param next           下一个执行器
     */
    public SelectExecutor(QueryOp queryOp, TypeView typeView, Map<String, Expression> sqlExpressions,
                          OpExecutorWithContext<RopContext> next) {
        super(queryOp, next);
        this.typeView = typeView;
        this.sqlExpressions = sqlExpressions;
    }

    /**
     * 获取别名生成器
     *
     * @return 别名生成器
     */
    public AliasGenerator getAliasGenerator() {
        if (this.aliasGenerator == null)
            this.aliasGenerator = new AliasGenerator();
        return this.aliasGenerator;
    }

    /**
     * 获取属性树生长器
     *
     * @return 属性树生长器
     */
    public AttributeTreeGrower getAttributeTreeGrower() {
        if (this.attributeTreeGrower == null)
            this.attributeTreeGrower = new AttributeTreeGrower();
        return this.attributeTreeGrower;
    }

    /**
     * 获取简单属性枚举器
     *
     * @return 简单属性枚举器
     */
    public AttributeTreeNodeEnumerator getSimpleAttributeEnumerator() {
        if (this.simpleAttributeEnumerator == null)
            this.simpleAttributeEnumerator = new AttributeTreeNodeEnumerator();
        return this.simpleAttributeEnumerator;
    }

    /**
     * 获取映射字段生成器
     *
     * @return 映射字段生成器
     */
    public TargetFieldGenerator getTargetFieldGenerator() {
        if (this.targetFieldGenerator == null)
            this.targetFieldGenerator = new TargetFieldGenerator();
        return this.targetFieldGenerator;
    }

    /**
     * 为简单属性生成投影列
     *
     * @param simpleAttr   视图属性
     * @param context      上下文
     * @param selectionSet 收集投影列的投影集
     */
    private void generateAttributeColumns(ViewAttribute simpleAttr, RopContext context, SelectionSet selectionSet) {
        Expression sqlExp;
        if (simpleAttr.getIsIntuitive())//直观属性
        {
            ViewAttributeSource attrSource = simpleAttr.getSources()[0];
            AssociationTree anchorTree = attrSource.getExtensionNode().asTree();
            String nodeAlias = anchorTree.accept(this.getAliasGenerator());
            MonomerSource source = context.getJoinMemo().getSource(context.getAliasRoot() + nodeAlias);
            AttributeTree bindingTree = attrSource.getAttributeNode().asTree();
            this.getTargetFieldGenerator().reset();
            String filedName = bindingTree.accept(this.getTargetFieldGenerator());
            Field field = new Field(source, filedName);
            sqlExp = Expression.field(field);
        } else //非直观属性
        {
            String attrName = simpleAttr.getName();
            sqlExp = this.sqlExpressions.get(attrName);
        }

        selectionSet.add(sqlExp, simpleAttr.getTargetField());
    }

    /**
     * 为复杂属性生成投影列
     *
     * @param complexAttr  复杂属性
     * @param joinMemo     源联接备忘录
     * @param selectionSet 收集投影列的投影集
     */
    private void generateAttributeColumns(ViewComplexAttribute complexAttr, JoinMemo joinMemo, SelectionSet selectionSet) {
        AssociationTree anchorTree = complexAttr.getAnchor().asTree();
        String nodeAlias = anchorTree.accept(this.aliasGenerator);
        MonomerSource source = joinMemo.getSource(nodeAlias);

        SelectionSet tempSet = new SelectionSet();
        SelectionColumnGenerator columnGen = new SelectionColumnGenerator(tempSet, source, (AssociationTreeNode) null);

        AttributeTree bindingTree = complexAttr.getBinding().asTree();
        bindingTree.accept(this.getAttributeTreeGrower());
        bindingTree.accept(columnGen);

        AttributeTree attrTree = new AttributeTree(complexAttr);
        attrTree.accept(this.getAttributeTreeGrower());
        Iterable<AttributeTreeNode> leafNodes = attrTree.accept(this.getSimpleAttributeEnumerator());

        for (AttributeTreeNode node : leafNodes) {
            for (SelectionColumn column : tempSet.getColumns()) {
                if (column instanceof ExpressionColumn) {
                    ExpressionColumn expressionColumn = (ExpressionColumn) column;
                    AttributeTree nodeTree = node.asTree();
                    String targetFiled = nodeTree.accept(this.getTargetFieldGenerator());
                    expressionColumn.setAlias(targetFiled);
                    selectionSet.add(expressionColumn);
                }
            }
        }
    }

    /**
     * 生成构造函数参数列
     * 参数列与属性列可能存在重复，重复的列不添加。
     * 注意复制已有列的源
     *
     * @param parameter    构造函数参数
     * @param selectionSet 投影列集合
     */
    private void generateConstructorParameterColumns(Parameter parameter, SelectionSet selectionSet) {

        //投影列的表达式
        Expression exp = this.sqlExpressions.get(parameter.getName());
        //已有的其他列
        ExpressionColumn other = selectionSet.getColumns().stream().filter(p -> p instanceof ExpressionColumn).map(p -> (ExpressionColumn) p).findFirst().orElse(null);
        //自己的构造参数 复制其他的源
        if (exp instanceof FieldExpression) {
            FieldExpression fieldExpression = (FieldExpression) exp;
            if (other != null && other.getExpression() instanceof FieldExpression) {
                FieldExpression otherExpression = (FieldExpression) other.getExpression();
                MonomerSource monomerSource = otherExpression.getField().getSource();
                //直接赋值 不搞拷贝
                if (monomerSource instanceof SimpleSource) {
                    SimpleSource simpleSource = new SimpleSource(((SimpleSource) monomerSource).getName(), ((SimpleSource) monomerSource).getAlias());
                    fieldExpression.getField().setSource(simpleSource);
                } else if (monomerSource instanceof SelectSource) {
                    SelectSource selectSource = new SelectSource(((SelectSource) monomerSource).getQuerySql(), ((SelectSource) monomerSource).getName());
                    fieldExpression.getField().setSource(selectSource);
                } else {
                    fieldExpression.getField().setSource(otherExpression.getField().getSource());
                }

            }
        }

        //构造投影列
        ExpressionColumn selection = new ExpressionColumn();
        selection.setExpression(exp);
        selection.setAlias(parameter.getName());
        if (!selectionSet.contains(selection) && selectionSet.getColumns().stream().filter(p -> p instanceof ExpressionColumn).allMatch(p -> (!((ExpressionColumn) p).getAlias().equals(parameter.getName()))))
            selectionSet.add(selection);
    }


    /**
     * 生成标识列
     *
     * @param selectionSet 收集投影列的投影集
     * @param context      关系运算上下文
     */
    private void generateIdColumns(SelectionSet selectionSet, RopContext context) {
        if (this.typeView != null && this.typeView.getReferenceElements() != null && this.typeView.getReferenceElements().length == 0)
            return;
        if (this.typeView != null) {
            StructuralType source = this.typeView.getSource();
            if (source instanceof IMappable) {
                IdColumnGenerator idGen = new IdColumnGenerator(this.getAliasGenerator(), context.getJoinMemo(), selectionSet);
                this.typeView.getExtension().accept(idGen);
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

        if (Arrays.stream(this.typeView.getParameterBindings()).anyMatch(p -> p.getReferring() == EParameterReferring.Index))
            ropContext.addIndexColumn();

        if (ropContext.getResultSql().getTakeNumber() > 0 || ropContext.getResultSql().getDistinct())
            ropContext.acceptResult();

        if (!this.typeView.getIsDecomposeExtremelyResult())
            ropContext.expandSource(this.typeView.getExtension(), false);

        SelectionSet selectionSet = new SelectionSet();

        this.generateIdColumns(selectionSet, ropContext);

        for (Attribute attribute : this.typeView.getAttributes()) {
            if (attribute instanceof ViewAttribute) {
                ViewAttribute viewAttribute = (ViewAttribute) attribute;
                this.generateAttributeColumns(viewAttribute, ropContext, selectionSet);
            } else if (attribute instanceof ViewComplexAttribute) {
                ViewComplexAttribute viewComplexAttribute = (ViewComplexAttribute) attribute;
                this.generateAttributeColumns(viewComplexAttribute, ropContext.getJoinMemo(), selectionSet);
            }
        }

        if (this.typeView.getConstructor().getParameters() != null && this.typeView.getConstructor().getParameters().size() > 0)
            //生成构造参数
            for (Parameter parameter : this.typeView.getConstructor().getParameters()) {
                this.generateConstructorParameterColumns(parameter, selectionSet);
            }

        selectionSet.setSourceAliasPrefix(ropContext.getAliasRoot());

        ropContext.getResultSql().setSelectionSet(selectionSet);

        ropContext.setResultType(this.typeView, this.next instanceof RopTerminator);

        if (this.next instanceof OpExecutorWithContext) {
            OpExecutorWithContext<RopContext> executor = (OpExecutorWithContext<RopContext>) this.next;
            executor.execute(ropContext);
        }
    }
}
