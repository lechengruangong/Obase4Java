/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提供访问对象上下文的快捷方式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-30 15:59:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.FunctionWithTwoArgs;
import io.obase.core.expression.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Obase Stream Api
 *
 * @param <T> 流内对象类型
 */
public interface ObaseStream<T> {

    /**
     * 按条件筛选
     * 此方法为延迟执行方法 调用时仅进行标记 调用终结方法时结算
     *
     * @param filterExpression 筛选表达式
     * @return 自身
     */
    ObaseStream<T> filter(final SerializedPredicate<T> filterExpression);

    /**
     * 按条件筛选
     * 此方法为延迟执行方法 调用时仅进行标记 调用终结方法时结算
     *
     * @param filterExpression 筛选表达式
     * @return 自身
     */
    ObaseStream<T> filter(LambdaExpression filterExpression);

    /**
     * 满足条件则将表达式加入filter
     *
     * @param condition        条件
     * @param filterExpression 表达式
     * @return 自身
     */
    ObaseStream<T> filterIf(boolean condition, final SerializedPredicate<T> filterExpression);

    /**
     * 满足条件则将表达式加入filter
     *
     * @param condition        条件
     * @param filterExpression 表达式
     * @return 自身
     */
    ObaseStream<T> filterIf(boolean condition, LambdaExpression filterExpression);

    /**
     * 映射为另一元素
     *
     * @param mapExpression 映射表达式
     * @param <R>           目标元素
     * @param targetClass   映射目标类型
     * @return 映射后的流
     */
    <R> ObaseStream<R> map(final SerializedFunction<T, R> mapExpression, Class<?> targetClass);

    /**
     * 平展映射为另一元素
     *
     * @param mapExpression 映射表达式
     * @param <R>           目标元素
     * @param targetClass   结果类型 注意此处的结果类型为List的泛型类型
     * @return 映射后的流
     */
    <R> ObaseStream<R> flatMap(final SerializedFunction<T, Iterable<R>> mapExpression, Class<R> targetClass);

    /**
     * 平展映射为另一元素
     *
     * @param getCollect 映射表达式
     * @param getResult  结果表达式
     * @param resultType 结果类型 注意此处的结果类型为List的泛型类型
     * @param <TCollect> 映射的对象类型
     * @param <TResult>  映射后的结果类型
     * @return 映射后的流
     */
    <TCollect, TResult> ObaseStream<TResult> flatMap(final SerializedFunction<T, Iterable<TCollect>> getCollect, final FunctionWithTwoArgs<T, TCollect, TResult> getResult, Class<TResult> resultType);

    /**
     * 筛选为不重复的对象
     *
     * @return 自身
     */
    ObaseStream<T> distinct();

    /**
     * 排序 会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    <R> ObaseStream<T> sorted(final SerializedFunction<T, R> get);

    /**
     * 排序 会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    <R> ObaseStream<T> sorted(final SerializedFunction<T, R> get, Comparator<T> comparable);

    /**
     * 子排序 不会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    <R> ObaseStream<T> thenSorted(final SerializedFunction<T, R> get);

    /**
     * 子排序 不会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    <R> ObaseStream<T> thenSorted(final SerializedFunction<T, R> get, Comparator<T> comparable);

    /**
     * 反向排序 会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    <R> ObaseStream<T> sortedDesc(final SerializedFunction<T, R> get);

    /**
     * 反向排序 会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    <R> ObaseStream<T> sortedDesc(final SerializedFunction<T, R> get, Comparator<T> comparable);

    /**
     * 反向子排序 不会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    <R> ObaseStream<T> thenSortedDesc(final SerializedFunction<T, R> get);

    /**
     * 反向子排序 不会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    <R> ObaseStream<T> thenSortedDesc(final SerializedFunction<T, R> get, Comparator<T> comparable);

    /**
     * 反序
     *
     * @return 反序后的结果流
     */
    ObaseStream<T> reverse();

