/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关系运算管道构建器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 15:50:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.Expression;
import io.obase.core.expression.*;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.Parameter;
import io.obase.core.odm.objectSys.*;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.core.query.*;
import io.obase.core.query.oop.OopExecutor;
import io.obase.core.query.typeView.*;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.common.SqlUtils;
import io.obase.providers.sql.sqlobject.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 关系运算管道构建器
 */
public class RopPipelineBuilder extends QueryOpVisitorWithOutArgs<OpExecutorWithContext<RopContext>, QueryOp> {

    /**
     * 缓存Sql表达式的字典
     */
    private static final Map<Expression, io.obase.providers.sql.sqlobject.Expression> typeViewExpressionDict = new HashMap<>();

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * ROP管道所使用的数据源
     */
    private final EDataSource targetSource;

    /**
     * 寄存器(寄存补充运算管道，避免重复生成。)
     */
    private OopExecutor complement;

    /**
     * 构造RopPipelineBuilder的新实例
     *
     * @param model        对象数据模型
     * @param targetSource 数据源
     */
    public RopPipelineBuilder(ObjectDataModel model, EDataSource targetSource) {

        this.model = model;
        this.targetSource = targetSource;

        this.specificPrev();
        this.specificPost();
    }

    /**
     * 获取补充运算管道
     *
     * @return 补充运算管道
     */
    public OopExecutor getComplement() {
        if (this.complement == null) {
            ComplementaryPipelineBuilder complementaryPipelineBuilder = new ComplementaryPipelineBuilder();
            this.complement = complementaryPipelineBuilder.getPipeline();
        }
        return this.complement;

    }

    /**
     * 获取构建出来的关系运算管道
     *
     * @return 关系运算管道
     */
    public OpExecutorWithContext<RopContext> getPipeline() {
        if (this.result == null) {
            this.result = new RopTerminator(this.outArgument, null);
        }
        return this.result;
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
        return true;
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
        outPreviousState.realValue = outPreVisitState.realValue = null;
        if (this.outArgument == null)
            this.outArgument = queryOp;
        this.result = new RopTerminator(queryOp, null);
        return false;
    }

    /**
     * 添加特定前置访问
     */
    private void specificPrev() {
        this.specify(EQueryOpName.All, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Any, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.ArithAggregate, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Contains, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Count, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Distinct, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.ElementAt, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Set, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.First, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Group, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Include, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Last, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Order, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Reverse, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Select, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Single, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Skip, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Take, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Where, this::specificPreVisitDelegate, this::predicatePre);
        this.specify(EQueryOpName.Non, this::specificPreVisitDelegate, this::predicatePre);
    }

    /**
     * 添加特定后置访问
     */
    private void specificPost() {
        this.specify(EQueryOpName.Non, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Where, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Take, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Skip, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Single, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Select, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Reverse, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Order, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Last, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Include, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Group, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.First, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Set, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.ElementAt, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Distinct, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Count, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Contains, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.ArithAggregate, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.Any, this::specificPostVisitDelegate, this::predicatePos);
        this.specify(EQueryOpName.All, this::specificPostVisitDelegate, this::predicatePos);
    }

    /**
     * 特定于前置访问的委托
     *
     * @param queryOp          查询运算
     * @param previousState    前置状态数据
     * @param outPreviousState 前置状态数据输出
     * @param outPreVisitState 前置访问数据
     * @return 是否继续访问
     */
    private boolean specificPreVisitDelegate(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState, ObjectReferencePack<Object> outPreVisitState) {
        outPreviousState.realValue = outPreVisitState.realValue = null;
        try {
            outPreVisitState.realValue = this.analyzeParameterExpression(queryOp);
            QueryOp next = queryOp.getNext();
            FunctionWithOneArg<QueryOp, QueryOp> complement;
            ObjectReferencePack<FunctionWithOneArg<QueryOp, QueryOp>> pack = new ObjectReferencePack<>();

            boolean needComplement;
            if (queryOp instanceof GroupAggregationOp && outPreVisitState.realValue == null) {
                needComplement = true;
                complement = p -> queryOp;
            } else if (queryOp instanceof WhereOp && outPreVisitState.realValue == null) {
                needComplement = true;
                if (this.outArgument != null)
                    complement = op1 -> this.outArgument;
                else
                    complement = op1 -> queryOp;
            } else {
                needComplement = this.needComplement(queryOp, pack);
                complement = pack.realValue;
            }

            if (!needComplement) {
                this.outArgument = null;
                if (next != null) {
                    return true;
                } else {
                    this.result = new RopTerminator(queryOp, null);
                    return false;
                }
            } else {
                this.outArgument = complement.invoke(next);
                this.result = new RopTerminator(queryOp, null);
            }

            return true;
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception ex) {
            this.outArgument = queryOp;
            this.result = new RopTerminator(queryOp, null);
            return false;
        }
    }

