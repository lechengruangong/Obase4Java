/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构查询分解器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:08:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.NewExpression;
import io.obase.core.odm.AssociationEnd;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.HeterogeneityPredicationProvider;
import io.obase.core.odm.objectSys.ObjectTypeNode;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.core.query.*;
import io.obase.core.query.typeView.ITypeViewBuilder;
import io.obase.core.query.typeView.NewExpressionBasedBuilder;

/**
 * 异构查询分解器。
 */
public class HeterogQueryDecomposer extends QueryOpVisitorWithArgs<AssociationTree, HeterogQuerySegments> {

    /**
     * 断言器
     */
    private final HeterogeneityPredicationProvider heterogeneityPredicationProvider;
    /**
     * 参数
     */
    private AssociationTree argument;
    /**
     * 主查询链分离出异构链后剩余的部分，称为补充链
     */
    private QueryOp complement;
    /**
     * 包含树，以当前访问节点为基点
     */
    private AssociationTree including;
    /**
     * 异构运算，主查询链从该运算节点处离断，前半段（含该节点）为异构查询，后半段为后续查询。
     */
    private QueryOp mainTail;
    /**
     * 从主查询中分离出的异构查询，称为主体链
     */
    private QueryOp mainQuery;
    /**
     * 前一步的包含树
     */
    private AssociationTree previousIncluding;
    /**
     * 是否支持
     */
    private boolean supported = true;

    /**
     * 初始化分解器
     */
    public HeterogQueryDecomposer(HeterogeneityPredicationProvider heterogeneityPredicationProvider) {
        this.heterogeneityPredicationProvider = heterogeneityPredicationProvider;
        this.specifyQueryOp();
    }

    /**
     * 执行通用后置访问逻辑
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问前一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     * @return 是否继续访问
     */
    @Override
    protected boolean postVisitGenerally(QueryOp queryOp, Object previousState, Object preVisitState) {
        if (!this.supported)
            this.supported = true;

        //包含运算 直接返回
        if (queryOp.getName() == EQueryOpName.Include) return false;

        QueryOp currentNode;
        if (this.mainQuery != null) {
            currentNode = queryOp.clone(this.mainQuery);
        } else {
            if (queryOp instanceof SelectOp) {
                SelectOp selectOp = (SelectOp) queryOp;
                if (selectOp.getIsNew())
                    currentNode = queryOp.clone(queryOp.getNext());
                else
                    currentNode = queryOp.clone(null);
            } else
                currentNode = queryOp.clone(null);
        }


        //赋值
        this.mainQuery = currentNode;
        if (this.mainTail != null) return false;

        this.mainTail = currentNode;
        return false;
    }

    /**
     * 执行通用前置访问逻辑
     *
     * @param queryOp          要访问的查询运算
     * @param previousState    访问前一运算时产生的状态数据
     * @param outPreviousState 返回一个状态数据，在遍历到下一运算时该数据将被视为前序状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    protected boolean preVisitGenerally(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState, ObjectReferencePack<Object> outPreVisitState) {
        outPreviousState.realValue = null;
        outPreVisitState.realValue = null;

        if (queryOp.getName() == EQueryOpName.Include) {
            this.previousIncluding = this.including;
            return true;
        }

        if (!queryOp.getHeterogeneous(this.heterogeneityPredicationProvider)) {
            this.previousIncluding = this.including;
            return true;
        }

        this.supported = queryOp.getName() == EQueryOpName.Select || queryOp.getName() == EQueryOpName.Where ||
                queryOp.getName() == EQueryOpName.Group;

        if (queryOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) queryOp;
            if (selectOp.getIsNew())
                return false;
        }
        if (this.supported) {
            outPreviousState = new ObjectReferencePack<>();
            outPreviousState.realValue = queryOp;
            this.complement = queryOp.clone(queryOp);
        } else {
            this.complement = queryOp.clone(((ObjectReferencePack<QueryOp>) previousState).realValue);
        }

        //合并
        AssociationTree chain = queryOp.getChainIncluding();
        AssociationTree.combine(chain, this.supported ? this.including : this.previousIncluding);

        return false;
    }

    /**
     * 获取访问操作参数
     *
     * @return 访问操作参数
     */
    @Override
    public AssociationTree getArgument() {
        return this.argument;
    }

    /**
     * 设置访问操作参数
     *
     * @param associationTree 访问操作参数
     */
    @Override
    public void setArgument(AssociationTree associationTree) {
        this.argument = associationTree;
    }

