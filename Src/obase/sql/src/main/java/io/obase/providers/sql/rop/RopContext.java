/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关系运算上下文.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 11:54:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.common.Utils;
import io.obase.core.expression.MemberExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.ObjectTypeNode;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.SourceJoiner;
import io.obase.providers.sql.sqlobject.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系运算上下文
 */
public class RopContext {

    /**
     * 生成Sql语句时使用的对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 相对于查询链结果类型的包含树
     */
    private final AssociationTree resultIncluding;

    /**
     * 数据源类型
     */
    private final EDataSource targetSource;

    /**
     * 别名生成器，用于生成退化路径和包含树各节点的别名。需要时创建。
     */
    private AliasGenerator aliasGenerator;
    /**
     * 别名根
     */
    private String aliasRoot;
    /**
     * 自查询运算开始或上次AcceptResult以来的退化路径
     * 退化投影操作可以形象地理解为在关联树中寻找一个节点，当attrTreeResult != null时则需要继续在锚定于此节点的某一属性树上寻找一个节点。后续运算将以此节点为根构建一棵新包含树。
     * 作为后续运算参数的表达式是与新包含树相对应的，如o.PropA中的o即对应于新树的根。而投影操作完成时，结果SQL语句的查询源（From
     * 子句）仍然对应于原树，因此在对表达式进行解析时，需要借助退化路径将新树的节点回退到原树，具体而言主要是两个问题：
     * （1）从表达式解析出的查询源，需要在其别名前附加一个前缀，该前缀根据关联退化路径生成；
     * （2）从表达式解析出的映射字段，需要在其名称前附加一个前缀，该前缀根据属性退化路径生成。
     * 如果投影结果为基元类型，它可能是由多个简单属性经数学运算而成的，这时无法运用上述回退机制。如果投影到复杂属性，可以结合运用（1）和（2）所述的回退机制；但当前版本不支持（2)。
     */
    private AssociationTreeNode atrophyPath;

    /**
     * 指示查询运算管道是否已执行了排序运算。
     */
    private boolean hasOrdered;

    /**
     * 包含树，包含所有挂起的包含运算，是根据关联关系生成的树形结构，根节点为当前查询结果类型（ResultType），节点表示包含运算的目标。
     */
    private AssociationTree including;

    /**
     * 基点源
     * 查询运算开始前和每次确认结果时会生成一个SimpleSource或SelectSource，后续运算可以看成是对该源的逐步修改。该源称为基点源。
     */
    private MonomerSource initialSource;

    /**
     * 查询基点类型，即查询源中的对象的类型。注：AcceptResult方法会将当前查询结果类型切换为基点类型。
     */
    private Class<?> initialType;

    /**
     * 源联接备忘录
     */
    private JoinMemo joinMemo;

    /**
     * 指示查询结果是否为枚举数
     */
    private boolean resultIsEnum = true;

    /**
     * 查询结果类型，如果查询结果为枚举数，为枚举元素的类型，如果为查询结果为单个值，则为该值的类型
     * 除以下三种运算外，结果类型等于基点类型：
     * （1）聚合运算，结果类型为int、long等值类型；
     * （2）测定运算，结果类型为bool；
     * （3）投影运算，结果类型为投影表达式的静态类型。
     */
    private TypeBase resultModelType;

    /**
     * 作为查询结果的Sql语句
     */
    private QuerySql resultSql;

