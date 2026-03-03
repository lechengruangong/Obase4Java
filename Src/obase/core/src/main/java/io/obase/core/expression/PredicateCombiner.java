/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：谓词条件合并器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:31:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import io.obase.core.odm.PrimitiveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 谓词条件合并器
 */
public class PredicateCombiner<T> {

    /**
     * 表达式翻译器
     */
    private final LambdaTranslator translator = new LambdaTranslator();

    /**
     * 拼合包装器
     */
    private final Wrapper wrapper = new Wrapper(this.translator);

    /**
     * 结果表达式
     */
    private LambdaExpression lambdaExpression;

    /**
     * 构造谓词条件合并器
     *
     * @param filterExpression 初始表达式 可以为null
     */
    public PredicateCombiner(final SerializedPredicate<T> filterExpression) {
        this.lambdaExpression = filterExpression == null ? null : this.translator.getLambdaExpression(filterExpression);
    }

    /**
     * 构造谓词条件合并器
     */
    public PredicateCombiner() {
        this.lambdaExpression = null;
    }

    /**
     * 静态方法 用And运算拼合表达式
     * 可以将两个拼合器的结果再次拼合成复合条件
     *
     * @param left  左表达式
     * @param right 右表达式
     * @return 拼合后的结果
     */
    public static LambdaExpression and(LambdaExpression left, LambdaExpression right) {
        //处理参数列表
        List<ParameterExpression> parameterExpressionList = getParameterExpressions(left, right);
        return Expression.lambda(parameterExpressionList.toArray(parameterExpressionList.toArray(new ParameterExpression[0])), Expression.and(left.getBody(), right.getBody(), Boolean.class));
    }

    /**
     * 静态方法 用Or运算条件拼合表达式
     * 可以将两个拼合器的结果再次拼合成复合条件
     *
     * @param left  左表达式
     * @param right 右表达式
     * @return 拼合后的结果
     */
    public static LambdaExpression or(LambdaExpression left, LambdaExpression right) {
        //处理参数列表
        List<ParameterExpression> parameterExpressionList = getParameterExpressions(left, right);
        return Expression.lambda(parameterExpressionList.toArray(parameterExpressionList.toArray(new ParameterExpression[0])), Expression.or(left.getBody(), right.getBody(), Boolean.class));
    }

    /**
     * 提取表达式中的参数
     *
     * @param lambdaExpression 要提取的表达式
     * @param second           目标表达式
     * @return 提取后的表达式
     */
    private static List<ParameterExpression> getParameterExpressions(LambdaExpression lambdaExpression, LambdaExpression second) {
        //处理参数列表
        List<ParameterExpression> parameterExpressionList = new ArrayList<>(Arrays.asList(lambdaExpression.getParameters()));
        for (ParameterExpression parameterExpression : second.getParameters()) {
            if (parameterExpressionList.stream().noneMatch(p -> p.getName().equals(parameterExpression.getName()) && p.getType().equals(parameterExpression.getType())))
                parameterExpressionList.add(parameterExpression);
        }
        //检测是否为多种主查询类型混合
        if (parameterExpressionList.stream().filter(ParameterExpression::getIsHost).collect(Collectors.groupingBy(ParameterExpression::getType)).size() > 1)
            throw new PredicateCombinerException("无法拼合条件:暂不支持多个查询类型的条件拼合.");
        return parameterExpressionList;
    }

    /**
     * 表示要进行与运算
     *
     * @param filterExpression 要进行与运算的表达式
     * @return 自身
     */
    public PredicateCombiner<T> and(final SerializedPredicate<T> filterExpression) {
        LambdaExpression second = this.translator.getLambdaExpression(filterExpression);
        this.merge(second, EExpressionType.AndAlso);
        return this;
    }

    /**
     * 表示要进行与运算
     *
     * @param second 要拼合的表达式
     * @return 自身
     */
    public PredicateCombiner<T> and(LambdaExpression second) {
        this.merge(second, EExpressionType.AndAlso);
        return this;
    }