    /**
     * 获取访问操作的结果
     *
     * @return 获取访问操作的结果
     */
    @Override
    public HeterogQuerySegments getResult() {
        //创建分解得到的片段
        HeterogQuerySegments segments = new HeterogQuerySegments();
        segments.MainQuery = this.mainQuery;
        segments.Complement = this.complement;
        segments.Including = this.supported ? this.including : this.previousIncluding;
        segments.MainTail = this.mainTail;
        this.mainTail = null;
        this.mainQuery = null;
        this.complement = null;
        this.including = null;
        this.supported = true;
        return segments;
    }

    /**
     * 针对特定操作进行额外的特殊处理
     */
    private void specifyQueryOp() {
        //1.生成包含树，是指生成当前包含树（_including），让它覆盖Include运算指定的包含树（可通过IncludOp类获取）。
        //2. “裁剪包含树”的返回值替换当前包含树。
        //3.裁剪包含树是指裁剪当前包含树（_including）。
        //4.抽取关联链，是指获取所述成员表达式指向的关联树节点，即MemberExtension.ExtractAssciation方法；裁剪包含树是指裁剪当前包含树，即CutIncluding(_including, 关联链)。
        //5.置空包含树是指，_including = null。

        //1.Include
        //生长包含树。
        //Inculde操作的具体委托

        IPreVisit includePreVisit = (queryOp, previousState, outPreviousState, outPreVisitState) -> {
            if (queryOp instanceof IncludeOp) {
                IncludeOp includeOp = (IncludeOp) queryOp;

                //生长包含树
                this.including = this.including == null
                        ? includeOp.getIncludingTree()
                        : this.including.grow(includeOp.getIncludingTree());
            }

            outPreviousState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        };
        //加入指定的操作
        this.specify(EQueryOpName.Include, includePreVisit, this::predicate);

        //2.Select
        //裁剪包含树，根据投影运算的不同状态选择CutIncluding方法的不同重载。
        //Select操作的具体委托
        IPreVisit selectPreVisit = (queryOp, previousState, outPreviousState, outPreVisitState) -> {

            if (queryOp instanceof SelectOp) {
                SelectOp selectOp = (SelectOp) queryOp;
                if (selectOp.getResultView() != null) {
                    //裁剪包含树
                    this.including = this.cutIncluding(this.including, selectOp.getResultView());
                } else {
                    ObjectType objectType = queryOp.getModel().getObjectType(selectOp.getResultType());
                    if (objectType != null) {
                        //裁剪包含树
                        AssociationTreeNode path = selectOp.getAtrophyPath() == null ? new ObjectTypeNode(objectType, null) : selectOp.getAtrophyPath().getAssociationPath();
                        this.including = this.cutIncluding(this.including, path);

                        if (this.including != null) {
                            //切下来的首个是关联端 取子树
                            if (this.including.getElement() instanceof AssociationEnd) {
                                this.including = this.including.getSubTrees()[0];
                            }
                            //自己投自己 置空
                            if (selectOp.getResultType().equals(selectOp.getSourceType()))
                                this.including = null;
                        }
                    }
                }
            }

            outPreviousState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        };

        //加入指定的操作
        this.specify(EQueryOpName.Select, selectPreVisit, this::predicate);

        //3.Group
        //如果是普通分组且组元素函数不为空，且组元素函数为成员表达式且指向对象类型（ObjectType），将该成员表达式作为退化路径裁剪包含树。
        //否则，置空包含树。
        //Group操作的具体委托
        IPreVisit groupPreVisit = (queryOp, previousState, outPreviousState, outPreVisitState) -> {

            if (queryOp instanceof GroupOp) {
                GroupOp groupOp = (GroupOp) queryOp;
                if (groupOp.getKeySelector().getBody() instanceof MemberExpression) {
                    MemberExpression memberExpression = (MemberExpression) groupOp.getKeySelector().getBody();
                    ObjectType objectType = queryOp.getModel().getObjectType(memberExpression.getType());
                    if (objectType != null) {
                        //裁剪包含树
                        ObjectTypeNode path = new ObjectTypeNode(objectType, null);
                        this.including = this.cutIncluding(this.including, path);
                    } else {
                        //置空包含树
                        this.including = null;
                    }
                } else {
                    //置空包含树
                    this.including = null;
                }
            }

            outPreviousState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        };
        //加入指定的操作
        this.specify(EQueryOpName.Group, groupPreVisit, this::predicate);

        //4.Count、ArithAggregate
        //置空包含树。
        //6.Join、Zip
        //置空包含树。
        //Count、ArithAggregate、Join、Zip操作的具体委托
        IPreVisit countOrArithAggregatePreVisit = (queryOp, previousState, outPreviousState, outPreVisitState) -> {

            if (queryOp instanceof CountOp)
                //置空包含树
                this.including = null;

            if (queryOp instanceof ArithAggregateOp)
                //置空包含树
                this.including = null;

            if (queryOp instanceof JoinOp)
                //置空包含树
                this.including = null;

            if (queryOp instanceof ZipOp)
                //置空包含树
                this.including = null;

            outPreviousState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        };


        //加入指定的操作
        this.specify(EQueryOpName.Count, countOrArithAggregatePreVisit, this::predicate);
        this.specify(EQueryOpName.ArithAggregate, countOrArithAggregatePreVisit, this::predicate);
        this.specify(EQueryOpName.Join, countOrArithAggregatePreVisit, this::predicate);
        this.specify(EQueryOpName.Zip, countOrArithAggregatePreVisit, this::predicate);

        //5.Accumulate
        //如果Seed不为空且SeedType与查询源类型不相同，置空包含树；否则，当ResultSelector不为空时，检查其Body：
        //（1）如果Body为NewExpression或MemberInitExpression，将其作为视图表达式生成视图，根据此视图裁剪包含树；
        //（2）如果为成员表达式且返回对象类型（序列），抽取关联链，据此关联链裁剪包含树；
        //（3）其它情况下，置空包含树。
        //Accumulate的具体委托

        IPreVisit accumulatePreVisit = (queryOp, previousState, outPreviousState, outPreVisitState) -> {

            if (queryOp instanceof AccumulateOp) {
                AccumulateOp accumulateOp = (AccumulateOp) queryOp;

                if (accumulateOp.getSeed() != null && accumulateOp.getSeed() != accumulateOp.getSourceType()) {
                    //置空包含树
                    this.including = null;
                } else if (accumulateOp.getResultSelector() != null) {
                    Expression body = accumulateOp.getResultSelector().getBody();
                    if (body instanceof NewExpression) {
                        ITypeViewBuilder builder = new NewExpressionBasedBuilder();

                        TypeView typeView = builder.build(body, queryOp.getModel().getStructuralType(body.getType()), queryOp.getModel(),
                                accumulateOp.getResultSelector().getParameters()[0]);
                        //裁剪包含树
                        this.including = this.cutIncluding(accumulateOp.getImpliedIncluding(), typeView);
                    } else if (body instanceof MemberExpression) {
                        MemberExpression memberExpression = (MemberExpression) body;
                        if (memberExpression.getType() == accumulateOp.getResultType()) {
                            //裁剪包含树
                            AssociationTree tree = memberExpression.extractAssociation(queryOp.getModel(), null);
                            this.including = this.cutIncluding(accumulateOp.getImpliedIncluding(), tree.getNode());
                        }

                    } else {
                        //置空包含树
                        this.including = null;
                    }
                }
            }

            outPreviousState.realValue = null;
            outPreVisitState.realValue = null;
            return false;
        };
        //加入指定的操作
        this.specify(EQueryOpName.Accumulate, accumulatePreVisit, this::predicate);
    }