    /**
     * 构造RopContext的新实例
     *
     * @param initialType     查询基点类型
     * @param model           对象数据模型
     * @param targetSource    数据源类型
     * @param resultIncluding 相对于查询链结果类型的包含树
     * @param includingTree   初始包含树
     */
    public RopContext(Class<?> initialType, ObjectDataModel model, EDataSource targetSource, AssociationTree resultIncluding, AssociationTree includingTree) {

        this.initialType = initialType;
        this.model = model;

        this.resultModelType = this.model.getObjectType(initialType);
        this.includingConstructorParameter();
        //构造初始源
        ObjectType objType = this.getModel().getObjectType(this.getInitialType());
        String sourceName = objType.getTargetTable();
        List<OrderRule> orderRules = objType.getStoringOrder();
        //初始源为SimpleSource
        this.initialSource = new SimpleSource(sourceName);

        List<Order> orders = new ArrayList<>();
        if (orderRules != null) {
            for (OrderRule r : orderRules) {
                IOrderBy orderBy = r.getOrderBy();
                boolean inverted = r.getInverted();
                Order order = new Order(this.initialSource, orderBy.getTargetField(), inverted ? EOrderDirection.Desc : EOrderDirection.Asc);
                orders.add(order);
            }
        }

        ((SimpleSource) this.initialSource).setStoringOrder(orders);
        this.resultSql = new QuerySql(this.initialSource);
        this.resultIncluding = resultIncluding;
        if (includingTree != null) {
            this.including = includingTree;
        } else {
            this.including = new AssociationTree(this.getModel().getObjectType(this.getResultType()));
        }
        this.resultSql.setSelectionSet(new SelectionSet());
        WildcardColumn wildcardColumn = new WildcardColumn();
        wildcardColumn.setSource(this.initialSource);
        this.resultSql.getSelectionSet().add(wildcardColumn);

        this.getJoinMemo().append(null, this.initialSource);

        this.targetSource = targetSource;
    }

    /**
     * 获取别名生成器
     *
     * @return 别名生成器
     */
    private AliasGenerator getAliasGenerator() {
        if (this.aliasGenerator == null)
            this.aliasGenerator = new AliasGenerator();
        return this.aliasGenerator;
    }

    /**
     * 获取查询基点类型，即查询源中的对象的类型。注：AcceptResult方法会将当前查询结果类型切换为基点类型。
     *
     * @return 查询基点类型
     */
    public Class<?> getInitialType() {
        return this.initialType;
    }

    /**
     * 获取查询结果类型，如果查询结果为枚举数，为枚举元素的类型，如果为查询结果为单个值，则为该值的类型
     * 除以下三种运算外，结果类型等于基点类型：
     * （1）聚合运算，结果类型为int、long等值类型；
     * （2）测定运算，结果类型为bool；
     * （3）投影运算，结果类型为投影表达式的静态类型。
     *
     * @return 查询结果类型
     */
    public Class<?> getResultType() {
        return this.resultModelType.getClrType();
    }

    /**
     * 获取查询结果类型的模型类型。
     * 如果查询结果为枚举数，为枚举元素的类型，如果为查询结果为单个值，则为该值的类型。
     * 除以下三种运算外，结果类型等于基点类型：
     * （1）聚合运算，结果类型为int、long等值类型；
     * （2）测定运算，结果类型为bool；
     * （3）投影运算，结果类型为投影表达式的静态类型。
     *
     * @return 查询结果类型的模型类型
     */
    public TypeBase getResultModelType() {
        return this.resultModelType;
    }

    /**
     * 获取一个值，指示查询结果是否为枚举数
     *
     * @return 查询结果是否为枚举数
     */
    public boolean getResultIsEnum() {
        return this.resultIsEnum;
    }

    /**
     * 获取源联接备忘录
     *
     * @return 源联接备忘录
     */
    public JoinMemo getJoinMemo() {
        if (this.joinMemo == null)
            this.joinMemo = new JoinMemo();
        return this.joinMemo;
    }