    /**
     * 表示要进行与运算
     *
     * @param getExpression 成员表达式
     * @param predicateType 谓词逻辑类型
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> and(final SerializedFunction<T, R> getExpression, EPredicateType predicateType, Object value) {
        LambdaExpression second = this.wrapExpression(getExpression, predicateType, value);
        this.merge(second, EExpressionType.AndAlso);
        return this;
    }

    /**
     * 表示要进行或运算
     *
     * @param filterExpression 要进行或运算的表达式
     * @return 自身
     */
    public PredicateCombiner<T> or(final SerializedPredicate<T> filterExpression) {
        LambdaExpression second = this.translator.getLambdaExpression(filterExpression);
        this.merge(second, EExpressionType.OrElse);
        return this;
    }

    /**
     * 表示要进行或运算
     *
     * @param second 要拼合的表达式
     * @return 自身
     */
    public PredicateCombiner<T> or(LambdaExpression second) {
        this.merge(second, EExpressionType.OrElse);
        return this;
    }

    /**
     * 表示要进行或运算
     *
     * @param getExpression 成员表达式
     * @param predicateType 谓词逻辑类型
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> or(final SerializedFunction<T, R> getExpression, EPredicateType predicateType, Object value) {
        LambdaExpression second = this.wrapExpression(getExpression, predicateType, value);
        this.merge(second, EExpressionType.OrElse);
        return this;
    }

    /**
     * 表示要将相等运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> equal(final SerializedFunction<T, R> getExpression, Object value) {
        return this.equal(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将相等运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> equal(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.Equal, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将不相等运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> notEqual(final SerializedFunction<T, R> getExpression, Object value) {
        return this.notEqual(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将不相等运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> notEqual(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.NotEqual, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将大于运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> greaterThan(final SerializedFunction<T, R> getExpression, Object value) {
        return this.greaterThan(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将大于运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> greaterThan(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.GreaterThan, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将大于等于运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> greaterThanOrEqual(final SerializedFunction<T, R> getExpression, Object value) {
        return this.greaterThanOrEqual(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将大于等于运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> greaterThanOrEqual(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.GreaterThanOrEqual, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将小于运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> lessThan(final SerializedFunction<T, R> getExpression, Object value) {
        return this.lessThan(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将小于运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> lessThan(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.LessThan, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将小于等于运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> lessThanOrEqual(final SerializedFunction<T, R> getExpression, Object value) {
        return this.lessThanOrEqual(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将小于等于运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> lessThanOrEqual(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.LessThanOrEqual, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将包含运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> contains(final SerializedFunction<T, R> getExpression, Object value) {
        return this.contains(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将包含运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> contains(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.Contains, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将以某某开头运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> startsWith(final SerializedFunction<T, R> getExpression, Object value) {
        return this.startsWith(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将以某某开头运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> startsWith(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.StartWith, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 表示要将以某某结尾运算使用且进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> endsWith(final SerializedFunction<T, R> getExpression, Object value) {
        return this.endsWith(getExpression, value, ECombineType.And);
    }

    /**
     * 表示要将以某某结尾运算使用指定的拼合方式进行拼合
     *
     * @param getExpression 成员表达式
     * @param value         值
     * @param <R>           属性
     * @return 自身
     */
    public <R> PredicateCombiner<T> endsWith(final SerializedFunction<T, R> getExpression, Object value, ECombineType combineType) {
        LambdaExpression second = this.wrapExpression(getExpression, EPredicateType.EndWith, value);
        this.combine(second, combineType);
        return this;
    }

    /**
     * 获取结果表达式
     *
     * @return 结果表达式
     */
    public LambdaExpression getLambdaExpression() {
        return this.lambdaExpression;
    }

    /**
     * 获取包装器
     *
     * @return 包装器
     */
    public Wrapper getWrapper() {
        return this.wrapper;
    }

    /**
     * 根据谓词类型合并合并
     *
     * @param second      要合并的表达式
     * @param combineType 合并的类型 And或者Or
     */
    private void combine(LambdaExpression second, ECombineType combineType) {
        switch (combineType) {
            case And:
                this.merge(second, EExpressionType.AndAlso);
                break;
            case Or:
                this.merge(second, EExpressionType.OrElse);
                break;
        }
    }

