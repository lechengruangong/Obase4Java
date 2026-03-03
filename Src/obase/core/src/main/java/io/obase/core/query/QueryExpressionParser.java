/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询表达式解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:53:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ObjectDataModel;

import java.io.Serializable;
import java.util.*;

/**
 * 查询表达式解析器
 */
public class QueryExpressionParser {

    /**
     * 对象数据模型
     */
    private final ObjectDataModel model;
    /**
     * 头QueryOp
     */
    private final List<QueryOp> queryOps = new ArrayList<>();
    /**
     * 表达式翻译器
     */
    private final LambdaTranslator translator = new LambdaTranslator();
    /**
     * 源类型 用于检查解析结果
     */
    private final Class<?> sourceType;
    /**
     * 解析结果
     */
    private QueryOp queryOp;
    /**
     * 目标的结果类型
     */
    private Class<?> resultTypeClass;

    /**
     * 初始化QueryExpressionParser类的新实例
     *
     * @param model      对象数据模型
     * @param sourceType 源类型
     */
    public QueryExpressionParser(ObjectDataModel model, Class<?> sourceType) {
        this.model = model;
        this.sourceType = sourceType;
    }

    /**
     * 获取解析出的查询链
     *
     * @return 获取解析出的查询链
     */
    public QueryOp getQueryOp() {
        if (this.queryOp == null) {
            for (int i = 0; i < this.queryOps.size(); i++) {
                if (i != this.queryOps.size() - 1)
                    this.queryOps.get(i).setNext(this.queryOps.get(i + 1));
                this.queryOps.get(i).model = this.model;
            }
            this.queryOp = this.queryOps.size() > 0 ? this.queryOps.get(0) : null;
        }
        return this.queryOp;
    }

    /**
     * 获取目标的结果类型
     *
     * @return 目标的结果类型
     */
    public Class<?> getResultTypeClass() {
        return this.resultTypeClass;
    }

    /**
     * 获取查询链的表达式
     *
     * @return 查询链的表达式
     */
    public Expression getExpression() {
        //此处会出现多个查询运算的表达式 且 这些表达式在后续并未使用 此处仅返回空 如有后续使用再进行修改
        return null;
    }