    /**
     * 后置访问逻辑的委托
     *
     * @param queryOp       查询运算
     * @param previousState 前置数据
     * @param preVisitState 前置访问数据
     */
    private void specificPostVisitDelegate(QueryOp queryOp, Object previousState, Object preVisitState) {
        this.result = this.createRopExecutorInstance(queryOp, preVisitState, this.result);
    }

    /**
     * 启用特定前置访问逻辑
     *
     * @param queryOp 查询运算
     * @return 访问逻辑
     */
    private ESpecialPredicate predicatePre(QueryOp queryOp) {
        return ESpecialPredicate.PostExecute;
    }

    /**
     * 启用特定后置访问逻辑
     *
     * @param queryOp 查询运算
     * @return 访问逻辑
     */
    private ESpecialPredicate predicatePos(QueryOp queryOp) {
        return ESpecialPredicate.PreExecute;
    }

    /**
     * 是否需要补充运算
     *
     * @param queryOp        查询运算
     * @param complementFunc 返回添加补充运算委托
     * @return 是否需要补充运算
     */
    private boolean needComplement(QueryOp queryOp, ObjectReferencePack<FunctionWithOneArg<QueryOp, QueryOp>> complementFunc) {
        boolean needComplement = false;
        FunctionWithOneArg<QueryOp, QueryOp> realFunc = complementFunc.realValue;
        switch (queryOp.getName()) {
            case All:
            case Any:
            case Contains:
            case Single:
                needComplement = true;
                realFunc = next -> new DetectionComplementaryOp(queryOp, this.model);
                break;
            case ElementAt:
                needComplement = true;
                realFunc = next -> new IndexComplementaryOp(queryOp, this.model);
                break;
            case First:
            case Last:
                needComplement = true;
                realFunc = next -> new FilteringComplementaryOp(queryOp, this.model);
                break;
            case Select:
                SelectOp selectOp = (SelectOp) queryOp;
                if (selectOp.getResultType() != String.class && Iterable.class.isAssignableFrom(selectOp.getResultType())) {
                    needComplement = true;
                    realFunc = next -> {
                        MultipleSelectionParser parser = new MultipleSelectionParser();
                        TypeView typeView = parser.parse(queryOp, this.model);

                        Class<?> typeViewType = typeView.getClrType();
                        //构造 key/element属性名称，和视图生成是属性生成规则一致
                        String keyAttr = typeView.getViewReferences()[0].getName();
                        if (keyAttr.startsWith("_")) {
                            keyAttr = keyAttr.substring(1);
                            keyAttr = keyAttr.replace(String.valueOf(keyAttr.toCharArray()[0]), String.valueOf(keyAttr.toCharArray()[0] -= 32));
                        }
                        //resultSelector表达式的参数
                        ParameterExpression sp = Expression.parameter("s", typeViewType);
                        try {
                            LambdaExpression keyAccess = Expression.lambda(new ParameterExpression[]{sp}, Expression.member(sp, typeViewType.getMethod("get" + keyAttr), sp, sp.getType()));
                            return QueryOp.select(keyAccess, this.model, next);
                        } catch (NoSuchMethodException e) {
                            throw new IllegalArgumentException("构造" + queryOp.getName() + "Op错误" + e.getMessage(), e);
                        }
                    };
                }
                break;
            case Group:
                GroupOp groupOp = (GroupOp) queryOp;
                if (!(groupOp instanceof GroupAggregationOp)) {

                    needComplement = true;
                    realFunc = next -> {
                        if (groupOp.getElementSelector() != null) {
                            GroupingParser parser = new GroupingParser();
                            TypeView typeView = parser.parse(groupOp, this.model);
                            Class<?> typeViewType = typeView.getClrType();

                            String keyAttr; //视图绑定到KeySelector的属性 的字段名称
                            String refOrEleAttr; //视图绑定到ElementSeletor的属性或引用 的字段名称

                            List<Attribute> attrs = typeView.getAttributes();
                            ViewReference[] refs = typeView.getViewReferences();

                            if (refs != null && refs.length > 0) {//表示typeView有一个属性和一个关联引用
                                keyAttr = attrs.get(0).getName();
                                refOrEleAttr = refs[0].getName();
                            } else {//表示typeView两个都是属性
                                Optional<Attribute> keyStr = attrs.stream().filter(p -> Objects.equals(((ViewAttribute) p).getBinding().toString(), groupOp.getKeySelector().getBody().toString())).findFirst();
                                if (keyStr.isPresent()) {
                                    keyAttr = keyStr.get().getName();
                                    if (keyAttr.startsWith("_")) {
                                        keyAttr = keyAttr.substring(1);
                                        keyAttr = keyAttr.replace(String.valueOf(keyAttr.toCharArray()[0]), StringUtils.capitalize(String.valueOf(keyAttr.toCharArray()[0])));
                                    }

                                } else {
                                    keyAttr = "";
                                }

                                Attribute refStr = attrs.stream().filter(p -> Objects.equals(((ViewAttribute) p).getBinding().toString(), groupOp.getElementSelector().getBody().toString())).findFirst().orElse(null);
                                if (refStr != null) {
                                    refOrEleAttr = refStr.getName();
                                    if (refOrEleAttr.startsWith("_")) {
                                        refOrEleAttr = refOrEleAttr.substring(1);
                                        refOrEleAttr = refOrEleAttr.replace(String.valueOf(refOrEleAttr.toCharArray()[0]), StringUtils.capitalize(String.valueOf(refOrEleAttr.toCharArray()[0])));
                                    }


                                } else {
                                    refOrEleAttr = "";
                                }

                            }

                            //keySelector和elementSelector 表达式的参数
                            ParameterExpression sp = Expression.parameter("s", typeViewType);

                            try {
                                LambdaExpression newKeySelector = Expression.lambda(new ParameterExpression[]{sp}, Expression.member(sp, typeViewType.getMethod("get" + keyAttr), sp, sp.getType()));
                                LambdaExpression newElementSelector = Expression.lambda(new ParameterExpression[]{sp}, Expression.member(sp, typeViewType.getMethod("get" + refOrEleAttr), sp, sp.getType()));

                                return QueryOp.groupBy(newKeySelector, newElementSelector, groupOp.getComparator(), this.model, next);
                            } catch (NoSuchMethodException e) {
                                throw new IllegalArgumentException("构造" + queryOp.getName() + "Op错误" + e.getMessage(), e);
                            }
                        } else
                            return groupOp;
                    };
                }
                break;
            default:
                break;
        }
        complementFunc.realValue = realFunc;
        return needComplement;
    }

