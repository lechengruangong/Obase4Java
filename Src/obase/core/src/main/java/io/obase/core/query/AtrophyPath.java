/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：退化投影运算形成的退化路径.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:37:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.objectSys.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示执行退化投影运算形成的退化路径。
 * 退化投影操作可以形象地理解为在关联树中寻找一个节点，需要时继续在锚定于此节点的某一属性树上寻找一个节点。后续运算将以此节点为根构建一棵新包含树。
 */
public class AtrophyPath {

    /**
     * 在关联树上的退化路径
     */
    private final AssociationTreeNode associationPath;

    /**
     * 在属性树上的退化路径
     */
    private final AttributeTreeNode attributePath;

    /**
     * 分解结果暂存
     */
    private final Map<HeterogeneityPredicationProvider, DecomposeResult> resultDict = new HashMap<>();
    /**
     * 平展点
     */
    private List<AssociationTreeNode> flatteningPoints;

    /**
     * 创建AtrophyPath实例
     *
     * @param assocPath 关联退化路径
     * @param attrPath  属性退化路径
     */
    public AtrophyPath(AssociationTreeNode assocPath, AttributeTreeNode attrPath) {
        this.associationPath = assocPath;
        this.attributePath = attrPath;
    }

    /**
     * 创建AtrophyPath实例，同时指定平展点
     *
     * @param assocPath       关联退化路径
     * @param flatteningPoint 平展点
     * @param attrPath        属性退化路径
     */
    public AtrophyPath(AssociationTreeNode assocPath, AssociationTreeNode flatteningPoint,
                       AttributeTreeNode attrPath) {
        this(assocPath, attrPath);
        this.flatteningPoints = new ArrayList<>();
        this.flatteningPoints.add(flatteningPoint);
    }

    /**
     * 创建表示退化路径的AtrophyPath实例，该退化路径无关联退化
     *
     * @param attrPath 属性退化路径
     */
    public AtrophyPath(AttributeTreeNode attrPath) {
        this.attributePath = attrPath;
        this.associationPath = null;
    }

    /**
     * 根据表达式生成退化路径
     *
     * @param model         对象数据模型
     * @param selectionExp  投影表达式
     * @param flatteningExp 平展表达式
     * @return 退化路径
     */
    public static AtrophyPath fromExpression(ObjectDataModel model, LambdaExpression selectionExp, LambdaExpression flatteningExp) {
        List<ParameterBinding> paraBindings = new ArrayList<>();
        AssociationTree subTree = null;
        AtrophyPath atrophy;

        if (flatteningExp != null)
            paraBindings.add(new ParameterBinding(selectionExp.getParameters()[1], flatteningExp.getBody()));
        ObjectReferencePack<AssociationTreeNode> assocTail = new ObjectReferencePack<>();
        ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();
        //抽取关联树和属性树。
        AssociationTree assocTree = selectionExp.getBody().extractAssociation(model, assocTail,
                attrTail, paraBindings.toArray(new ParameterBinding[0]));
        if (flatteningExp != null)
            subTree = assocTree.searchSub(flatteningExp.getBody(), model);

        if (subTree != null)
            atrophy = new AtrophyPath(assocTail.realValue, subTree.getNode(), attrTail.realValue);
        else
            atrophy = new AtrophyPath(assocTail.realValue, attrTail.realValue);


        return atrophy;
    }

    /**
     * 生成表示退化路径的表达式
     *
     * @param flatteningExps 返回平展表达式（形如o=>o.Prop），无平展点返回null
     * @return 一个Lambda表达式，形如o=>o.Prop或(o, c) = c.Prop，其中形参c绑定于平展表达式
     */
    public LambdaExpression generateExpression(ObjectReferencePack<LambdaExpression[]> flatteningExps) {
        AssociationTree associationTree = this.associationPath.getRoot().asTree();
        AttributeTree attributeTree = this.attributePath != null ? this.attributePath.asTree() : null;

        Class<?> sourceType = this.associationPath.getRoot().getRepresentedType().getClrType();
        ParameterExpression sourcePara = Expression.parameter("", sourceType);

        Map<AssociationTreeNode, ParameterExpression> collectionParas = new HashMap<>();

        AssociationExpressionGenerator associationExpressionGenerator = new AssociationExpressionGenerator(sourcePara, collectionParas::get);
        //生成关联部分。
        LambdaExpression hostExp = associationTree.accept(associationExpressionGenerator);
        LambdaExpression resultExp;
        //生成属性部分并合并关联部分。
        if (attributeTree != null) {
            AttributeExpressionGenerator AttributeExpressionGenerator = new AttributeExpressionGenerator(hostExp);
            resultExp = attributeTree.accept(AttributeExpressionGenerator);
        } else {
            resultExp = hostExp;
        }

        List<ParameterExpression> parameters = new ArrayList<>();
        parameters.add(sourcePara);

        List<LambdaExpression> collectionSelectors = new ArrayList<>();
        //生成中介投影函数
        if (this.flatteningPoints != null) {
            for (AssociationTreeNode item : this.flatteningPoints) {
                ParameterExpression par = Expression.parameter("", item.getRepresentedType().getClrType());
                parameters.add(par);
                collectionParas.put(item, par);
                LambdaExpression collectionExp = item.asTree().accept(associationExpressionGenerator);
                ParameterExpression[] pars = new ParameterExpression[1];
                pars[0] = par;
                collectionSelectors.add(Expression.lambda(pars, collectionExp));
            }
        }

        flatteningExps.realValue = collectionSelectors.size() > 0 ? collectionSelectors.toArray(new LambdaExpression[0]) : null;
        return Expression.lambda(parameters.toArray(new ParameterExpression[0]), resultExp.getBody());
    }