    /**
     * 根据表达式类型合并
     *
     * @param second         要合并的表达式
     * @param expressionType 合并的类型 AndAlso或者OrElse
     */
    private void merge(LambdaExpression second, EExpressionType expressionType) {
        if (this.needMerge(second)) {
            List<ParameterExpression> parameterExpressionList = getParameterExpressions(this.lambdaExpression, second);
            //实际拼合
            if (expressionType == EExpressionType.AndAlso) {
                this.lambdaExpression = Expression.lambda(parameterExpressionList.toArray(parameterExpressionList.toArray(new ParameterExpression[0])), Expression.and(this.lambdaExpression.getBody(), second.getBody(), Boolean.class));
            } else if (expressionType == EExpressionType.OrElse) {
                this.lambdaExpression = Expression.lambda(parameterExpressionList.toArray(parameterExpressionList.toArray(new ParameterExpression[0])), Expression.or(this.lambdaExpression.getBody(), second.getBody(), Boolean.class));
            } else {
                throw new PredicateCombinerException("无法拼合条件:暂不支持" + expressionType + "类型的条件拼合.");
            }
        }
    }

    /**
     * 判断是否需要合并
     *
     * @param second 要合并的运算
     * @return 是否需要合并
     */
    private boolean needMerge(LambdaExpression second) {
        if (this.lambdaExpression == null && second == null)
            throw new PredicateCombinerException("无法拼合条件:源表达式和目标表达式均为空.");
        if (this.lambdaExpression == null) {
            this.lambdaExpression = second;
            return false;
        }
        return second != null;
    }

    /**
     * 使用包装器获取具体的表达式
     *
     * @param getExpression 成员表达式
     * @param predicateType 谓词逻辑类型
     * @param value         值
     * @param <R>           属性
     * @return 解析后的表达式
     */
    private <R> LambdaExpression wrapExpression(final SerializedFunction<T, R> getExpression, EPredicateType predicateType, Object value) {
        switch (predicateType) {
            case Equal:
                return this.wrapper.eq(getExpression, value);
            case NotEqual:
                return this.wrapper.ne(getExpression, value);
            case LessThan:
                return this.wrapper.lt(getExpression, value);
            case GreaterThan:
                return this.wrapper.gt(getExpression, value);
            case LessThanOrEqual:
                return this.wrapper.le(getExpression, value);
            case GreaterThanOrEqual:
                return this.wrapper.ge(getExpression, value);
            case Contains:
                return this.wrapper.cs(getExpression, value);
            case StartWith:
                return this.wrapper.sw(getExpression, value);
            case EndWith:
                return this.wrapper.ew(getExpression, value);
        }
        throw new PredicateCombinerException("无法拼合条件:未知的谓词逻辑类型" + predicateType + ".");
    }

    /**
     * 条件拼合器的包装器
     */
    public class Wrapper {

        /**
         * 表达式翻译器
         */
        private final LambdaTranslator translator;

        /**
         * 初始化包装器
         *
         * @param translator 表达式翻译器
         */
        public Wrapper(LambdaTranslator translator) {
            this.translator = translator;
        }