    /**
     * 解析参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyzeParameterExpression(QueryOp op) {
        switch (op.getName()) {
            case All:
                return this.analyze((AllOp) op);
            case Any:
                return this.analyze((AnyOp) op);
            case ArithAggregate:
                return this.analyze((ArithAggregateOp) op);
            case Contains:
                return this.analyze((ContainsOp) op);
            case Count:
                return this.analyze((CountOp) op);
            case Distinct:
                return this.analyze((DistinctOp) op);
            case ElementAt:
                return this.analyze((ElementAtOp) op);
            case Set:
                return this.analyze((SetOp) op);
            case First:
                return this.analyze((FirstOp) op);
            case Group:
                return this.analyze((GroupOp) op);
            case Include:
                return this.analyze((IncludeOp) op);
            case Last:
                return this.analyze((LastOp) op);
            case Order:
                return this.analyze((OrderOp) op);
            case Reverse:
                return this.analyze((ReverseOp) op);
            case Select:
                return this.analyze((SelectOp) op);
            case Single:
                return this.analyze((SingleOp) op);
            case Skip:
                return this.analyze((SkipOp) op);
            case Take:
                return this.analyze((TakeOp) op);
            case Where:
                return this.analyze((WhereOp) op);
            case Non:
                return this.analyze((EveryOp) op);
            default:
                throw new IllegalArgumentException("不支持的查询运算: " + op.getName());
        }
    }

    /**
     * 创建关系运算执行器实例
     *
     * @param op            查询运算
     * @param preVisitState 前置数据
     * @param next          运算器的下一节
     * @return Rop管道
     */
    private OpExecutorWithContext<RopContext> createRopExecutorInstance(QueryOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        switch (op.getName()) {
            case All:
                return this.create((AllOp) op, preVisitState, next);
            case Any:
                return this.create((AnyOp) op, preVisitState, next);
            case ArithAggregate:
                return this.create((ArithAggregateOp) op, preVisitState, next);
            case Contains:
                return this.create((ContainsOp) op, preVisitState, next);
            case Count:
                return this.create((CountOp) op, preVisitState, next);
            case Distinct:
                return this.create((DistinctOp) op, preVisitState, next);
            case ElementAt:
                return this.create((ElementAtOp) op, preVisitState, next);
            case Set:
                return this.create((SetOp) op, preVisitState, next);
            case First:
                return this.create((FirstOp) op, preVisitState, next);
            case Group:
                return this.create((GroupOp) op, preVisitState, next);
            case Include:
                return this.create((IncludeOp) op, preVisitState, next);
            case Last:
                return this.create((LastOp) op, preVisitState, next);
            case Order:
                return this.create((OrderOp) op, preVisitState, next);
            case Reverse:
                return this.create((ReverseOp) op, preVisitState, next);
            case Select:
                return this.create((SelectOp) op, preVisitState, next);
            case Single:
                return this.create((SingleOp) op, preVisitState, next);
            case Skip:
                return this.create((SkipOp) op, preVisitState, next);
            case Take:
                return this.create((TakeOp) op, preVisitState, next);
            case Where:
                return this.create((WhereOp) op, preVisitState, next);
            case Non:
                return this.create((EveryOp) op, preVisitState, next);
            default:
                throw new IllegalArgumentException("不支持的查询运算: " + op.getName());
        }
    }