    /**
     * 获取关联退化路径
     *
     * @return 关联退化路径
     */
    public AssociationTreeNode getAssociationPath() {
        return this.associationPath;
    }

    /**
     * 获取属性退化路径
     *
     * @return 属性退化路径
     */
    public AttributeTreeNode getAttributePath() {
        return this.attributePath;
    }

    /**
     * 获取平展点
     *
     * @return 平展点
     */
    public AssociationTreeNode[] getFlatteningPoints() {
        if (this.flatteningPoints == null)
            return new AssociationTreeNode[0];
        return this.flatteningPoints.toArray(new AssociationTreeNode[0]);
    }

    /**
     * 获取一个值，该值指示退化路径是否为异构的
     *
     * @return 退化路径是否为异构的
     */
    public Boolean getHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        if (predicationProvider == null)
            predicationProvider = new StorageHeterogeneityPredicationProvider();
        AssociationTreeHeterogeneityPredicater predicater = new AssociationTreeHeterogeneityPredicater(predicationProvider);
        return this.associationPath.getRoot().asTree().accept(predicater);
    }

    /**
     * 添加平展点
     *
     * @param point 一个关联树节点，退化路径在此节点处实施平展。
     */
    public void addFlatteningPoint(AssociationTreeNode point) {
        if (this.flatteningPoints == null) this.flatteningPoints = new ArrayList<>();
        this.flatteningPoints.add(point);
    }

    /**
     * 对退化路径实施极限分解
     *
     * @param attachingPath 返回附加路径
     * @param attachingNode 返回附加节点
     * @param attachingRef  返回附加引用
     * @return 分解得到的退化路径
     */
    public AtrophyPath decomposeExtremely(ObjectReferencePack<AtrophyPath> attachingPath, ObjectReferencePack<AssociationTreeNode> attachingNode,
                                          ObjectReferencePack<ReferenceElement> attachingRef, HeterogeneityPredicationProvider predicationProvider) {
        if (predicationProvider == null)
            predicationProvider = new StorageHeterogeneityPredicationProvider();

        if (this.resultDict.containsKey(predicationProvider)) {
            DecomposeResult result = this.resultDict.get(predicationProvider);
            attachingPath.realValue = result.AttachingPath;
            attachingNode.realValue = result.AttachingNode;
            attachingRef.realValue = result.AttachingRef;
            return result.BasePath;
        }
        AssociationTreeDecomposer decomposer = new AssociationTreeDecomposer(predicationProvider);
        decomposer.setArgument(true);
        //极限分解退化路径所代表的关联树
        AssociationTree tree = this.associationPath.getRoot().asTree();
        tree.accept(decomposer);
        AssociationTreeAttachingItem outArg = null;
        if (decomposer.getOutArgument() != null && decomposer.getOutArgument().length > 0) {
            outArg = decomposer.getOutArgument()[0];
        }

        attachingNode.realValue = outArg == null ? null : outArg.getAttachingNode();
        attachingRef.realValue = outArg == null ? null : outArg.getAttachingReference();
        attachingPath.realValue = new AtrophyPath(outArg == null ? null : this.getLastAssociationTreeNode(outArg.getAttachingTree()), this.attributePath);

        AtrophyPath basePath = new AtrophyPath(outArg == null ? null : outArg.getAttachingNode(), null);
        if (this.flatteningPoints != null) {
            FlatteningPointAdder pointAdder = new FlatteningPointAdder(this, basePath, attachingPath.realValue);
            tree.accept(pointAdder);
        }
        //暂存结果
        DecomposeResult temp = new DecomposeResult();
        temp.BasePath = basePath;
        temp.AttachingPath = attachingPath.realValue;
        temp.AttachingNode = attachingNode.realValue;
        temp.AttachingRef = attachingRef.realValue;
        this.resultDict.put(predicationProvider, temp);

        return basePath;
    }

    /**
     * 找到某关联树的末节点
     *
     * @param tree 关联树
     * @return 末节点
     */
    private AssociationTreeNode getLastAssociationTreeNode(AssociationTree tree) {
        AssociationTree current = tree;
        AssociationTreeNode result = current.getNode();

        while (current.getSubCount() > 0) {
            current = current.getSubTrees()[0];
            result = current.getNode();
        }

        return result;
    }


    /**
     * 分解结果
     */
    private static class DecomposeResult {
        /**
         * 基础路径
         */
        public AtrophyPath BasePath;

        /**
         * 附加陆军
         */
        public AtrophyPath AttachingPath;

        /**
         * 附加节点
         */
        public AssociationTreeNode AttachingNode;

        /**
         * 附加引用
         */
        public ReferenceElement AttachingRef;
    }
}