    /**
     * 针对特定几种操作的断言函数
     *
     * @param op 查询运算
     * @return 逻辑断言
     */
    private ESpecialPredicate predicate(QueryOp op) {
        switch (op.getName()) {
            case Include:
                return ESpecialPredicate.PostExecute;
            case Select:
            case Group:
            case Count:
            case ArithAggregate:
            case Accumulate:
            case Join:
            case Zip: {
                //这些操作不是异构的
                if (!op.getHeterogeneous(this.heterogeneityPredicationProvider)) return ESpecialPredicate.PostExecute;
                return ESpecialPredicate.False;
            }
            default:
                return ESpecialPredicate.False;
        }
    }

    /**
     * 根据退化投影路径裁剪包含树
     *
     * @param including  要裁剪的包含树
     * @param assoResult 退化路径
     * @return 裁剪后得到的新包含树
     */
    private AssociationTree cutIncluding(AssociationTree including, AssociationTreeNode assoResult) {
        return including == null ? null : including.searchSub(assoResult);
    }

    /**
     * 根据一般投影结果视图裁剪包含树
     *
     * @param including 要裁剪的包含树
     * @param typeView  投影结果视图
     * @return 裁剪后的新包含树
     */
    private AssociationTree cutIncluding(AssociationTree including, TypeView typeView) {
        AssociationTree newIncluding = new AssociationTree(typeView);

        ReferenceElement[] elements = typeView.getReferenceElements();

        //裁剪包含树
        for (ReferenceElement referenceElement : elements) {
            if (referenceElement instanceof ViewReference) {
                ViewReference viewReference = (ViewReference) referenceElement;
                AssociationTree anchorTree = including.searchSub(viewReference.getAnchor());
                if (anchorTree != null) {
                    AssociationTree bindingTree = newIncluding.removeSub(viewReference.getBinding().getName());
                    if (bindingTree != null) {
                        newIncluding.addSubTree(bindingTree, viewReference.getName());
                    }
                }

                newIncluding.grow(viewReference.getName());
            }
        }

        return newIncluding;
    }
}
