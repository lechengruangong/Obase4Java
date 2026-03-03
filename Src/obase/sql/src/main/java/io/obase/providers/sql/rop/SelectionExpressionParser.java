/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：投影表达式解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 10:50:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.*;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.providers.sql.sqlobject.ExpressionColumn;
import io.obase.providers.sql.sqlobject.ISelectionSet;
import io.obase.providers.sql.sqlobject.SelectionSet;

/**
 * 投影表达式解析器
 */
public class SelectionExpressionParser extends ExpressionVisitor {

    /**
     * 宿主别名
     */
    private final static String hostAlias = "obase$result";

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 形参绑定
     */
    private final ParameterBinding[] parameterBindings;

    /**
     * 子树求值器
     */
    private final SubTreeEvaluator subTreeEvaluator;

    /**
     * 存储访问者模式中的关联树节点
     */
    private AssociationTreeNode assocResult;

    /**
     * 存储访问者模式中的属性树节点
     */
    private AttributeTreeNode attrResult;

    /**
     * 指示是否将每个元素的投影结果序列组合为一个序列。注：当投影结果为单值时，该属性为false。
     */
    private boolean isCombining;

    /**
     * 投影集
     */
    private ISelectionSet set;

    /**
     * 存储问者模式中传入的投影集
     */
    private ISelectionSet tempSet;

    /**
     * 构造SelectionExpressionParser的新实例
     *
     * @param model             对象数据模型
     * @param subTreeEvaluator  子树求值器
     * @param isCombining       指示是否将每个元素的投影结果序列组合为一个序列。注：当投影结果为单值时，该属性为false
     * @param parameterBindings 形参绑定
     */
    public SelectionExpressionParser(ObjectDataModel model, SubTreeEvaluator subTreeEvaluator, boolean isCombining, ParameterBinding[] parameterBindings) {
        this.subTreeEvaluator = subTreeEvaluator;
        this.model = model;
        this.isCombining = isCombining;
        this.parameterBindings = parameterBindings;
    }

    /**
     * 获取指示是否将每个元素的投影结果序列组合为一个序列。注：当投影结果为单值时，该属性为false。
     *
     * @return 是否将每个元素的投影结果序列组合为一个序列
     */
    public boolean getIsCombining() {
        return this.isCombining;
    }

    /**
     * 设置指示是否将每个元素的投影结果序列组合为一个序列。注：当投影结果为单值时，该属性为false。
     *
     * @param combining 是否将每个元素的投影结果序列组合为一个序列
     */
    public void setIsCombining(boolean combining) {
        this.isCombining = combining;
    }

    /**
     * 默认的访问Lambda表达式
     * 先访问Body 然后挨个访问Parameter
     * 最后返回自身
     *
     * @param lambdaExpression Lambda表达式
     * @return 自身
     */
    @Override
    protected Expression visitLambda(LambdaExpression lambdaExpression) {
        return this.visit(lambdaExpression.getBody());
    }

    /**
     * 默认的访问新建表达式
     * 先访问Argument 然后返回自身
     *
     * @param newExpression 新建表达式
     * @return 新建表达式自身
     */
    @Override
    protected Expression visitNew(NewExpression newExpression) {
        ExpressionTranslator sqlExpTranslator = new ExpressionTranslator(this.model, this.subTreeEvaluator, null);
        ISelectionSet selection = new SelectionSet();

        //将每个参数作为投影列 对应的匿名属性名称作为别名
        for (int i = 0; i < newExpression.getArgument().length; i++) {
            io.obase.providers.sql.sqlobject.Expression sqlExp = sqlExpTranslator.translate(newExpression.getArgument()[i]);
            ExpressionColumn expressionColumn = new ExpressionColumn();
            expressionColumn.setExpression(sqlExp);
            expressionColumn.setAlias(newExpression.getMembers()[i].getName());
            selection.add(expressionColumn);
        }

        this.set = selection;

        return newExpression;
    }

    /**
     * 默认的访问成员表达式
     * 访问成员表达式的Expression 而后返回自身
     *
     * @param memberExpression 成员表达式
     * @return 成员表达式自身
     */
    @Override
    protected Expression visitMember(MemberExpression memberExpression) {
        //取模型类型
        StructuralType modelType = memberExpression.getHost() != null ? this.model.getStructuralType(memberExpression.getHost().getType()) : this.model.getStructuralType(memberExpression.getExpression().getType());

        //是否使用翻译表达式的方式处理
        boolean shouldTranslate = false;

        if (modelType != null) {
            Attribute attr = modelType.getAttribute(memberExpression.getMemberName());

            //判断类型是否为空或为复杂类型
            if (!(attr != null && !attr.getIsComplex())) {
                ObjectReferencePack<AssociationTreeNode> associationTreeNodes = new ObjectReferencePack<>();
                ObjectReferencePack<AttributeTreeNode> attributeTreeNodes = new ObjectReferencePack<>();
                MemberExpressionExtension.generateSelectionColumn(memberExpression, this.model, this.tempSet, this.parameterBindings, associationTreeNodes, attributeTreeNodes, null);
                this.assocResult = associationTreeNodes.realValue;
                this.attrResult = attributeTreeNodes.realValue;

                this.set = this.tempSet;
            } else {
                shouldTranslate = true;
            }
        } else {
            shouldTranslate = true;
        }

        //判断标识
        if (shouldTranslate) {
            io.obase.providers.sql.sqlobject.Expression sqlExp = new ExpressionTranslator(this.model, this.subTreeEvaluator, this.parameterBindings).translate(memberExpression);
            ExpressionColumn expressionColumn = new ExpressionColumn();
            expressionColumn.setExpression(sqlExp);
            expressionColumn.setAlias(hostAlias);
            this.set = new SelectionSet(expressionColumn);
        }

        return memberExpression;
    }

    /**
     * 解析指定的投影表达式
     *
     * @param expression  要解析的投影表达式
     * @param assocResult 关联树节点
     * @param attrResult  属性书节点
     * @return 投影集合
     */
    public ISelectionSet parse(Expression expression, ObjectReferencePack<AssociationTreeNode> assocResult,
                               ObjectReferencePack<AttributeTreeNode> attrResult) {
        SelectionSet selectionSet = new SelectionSet();
        return this.parse(expression, selectionSet, assocResult, attrResult);
    }

    /**
     * 解析指定的投影表达式
     *
     * @param expression   要解析的投影表达式
     * @param selectionSet 投影集，用于在解析过程中收集投影列的容器
     * @param assocResult  关联树节点
     * @param attrResult   属性书节点
     * @return 投影集合
     */
    public ISelectionSet parse(Expression expression, ISelectionSet selectionSet,
                               ObjectReferencePack<AssociationTreeNode> assocResult, ObjectReferencePack<AttributeTreeNode> attrResult) {
        this.tempSet = selectionSet;
        this.visit(expression);
        //out值
        assocResult.realValue = this.assocResult;
        attrResult.realValue = this.attrResult;
        //结果
        return this.set;
    }
}