    /**
     * 解析NonQueryOp参数中的表达式
     *
     * @param op 查询运算
     * @return 空
     */
    private Object analyze(EveryOp op) {
        return null;
    }

    /**
     * 创建NonQueryOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(EveryOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return next;
    }

    /**
     * 解析WhereOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(WhereOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建WhereOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(WhereOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        OpExecutorWithContext<RopContext> executor = next;
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析TakeOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(TakeOp op) {
        return null;
    }

    /**
     * 创建TakeOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(TakeOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return new TakeExecutor(op, op.getCount(), next);
    }

    /**
     * 解析SkipOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(SkipOp op) {
        return null;
    }

    /**
     * 创建SkipOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(SkipOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return new SkipExecutor(op, op.getCount(), next);
    }

    /**
     * 解析SingleOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(SingleOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建SingleOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(SingleOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        OpExecutorWithContext<RopContext> executor = next;
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析SelectOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(SelectOp op) {
        // 若满足以下任一条件，断言不适用关系运算：
        // （1）为CollectionSelectionOp且IndexRefferred == true且IsNew==true；
        // （2）IsNew==true，且投影目标视图的某一成员绑定到New或MemberInit表达式
        // 若满足以下任一条件，从运算参数中解析出视图（调用SelectOp.ResultView）：
        // （1）ResultType是IEnumerable，且从投影表达式中抽取的关联树有子节点；
        // （2）IsNew==true。

        TypeView tv = null;
        Map<String, io.obase.providers.sql.sqlobject.Expression> dic = null;
        ISelectionSet set = null;
        AssociationTreeNode associationTree = null;
        AttributeTreeNode attributeTree = null;
        LambdaExpression collectionSelector = null;

        if (op.getIsNew())
            tv = op.getResultView();

        if (op.getResultType() != String.class && Iterable.class.isAssignableFrom(op.getResultType())) {
            AssociationTree assocTree = op.getResultSelector().getBody().extractAssociation(this.model, null);
            if (assocTree != null && assocTree.getSubCount() > 0) {
                MultipleSelectionParser parser = new MultipleSelectionParser();
                tv = parser.parse(op, this.model);
            }
        }

        List<ParameterBinding> bindings = new ArrayList<>();
        if (op instanceof CollectionSelectOp) {
            CollectionSelectOp collectionSelectOp = (CollectionSelectOp) op;
            collectionSelector = collectionSelectOp.getCollectionSelector();

            if (collectionSelector.getParameters().length == 2) {
                bindings.add(new ParameterBinding(collectionSelector.getParameters()[1], EParameterReferring.Index, null));
            }
            if (collectionSelectOp.getResultSelector().getParameters().length >= 2) {
                ParameterExpression parameter = collectionSelectOp.getResultSelector().getParameters()[1];
                bindings.add(new ParameterBinding(parameter, collectionSelector));
            }
        }

        if (tv != null) {
            dic = this.translateTypeView(tv);
        } else {
            LambdaExpression keyExp = op.getResultSelector();
            SubTreeEvaluator tree = new SubTreeEvaluator(keyExp);
            SelectionExpressionParser parser = new SelectionExpressionParser(this.model, tree, false, bindings.toArray(new ParameterBinding[0]));
            ObjectReferencePack<AssociationTreeNode> assoResult = new ObjectReferencePack<>();
            ObjectReferencePack<AttributeTreeNode> attrResult = new ObjectReferencePack<>();
            set = parser.parse(keyExp, assoResult, attrResult);
            associationTree = assoResult.realValue;
            attributeTree = attrResult.realValue;
        }


        return new Object[]{tv, dic, set, associationTree, attributeTree, collectionSelector};
    }

    /**
     * 创建SelectOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(SelectOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        // 若满足以下任一条件，断言不适用关系运算：
        // （1）为CollectionSelectionOp且IndexRefferred == true且IsNew==true；
        // （2）IsNew==true，且投影目标视图的某一成员绑定到New或MemberInit表达式
        // 若满足以下任一条件，从运算参数中解析出视图：
        // （1）ResultType是IEnumerable，且从投影表达式中抽取的关联树有子节点；
        // （2）IsNew==true。
        // 如果解析了视图生成SelectExecutor，否则生成AtrophyExecutor。
        // 如果ResultType是IEnumerable，补充一个退化投影的对象运算。

        OpExecutorWithContext<RopContext> executor = next;
        if (!(preVisitState instanceof Object[])) return executor;

        Object[] unboxed = (Object[]) preVisitState;

        TypeView typeView = (TypeView) unboxed[0];
        Map<String, io.obase.providers.sql.sqlobject.Expression> dic = (HashMap<String, io.obase.providers.sql.sqlobject.Expression>) unboxed[1];
        ISelectionSet set = (ISelectionSet) unboxed[2];
        AssociationTreeNode associationTree = (AssociationTreeNode) unboxed[3];
        AttributeTreeNode attributeTree = (AttributeTreeNode) unboxed[4];
        LambdaExpression collectionSelector = (LambdaExpression) unboxed[5];
        if (typeView != null)
            executor = new SelectExecutor(op, typeView, dic, executor);
        else
            executor = new AtrophySelectExecutor(op, op.getResultSelector(), collectionSelector, set, associationTree, attributeTree, null, executor);
        return executor;
    }

    /**
     * 解析ReverseOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(ReverseOp op) {
        return null;
    }

    /**
     * 创建ReverseOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(ReverseOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return new ReverseExecutor(op, next);
    }

    /**
     * 解析OrderOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(OrderOp op) {
        return this.translate(op.getKeySelector());
    }

    /**
     * 创建OrderOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(OrderOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        io.obase.providers.sql.sqlobject.Expression exp = (io.obase.providers.sql.sqlobject.Expression) preVisitState;
        return new OrderExecutor(op, op.getKeySelector(), exp, op.getDescending(), op.getClearPrevious(), next);
    }

    /**
     * 解析LastOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(LastOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建LastOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(LastOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        //分解为：Where(条件) >>反序运算 >> 提取运算(1) >> 补充运算。
        OpExecutorWithContext<RopContext> executor = next;
        executor = new TakeExecutor(op, 1, executor);
        executor = new ReverseExecutor(op, executor);

        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析IncludeOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(IncludeOp op) {
        return null;
    }

    /**
     * 创建IncludeOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(IncludeOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return new IncludeExecutor(op, op.getSelectors()[0], null, op.getSourceType(), next);
    }

    /**
     * 解析GroupOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(GroupOp op) {
        // 若满足以下任一条件，从运算参数中解析出视图（使用视图查询解析器）：
        // （1）为普通分组运算；
        // （2）为分组聚合运算且IsNew==true。
        TypeView typeView = null;
        Map<String, io.obase.providers.sql.sqlobject.Expression> dic = null;
        ISelectionSet set = null;
        AssociationTreeNode associationTree = null;
        AttributeTreeNode attributeTree = null;

        if (op instanceof GroupAggregationOp) {
            GroupAggregationOp groupAggregationOp = (GroupAggregationOp) op;
            if (groupAggregationOp.getIsNew()) {
                try {
                    GroupingAggregationParser parser = new GroupingAggregationParser();
                    typeView = parser.parse(op, this.model);
                    dic = this.translateTypeView(typeView);
                } catch (Exception ex) {
                    //发生异常 一般为无法翻译的Sql函数
                    return null;
                }
            }

        } else {
            ViewQueryParserFactory viewQueryParserFactory = new ViewQueryParserFactory();
            ViewQueryParser parser = viewQueryParserFactory.create(op);
            if (parser != null) {
                typeView = parser.parse(op, this.model);
                dic = this.translateTypeView(typeView);
            } else {
                //null 表示此分组运算不适用于关系运算
                return null;
            }

        }

        return new Object[]{typeView, dic, set, associationTree, attributeTree};
    }

    /**
     * 创建GroupOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(GroupOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        // 若满足以下任一条件，从运算参数中解析出视图：
        // （1）为普通分组运算；
        // （2）为分组聚合运算且IsNew==true。
        // 如果为普通分组运算，生成SelectExecutor并补充一个执行分组操作的对象运算；
        // 如果为分组聚合运算，首先生成GroupAggregationExecutor，然后再判断：如果IsNew==true生成SelectExecutor，否则生成AtrophyExecutor。
        OpExecutorWithContext<RopContext> executor = next;
        //前置运算后为空 表示不适用关系运算
        if (preVisitState == null) {
            return next;
        }
        Object[] unboxed = (Object[]) preVisitState;
        TypeView typeView = (TypeView) unboxed[0];
        AssociationTreeNode associationTree = (AssociationTreeNode) unboxed[3];
        AttributeTreeNode attributeTree = (AttributeTreeNode) unboxed[4];
        Map<String, io.obase.providers.sql.sqlobject.Expression> dic = (HashMap<String, io.obase.providers.sql.sqlobject.Expression>) unboxed[1];
        ISelectionSet set = (ISelectionSet) unboxed[2];

        if (op instanceof GroupAggregationOp) {
            GroupAggregationOp groupAggregationOp = (GroupAggregationOp) op;

            LambdaExpression keyExp = groupAggregationOp.getKeySelector();
            if (groupAggregationOp.getIsNew())
                executor = new SelectExecutor(op, typeView, dic, executor);
            else
                executor = new AtrophySelectExecutor(op, keyExp, groupAggregationOp.getElementSelector(), set,
                        associationTree, attributeTree, null, executor);

            SubTreeEvaluator tree = new SubTreeEvaluator(keyExp);
            ExpressionTranslator tr = new ExpressionTranslator(this.model, tree, null);
            io.obase.providers.sql.sqlobject.Expression groupBy = tr.translate(keyExp);
            executor = new GroupAggregationExecutor(op, keyExp, groupBy, executor);

        } else {
            executor = new SelectExecutor(op, typeView, dic, executor);
        }

        return executor;
    }

    /**
     * 解析FirstOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(FirstOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建FirstOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(FirstOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        //分解为：Where(条件) >> 提取运算(1) >> 补充运算。
        OpExecutorWithContext<RopContext> executor = new TakeExecutor(op, 1, next);
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析SetOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(SetOp op) {
        //实际上没有Set操作
        return null;
    }

    /**
     * 创建SetOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(SetOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        if (preVisitState instanceof ISetOperand) {
            ISetOperand operand = (ISetOperand) preVisitState;
            return new SetOpExecutor(op, operand, op.getOperator(), next);
        }

        return next;
    }

    /**
     * 解析ElementAtOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(ElementAtOp op) {
        return null;
    }

    /**
     * 创建ElementAtOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(ElementAtOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        //分解为：提取运算(index + 1) >> 略过运算(index) >> 补充运算。
        OpExecutorWithContext<RopContext> executor = new SkipExecutor(op, op.getIndex(), next);
        executor = new TakeExecutor(op, op.getIndex() + 1, executor);
        return executor;
    }

    /**
     * 解析DistinctOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(DistinctOp op) {
        return null;
    }

    /**
     * 创建DistinctOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(DistinctOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        return new DistinctExecutor(op, next);
    }

    /**
     * 解析CountOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(CountOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建CountOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(CountOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        // 分解为：筛选运算(条件表达式) >> （无参）聚合运算。
        OpExecutorWithContext<RopContext> executor =
                new AggregateExecutor(op, EAggregationFunction.Count, op.getSourceType(), next);
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析ContainsOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(ContainsOp op) {
        return SqlUtils.generateCriteria(op.getItem(), this.model.getObjectType(op.getSourceType()));
    }

    /**
     * 创建ContainsOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(ContainsOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        // 分解为：筛选运算(生成筛选条件(测定条件或测试对象)) >> Count聚合运算 >> 补充运算，
        // 其中，生成筛选条件：
        // ObjectMapper om = new ObjectMapper();
        // criteria = om.GenerateCriteria(测试对象, model[typeof(TSource)])。
        OpExecutorWithContext<RopContext> executor =
                new AggregateExecutor(op, EAggregationFunction.Count, op.getSourceType(), next);
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析ArithAggregateOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(ArithAggregateOp op) {
        LambdaExpression keyExp = op.getSelector();
        SubTreeEvaluator tree = new SubTreeEvaluator(keyExp);
        SelectionExpressionParser parser = new SelectionExpressionParser(this.model, tree, false, null);
        ObjectReferencePack<AssociationTreeNode> assocResult = new ObjectReferencePack<>();
        ObjectReferencePack<AttributeTreeNode> attrResult = new ObjectReferencePack<>();
        ISelectionSet selectionSet = parser.parse(keyExp, assocResult, attrResult);

        for (SelectionColumn setItem : selectionSet.getColumns()) {
            if (setItem instanceof ExpressionColumn) {
                ExpressionColumn expression = (ExpressionColumn) setItem;
                expression.setAlias("");
            }
        }
        return new Object[]{op.getSelector(), null, selectionSet, assocResult.realValue, attrResult.realValue, null};
    }

    /**
     * 创建ArithAggregateOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(ArithAggregateOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        Object[] unboxed = (Object[]) preVisitState;

        //分解为：投影运算(投影表达式) >> （无参）聚合运算。
        OpExecutorWithContext<RopContext> executor;
        switch (op.getOperator()) {
            case Average:
                executor = new AggregateExecutor(op, EAggregationFunction.Average, op.getResultType(), next);
                break;
            case Max:
                executor = new AggregateExecutor(op, EAggregationFunction.Max, op.getResultType(), next);
                break;
            case Min:
                executor = new AggregateExecutor(op, EAggregationFunction.Min, op.getResultType(), next);
                break;
            case Sum:
                executor = new AggregateExecutor(op, EAggregationFunction.Sum, op.getResultType(), next);
                break;
            default:
                executor = new AggregateExecutor(op, EAggregationFunction.None, op.getResultType(), next);
                break;
        }


        LambdaExpression expression = (LambdaExpression) unboxed[0];
        LambdaExpression collectionSelector = (LambdaExpression) unboxed[1];
        ISelectionSet selectionSet = (ISelectionSet) unboxed[2];
        AssociationTreeNode assoResult = (AssociationTreeNode) unboxed[3];
        AttributeTreeNode attrResult = (AttributeTreeNode) unboxed[4];
        String resultAlias = (String) unboxed[5];

        executor = new AtrophySelectExecutor(op, expression, collectionSelector, selectionSet, assoResult,
                attrResult,
                resultAlias, executor);

        return executor;
    }

    /**
     * 解析AnyOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(AnyOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建AnyOp执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(AnyOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        //分解为：筛选运算(生成筛选条件(测定条件或测试对象)) >> Count聚合运算 >> 补充运算，其中，生成筛选条件：criteria = 测定条件。
        OpExecutorWithContext<RopContext> executor;
        executor = new AggregateExecutor(op, EAggregationFunction.Count, op.getSourceType(), next);
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria, executor);
        }
        return executor;
    }

    /**
     * 解析AllOp参数中的表达式
     *
     * @param op 查询运算
     * @return 解析结果
     */
    private Object analyze(AllOp op) {
        return this.translateToICriteria(op.getPredicate());
    }