    /**
     * 直接加入QueryOp
     *
     * @param methodName 方法名
     * @param predicate  表达式
     * @param arguments  参数
     */
    public void addQueryOp(String methodName, LambdaExpression predicate, Object[] arguments) {

        this.resultTypeClass = predicate == null ? null : predicate.getType();

        //检查表达式
        this.checkExpressionSourceType(methodName, predicate);

        switch (methodName.toLowerCase(Locale.ROOT)) {
            case "filter": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为Where
                QueryOp where = QueryOp.where(predicate, this.model);
                this.queryOps.add(where);
                break;
            }
            case "count": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为count
                this.queryOps.add(predicate == null
                        ? QueryOp.counts((Class<?>) arguments[0], this.model)
                        : QueryOp.counts(predicate, this.model));
                break;
            }
            case "anymatch": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为any
                QueryOp op = predicate == null
                        ? QueryOp.any((Class<?>) arguments[0], this.model)
                        : QueryOp.any(predicate, this.model);
                this.queryOps.add(op);
                break;
            }
            case "allmatch": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为All
                QueryOp op = predicate == null
                        ? QueryOp.all(null, this.model)
                        : QueryOp.all(predicate, this.model);
                this.queryOps.add(op);
                break;
            }
            case "findfirst": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为first
                this.queryOps.add(predicate == null
                        ? QueryOp.first((Class<?>) arguments[0], true, this.model)
                        : QueryOp.first(predicate, true, this.model));
                break;
            }
            case "findlast": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为last
                this.queryOps.add(predicate == null
                        ? QueryOp.last((Class<?>) arguments[0], true, this.model)
                        : QueryOp.last(predicate, true, this.model));
                break;
            }
            case "single": {
                this.generateLambdaExpressionParameterFromCons(predicate, (Class<?>) arguments[0]);
                //映射为first
                this.queryOps.add(predicate == null
                        ? QueryOp.single((Class<?>) arguments[0], true, this.model)
                        : QueryOp.single(predicate, true, this.model));
                break;
            }
        }
    }

    /**
     * 具体解析方法
     *
     * @param methodName 调用的方法名称
     * @param expression 表达式组
     */
    public void parse(String methodName, Serializable expression, Object[] arguments) {

        Expression lambdaExpression = expression == null ? null : this.translator.getLambdaExpression(expression);
        this.resultTypeClass = lambdaExpression == null ? null : lambdaExpression.getType();

        //根据名称组成查询链 存放在queryOp内
        //所使用的表达式也一并构造

        //检查表达式
        this.checkExpressionSourceType(methodName, lambdaExpression);


        switch (methodName.toLowerCase(Locale.ROOT)) {
            case "filter": {
                //映射为Where
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                QueryOp where = QueryOp.where((LambdaExpression) lambdaExpression, this.model);
                this.queryOps.add(where);
                break;
            }
            case "map": {
                //映射为Select
                LambdaExpression selector = (LambdaExpression) lambdaExpression;
                if (selector != null) {
                    if (Iterable.class.isAssignableFrom(selector.getType()))
                        this.queryOps.add(QueryOp.select(selector, (Class<?>) arguments[0], this.model));
                    else {
                        QueryOp op = QueryOp.select(selector, this.model, this.queryOp);
                        this.queryOps.add(op);
                    }
                }
                break;
            }
            case "flatmap": {
                //映射为SelectMany
                if (arguments.length == 3) {
                    LambdaExpression collectionSelector = this.translator.getLambdaExpression((Serializable) arguments[0]);
                    LambdaExpression resultSelector = arguments[1] == null ? null : this.translator.getLambdaExpression((Serializable) arguments[1]);
                    this.queryOps.add(QueryOp.select(resultSelector, collectionSelector, this.model));
                } else {
                    LambdaExpression selector = this.translator.getLambdaExpression((Serializable) arguments[0]);
                    this.queryOps.add(QueryOp.select(selector, (Class<?>) arguments[1], this.model));
                }
                break;
            }
            case "distinct": {
                //映射为distinct
                this.queryOps.add(QueryOp.distinct((Class<?>) arguments[0], null, this.model));
                break;
            }
            case "sorted": {
                //映射为OrderBy
                LambdaExpression selector = (LambdaExpression) lambdaExpression;
                Comparator<?> comparable = null;
                if (arguments != null)
                    comparable = (Comparator<?>) arguments[0];
                this.queryOps.add(QueryOp.orderBy(selector, false, comparable, this.model));
                break;
            }
            case "sorteddesc": {
                //映射为OrderByDesc
                LambdaExpression selector = (LambdaExpression) lambdaExpression;
                Comparator<?> comparable = null;
                if (arguments != null)
                    comparable = (Comparator<?>) arguments[0];
                this.queryOps.add(QueryOp.orderBy(selector, true, comparable, this.model));
                break;
            }
            case "thensorted": {
                //映射为OrderBy
                LambdaExpression selector = (LambdaExpression) lambdaExpression;
                Comparator<?> comparable = null;
                if (arguments != null)
                    comparable = (Comparator<?>) arguments[0];
                this.queryOps.add(QueryOp.thenOrderBy(selector, false, comparable, this.model));
                break;
            }
            case "thensorteddesc": {
                //映射为OrderByDesc
                LambdaExpression selector = (LambdaExpression) lambdaExpression;
                Comparator<?> comparable = null;
                if (arguments != null)
                    comparable = (Comparator<?>) arguments[0];
                this.queryOps.add(QueryOp.thenOrderBy(selector, true, comparable, this.model));
                break;
            }
            case "limit": {
                //映射为Take
                this.queryOps.add(QueryOp.take((Class<?>) arguments[0], (int) arguments[1], this.model));
                break;
            }
            case "skip": {
                //映射为skip
                this.queryOps.add(QueryOp.skip((Class<?>) arguments[0], (int) arguments[1], this.model));
                break;
            }
            case "min": {
                //映射为min
                LambdaExpression selector = null;
                if (expression != null)
                    selector = (LambdaExpression) lambdaExpression;
                QueryOp op = QueryOp.min(selector, this.model);
                this.queryOps.add(op);
                break;
            }
            case "max": {
                //映射为max
                LambdaExpression selector = null;
                if (expression != null)
                    selector = (LambdaExpression) lambdaExpression;
                this.queryOps.add(QueryOp.max(selector, this.model));
                break;
            }
            case "avg": {
                //映射为avg
                LambdaExpression selector = null;
                if (expression != null)
                    selector = (LambdaExpression) lambdaExpression;
                this.queryOps.add(QueryOp.average(selector, this.model));
                break;
            }
            case "sum": {
                //映射为avg
                LambdaExpression selector = null;
                if (expression != null)
                    selector = (LambdaExpression) lambdaExpression;
                this.queryOps.add(QueryOp.sum(selector, this.model));
                break;
            }
            case "count": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为count
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                this.queryOps.add(predicate == null
                        ? QueryOp.counts((Class<?>) arguments[0], this.model)
                        : QueryOp.counts(predicate, this.model));
                break;
            }
            case "anymatch": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为any
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                QueryOp op = predicate == null
                        ? QueryOp.any((Class<?>) arguments[0], this.model)
                        : QueryOp.any(predicate, this.model);
                this.queryOps.add(op);
                break;
            }
            case "allmatch": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为All
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                QueryOp op = predicate == null
                        ? QueryOp.all(null, this.model)
                        : QueryOp.all(predicate, this.model);
                this.queryOps.add(op);
                break;
            }
            case "findfirst": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为first
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                this.queryOps.add(predicate == null
                        ? QueryOp.first((Class<?>) arguments[0], true, this.model)
                        : QueryOp.first(predicate, true, this.model));
                break;
            }
            case "findlast": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为last
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                this.queryOps.add(predicate == null
                        ? QueryOp.last((Class<?>) arguments[0], true, this.model)
                        : QueryOp.last(predicate, true, this.model));
                break;
            }
            case "elementat": {
                int index = 0;
                if (arguments.length > 0) index = (int) arguments[1];
                this.queryOps.add(QueryOp.elementAt((Class<?>) arguments[0], index, true, this.model));
                break;
            }
            case "reverse": {
                this.queryOps.add(QueryOp.reverse((Class<?>) arguments[0], this.model));
                break;
            }
            case "single": {
                this.generateLambdaExpressionParameterFromCons((LambdaExpression) lambdaExpression, (Class<?>) arguments[0]);
                //映射为first
                LambdaExpression predicate = null;
                if (expression != null)
                    predicate = (LambdaExpression) lambdaExpression;
                this.queryOps.add(predicate == null
                        ? QueryOp.single((Class<?>) arguments[0], true, this.model, null)
                        : QueryOp.single(predicate, true, this.model));
                break;
            }
            case "groupby": {
                if (arguments.length == 3) {
                    //映射为Group
                    LambdaExpression keySelector = this.translator.getLambdaExpression((Serializable) arguments[0]);
                    LambdaExpression elementOrResultSelector = arguments[1] == null ? null : this.translator.getLambdaExpression((Serializable) arguments[1]);
                    this.queryOps.add(QueryOp.groupBy(keySelector, elementOrResultSelector, null, this.model));
                } else if (arguments.length == 4) {
                    //映射为Group
                    LambdaExpression keySelector = this.translator.getLambdaExpression((Serializable) arguments[0]);
                    LambdaExpression elementSelector = arguments[1] == null ? null : this.translator.getLambdaExpression((Serializable) arguments[1]);
                    LambdaExpression ResultSelector = arguments[2] == null ? null : this.translator.getLambdaExpression((Serializable) arguments[2]);
                    this.queryOps.add(QueryOp.groupBy(keySelector, elementSelector, ResultSelector, null, this.model));
                }
                break;
            }
            case "include": {
                if (lambdaExpression != null) {
                    LambdaExpression exp = (LambdaExpression) lambdaExpression;
                    this.queryOps.add(QueryOp.include(exp, this.model, this.queryOp));
                } else {
                    String exp = (String) arguments[0];
                    Class<?> sourceType = (Class<?>) arguments[1];
                    this.queryOps.add(QueryOp.include(exp, sourceType, this.model, this.queryOp));
                }

                break;
            }
            default:
        }
    }

    /**
     * 检查表达式的源类型
     *
     * @param methodName       方法
     * @param lambdaExpression 解析出来的表达式
     */
    private void checkExpressionSourceType(String methodName, Expression lambdaExpression) {
        //检查是否与源类型相同 仅在表达式不为空 且 是存在继承的模型类型时
        //注意不检查Map 和 FlatMap 投影方法导致查询源转换是正常的
        if (lambdaExpression != null && this.model.getStructuralType(this.sourceType) != null && this.model.getStructuralType(this.sourceType).getDerivingFrom() != null
                && !methodName.equalsIgnoreCase("map") && !methodName.equalsIgnoreCase("flatmap")) {
            ParameterExpression hostParameter = Arrays.stream(((LambdaExpression) lambdaExpression).getParameters()).filter(ParameterExpression::getIsHost).findFirst().orElse(null);
            if (hostParameter != null) {
                Class<?> hostType = hostParameter.getType();
                if (!hostType.equals(this.sourceType))
                    throw new IllegalArgumentException("查询方法" + methodName + "的表达式解析类型与源类型不符,请将传入的方法引用改为表达式形式.如Bean::getCode改为p->p.getCode().");
            }
        }
    }

    /**
     * 从静态表达式中生成Lambda表达式的参数
     *
     * @param lambdaExpression 表达式
     * @param typeClass        表达式的类型
     */
    private void generateLambdaExpressionParameterFromCons(LambdaExpression lambdaExpression, Class<?> typeClass) {
        if (lambdaExpression != null) {
            if (lambdaExpression.getParameters().length == 0) {
                lambdaExpression.addParameter(Expression.parameter("this", typeClass));
            }
        }
    }
}

