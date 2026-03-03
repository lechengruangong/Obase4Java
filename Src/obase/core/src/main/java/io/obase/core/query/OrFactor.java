/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：平展后的或因子.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:17:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.MemberExpressionExtractor;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.common.Utils;
import io.obase.core.expression.*;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeHeterogeneityPredicater;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewAttribute;
import io.obase.core.query.typeView.ViewElementAdder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示平展筛选条件得到的或因子。
 * 条件平展
 * 将条件表示为深度为2的树，其中，根节点为或运算，有多个子节点，每个子节点表示一个运算数，称为或因子。一个或因子可以是一个非逻辑运算的布尔表达式，也可以是多个非逻
 * 辑运算布尔表达式的连续“与”运算。
 */
public class OrFactor {

    /**
     * 或因子项。一个或因子等价于所有项的连续与运算
     */
    private final Expression[] items;

    /**
     * 在表达式中代表查询源的形式参数
     */
    private final ParameterExpression sourceParameter;

    /**
     * 查询源类型
     */
    private final ReferringType sourceType;

    /**
     * 基础因子
     */
    private OrFactor baseFactor;

    /**
     * 校验因子
     */
    private OrFactor checkFactor;

    /**
     * 该值指示或因子是否为异构的
     */
    private Boolean heterogeneous = null;

    /**
     * 初始化OrFactor类的新实例
     *
     * @param items      或因子项
     * @param sourceType 查询源类型
     * @param sourcePara 表达式中代表查询源的形参
     */
    public OrFactor(Expression[] items, ReferringType sourceType, ParameterExpression sourcePara) {
        this.items = items;
        this.sourceParameter = sourcePara;
        this.sourceType = sourceType;
    }

    /**
     * 用表达式表示多个或因子的连续或运算。
     *
     * @param orFactors 或因子集合
     * @return 表达式
     */
    public static LambdaExpression toLambda(OrFactor[] orFactors) {
        //每个都转换成lambda
        List<LambdaExpression> orLambdaExpressionList = Arrays.stream(orFactors).map(OrFactor::toLambda).collect(Collectors.toList());

        //每个都是或运算
        Expression exp = null;
        for (LambdaExpression orExpression : orLambdaExpressionList) {
            if (exp == null)
                exp = orExpression;
            else
                exp = Expression.or(exp, orExpression, exp.getType());
        }

        ParameterExpression parameter = Expression.parameter("p", orFactors[0].getSourceType().getClrType());

        if (exp == null)
            throw new IllegalArgumentException("无法为空的或因子数组构造表达式");

        return Expression.lambda(new ParameterExpression[]{parameter}, exp);
    }

    /**
     * 获取或因子项
     *
     * @return 或因子项
     */
    public Expression[] getItems() {
        return this.items;
    }

    /**
     * 获取实施极限分解后的基础因子
     *
     * @return 实施极限分解后的基础因子
     */
    public OrFactor getBaseFactor() {
        return this.baseFactor;
    }

    /**
     * 获取实施极限分解后的校验因子
     *
     * @return 实施极限分解后的校验因子
     */
    public OrFactor getCheckFactor() {
        return this.checkFactor;
    }

    /**
     * 获取查询源类型
     *
     * @return 查询源类型
     */
    public ReferringType getSourceType() {
        return this.sourceType;
    }

    /**
     * 获取在表达式中代表查询源的形式参数
     *
     * @return 表达式中代表查询源的形式参数
     */
    public ParameterExpression getSourceParameter() {
        return this.sourceParameter;
    }

    /**
     * 获取一个值，该值指示或因子是否为异构的
     *
     * @return 或因子是否为异构的
     */
    public boolean getHeterogeneous() {
        if (this.heterogeneous == null) {
            this.decomposeExtremely();
        }

        return this.heterogeneous != null && this.heterogeneous;
    }

    /**
     * 将当前或因子与指定的或因子合并，生成一个新的或因子。
     *
     * @param other 参与合并的或因子
     * @return 合并后的或因子
     */
    public OrFactor and(OrFactor other) {
        if (other.sourceType != this.sourceType)
            throw new IllegalArgumentException("要合并的查询源,与本身查询源不符,无法合并.");

        if (other.sourceParameter != this.sourceParameter)
            throw new IllegalArgumentException("要合并的形式参数与本身形式参数不符,无法合并.");

        List<Expression> expressions = new ArrayList<>();
        expressions.addAll(Arrays.asList(this.items));
        expressions.addAll(Arrays.asList(other.items));

        return new OrFactor(expressions.toArray(new Expression[0]), this.sourceType, this.sourceParameter);
    }