    /**
     * 提取多少个元素
     *
     * @param maxSize 最多跳过的元素数量
     * @return 自身
     */
    ObaseStream<T> limit(int maxSize);

    /**
     * 跳过多少个元素
     *
     * @param n 跳过多少个
     * @return 自身
     */
    ObaseStream<T> skip(int n);

    /**
     * 求某个属性最小值
     *
     * @param get 某个属性
     * @return 最小值
     */
    <R> R min(final SerializedFunction<T, R> get);


    /**
     * 求某个属性最小值
     *
     * @param get 某个属性
     * @return 最大值
     */
    <R> R max(final SerializedFunction<T, R> get);


    /**
     * 求平均值
     *
     * @param intResult 参与的运算
     * @return 平均值
     */
    double avgInt(final SerializedIntResult<T> intResult);

    /**
     * 求平均值
     *
     * @param doubleResult 参与的运算
     * @return 平均值
     */
    double avg(final SerializedDoubleResult<T> doubleResult);

    /**
     * 求平均值
     *
     * @param floatResult 参与的运算
     * @return 平均值
     */
    float avg(final SerializedFloatResult<T> floatResult);

    /**
     * 求平均值
     *
     * @param bigDecimalResult 参与的运算
     * @return 平均值
     */
    BigDecimal avg(final SerializedBigDecimalResult<T> bigDecimalResult);

    /**
     * 求和
     *
     * @param intResult 表达式
     * @return 和
     */
    int sumInt(final SerializedIntResult<T> intResult);

    /**
     * 求和
     *
     * @param longResult 表达式
     * @return 和
     */
    long sum(final SerializedLongResult<T> longResult);

    /**
     * 求和
     *
     * @param doubleResult 表达式
     * @return 平均值
     */
    double sum(final SerializedDoubleResult<T> doubleResult);

    /**
     * 求和
     *
     * @param floatResult 参与的运算
     * @return 平均值
     */
    float sum(final SerializedFloatResult<T> floatResult);

    /**
     * 求平均值
     *
     * @param bigDecimalResult 参与的运算
     * @return 平均值
     */
    BigDecimal sum(final SerializedBigDecimalResult<T> bigDecimalResult);


    /**
     * 计数
     *
     * @param predicate 计数条件
     * @return 数量
     */
    long count(final SerializedPredicate<T> predicate);

    /**
     * 计数
     *
     * @param predicate 计数条件
     * @return 数量
     */
    long count(LambdaExpression predicate);

    /**
     * 计数
     *
     * @return 计数
     */
    long count();

    /**
     * 任意匹配
     *
     * @return 是否满足任意匹配
     */
    boolean anyMatch();

    /**
     * 任意匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足任意匹配
     */
    boolean anyMatch(final SerializedPredicate<T> predicate);

    /**
     * 任意匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足任意匹配
     */
    boolean anyMatch(LambdaExpression predicate);

    /**
     * 全部匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足全部匹配
     */
    boolean allMatch(final SerializedPredicate<T> predicate);

    /**
     * 全部匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足全部匹配
     */
    boolean allMatch(LambdaExpression predicate);

    /**
     * 第一个满足条件的对象
     *
     * @return 第一个满足条件的对象
     */
    Optional<T> findFirst();

    /**
     * 第一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    Optional<T> findFirst(final SerializedPredicate<T> predicate);

    /**
     * 第一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    Optional<T> findFirst(LambdaExpression predicate);

    /**
     * 最后一个满足条件的对象
     *
     * @return 第一个满足条件的对象
     */
    Optional<T> findLast();

    /**
     * 最后一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    Optional<T> findLast(final SerializedPredicate<T> predicate);

    /**
     * 最后一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    Optional<T> findLast(LambdaExpression predicate);


    /**
     * 获取指定位置的元素
     *
     * @param index 索引
     * @return 指定位置的元素
     */
    Optional<T> elementAt(int index);

