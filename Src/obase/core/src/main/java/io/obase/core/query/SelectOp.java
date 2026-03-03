/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Select运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:37:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.EExpressionType;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.HeterogeneityPredicationProvider;
import io.obase.core.odm.objectSys.ObjectTypeNode;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.core.query.typeView.NewSelectionParser;

/**
 * 表示Select运算
 */
public class SelectOp extends QueryOp {

    /**
     * 退化路径
     */
    private AtrophyPath atrophyPath;

    /**
     * 应用于每个元素的投影函数
     */
    private LambdaExpression resultSelector;

    /**
     * 投影结果视图
     */
    private TypeView resultView;

    /**
     * 创建QueryOp的新实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     */
    SelectOp(LambdaExpression resultSelector, ObjectDataModel model) {
        super(EQueryOpName.Select, resultSelector.getParameters()[0].getType());
        this.resultSelector = resultSelector;
        this.model = model;
    }

    /**
     * 创建表示一般投影运算的SelectOp实例
     *
     * @param resultView 目标视图
     */
    SelectOp(TypeView resultView, ObjectDataModel model) {
        this(resultView.generateExpression(new ObjectReferencePack<>()), model);
        this.resultView = resultView;
    }


    /**
     * 创建表示退化投影运算的SelectOp实例
     *
     * @param atrophyPath 退化路径
     */
    public SelectOp(AtrophyPath atrophyPath, ObjectDataModel model) {
        this(atrophyPath.generateExpression(new ObjectReferencePack<>()), model);
        this.atrophyPath = atrophyPath;
    }

    /**
     * 获取退化路径，（仅适用于退化投影运算）
     *
     * @return 退化路径
     */
    public AtrophyPath getAtrophyPath() {
        if (this.getIsNew()) return null;
        if (this.atrophyPath != null) return this.atrophyPath;
        this.atrophyPath = AtrophyPath.fromExpression(this.getModel(), this.resultSelector, null);
        return this.atrophyPath;
    }

    /**
     * 获取一个值，该值指示投影运算是否将元素在序列中的索引作为（第二个）参数
     *
     * @return 指示投影运算是否将元素在序列中的索引作为（第二个）参数
     */
    public boolean getIndexReferred() {
        boolean indexReferred = false;
        ParameterExpression[] parameterExpressions = this.resultSelector.getParameters();
        if (parameterExpressions.length == 2 && parameterExpressions[1].getType() == int.class)
            indexReferred = true;

        return indexReferred;
    }

    /**
     * 获取一个值，该值指示投影运算是否为多重投影。
     * 多重投影是指投影到一个具有多重性的引用元素或其下级元素（下级元素不要求多重性）的运算。
     * 下级元素是指关联树中代表当前元素的节点的后代所代表的元素，或者是当前节点或其后代所含属性树节点所代表的属性。
     *
     * @return 指示投影运算是否为多重投影
     */
    public boolean getIsMultiple() {
        return Iterable.class.isAssignableFrom(this.getResultType());
    }

    /**
     * 获取一个值，该值指示投影运算是否为实例化投影。
     *
     * @return 指示投影运算是否为实例化投影
     */
    public boolean getIsNew() {
        if (this.resultSelector == null) return false;
        return this.resultSelector.getBody().getExpressionType() == EExpressionType.New;
    }

    /**
     * 获取应用于每个元素的投影函数
     *
     * @return 投影函数
     */
    public LambdaExpression getResultSelector() {
        if (this.resultSelector != null) return this.resultSelector;
        if (this.getIsNew()) {
            this.resultSelector = this.getResultView() == null ? null : this.getResultView().generateExpression(new ObjectReferencePack<>());
        } else
            this.resultSelector = this.getAtrophyPath().generateExpression(new ObjectReferencePack<>());
        return this.resultSelector;
    }

    /**
     * 获取投影结果类型
     *
     * @return 投影结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getResultSelector().getBody().getType();
    }

    /**
     * 获取投影结果视图，（仅适用于一般投影运算）
     *
     * @return 投影结果视图
     */
    public TypeView getResultView() {
        if (this.resultView != null) return this.resultView;
        if (!this.getIsNew()) return null;

        StructuralType modelType = this.model.getStructuralType(this.getResultType());
        TypeView modelTypeView = null;
        if (modelType instanceof TypeView) {
            TypeView typeView = (TypeView) modelType;
            if (typeView.getSource().getClrType() == this.getSourceType()) {
                modelTypeView = typeView;
            } else {
                Class<?> dirivedType = ImpliedTypeManager.getCurrent().applyType(this.getResultType(),
                        new IdentityArray(typeView.getSource().getFullName()), null);
                modelTypeView = this.model.getTypeView(dirivedType); //从模型中获取模型视图
            }
        }
        if (modelTypeView == null) {
            NewSelectionParser parser = new NewSelectionParser();
            this.resultView = parser.parse(this, this.getModel());
        } else {
            this.resultView = modelTypeView;
        }

        return this.resultView;
    }

    /**
     * 判定查询运算是否是异构的
     *
     * @return 是否是异构的
     */
    @Override
    protected final boolean isHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        boolean result;
        if (this.getIsNew()) {
            result = this.getResultView().getHeterogeneous(predicationProvider);
        } else {
            if (this.atrophyPath == null) result = super.isHeterogeneous(predicationProvider);
            else result = this.atrophyPath.getHeterogeneous(predicationProvider);
        }

        return result;
    }

    /**
     * 从查询运算中提取隐含包含
     *
     * @return 隐含包含
     */
    @Override
    protected final AssociationTree takeImpliedIncluding() {
        AssociationTree includingTree;
        if (this.getIsNew()) //一般投影
        {
            if (this.resultView == null) {
                includingTree = super.takeImpliedIncluding();
            } else {
                //创建一个代表视图源的关联树节点，作为包含树根节点；
                AssociationTreeNode
                        rootNode = new ObjectTypeNode((ObjectType) this.resultView.getSource(), null);
                //生成包含树
                includingTree = rootNode.asTree();
                //生长上诉含树，覆盖视图源扩展；
                includingTree.grow(this.resultView.getExtension());
                //对每一视图引用，在包含树中搜索其锚点，为该锚点添加一个代表视图引用绑定目标的子节点。
                for (ReferenceElement refEle : this.resultView.getReferenceElements()) {
                    ViewReference viewRef = (ViewReference) refEle;
                    //包含树中搜索锚点
                    AssociationTree node = includingTree.searchSub(viewRef.getAnchor());
                    if (node.getNode() instanceof ObjectTypeNode) {
                        ObjectTypeNode objectTypeNode = (ObjectTypeNode) node.getNode();
                        //为锚点添加一个代表视图引用绑定目标的子节点。
                        viewRef.getAnchor().addChild(objectTypeNode, node.getElementName());
                    }
                }
            }
        } else {
            if (this.atrophyPath != null)
                includingTree = this.atrophyPath.getAssociationPath().asTree();
            else
                includingTree = super.takeImpliedIncluding();
        }

        return includingTree;
    }

    /**
     * 获取参数
     *
     * @return 参数
     */
    @Override
    protected Expression[] gotArguments() {
        if (this.getResultSelector() == null)
            return new Expression[0];
        return new MemberExpressionExtractor(new SubTreeEvaluator(this.getResultSelector())).extractMember(this.getResultSelector()).stream().distinct().toArray(Expression[]::new);
    }
}