        /**
         * 构造一个内含相等的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression eq(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.Equal, value);
            return Expression.lambda(expression.getParameters(), Expression.equal(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含不相等的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression ne(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.NotEqual, value);
            return Expression.lambda(expression.getParameters(), Expression.notEqual(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含小于的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression lt(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.LessThan, value);
            return Expression.lambda(expression.getParameters(), Expression.lessThan(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含大于的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression gt(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.GreaterThan, value);
            return Expression.lambda(expression.getParameters(), Expression.greaterThan(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含小于等于的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression le(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.LessThanOrEqual, value);
            return Expression.lambda(expression.getParameters(), Expression.lessThanOrEqual(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含大于等于的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression ge(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.GreaterThanOrEqual, value);
            return Expression.lambda(expression.getParameters(), Expression.greaterThanOrEqual(expression.getBody(), Expression.constant(this.getEnumOrdinal(value)), Boolean.class));
        }

        /**
         * 构造一个内含Contains的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression cs(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.Contains, value);
            try {
                return Expression.lambda(expression.getParameters(), Expression.call(new Expression[]{Expression.constant(this.getEnumOrdinal(value))}, String.class.getMethod("contains", CharSequence.class), expression.getBody()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法获取String的contains方法签名", e);
            }
        }

        /**
         * 构造一个内含StartWith的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression sw(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.StartWith, value);
            try {
                return Expression.lambda(expression.getParameters(), Expression.call(new Expression[]{Expression.constant(this.getEnumOrdinal(value))}, String.class.getMethod("startsWith", String.class), expression.getBody()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法获取String的startsWith方法签名", e);
            }
        }

        /**
         * 构造一个内含EndWith的二元Lambda表达式
         *
         * @param getExpression 成员表达式
         * @param value         值
         * @param <R>           成员
         * @return 二元Lambda表达式
         */
        public <R> LambdaExpression ew(final SerializedFunction<T, R> getExpression, Object value) {
            LambdaExpression expression = this.translator.getLambdaExpression(getExpression);
            this.checkExpression(expression, EPredicateType.EndWith, value);
            try {
                return Expression.lambda(expression.getParameters(), Expression.call(new Expression[]{Expression.constant(this.getEnumOrdinal(value))}, String.class.getMethod("endsWith", String.class), expression.getBody()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法获取String的endsWith方法签名", e);
            }
        }

        /**
         * 检查表达式是否合规方法
         *
         * @param expression    表达式
         * @param predicateType 谓词逻辑类型
         * @param value         值
         */
        private void checkExpression(LambdaExpression expression, EPredicateType predicateType, Object value) {
            Expression exp = expression.getBody();
            //检查表达式
            if (!(exp instanceof MemberExpression)) {
                throw new PredicateCombinerException("无法拼合条件:要拼合的表达式必须是成员表达式.");
            }
            MemberExpression memberExpression = (MemberExpression) exp;
            //如果是空值
            if (value == null) {
                if (!this.checkNullCombineType(predicateType, memberExpression.getType()))
                    throw new PredicateCombinerException("无法拼合条件:要拼合的表达式的成员类型" + memberExpression.getType().getSimpleName() + "不支持与空值进行" + predicateType + "谓词类型的运算.");
                return;
            }
            //不是空值 继续检查
            Class<?> valueType = value.getClass();
            //类型是否符合
            if (!(memberExpression.getType().equals(valueType) || this.getWrapperClass(memberExpression.getType()).equals(valueType))) {
                throw new PredicateCombinerException("无法拼合条件:要拼合的表达式的类型是" + memberExpression.getType().getSimpleName() + ",与传入的对象类型" + valueType.getSimpleName() + "不符.");
            }
            //是否是基元类型
            if (!PrimitiveType.isObasePrimitive(valueType)) {
                throw new PredicateCombinerException("无法拼合条件:要拼合的条件参数必须是Obase定义的基元类型.");
            }
            //谓词运算是否支持
            if (!this.checkCombineType(predicateType, valueType)) {
                throw new PredicateCombinerException("无法拼合条件:要拼合的表达式的成员类型" + valueType.getSimpleName() + "不支持进行谓词类型" + predicateType + "的运算.");
            }
        }

        /**
         * 检查谓词逻辑类型是否受此类型的支持
         *
         * @param predicateType 谓词逻辑类型
         * @param valueType     值类型
         * @return 是否支持此谓词类型
         */
        private boolean checkCombineType(EPredicateType predicateType, Class<?> valueType) {
            //数值和时间类型支持 等于 不等于 大于 小于 大于等于 小于等于
            boolean numberAndTimeSupport = predicateType == EPredicateType.Equal || predicateType == EPredicateType.NotEqual || predicateType == EPredicateType.LessThan || predicateType == EPredicateType.GreaterThan
                    || predicateType == EPredicateType.LessThanOrEqual || predicateType == EPredicateType.GreaterThanOrEqual;
            //布尔值,UUID和枚举类型支持 等于 不等于
            boolean booleanUUIDAndEnumSupport = predicateType == EPredicateType.Equal || predicateType == EPredicateType.NotEqual;
            //字符串支持 等于 不等于 包含 以XX开头 以XX结尾
            boolean stringSupport = predicateType == EPredicateType.Equal || predicateType == EPredicateType.NotEqual || predicateType == EPredicateType.Contains || predicateType == EPredicateType.StartWith
                    || predicateType == EPredicateType.EndWith;
            //数值类型
            if (valueType.equals(int.class) || valueType.equals(Integer.class) || valueType.equals(short.class) || valueType.equals(Short.class) || valueType.equals(long.class) || valueType.equals(Long.class)
                    || valueType.equals(char.class) || valueType.equals(Character.class) || valueType.equals(double.class) || valueType.equals(Double.class) || valueType.equals(float.class) || valueType.equals(Float.class)
                    || valueType.equals(byte.class) || valueType.equals(Byte.class) || valueType.equals(Date.class) || valueType.equals(LocalDateTime.class) || valueType.equals(LocalDate.class) || valueType.equals(LocalTime.class)
                    || valueType.equals(BigDecimal.class)) {
                return numberAndTimeSupport;
            }
            //布尔 UUID 枚举
            if (valueType.equals(boolean.class) || valueType.equals(Boolean.class) || valueType.equals(UUID.class) || valueType.isEnum()) {
                return booleanUUIDAndEnumSupport;
            }
            //字符串
            if (valueType.equals(String.class)) {
                return stringSupport;
            }
            return false;
        }

        /**
         * 检查谓词逻辑类型是否受空值类型的支持
         *
         * @param predicateType 谓词逻辑类型
         * @param valueType     Get类型
         * @return 是否支持此谓词类型
         */
        private boolean checkNullCombineType(EPredicateType predicateType, Class<?> valueType) {
            //空值支持 等于 不等于
            boolean nullSupported = predicateType == EPredicateType.Equal || predicateType == EPredicateType.NotEqual;
            //包装类 字符串 日期 可以与空值比较
            if (valueType.equals(Integer.class) || valueType.equals(Short.class) || valueType.equals(Long.class) || valueType.equals(Character.class) || valueType.equals(Double.class) || valueType.equals(Float.class)
                    || valueType.equals(Byte.class) || valueType.equals(Date.class) || valueType.equals(LocalDateTime.class) || valueType.equals(LocalDate.class) || valueType.equals(LocalTime.class) || valueType.equals(String.class)
                    || valueType.equals(BigDecimal.class))
                return nullSupported;
            return false;
        }

        /**
         * 将value类型转换为其相应的包装类型
         *
         * @param valueType 值类型
         * @return 相应的包装类型
         */
        private Class<?> getWrapperClass(Class<?> valueType) {
            if (valueType.equals(int.class)) {
                return Integer.class;
            }
            if (valueType.equals(short.class)) {
                return Short.class;
            }
            if (valueType.equals(long.class)) {
                return Long.class;
            }
            if (valueType.equals(char.class)) {
                return Character.class;
            }
            if (valueType.equals(double.class)) {
                return Double.class;
            }
            if (valueType.equals(float.class)) {
                return Float.class;
            }
            if (valueType.equals(byte.class)) {
                return Byte.class;
            }
            if (valueType.equals(boolean.class)) {
                return Boolean.class;
            }
            return valueType;
        }

        /**
         * 处理枚举值
         *
         * @param value 值
         * @return 如果是枚举 返回ordinal 否则返回本身
         */
        private Object getEnumOrdinal(Object value) {
            if (value != null && value.getClass().isEnum()) {
                List<Enum<?>> enumList = MethodChecker.getInstance().enums.get(value.getClass().getName().replace('.', '/'));
                for (Enum<?> e : enumList) {
                    if (e.equals(value))
                        return e.ordinal();
                }
            }
            return value;
        }
    }
}