    /**
     * 取符合条件的某个单个对象
     *
     * @return 单个对象
     */
    Optional<T> single();

    /**
     * 取符合条件的某个单个对象
     *
     * @param predicate 条件
     * @return 单个对象
     */
    Optional<T> single(final SerializedPredicate<T> predicate);

    /**
     * 取符合条件的某个单个对象
     *
     * @param predicate 条件
     * @return 单个对象
     */
    Optional<T> single(LambdaExpression predicate);

    /**
     * 强制包含
     *
     * @param includeExpression 表达式
     * @param <R>               要包含的对象
     * @return 自身
     */
    <R> ObaseStream<T> include(final SerializedFunction<T, R> includeExpression);

    /**
     * 强制包含
     *
     * @param includeExpression 字符串形式的表达式
     * @param <R>               要包含的对象
     * @return 自身
     */
    <R> ObaseStream<T> include(String includeExpression);

    /**
     * 分组
     *
     * @param getKey 键属性的表达式
     * @param <TKey> 分组键
     * @return 分组结果
     */
    <TKey> ObaseStream<IGroupingBy<TKey, T>> groupBy(final SerializedFunction<T, TKey> getKey);

    /**
     * 分组
     *
     * @param getKey     键属性的表达式
     * @param getElement 元素属性的表达式
     * @param <TKey>     分组键
     * @return 分组结果流
     */
    <TKey, TElement> ObaseStream<IGroupingBy<TKey, TElement>> groupBy(final SerializedFunction<T, TKey> getKey, final SerializedFunction<T, TElement> getElement);

    /**
     * 分组并投影
     *
     * @param getKey     键属性的表达式
     * @param getResult  结果的表达式
     * @param <TKey>     分组键
     * @param <TResult>  分组结果
     * @param resultType 结果类型
     * @return 分组结果流
     */
    <TKey, TResult> ObaseStream<TResult> groupBy(final SerializedFunction<T, TKey> getKey, final FunctionWithTwoArgs<TKey, IAggregation<T>, TResult> getResult, Class<?> resultType);

    /**
     * 分组并投影
     *
     * @param getKey     键属性的表达式
     * @param getElement 元素属性的表达式
     * @param getResult  结果的表达式
     * @param <TKey>     分组键
     * @param <TElement> 分组元素
     * @param <TResult>  分组结果
     * @param resultType 结果类型
     * @return 分组结果流
     */
    <TKey, TElement, TResult> ObaseStream<TResult> groupBy(final SerializedFunction<T, TKey> getKey, final SerializedFunction<T, TElement> getElement, final FunctionWithTwoArgs<TKey, IAggregation<TElement>, TResult> getResult, Class<?> resultType);

    /**
     * 转换为数组
     * 此方法为终结方法 会结算所有的操作
     *
     * @return 数组
     */
    T[] toArray();

    /**
     * 转换为列表
     * 此方法为终结方法 会结算所有的操作
     *
     * @return 列表
     */
    List<T> toList();

    /**
     * 转换为HashMap 相同键的结果保留一个
     *
     * @param getKey    键属性的表达式
     * @param getResult 结果属性的表达式
     * @param <TKey>    键
     * @param <TResult> 结果
     * @return 分组结果HashMap 相同键的结果保留一个
     */
    <TKey, TResult> HashMap<TKey, TResult> toHashMap(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TResult> getResult);


    /**
     * 转换为HashMap 并将相同键的结果放入Iterable中
     *
     * @param getKey    键属性的表达式
     * @param getResult 结果属性的表达式
     * @param <TKey>    键
     * @param <TResult> 结果
     * @return 分组结果HashMap 相同键的结果放入Iterable中
     */
    <TKey, TResult> HashMap<TKey, Iterable<TResult>> toHashMapWithIterableResult(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TResult> getResult);

}