    /**
     * 用表达式表示或因子
     *
     * @return 表达式表示形式
     */
    public LambdaExpression toLambda() {

        Expression exp = this.items[0];

        for (int i = 1; i < this.items.length; i++) {
            exp = Expression.or(exp, this.items[i], exp.getType());
        }

        ParameterExpression parameter = Expression.parameter("p", this.sourceType.getClrType());

        if (exp == null)
            throw new IllegalArgumentException("无法为空的或因子数组构造表达式");

        return Expression.lambda(new ParameterExpression[]{parameter}, exp);
    }

    /**
     * 将当前或因子作为校验因子，生成检验视图
     *
     * @param checkAttrs 校验属性
     * @return 类型视图
     */
    public TypeView generateCheckView(ObjectReferencePack<ViewAttribute[]> checkAttrs) {
        //用于申请隐含类型的字段描述器
        List<FieldDescriptor> filedDescriptors = new ArrayList<>();

        FieldDescriptor[] checkFields = null;

        if (this.checkFactor != null && this.checkFactor.getItems() != null) {
            checkFields = Arrays.stream(this.checkFactor.getItems()).map(p -> {
                if (p instanceof BinaryExpression) {
                    BinaryExpression binaryExpression = (BinaryExpression) p;
                    return new FieldDescriptor(binaryExpression.getLeft(), null);
                }
                return null;
            }).toArray(FieldDescriptor[]::new);
            filedDescriptors.addAll(Arrays.stream(checkFields).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        Attribute[] filterAttrs = this.sourceType.getFilterKey();
        FieldDescriptor[] filterFields = null;
        if (filterAttrs != null) {
            //构造过滤属性描述
            filterFields = Arrays.stream(filterAttrs).map(p -> new FieldDescriptor(p.getDataType())).toArray(FieldDescriptor[]::new);
            filedDescriptors.addAll(Arrays.asList(filterFields));
        }

        //申请隐含类型
        Class<?> impliedType =
                ImpliedTypeManager.getCurrent().applyType(this.sourceType.getClrType(), filedDescriptors.toArray(new FieldDescriptor[0]),
                        new IdentityArray(this.sourceType.getFullName()), null);

        //视图
        TypeView result = new TypeView(this.sourceType, impliedType, this.sourceParameter);
        result.setIsDecomposeExtremelyResult(true);

        List<ViewAttribute> checkAttributes = null;
        //添加校验属性
        if (checkFields != null) {
            checkAttributes = new ArrayList<>();
            ViewElementAdder adder = new ViewElementAdder(result, this.sourceType.getModel());
            for (FieldDescriptor checkField : checkFields) {
                //添加
                TypeElement element = adder.addElement(Utils.getFieldIncludeSuperclass(impliedType, checkField.getName()),
                        checkField.getValueExpression(), null);
                if (element instanceof ViewAttribute) {
                    ViewAttribute viewAttribute = (ViewAttribute) element;
                    checkAttributes.add(viewAttribute);
                }

            }
        }

        //添加过滤属性
        if (filterAttrs != null) {
            for (int i = 0; i < filterAttrs.length; i++) {
                ViewAttribute viewAttribute = new ViewAttribute(filterFields[i].getName(), filterAttrs[i], null);
                result.addElement(viewAttribute);
            }
        }

        if (checkAttributes != null)
            checkAttrs.realValue = checkAttributes.toArray(new ViewAttribute[0]);
        result.generateType();
        return result;
    }

    /**
     * 对或因子实施极限分解
     */
    private void decomposeExtremely() {
        //已分解过
        if (this.baseFactor != null || this.checkFactor != null || this.heterogeneous != null) return;

        boolean result = false;
        //异构的和不异勾的
        List<Expression> heterogItem = new ArrayList<>();
        List<Expression> homogItem = new ArrayList<>();

        for (Expression item : this.items) {
            List<MemberExpression> memberExpressions = new MemberExpressionExtractor((new SubTreeEvaluator(item))).extractMember(item);

            for (MemberExpression memberExpression : memberExpressions) {
                //判断是否异构
                AssociationTree associationTree = memberExpression.extractAssociation(this.sourceType.getModel(), null);
                AssociationTreeHeterogeneityPredicater predicater = new AssociationTreeHeterogeneityPredicater(new StorageHeterogeneityPredicationProvider());
                boolean itemHeter = associationTree.accept(predicater);
                if (itemHeter) {
                    result = true;
                    break;
                }
                result = false;
            }

            //加入不同的集合
            if (result)
                heterogItem.add(item);
            else
                homogItem.add(item);
        }

        this.heterogeneous = result;

        if (heterogItem.size() == 0) this.checkFactor = null;

        this.baseFactor = heterogItem.size() == 0 ? null : new OrFactor(heterogItem.toArray(new Expression[0]), this.sourceType, this.sourceParameter);

        if (homogItem.size() == 0) this.baseFactor = null;

        this.checkFactor = homogItem.size() == 0 ? null : new OrFactor(homogItem.toArray(new Expression[0]), this.sourceType, this.sourceParameter);
    }
}