    /**
     * 获取别名根，在基点类型与投影结果类型之间沿关联关系生成的别名字符串。在联表查询中生成别名时，将以此字符串作为前缀，故称为别名根。
     * 如果结果类型不为ObjectType，别名根为空。
     *
     * @return 别名根
     */
    @Deprecated
    public String getAliasRoot() {
        if (this.aliasRoot != null) return this.aliasRoot;
        //退化路径不存在或只有一个根节点
        if (this.atrophyPath == null) return null;
        if (this.atrophyPath instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) this.atrophyPath;
            if (objectTypeNode.getParent() == null)
                return null;
        }
        String nodeAlias = null;
        if (this.atrophyPath != null) {
            nodeAlias = this.atrophyPath.asTree().accept(this.getAliasGenerator());
        }
        MonomerSource source = this.joinMemo.getSource(nodeAlias);
        if (source != null)
            this.aliasRoot = source.getSymbol();
        return this.aliasRoot;
    }

    /**
     * 获取作为查询结果的Sql语句
     *
     * @return 查询结果的Sql语句
     */
    public QuerySql getResultSql() {
        return this.resultSql;
    }

    /**
     * 设置作为查询结果的Sql语句
     *
     * @param resultSql 查询结果的Sql语句
     */
    public void setResultSql(QuerySql resultSql) {
        this.resultSql = resultSql;
    }

    /**
     * 获取生成Sql语句时使用的对象数据模型
     *
     * @return 对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 获取包含树，包含所有挂起的包含运算，是根据关联关系生成的树形结构，根节点为当前查询结果类型（ResultType），节点表示包含运算的目标
     *
     * @return 包含树
     */
    public AssociationTree getIncluding() {
        if (this.including == null) {
            this.including = new AssociationTree(this.getModel().getObjectType(this.getResultType()));
        }
        return this.including;
    }

    /**
     * 获取自查询运算开始或上次AcceptResult以来的退化路径
     *
     * @return 退化路径
     */
    public AssociationTreeNode getAtrophyPath() {
        return this.atrophyPath;
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    public EDataSource getSourceType() {
        return this.targetSource;
    }

    /**
     * 相对于查询链结果类型的包含树
     *
     * @return 结果类型的包含树
     */
    public AssociationTree getResultIncluding() {
        return this.resultIncluding;
    }

    /**
     * 指示查询运算管道是否已执行了排序运算。
     *
     * @return 是否已执行了排序运算
     */
    public boolean getHasOrdered() {
        return this.hasOrdered;
    }

    /**
     * 设置查询运算管道是否已执行了排序运算。
     *
     * @param hasOrdered 是否已执行了排序运算
     */
    public void setHasOrdered(boolean hasOrdered) {
        this.hasOrdered = hasOrdered;
    }

    /**
     * 接收当前查询结果并将其作为后续查询的基点，执行以下操作：
     * （1）将查询结果类型作为基点类型；
     * （2）置空退化路径；
     * （3）清空联接备忘录；
     * （4）将查询结果更换为以当前结果为源的QuerySql新实例。
     */
    public void acceptResult() {
        this.initialType = this.getResultType(); //将查询结果类型作为基点类型；
        this.atrophyPath = null; //置空退化路径；
        this.aliasRoot = null; //清空根别名；
        this.getJoinMemo().reset(); //清空联接备忘录；

        if (this.resultModelType instanceof IMappable) {
            IMappable mappable = (IMappable) this.resultModelType;
            if (mappable instanceof ObjectType) {
                ObjectType objectType = (ObjectType) mappable;
                this.initialSource = new SelectSource(this.resultSql, Utils.getDerivedTargetTable(objectType));
            } else {
                this.initialSource = new SelectSource(this.resultSql, mappable.getTargetName());
            }

        } else {
            this.initialSource = new SelectSource(this.resultSql, "OTB");
        }

        //构造一通配符列
        WildcardColumn wildcardColumn = new WildcardColumn();
        wildcardColumn.setSource(this.initialSource);
        //将查询结果类型切换为基点类型
        this.resultSql = new QuerySql(this.initialSource);
        //加入至结果Sql的投影集
        this.resultSql.getSelectionSet().add(wildcardColumn);
        if (this.resultSql.getTakeNumber() == 0 && this.resultSql.getSource().getCanBubbleOrder())
            this.resultSql.bubbleOrder();
        this.getJoinMemo().append(null, this.initialSource);
        //设置为 未排序
        this.hasOrdered = false;
    }

    /**
     * 依据关联关系拓展源以使其覆盖包含树，同时填写源联接备忘录。
     *
     * @param assoTree     要覆盖的关联树
     * @param autoDistinct 是否自动去重
     */
    public void expandSource(AssociationTree assoTree, boolean autoDistinct) {
        if (assoTree != null && assoTree.getSubTrees().length > 0) {
            //无排序 则进行排序冒泡
            if (this.resultSql.getOrders().size() == 0 && this.resultSql.getSource().getCanBubbleOrder())
                this.resultSql.bubbleOrder();
            MonomerSource baseSource = this.getJoinMemo().getSource(this.getAliasRoot());
            ISource source = this.resultSql.getSource();
            if (assoTree.getRepresentedType() != null)
                this.resultSql.setSource(this.joinByAssociationTree(assoTree, baseSource, this.getAliasRoot(), source, autoDistinct, ESourceJoinType.Left));
        }
    }

    /**
     * 依据关联关系拓展源以使其覆盖指定的表达式，同时填写源联接备忘录
     *
     * @param expression   要覆盖的表达式
     * @param joinType     Join运算类型
     * @param autoDistinct 是否自动去重
     */
    public void expandSource(io.obase.core.expression.Expression expression, ESourceJoinType joinType, boolean autoDistinct) {
        //构造表达式提取器
        MemberExpressionExtractor memberExpressionExtractor = new MemberExpressionExtractor(new SubTreeEvaluator(expression));
        List<MemberExpression> members = memberExpressionExtractor.extractMember(expression);

        //冒泡排序
        if (members != null && members.size() > 0 && this.resultSql.getOrders() != null && this.resultSql.getOrders().size() == 0 && this.resultSql.getSource().getCanBubbleOrder())
            this.resultSql.bubbleOrder();

        MonomerSource baseSource = this.getJoinMemo().getSource(this.getAliasRoot());

        //所有连接源
        ISource source = this.resultSql.getSource();
        if (members != null) {
            for (MemberExpression member : members) {
                AssociationTree assocTree = member.extractAssociation(this.model, null);
                source = this.joinByAssociationTree(assocTree, baseSource, this.getAliasRoot(), source, autoDistinct, joinType);

            }
        }

        this.resultSql.setSource(source);
    }

    /**
     * 依据关联关系拓展源以使其覆盖指定的关联树，同时填写源联接备忘录
     *
     * @param autoDistinct 是否自动去重
     */
    public void expandSource(boolean autoDistinct) {
        this.expandSource(this.including, autoDistinct);
    }

    /**
     * 强制包含引用型构造参数（即绑定到引用元素的参数）
     */
    private void includingConstructorParameter() {
        if (this.getResultModelType() instanceof ReferringType) {
            ReferringType referringType = (ReferringType) this.getResultModelType();
            List<Parameter> paras = referringType.getConstructor().getParameters();
            this.including = new AssociationTree(referringType);
            if (paras != null) {

                for (Parameter para : paras) {
                    if (para.getElementType() == EElementType.Attribute) continue;
                    this.including.grow(para.getElementName());
                }
            }
        }
    }

    /**
     * 根据关联树联接源
     *
     * @param assocTree    关联树
     * @param baseSource   基础源
     * @param baseAlias    基础别名
     * @param leftSource   左端源
     * @param autoDistinct 是否自动去重
     * @param joinType     联接类型
     * @return 连接后的源
     */
    private ISource joinByAssociationTree(AssociationTree assocTree, MonomerSource baseSource, String baseAlias,
                                          ISource leftSource, boolean autoDistinct, ESourceJoinType joinType) {
        //构造连接器
        ReferringType objType = assocTree.getRepresentedType();
        SourceJoiner sourceJoiner = new SourceJoiner(objType, baseSource, baseAlias, leftSource);

        AssociationTree[] subTrees = assocTree.getSubTrees();
        //返回值
        ISource resultSource = leftSource;

        for (AssociationTree sub : subTrees) {
            String elementName = sub.getElementName();
            ObjectReferencePack<MonomerSource> targetSource = new ObjectReferencePack<>();
            ObjectReferencePack<String> targetAlias = new ObjectReferencePack<>();
            ISource joinedSource = sourceJoiner.join(elementName, targetSource, targetAlias, joinType);

            if (!this.getJoinMemo().exists(targetAlias.realValue)) {
                this.getJoinMemo().append(targetAlias.realValue, targetSource.realValue);
                TypeElement element = objType.getElement(elementName);

                //如果_resultModelType为主引类型 且为一对多 并自动去重 则对查询结果去重
                if (this.resultModelType instanceof ReferringType && element != null && element.getIsMultiple() && autoDistinct)
                    this.resultSql.setDistinct(true);
                resultSource = joinedSource;
            }

            resultSource = this.joinByAssociationTree(sub, targetSource.realValue, targetAlias.realValue, resultSource, autoDistinct, joinType);
            sourceJoiner.setLeftSource(resultSource);
        }

        return resultSource;
    }

    /**
     * 在退化投影运算完成时设置运算结果类型，根据该运算在关联树上的投影结果和在属性树上的投影结果
     *
     * @param assocResult   在关联树上的投影结果
     * @param attrResult    在属性树上的投影结果
     * @param pipelineEnded 指示运算管道是否已终结
     */
    public void setResultType(AssociationTreeNode assocResult, AttributeTreeNode attrResult, boolean pipelineEnded) {
        this.resultIsEnum = true;

        if (attrResult == null) {
            this.resultModelType = assocResult.getRepresentedType();
            this.including = this.including.searchSub(assocResult);
            this.includingConstructorParameter();

            if (this.atrophyPath != null) {
                AssociationTree sub = this.atrophyPath.asTree().getRoot().getSubTrees()[0];
                if (sub != null) {
                    assocResult.addChild((ObjectTypeNode) sub.getNode(), null);
                }
            }
            this.aliasRoot = null;
            this.atrophyPath = assocResult;
        } else {
            this.resultModelType = attrResult.getAttributeType();
            this.including = null;
            this.aliasRoot = null;
            this.atrophyPath = null;

            if (!pipelineEnded)
                this.acceptResult();
        }
    }

    /**
     * 设置查询结果类型为一个基元类型，同时清空退化路径和包含树
     *
     * @param primitiveType 运算结果类型
     * @param isEnumerable  指示运算结果是否为可枚举的
     * @param pipelineEnded 指示运算管道是否已终结
     */
    public void setResultType(PrimitiveType primitiveType, boolean isEnumerable, boolean pipelineEnded) {
        TypeBase typeBase = this.model.getTypeOrNull(primitiveType.getClrType());
        this.resultModelType = primitiveType;
        if (typeBase != null)
            this.resultModelType = typeBase;
        this.resultIsEnum = isEnumerable;
        this.including = null;
        if (!pipelineEnded)
            this.acceptResult();
    }

    /**
     * 在一般投影运算完成时将查询结果类型设置为类型视图，（强制为可枚举类型），同时根据视图结构裁剪关联树。
     *
     * @param typeView    类型视图
     * @param pipelineEnd 指示运算管道是否已终结
     */
    public void setResultType(TypeView typeView, boolean pipelineEnd) {
        this.resultModelType = typeView;
        AssociationTree newIncluding = new AssociationTree(typeView);

        ReferenceElement[] elements = typeView.getReferenceElements();
        for (ReferenceElement referenceElement : elements) {
            if (referenceElement instanceof ViewReference) {
                ViewReference viewReference = (ViewReference) referenceElement;
                if (viewReference.getAnchor() != null) {
                    AssociationTree anchorTree = newIncluding.searchSub(viewReference.getAnchor());
                    if (anchorTree != null) {
                        AssociationTree bindingTree = newIncluding.removeSub(viewReference.getBinding().getName());
                        if (bindingTree != null) {
                            newIncluding.addSubTree(bindingTree, viewReference.getName());
                        }
                    }
                    //分解异构视图生成的不进行生长
                    if (!typeView.getIsDecomposeExtremelyResult()) {
                        newIncluding.grow(viewReference.getName());
                    }
                }

            }
        }

        this.including = newIncluding;
        if (!pipelineEnd)
            this.acceptResult();
    }

    /**
     * 在当前查询结果中追加一个索引列
     */
    public void addIndexColumn() {
        switch (this.getSourceType()) {

            case SqlServer: {
                if (this.resultSql.getOrders().size() == 0) this.resultSql.bubbleOrder();
                if (this.resultSql.getOrders().size() > 0) {
                    FunctionExpression index = Expression.function("row_number");
                    OverClause over = new OverClause(this.resultSql.getOrders().toArray(new Order[0]));
                    index.setOver(over);
                    String alias = "obase$index";
                    if (this.resultSql.getSelectionSet() != null)
                        this.resultSql.getSelectionSet().add(index, alias);
                    this.acceptResult();
                }
                break;
            }
            case Oracle:
            case Oledb:
            case MySql:
            case Sqlite:
            case Other:
                break;
        }
    }
}