    /**
     * 创建All执行器
     *
     * @param op            查询运算
     * @param preVisitState 前序访问数据
     * @param next          管道中的下一个
     * @return 执行器管道
     */
    private OpExecutorWithContext<RopContext> create(AllOp op, Object preVisitState, OpExecutorWithContext<RopContext> next) {
        //分解为：筛选运算(生成筛选条件(测定条件)) >> Count聚合运算 >> 补充运算，其中，生成筛选条件：criteria = 测定条件.Not()。
        OpExecutorWithContext<RopContext> executor =
                new AggregateExecutor(op, EAggregationFunction.Count, op.getSourceType(), next);
        if (preVisitState instanceof ICriteria) {
            ICriteria criteria = (ICriteria) preVisitState;
            executor = new WhereExecutor(op, criteria.not(), executor);
        }
        return executor;
    }

    /**
     * 翻译类型视图中的表达式
     *
     * @param typeView 类型视图
     * @return 返回解析结果的字典。键为属性名称，值为Sql表达式，该字典将用于构造投影运算执行器
     */
    private Map<String, io.obase.providers.sql.sqlobject.Expression> translateTypeView(TypeView typeView) {
        Map<String, io.obase.providers.sql.sqlobject.Expression> result = new HashMap<>();
        if (typeView == null) return result;

        for (Attribute attr : typeView.getAttributes()) {
            if (attr.getIsComplex()) continue;
            if (attr instanceof ViewAttribute) {
                //获取绑定表达式
                Expression attrExp = ((ViewAttribute) attr).getBinding();
                io.obase.providers.sql.sqlobject.Expression sqlExp;
                if (attrExp != null && !typeViewExpressionDict.containsKey(attrExp)) //缓存中不存在
                {
                    //翻译表达式
                    ExpressionTranslator translator = new ExpressionTranslator(this.model, new SubTreeEvaluator(attrExp), typeView.getParameterBindings());
                    sqlExp = translator.translate(attrExp);
                    //添加到缓存
                    typeViewExpressionDict.put(attrExp, sqlExp);
                }

                sqlExp = typeViewExpressionDict.get(attrExp);
                result.put(attr.getName(), sqlExp);
            }
        }

        //处理构造函数参数 构造函数参数内也有可能为需要投影的字段
        List<Parameter> parameters = typeView.getConstructor().getParameters();
        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters) {
                //获取绑定表达式
                Expression parameterExp = parameter.getExpression();
                if (parameterExp instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = (MethodCallExpression) parameterExp;
                    if (methodCallExpression.getObject().getType() != IAggregation.class)
                        parameterExp = methodCallExpression.getObject();
                }
                if (parameterExp == null) continue;
                io.obase.providers.sql.sqlobject.Expression sqlExp;
                if (!typeViewExpressionDict.containsKey(parameterExp)) //缓存中不存在
                {
                    SubTreeEvaluator subTree = new SubTreeEvaluator(parameterExp);
                    //翻译表达式
                    ExpressionTranslator translator = new ExpressionTranslator(this.model, subTree, typeView.getParameterBindings());
                    sqlExp = translator.translate(parameterExp);
                    //添加到缓存
                    typeViewExpressionDict.put(parameterExp, sqlExp);
                }

                sqlExp = typeViewExpressionDict.get(parameterExp);
                result.put(parameter.getName(), sqlExp);
            }
        }

        return result;
    }

    /**
     * 表达式翻译成ICriteria
     *
     * @param predicate 表达式
     * @return 条件
     */
    private ICriteria translateToICriteria(Expression predicate) {
        try {
            if (predicate == null) return null;
            SubTreeEvaluator tree = new SubTreeEvaluator(predicate);
            List<ParameterBinding> bindings = new ArrayList<>();
            if (predicate instanceof LambdaExpression) {
                LambdaExpression lambdaExpression = (LambdaExpression) predicate;
                if (lambdaExpression.getParameters().length == 2) {
                    bindings.add(new ParameterBinding(lambdaExpression.getParameters()[1], EParameterReferring.Index, null));
                }
            }

            CriteriaExpressionParser parser = new CriteriaExpressionParser(this.model, tree, this.targetSource, bindings.toArray(new ParameterBinding[0]));
            return parser.parse(predicate);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * 翻译表达式
     *
     * @param exp 表达式
     * @return 翻译后的表达式
     */
    private io.obase.providers.sql.sqlobject.Expression translate(Expression exp) {
        SubTreeEvaluator tree = new SubTreeEvaluator(exp);
        ExpressionTranslator tr = new ExpressionTranslator(this.model, tree, null);
        return tr.translate(exp);
    }
}
