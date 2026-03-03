/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:23:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.*;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.PrimitiveType;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.TypeBase;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeHeterogeneityPredicater;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.HeterogeneityPredicationProvider;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.query.oop.OopExecutor;
import io.obase.core.query.oop.OopPipelineBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * 表示查询运算。
 * 对对象集的一次操作称为一次查询运算，System.Linq.Queryable类定义的扩展方法定义了绝大多数查询运算。当前支持的所有查询运算请参见eQueryOpName枚举。
 * 多个查询运算串联构成一个查询链，或称查询表达式。运算串联是指前一个运算的结果作为后一个的源。
 * QueryOp类是对查询运算的描述，记载查询的名称、查询源类型、参数等信息，同时引用查询链中的下一个运算
 */
public abstract class QueryOp implements Cloneable {

    /**
     * 查询运算的名称
     */
    private final EQueryOpName name;


    /**
     * 查询源的类型
     */
    private final Class<?> sourceType;
    /**
     * 适用于查询运算的对象数据模型
     */
    protected ObjectDataModel model;
    /**
     * 查询运算中隐含的包含运算，称为隐含包含
     * 如果一个查询运算虽未显示要求包含一个引用，但该运算的执行依赖于该引用，则称该查询运算隐含包含该引用
     */
    private AssociationTree impliedIncluding;
    /**
     * 查询链中的下一个运算
     */
    private QueryOp next;

    /**
     * 寄存器（寄存查询链的尾部节点）
     */
    private QueryOp tail;

    /**
     * 创建QueryOp的新实例
     *
     * @param name       运算名称
     * @param sourceType 查询源的类型
     */
    protected QueryOp(EQueryOpName name, Class<?> sourceType) {
        this.name = name;
        this.sourceType = sourceType;
    }

    /**
     * 创建表示Accumulate运算的QueryOp实例
     *
     * @param accumulator    累加函数
     * @param seed           种子值
     * @param resultSelector 结果函数，用于将累加器的最终值转换为结果值
     * @return Accumulate运算
     */
    public static QueryOp accumulate(LambdaExpression accumulator, Object seed, LambdaExpression resultSelector, ObjectDataModel model) {
        return accumulate(accumulator, seed, resultSelector, model, null);
    }

    /**
     * 创建表示Accumulate运算的QueryOp实例
     *
     * @param accumulator    累加函数
     * @param seed           种子值
     * @param resultSelector 结果函数，用于将累加器的最终值转换为结果值
     * @param nextOp         后续运算
     * @return Accumulate运算
     */
    public static QueryOp accumulate(LambdaExpression accumulator, Object seed, LambdaExpression resultSelector, ObjectDataModel model, QueryOp nextOp) {
        AccumulateOp accumulateOp = new AccumulateOp(accumulator, seed, resultSelector, model);
        accumulateOp.setNext(nextOp);
        return accumulateOp;
    }

    /**
     * 创建表示All运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     * @return All运算
     */
    public static QueryOp all(LambdaExpression predicate, ObjectDataModel model) {
        return all(predicate, model, null);
    }

    /**
     * 创建表示All运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     * @param nextOp    后续运算
     * @return All运算
     */
    public static QueryOp all(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        AllOp allOp = new AllOp(predicate, model);
        allOp.setNext(nextOp);
        return allOp;
    }

    /**
     * 创建表示Any运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     * @return Any运算
     */
    public static QueryOp any(LambdaExpression predicate, ObjectDataModel model) {
        return any(predicate, model, null);
    }

    /**
     * 创建表示Any运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试元素是否满足条件
     * @param nextOp    后续运算
     * @return Any运算
     */
    public static QueryOp any(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        AnyOp anyOp = new AnyOp(predicate, model);
        anyOp.setNext(nextOp);
        return anyOp;
    }

    /**
     * 创建表示Any运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @return Any运算
     */
    public static QueryOp any(Class<?> sourceType, ObjectDataModel model) {
        return any(sourceType, model, null);
    }

    /**
     * 创建表示Any运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @param nextOp     后续运算
     * @return Any运算
     */
    public static QueryOp any(Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        AnyOp anyOp = new AnyOp(sourceType);
        anyOp.setNext(nextOp);
        anyOp.model = model;
        return anyOp;
    }

    /**
     * 创建表示运算符为Average的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @return Average运算
     */
    public static QueryOp average(LambdaExpression selector, ObjectDataModel model) {
        return average(selector, model, null);
    }

    /**
     * 创建表示运算符为Average的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @param nextOp   后续运算
     * @return Average运算
     */
    public static QueryOp average(LambdaExpression selector, ObjectDataModel model, QueryOp nextOp) {
        ArithAggregateOp arithAggregateOp = new ArithAggregateOp(EAggregationOperator.Average, model, selector);
        arithAggregateOp.setNext(nextOp);
        return arithAggregateOp;
    }

    /**
     * 创建表示Cast运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param resultType 转换目标类型
     * @return Cast运算
     */
    public static QueryOp cast(Class<?> resultType, Class<?> sourceType, ObjectDataModel model) {
        return cast(resultType, sourceType, model, null);
    }

    /**
     * 创建表示Cast运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param resultType 转换目标类型
     * @param nextOp     后续运算
     * @return Cast运算
     */
    public static QueryOp cast(Class<?> resultType, Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        CastOp castOp = new CastOp(resultType, sourceType);
        castOp.setNext(nextOp);
        castOp.model = model;
        return castOp;
    }

    /**
     * 创建表示Contains运算的QueryOp实例
     *
     * @param item     要在序列中查找的对象
     * @param comparer 相等比较器，用于测试两个元素是否相等
     * @return Contains运算
     */
    public static QueryOp contains(Object item, Comparator<?> comparer, ObjectDataModel model) {
        return contains(item, comparer, model, null);
    }

    /**
     * 创建表示Contains运算的QueryOp实例
     *
     * @param item     要在序列中查找的对象
     * @param comparer 相等比较器，用于测试两个元素是否相等
     * @param nextOp   后续运算
     * @return Contains运算
     */
    public static QueryOp contains(Object item, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        ContainsOp containsOp = new ContainsOp(item, comparer, item.getClass());
        containsOp.setNext(nextOp);
        containsOp.model = model;
        return containsOp;
    }

    /**
     * 创建表示Count运算的QueryOp实例
     *
     * @param predicate 断言函数，用于判定元素是否参与计数
     * @return Count运算
     */
    public static QueryOp counts(LambdaExpression predicate, ObjectDataModel model) {
        return counts(predicate, model, null);
    }

    /**
     * 创建表示Count运算的QueryOp实例
     *
     * @param predicate 断言函数，用于判定元素是否参与计数
     * @param nextOp    后续运算
     * @return Count运算
     */
    public static QueryOp counts(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        CountOp countOp = new CountOp(predicate, model);
        countOp.setNext(nextOp);
        countOp.model = model;
        return countOp;
    }

    /**
     * 创建表示Count运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @return Count运算
     */
    public static QueryOp counts(Class<?> sourceType, ObjectDataModel model) {
        return counts(sourceType, model, null);
    }

    /**
     * 创建表示Count运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @param nextOp     后续运算
     * @return Count运算
     */
    public static QueryOp counts(Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        CountOp countOp = new CountOp(sourceType);
        countOp.setNext(nextOp);
        countOp.model = model;
        return countOp;
    }

    /**
     * 创建表示DefaultIfEmpty运算的QueryOp实例
     *
     * @param sourceType   查询源类型
     * @param defaultValue 序列为空时要返回的值
     * @return DefaultIfEmpty运算
     */
    public static QueryOp defaultIfEmpty(Class<?> sourceType, Object defaultValue, ObjectDataModel model) {
        return defaultIfEmpty(sourceType, defaultValue, model, null);
    }

    /**
     * 创建表示DefaultIfEmpty运算的QueryOp实例
     *
     * @param sourceType   查询源类型
     * @param defaultValue 序列为空时要返回的值
     * @param nextOp       后续运算
     * @return DefaultIfEmpty运算
     */
    public static QueryOp defaultIfEmpty(Class<?> sourceType, Object defaultValue, ObjectDataModel model, QueryOp nextOp) {
        DefaultIfEmptyOp defaultIfEmptyOp = new DefaultIfEmptyOp(sourceType, defaultValue);
        defaultIfEmptyOp.setNext(nextOp);
        defaultIfEmptyOp.model = model;
        return defaultIfEmptyOp;
    }

    /**
     * 创建表示Distinct运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param comparer   相等比较器，用于测试两个元素是否相等
     * @return Distinct运算
     */
    public static QueryOp distinct(Class<?> sourceType, Comparator<?> comparer, ObjectDataModel model) {
        return distinct(sourceType, comparer, model, null);
    }

    /**
     * 创建表示Distinct运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param comparer   相等比较器，用于测试两个元素是否相等
     * @param nextOp     后续运算
     * @return Distinct运算
     */
    public static QueryOp distinct(Class<?> sourceType, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        DistinctOp distinctOp = new DistinctOp(sourceType, comparer);
        distinctOp.setNext(nextOp);
        distinctOp.model = model;
        return distinctOp;
    }

    /**
     * 创建表示ElementAt运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param index         要检索的从零开始的元素索引
     * @param returnDefault 指示当指定索引处无元素时是否返回默认值
     * @return ElementAt运算
     */
    public static QueryOp elementAt(Class<?> sourceType, int index, boolean returnDefault, ObjectDataModel model) {
        return elementAt(sourceType, index, returnDefault, model, null);
    }

    /**
     * 创建表示ElementAt运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param index         要检索的从零开始的元素索引
     * @param returnDefault 指示当指定索引处无元素时是否返回默认值
     * @param nextOp        后续运算
     * @return ElementAt运算
     */
    public static QueryOp elementAt(Class<?> sourceType, int index, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        ElementAtOp elementAtOp = new ElementAtOp(sourceType, index, returnDefault);
        elementAtOp.setNext(nextOp);
        elementAtOp.model = model;
        return elementAtOp;
    }

    /**
     * 创建表示无参运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @return 无参运算
     */
    public static QueryOp every(Class<?> sourceType, ObjectDataModel model) {
        return every(sourceType, model, null);
    }

    /**
     * 创建表示无参运算的QueryOp实例
     *
     * @param sourceType 源类型
     * @param nextOp     后续运算
     * @return 无参运算
     */
    public static QueryOp every(Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        EveryOp everyOp = new EveryOp(sourceType);
        everyOp.setNext(nextOp);
        everyOp.model = model;
        return everyOp;
    }

    /**
     * 创建表示First运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @return First运算
     */
    public static QueryOp first(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        return first(predicate, returnDefault, model, null);
    }

    /**
     * 创建表示First运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @param nextOp        后续运算
     * @return First运算
     */
    public static QueryOp first(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        FirstOp firstOp = new FirstOp(predicate, model, returnDefault);
        firstOp.setNext(nextOp);
        firstOp.model = model;
        return firstOp;
    }

    /**
     * 创建表示First运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @return First运算
     */
    public static QueryOp first(Class<?> sourceType, boolean returnDefault, ObjectDataModel model) {
        return first(sourceType, returnDefault, model, null);
    }

    /**
     * 创建表示First运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @param nextOp        后续运算
     * @return First运算
     */
    public static QueryOp first(Class<?> sourceType, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        FirstOp firstOp = new FirstOp(sourceType, returnDefault);
        firstOp.setNext(nextOp);
        firstOp.model = model;
        return firstOp;
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector 鍵函数，用于从每个元素提取分组鍵
     * @param comparer    相等比较器，用于测试两个分组鍵是否相等
     * @return Group运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, Comparator<?> comparer, ObjectDataModel model) {
        return groupBy(keySelector, comparer, model, null);
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector 鍵函数，用于从每个元素提取分组鍵
     * @param comparer    相等比较器，用于测试两个分组鍵是否相等
     * @param nextOp      后续运算
     * @return Group运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        GroupOp groupOp = new GroupOp(keySelector, comparer, null, model);
        groupOp.setNext(nextOp);
        groupOp.model = model;
        return groupOp;
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     * @param comparer        相等比较器，用于测试两个分组鍵是否相等
     * @return Group运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, LambdaExpression elementSelector, Comparator<?> comparer, ObjectDataModel model) {
        return groupBy(keySelector, elementSelector, comparer, model, null);
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     * @param comparer        相等比较器，用于测试两个分组鍵是否相等
     * @param nextOp          后续运算
     * @return Group运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, LambdaExpression elementSelector, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        GroupOp groupOp = new GroupOp(keySelector, comparer, elementSelector, model);
        groupOp.setNext(nextOp);
        groupOp.model = model;
        return groupOp;
    }

    /**
     * 创建表示GroupAggregationOp运算的QueryOp实例
     *
     * @param keySelector    鍵函数，用于从每个元素提取分组鍵
     * @param resultSelector 聚合投影函数，用于对每个组生成聚合值
     * @param comparer       相等比较器，用于测试两个分组鍵是否相等
     * @return GroupAggregationOp运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, Comparator<?> comparer, LambdaExpression resultSelector, ObjectDataModel model) {
        return groupBy(keySelector, comparer, resultSelector, model, null);
    }

    /**
     * 创建表示GroupAggregationOp运算的QueryOp实例
     *
     * @param keySelector    鍵函数，用于从每个元素提取分组鍵
     * @param resultSelector 聚合投影函数，用于对每个组生成聚合值
     * @param comparer       相等比较器，用于测试两个分组鍵是否相等
     * @param nextOp         后续运算
     * @return GroupAggregationOp运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, Comparator<?> comparer, LambdaExpression resultSelector, ObjectDataModel model, QueryOp nextOp) {
        GroupAggregationOp groupOp = new GroupAggregationOp(resultSelector, keySelector, comparer, null, model);
        groupOp.setNext(nextOp);
        groupOp.model = model;
        return groupOp;
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     * @param resultSelector  聚合投影函数，用于对每个组生成聚合值
     * @param comparer        相等比较器，用于测试两个分组鍵是否相等
     * @return GroupAggregationOp运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, LambdaExpression elementSelector,
                                  LambdaExpression resultSelector, Comparator<?> comparer, ObjectDataModel model) {
        return groupBy(keySelector, elementSelector, resultSelector, comparer, model, null);
    }

    /**
     * 创建表示Group运算的QueryOp实例
     *
     * @param keySelector     鍵函数，用于从每个元素提取分组鍵
     * @param elementSelector 组元素函数，用于从每个元素提取组元素
     * @param resultSelector  聚合投影函数，用于对每个组生成聚合值
     * @param comparer        相等比较器，用于测试两个分组鍵是否相等
     * @param nextOp          后续运算
     * @return GroupAggregationOp运算
     */
    public static QueryOp groupBy(LambdaExpression keySelector, LambdaExpression elementSelector,
                                  LambdaExpression resultSelector, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        GroupAggregationOp groupOp = new GroupAggregationOp(resultSelector, keySelector, comparer, elementSelector, model);
        groupOp.setNext(nextOp);
        groupOp.model = model;
        return groupOp;
    }

    /**
     * 创建表示Include运算的QueryOp实例
     *
     * @param selector 包含表达式，用于指示包含路径
     * @return Include运算
     */
    public static QueryOp include(LambdaExpression selector, ObjectDataModel model) {
        return include(selector, model, null);
    }

    /**
     * 创建表示Include运算的QueryOp实例
     *
     * @param selector 包含表达式，用于指示包含路径
     * @param nextOp   后续运算
     * @return Include运算
     */
    public static QueryOp include(LambdaExpression selector, ObjectDataModel model, QueryOp nextOp) {
        IncludeOp includeOp = new IncludeOp(selector, model);
        includeOp.setNext(nextOp);
        includeOp.model = model;
        return includeOp;
    }

    /**
     * 创建表示Include运算的QueryOp实例
     *
     * @param includingPath 包含路径
     * @param sourceType    源类型
     * @param model         对象数据模型
     * @return Include运算
     */
    public static QueryOp include(String includingPath, Class<?> sourceType, ObjectDataModel model) {
        return include(includingPath, sourceType, model, null);
    }

    /**
     * 创建表示Include运算的QueryOp实例
     *
     * @param includingPath 包含路径
     * @param sourceType    源类型
     * @param model         对象数据模型
     * @param nextOp        后续运算
     * @return Include运算
     */
    public static QueryOp include(String includingPath, Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        IncludeOp includeOp = new IncludeOp(includingPath, sourceType, model);
        includeOp.setNext(nextOp);
        includeOp.model = model;
        return includeOp;
    }

    /**
     * 创建表示Join运算的QueryOp实例
     *
     * @param innerSource      要与第一个序列联接的序列
     * @param outerKeySelector 联接鍵函数，用于从第一个序列的每个元素提取联接鍵
     * @param innerKeySelector 联接鍵函数，用于从第二个序列的每个元素提取联接鍵
     * @param resultSelector   结果投影函数，用于从两个匹配元素创建结果元素
     * @param comparer         相等比较器，用于测试来自两个元素的联接鍵是否相等
     * @return Join运算
     */
    public static QueryOp join(Iterable<?> innerSource, LambdaExpression outerKeySelector,
                               LambdaExpression innerKeySelector, LambdaExpression resultSelector, Comparator<?> comparer,
                               ObjectDataModel model) {
        return join(innerSource, outerKeySelector, innerKeySelector, resultSelector, comparer, model, null);
    }

    /**
     * 创建表示Join运算的QueryOp实例
     *
     * @param innerSource      要与第一个序列联接的序列
     * @param outerKeySelector 联接鍵函数，用于从第一个序列的每个元素提取联接鍵
     * @param innerKeySelector 联接鍵函数，用于从第二个序列的每个元素提取联接鍵
     * @param resultSelector   结果投影函数，用于从两个匹配元素创建结果元素
     * @param comparer         相等比较器，用于测试来自两个元素的联接鍵是否相等
     * @param nextOp           后续运算
     * @return Join运算
     */
    public static QueryOp join(Iterable<?> innerSource, LambdaExpression outerKeySelector,
                               LambdaExpression innerKeySelector, LambdaExpression resultSelector, Comparator<?> comparer,
                               ObjectDataModel model, QueryOp nextOp) {
        JoinOp joinOp = new JoinOp(innerSource, outerKeySelector, innerKeySelector, resultSelector, comparer, model);
        joinOp.setNext(nextOp);
        joinOp.model = model;
        return joinOp;
    }

    /**
     * 创建表示Last运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @return Last运算
     */
    public static QueryOp last(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        return last(predicate, returnDefault, model, null);
    }

    /**
     * 创建表示Last运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @param nextOp        后续运算
     * @return Last运算
     */
    public static QueryOp last(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        LastOp lastOp = new LastOp(predicate, returnDefault, model);
        lastOp.setNext(nextOp);
        lastOp.model = model;
        return lastOp;
    }

    /**
     * 创建表示Last运算的QueryOp实例
     *
     * @param sourceType    查询源
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @return Last运算
     */
    public static QueryOp last(Class<?> sourceType, boolean returnDefault, ObjectDataModel model) {
        return last(sourceType, returnDefault, model, null);
    }

    /**
     * 创建表示Last运算的QueryOp实例
     *
     * @param sourceType    查询源
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     * @param nextOp        后续运算
     * @return Last运算
     */
    public static QueryOp last(Class<?> sourceType, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        LastOp lastOp = new LastOp(sourceType, returnDefault);
        lastOp.setNext(nextOp);
        lastOp.model = model;
        return lastOp;
    }

    /**
     * 创建表示运算符为Max的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @return Max运算
     */
    public static QueryOp max(LambdaExpression selector, ObjectDataModel model) {
        return max(selector, model, null);
    }

    /**
     * 创建表示运算符为Max的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @param nextOp   后续运算
     * @return Max运算
     */
    public static QueryOp max(LambdaExpression selector, ObjectDataModel model, QueryOp nextOp) {
        ArithAggregateOp arithAggregateOp = new ArithAggregateOp(EAggregationOperator.Max, model, selector);
        arithAggregateOp.setNext(nextOp);
        arithAggregateOp.model = model;
        return arithAggregateOp;
    }

    /**
     * 创建表示运算符为Min的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @return Min运算
     */
    public static QueryOp min(LambdaExpression selector, ObjectDataModel model) {
        return min(selector, model, null);
    }

    /**
     * 创建表示运算符为Min的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @param nextOp   后续运算
     * @return Min运算
     */
    public static QueryOp min(LambdaExpression selector, ObjectDataModel model, QueryOp nextOp) {
        ArithAggregateOp arithAggregateOp = new ArithAggregateOp(EAggregationOperator.Min, model, selector);
        arithAggregateOp.setNext(nextOp);
        arithAggregateOp.model = model;
        return arithAggregateOp;
    }


    /**
     * 创建表示OfType运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param resultType 作为筛选依据的类型
     * @param nextOp     后续运算
     * @return OfType运算
     */
    public static QueryOp ofType(Class<?> sourceType, Class<?> resultType, ObjectDataModel model, QueryOp nextOp) {
        OfTypeOp ofTypeOp = new OfTypeOp(resultType, sourceType);
        ofTypeOp.setNext(nextOp);
        ofTypeOp.model = model;
        return ofTypeOp;
    }

    /**
     * 创建表示Order运算的QueryOp实例，该Order运算清除之前的排序结果。
     *
     * @param keySelector 鍵函数，用于从每个元素抽取排序鍵
     * @param descending  指示是否反序排列
     * @param comparer    比较器，用于比较排序鍵的大小
     * @return Order运算
     */
    public static QueryOp orderBy(LambdaExpression keySelector, boolean descending, Comparator<?> comparer,
                                  ObjectDataModel model) {
        return orderBy(keySelector, descending, comparer, model, null);
    }

    /**
     * 创建表示Order运算的QueryOp实例，该Order运算清除之前的排序结果。
     *
     * @param keySelector 鍵函数，用于从每个元素抽取排序鍵
     * @param descending  指示是否反序排列
     * @param comparer    比较器，用于比较排序鍵的大小
     * @param nextOp      后续运算
     * @return Order运算
     */
    public static QueryOp orderBy(LambdaExpression keySelector, boolean descending, Comparator<?> comparer,
                                  ObjectDataModel model, QueryOp nextOp) {
        OrderOp orderOp = new OrderOp(keySelector, descending, true, comparer, model);
        orderOp.setNext(nextOp);
        orderOp.model = model;
        return orderOp;
    }

    /**
     * 创建表示Reverse运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @return Reverse运算
     */
    public static QueryOp reverse(Class<?> sourceType, ObjectDataModel model) {
        return reverse(sourceType, model, null);
    }

    /**
     * 创建表示Reverse运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param nextOp     后续运算
     * @return Reverse运算
     */
    public static QueryOp reverse(Class<?> sourceType, ObjectDataModel model, QueryOp nextOp) {
        ReverseOp reverseOp = new ReverseOp(sourceType);
        reverseOp.setNext(nextOp);
        reverseOp.model = model;
        return reverseOp;
    }

    /**
     * 创建表示Select运算的QueryOp实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, ObjectDataModel model) {
        return select(resultSelector, model, null);
    }

    /**
     * 创建表示Select运算的QueryOp实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     * @param nextOp         后续运算
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, ObjectDataModel model, QueryOp nextOp) {
        SelectOp selectOp = new SelectOp(resultSelector, model);
        selectOp.setNext(nextOp);
        selectOp.model = model;
        return selectOp;
    }

    /**
     * 创建表示对结果进行合并的多重投影运算的QueryOp实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     * @param resultType     对每个元素投影的结果的类型
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, Class<?> resultType, ObjectDataModel model) {
        return select(resultSelector, resultType, model, null);
    }

    /**
     * 创建表示对结果进行合并的多重投影运算的QueryOp实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     * @param resultType     对每个元素投影的结果的类型
     * @param nextOp         后续运算
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, Class<?> resultType, ObjectDataModel model, QueryOp nextOp) {
        CombiningSelectOp combiningSelectOp = new CombiningSelectOp(resultSelector, resultType, model);
        combiningSelectOp.setNext(nextOp);
        combiningSelectOp.model = model;
        return combiningSelectOp;
    }

    /**
     * 创建表示“集合中介投影”运算的QueryOp实例
     *
     * @param resultSelector     结果投影函数，应用于每个中间序列的每个元素
     * @param collectionSelector 中介投影函数，应用于输入序列的每个元素
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, LambdaExpression collectionSelector,
                                 ObjectDataModel model) {
        return select(resultSelector, collectionSelector, model, null);
    }

    /**
     * 创建表示“集合中介投影”运算的QueryOp实例
     *
     * @param resultSelector     结果投影函数，应用于每个中间序列的每个元素
     * @param collectionSelector 中介投影函数，应用于输入序列的每个元素
     * @param nextOp             后续运算
     * @return Select运算
     */
    public static QueryOp select(LambdaExpression resultSelector, LambdaExpression collectionSelector,
                                 ObjectDataModel model, QueryOp nextOp) {
        CollectionSelectOp collectionSelectOp = new CollectionSelectOp(resultSelector, collectionSelector, model);
        collectionSelectOp.setNext(nextOp);
        collectionSelectOp.model = model;
        return collectionSelectOp;
    }

    /**
     * 创建表示一般投影运算的SelectOp实例
     *
     * @param resultView 投影结果视图
     * @return Select运算
     */
    public static SelectOp select(TypeView resultView, ObjectDataModel model) {
        return select(resultView, model, null);
    }

    /**
     * 创建表示一般投影运算的SelectOp实例
     *
     * @param resultView 投影结果视图
     * @param nextOp     查询链中的下一个节点
     * @return Select运算
     */
    public static SelectOp select(TypeView resultView, ObjectDataModel model, QueryOp nextOp) {
        SelectOp selectOp;
        //如果视图具有平展点
        if (resultView.getFlatteningPoints() != null && resultView.getFlatteningPoints().length > 0) {
            selectOp = new CollectionSelectOp(resultView, model);
        } else {
            selectOp = new SelectOp(resultView, model);
        }
        selectOp.setNext(nextOp);
        selectOp.model = model;
        return selectOp;
    }

    /**
     * 创建表示退化投影运算的SelectOp实例
     *
     * @param atrophyPath 退化路径
     * @param combining   多重投影时指示是否对结果实施合并
     * @return Select运算
     */
    public static SelectOp select(AtrophyPath atrophyPath, boolean combining, ObjectDataModel model) {
        return select(atrophyPath, combining, model, null);
    }

    /**
     * 创建表示退化投影运算的SelectOp实例
     *
     * @param atrophyPath 退化路径
     * @param combining   多重投影时指示是否对结果实施合并
     * @param nextOp      查询链中的下一个节点
     * @return Select运算
     */
    public static SelectOp select(AtrophyPath atrophyPath, boolean combining, ObjectDataModel model, QueryOp nextOp) {
        SelectOp selectOp;
        //如果视图具有平展点
        if (atrophyPath.getFlatteningPoints() != null && atrophyPath.getFlatteningPoints().length > 0 || combining) {
            selectOp = new CollectionSelectOp(atrophyPath, model);
        } else {
            selectOp = new SelectOp(atrophyPath, model);
        }
        selectOp.setNext(nextOp);
        selectOp.model = model;
        return selectOp;
    }

    /**
     * 创建表示SequenceEqual运算的QueryOp实例
     *
     * @param other    参与比较的另一序列
     * @param comparer 相等比较器，用于测试来自两个序列的元素是否相等
     * @return SequenceEqual运算
     */
    public static QueryOp sequenceEqual(Iterable<?> other, Comparator<?> comparer, ObjectDataModel model) {
        return sequenceEqual(other, comparer, model, null);
    }

    /**
     * 创建表示SequenceEqual运算的QueryOp实例
     *
     * @param other    参与比较的另一序列
     * @param comparer 相等比较器，用于测试来自两个序列的元素是否相等
     * @param nextOp   后续运算
     * @return SequenceEqual运算
     */
    public static QueryOp sequenceEqual(Iterable<?> other, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        SequenceEqualOp sequenceEqualOp = new SequenceEqualOp(other, comparer, other.getClass());
        sequenceEqualOp.setNext(nextOp);
        sequenceEqualOp.model = model;
        return sequenceEqualOp;
    }

    /**
     * 创建表示Set运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param other      参与运算的另一集合
     * @param operator   集运算符
     * @param comparer   相等比较器，用于测试来自于两个集合的元素是否相等
     * @param nextOp     后续运算
     * @return Set运算
     */
    public static QueryOp set(Class<?> sourceType, Iterable<?> other, ESetOperator operator,
                              Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        SetOp setOp = new SetOp(sourceType, operator, other, comparer);
        setOp.setNext(nextOp);
        setOp.model = model;
        return setOp;
    }

    /**
     * 创建表示Single运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示不满足条件时是否返回默认值
     * @return Single运算
     */
    public static QueryOp single(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        return single(predicate, returnDefault, model, null);
    }

    /**
     * 创建表示Single运算的QueryOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示不满足条件时是否返回默认值
     * @param nextOp        后续运算
     * @return Single运算
     */
    public static QueryOp single(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        SingleOp singleOp = new SingleOp(predicate, returnDefault, model);
        singleOp.setNext(nextOp);
        return singleOp;
    }

    /**
     * 创建表示Single运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示不满足条件时是否返回默认值
     * @return Single运算
     */
    public static QueryOp single(Class<?> sourceType, boolean returnDefault, ObjectDataModel model) {
        return single(sourceType, returnDefault, model, null);
    }

    /**
     * 创建表示Single运算的QueryOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示不满足条件时是否返回默认值
     * @param nextOp        后续运算
     * @return Single运算
     */
    public static QueryOp single(Class<?> sourceType, boolean returnDefault, ObjectDataModel model, QueryOp nextOp) {
        SingleOp singleOp = new SingleOp(sourceType, returnDefault);
        singleOp.setNext(nextOp);
        singleOp.model = model;
        return singleOp;
    }

    /**
     * 创建表示Skip运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param count      要略过的个数
     * @return Skip运算
     */
    public static QueryOp skip(Class<?> sourceType, int count, ObjectDataModel model) {
        return skip(sourceType, count, model, null);
    }

    /**
     * 创建表示Skip运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param count      要略过的个数
     * @param nextOp     后续运算
     * @return Skip运算
     */
    public static QueryOp skip(Class<?> sourceType, int count, ObjectDataModel model, QueryOp nextOp) {
        SkipOp skipOp = new SkipOp(sourceType, count);
        skipOp.setNext(nextOp);
        skipOp.model = model;
        return skipOp;
    }

    /**
     * 创建表示SkipWhile运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @return SkipWhile运算
     */
    public static QueryOp skipWhile(LambdaExpression predicate, ObjectDataModel model) {
        return skipWhile(predicate, model, null);
    }

    /**
     * 创建表示SkipWhile运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @param nextOp    后续运算
     * @return SkipWhile运算
     */
    public static QueryOp skipWhile(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        SkipWhileOp skipWhileOp = new SkipWhileOp(predicate, model);
        skipWhileOp.setNext(nextOp);
        skipWhileOp.model = model;
        return skipWhileOp;
    }

    /**
     * 创建表示运算符为Sum的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @return Sum运算
     */
    public static QueryOp sum(LambdaExpression selector, ObjectDataModel model) {
        return sum(selector, model, null);
    }

    /**
     * 创建表示运算符为Sum的算术聚合运算的QueryOp实例
     *
     * @param selector 投影函数，应用于每个元素然后以投影结果参与聚合
     * @param nextOp   后续运算
     * @return Sum运算
     */
    public static QueryOp sum(LambdaExpression selector, ObjectDataModel model, QueryOp nextOp) {
        ArithAggregateOp arithAggregateOp = new ArithAggregateOp(EAggregationOperator.Sum, model, selector);
        arithAggregateOp.setNext(nextOp);
        arithAggregateOp.model = model;
        return arithAggregateOp;
    }

    /**
     * 创建表示Take运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param count      要提取的个数
     * @return Take运算
     */
    public static QueryOp take(Class<?> sourceType, int count, ObjectDataModel model) {
        return take(sourceType, count, model, null);
    }

    /**
     * 创建表示Take运算的QueryOp实例
     *
     * @param sourceType 查询源类型
     * @param count      要提取的个数
     * @param nextOp     后续运算
     * @return Take运算
     */
    public static QueryOp take(Class<?> sourceType, int count, ObjectDataModel model, QueryOp nextOp) {
        TakeOp takeOp = new TakeOp(sourceType, count);
        takeOp.setNext(nextOp);
        takeOp.model = model;
        return takeOp;
    }

    /**
     * 创建表示TakeWhile运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @return TakeWhile运算
     */
    public static QueryOp takeWhile(LambdaExpression predicate, ObjectDataModel model) {
        return takeWhile(predicate, model, null);
    }

    /**
     * 创建表示TakeWhile运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @param nextOp    后续运算
     * @return TakeWhile运算
     */
    public static QueryOp takeWhile(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        TakeWhileOp takeWhileOp = new TakeWhileOp(predicate, model);
        takeWhileOp.setNext(nextOp);
        return takeWhileOp;
    }

    /**
     * 创建表示Order运算的QueryOp实例，该Order运算不清除之前的排序结果。
     *
     * @param keySelector 鍵函数，用于从每个元素抽取排序鍵
     * @param descending  指示是否反序排列
     * @param comparer    比较器，用于比较排序鍵的大小
     * @return Order运算
     */
    public static QueryOp thenOrderBy(LambdaExpression keySelector, boolean descending, Comparator<?> comparer, ObjectDataModel model) {
        return thenOrderBy(keySelector, descending, comparer, model, null);
    }

    /**
     * 创建表示Order运算的QueryOp实例，该Order运算不清除之前的排序结果。
     *
     * @param keySelector 鍵函数，用于从每个元素抽取排序鍵
     * @param descending  指示是否反序排列
     * @param comparer    比较器，用于比较排序鍵的大小
     * @param nextOp      后续运算
     * @return Order运算
     */
    public static QueryOp thenOrderBy(LambdaExpression keySelector, boolean descending, Comparator<?> comparer, ObjectDataModel model, QueryOp nextOp) {
        OrderOp orderOp = new OrderOp(keySelector, descending, false, comparer, model);
        orderOp.setNext(nextOp);
        return orderOp;
    }

    /**
     * 创建表示Where运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @return Where运算
     */
    public static QueryOp where(LambdaExpression predicate, ObjectDataModel model) {
        return where(predicate, model, null);
    }

    /**
     * 创建表示Where运算的QueryOp实例
     *
     * @param predicate 断言函数，用于测试每个元素是否满足条件
     * @param nextOp    后续运算
     * @return Where运算
     */
    public static QueryOp where(LambdaExpression predicate, ObjectDataModel model, QueryOp nextOp) {
        WhereOp whereOp = new WhereOp(predicate, model);
        whereOp.setNext(nextOp);
        return whereOp;
    }

    /**
     * 创建表示Zip运算的QueryOp实例
     *
     * @param second         要合并的第二个序列
     * @param firstType      第一个序列的元素类型 即源的类型
     * @param resultSelector 合并投影函数，用于指定如何合并这两个序列中的元素
     * @return Zip运算
     */
    public static QueryOp zip(Iterable<?> second, Class<?> firstType, LambdaExpression resultSelector, ObjectDataModel model) {
        return zip(second, firstType, resultSelector, model, null);
    }

    /**
     * 创建表示Zip运算的QueryOp实例
     *
     * @param second         要合并的第二个序列
     * @param firstType      第一个序列的元素类型 即源的类型
     * @param resultSelector 合并投影函数，用于指定如何合并这两个序列中的元素
     * @param nextOp         后续运算
     * @return Zip运算
     */
    public static QueryOp zip(Iterable<?> second, Class<?> firstType, LambdaExpression resultSelector, ObjectDataModel model, QueryOp nextOp) {
        ZipOp zipOp = new ZipOp(second, resultSelector, firstType);
        zipOp.setNext(nextOp);
        zipOp.model = model;
        return zipOp;
    }

    /**
     * 创建表示Zip运算的QueryOp实例
     *
     * @param second     要合并的第二个序列
     * @param firstType  第一个序列的元素类型 即源的类型
     * @param resultType 结果
     * @return Zip运算
     */
    public static QueryOp zip(Iterable<?> second, Class<?> firstType, Class<?> resultType, ObjectDataModel model) {
        return zip(second, firstType, resultType, model, null);
    }

    /**
     * 创建表示Zip运算的QueryOp实例
     *
     * @param second     要合并的第二个序列
     * @param firstType  第一个序列的元素类型 即源的类型
     * @param resultType 结果
     * @param nextOp     后续运算
     * @return Zip运算
     */
    public static QueryOp zip(Iterable<?> second, Class<?> firstType, Class<?> resultType, ObjectDataModel model, QueryOp nextOp) {
        ZipOp zipOp = new ZipOp(firstType, resultType, second, firstType);
        zipOp.setNext(nextOp);
        zipOp.model = model;
        return zipOp;
    }

    /**
     * 获取表达式的宿主类型
     *
     * @param lambdaExpression 表达式
     * @return 宿主类型
     */
    public static Class<?> getParameterHostType(LambdaExpression lambdaExpression) {
        ParameterExpression parameterExpression = Arrays.stream(lambdaExpression.getParameters()).filter(ParameterExpression::getIsHost).findFirst().orElse(null);
        if (parameterExpression != null)
            return parameterExpression.getType();
        return null;
    }

    /**
     * 获取查询链中的下一个运算
     *
     * @return 获取查询链中的下一个运算
     */
    public QueryOp getNext() {
        return this.next;
    }

    /**
     * 设置下一个运算
     *
     * @param next 下一个运算
     */
    public void setNext(QueryOp next) {
        this.next = next;
    }

    /**
     * 获取运算名称
     *
     * @return 获取运算名称
     */
    public EQueryOpName getName() {
        return this.name;
    }

    /**
     * 获取运算参数
     *
     * @return 获取运算参数
     */
    @Deprecated
    private Expression[] getArguments() {
        return this.gotArguments();
    }

    /**
     * 获取结果类型
     *
     * @return 结果类型
     */
    public abstract Class<?> getResultType();

    /**
     * 获取源对象的类型
     *
     * @return 获取源对象的类型
     */
    public Class<?> getSourceType() {
        return this.sourceType;
    }

    /**
     * 获取查询链的尾部节点
     *
     * @return 获取查询链的尾部节点
     */
    public QueryOp getTail() {
        if (this.tail != null)
            return this.tail;
        this.tail = this.next == null ? this : this.next.getTail();
        return this.tail;
    }

    /**
     * 获取查询运算的隐含包含
     * 如果一个查询运算虽未显示要求包含一个引用，但该运算的执行依赖于该引用，则称该查询运算隐含包含该引用。
     *
     * @return 获取查询运算的隐含包含
     */
    public AssociationTree getImpliedIncluding() {
        if (this.impliedIncluding == null)
            this.impliedIncluding = this.takeImpliedIncluding();
        return this.impliedIncluding;
    }

    /**
     * 获取适用于查询运算的对象数据模型。
     * 如果查询源为基元类型，返回null。
     *
     * @return 模型
     */
    public ObjectDataModel getModel() {
        if (PrimitiveType.isObasePrimitive(this.sourceType))
            return null;
        return this.model;
    }

    /**
     * 查询源的模型类型
     *
     * @return 源的模型类型
     */
    public TypeBase getSourceModelType() {
        if (this.model != null)
            return this.model.getTypeOrNull(this.sourceType);
        return null;
    }

    /**
     * 获取一个值，该值指示查询运算是否为异构的
     *
     * @return 指示查询运算是否为异构的
     */
    public Boolean getHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        if (predicationProvider == null)
            predicationProvider = new StorageHeterogeneityPredicationProvider();
        return this.isHeterogeneous(predicationProvider);
    }

    /**
     * 接受访问者对查询链各节点的访问
     *
     * @param visitor       一个查询链访问者，它执行不接收参数且无返回值的操作
     * @param previousState 前一个操作的状态值
     */
    public void accept(QueryOpVisitor visitor, Object previousState) {
        ObjectReferencePack<Object> outPreVisitState = new ObjectReferencePack<>(), outPreviousState = new ObjectReferencePack<>();
        boolean re = visitor.preVisit(this, null, outPreviousState, outPreVisitState); //访问后续节点前 执行操作。
        if (re && this.getNext() != null)
            this.getNext().accept(visitor, outPreviousState.realValue); //后续节点接受访问者。
        visitor.postVisit(this, previousState, outPreVisitState.realValue);
    }

    /**
     * 接受访问者对查询链各节点的访问
     *
     * @param <TResult> 结果类型
     * @param visitor   一个查询链访问者，它执行不接收参数且有返回值的操作
     */
    public <TResult> TResult accept(QueryOpVisitorWithResult<TResult> visitor) {
        this.accept(visitor, null);
        return visitor.getResult();
    }

    /**
     * 接受访问者对查询链各节点的访问
     *
     * @param visitor   一个查询链访问者，它执行不接收参数且有返回值和一个输出参数的操作
     * @param outArg    输出参数的值
     * @param <TResult> 结果类型
     * @param <TOut>    值类型
     * @return 访问结果
     */
    public <TResult, TOut> TResult accept(QueryOpVisitorWithOutArgs<TResult, TOut> visitor, ObjectReferencePack<TOut> outArg) {
        this.accept(visitor, (Object) null);
        outArg.realValue = visitor.getOutArgument();
        return visitor.getResult();
    }

    /**
     * 接受访问者对查询链各节点的访问
     *
     * @param visitor   一个查询链访问者，它执行不接收参数且有返回值和一个访问参数的操作
     * @param arg       输出参数的值
     * @param <TArg>    参数类型
     * @param <TResult> 结果类型
     * @return 访问结果
     */
    public <TArg, TResult> TResult accept(QueryOpVisitorWithArgs<TArg, TResult> visitor, TArg arg) {
        visitor.setArgument(arg);
        this.accept(visitor, (Object) null);
        return visitor.getResult();
    }

    /**
     * 生成查询运算的副本，并将该副本作为指定运算的前序运算
     *
     * @param nextOp 副本的后续运算
     * @return 克隆后的查询操作
     */
    public QueryOp clone(QueryOp nextOp) {
        QueryOp newOp;
        newOp = this.clone();
        if (newOp != null) {
            newOp.next = nextOp;
        }
        return newOp;
    }

    /**
     * 为查询链生成对象运算管道
     *
     * @return 运算管道执行器
     */
    public OopExecutor generatePipeline() {
        OopPipelineBuilder builder = new OopPipelineBuilder();
        this.accept(builder);
        return builder.getResult();
    }

    /**
     * 使用指定的对象运算管道生成器，为查询链生成对象运算管道
     *
     * @param builder 运算管道生成器
     * @return 运算管道执行器
     */
    public OopExecutor generatePipeline(OopPipelineBuilder builder) {
        this.accept(builder);
        return builder.getResult();
    }

    /**
     * 获取查询链中所有包含运算（显式或隐含）的包含链构成的包含树，该树根节点代表查询链的基点源类型
     *
     * @return 包含链构成的包含树
     */
    public AssociationTree getChainIncluding() {
        if (this.getImpliedIncluding() == null)
            return null;
        IncludingCollector collector = new IncludingCollector(this.getImpliedIncluding().getRepresentedType());
        this.accept(collector);
        return collector.getResult()[0];
    }

    /**
     * 从查询链首跳过指定步骤后截取剩余部分
     *
     * @param stepCount 从1开始的步骤数
     * @return 跳过指定步骤后截取剩余部分
     */
    public QueryOp jump(int stepCount) {
        QueryOp nextOp = this.next;
        while (stepCount-- > 0) {
            if (nextOp == null) throw new IllegalArgumentException("跳过步骤数过长");
            nextOp = nextOp.getNext();
        }

        return this.clone(nextOp);
    }

    /**
     * 判定查询运算是否是异构的
     *
     * @return 是否是异构的
     */
    protected boolean isHeterogeneous(HeterogeneityPredicationProvider predicationProvider) {
        //获取查询运算的参数
        if (this.getArguments() != null) {
            for (Expression argument : this.getArguments()) {
                //只处理成员访问参数
                if (argument == null || argument.getExpressionType() != EExpressionType.MemberAccess)
                    continue;
                MemberExpression memberExp = (MemberExpression) argument;
                if (this.model == null)
                    return false;
                AssociationTree assocTree;
                assocTree = memberExp.extractAssociation(this.model, null);
                AssociationTreeHeterogeneityPredicater heterogeneityPredicater = new AssociationTreeHeterogeneityPredicater(predicationProvider);
                assocTree.accept(heterogeneityPredicater);
                if (heterogeneityPredicater.getResult()) return true;
            }
        }

        return false;
    }

    /**
     * 从查询运算中提取隐含包含
     *
     * @return 隐含包含
     */
    protected AssociationTree takeImpliedIncluding() {
        if (this.getArguments() != null) {
            for (Expression argument : this.getArguments()) {
                if (argument == null || argument.getExpressionType() != EExpressionType.MemberAccess)
                    continue;
                MemberExpression member = (MemberExpression) argument;
                if (this.impliedIncluding != null)
                    this.impliedIncluding.grow(member.getMemberName());
            }
        }

        return this.impliedIncluding;
    }

    /**
     * 将查询链的末节点替换为新的运算
     *
     * @param newTail 新的末节点，值为null表示移除当前末节点
     * @return 返回替换末节点后的查询链。如果查询只有一个节点，返回的是新节点；否则返回的是当前节点
     */
    public QueryOp replaceTail(QueryOp newTail) {

        if (this.next == null) return newTail;
        QueryOp current = this;
        QueryOp currentNext = this.next;
        this.tail = newTail;
        while (currentNext != null && currentNext.getNext() != null) {
            current = currentNext;
            currentNext = currentNext.next;
            current.tail = newTail;
        }

        current.next = newTail;
        current.tail = newTail;
        return this;
    }

    /**
     * 在查询链中搜索指定子链，并将其替换为指定的新子链。
     *
     * @param subChain 要替换的子链
     * @param newSub   新的子链，值为null表示移除指定子链。
     * @return 替换后的查询链
     */
    public QueryOp replace(QueryOp subChain, QueryOp newSub) {
        if (this.next == null) return newSub;
        QueryOp current = this;
        QueryOp currentNext = this.next;
        if (currentNext == subChain)
            current.next = newSub;
        while (currentNext != null && currentNext.getNext() != null) {
            if (currentNext == subChain) {
                current.next = newSub;
                break;
            }

            current = currentNext;
            currentNext = currentNext.getNext();
        }
        return this;
    }

    /**
     * 由实现类重写 获取表达式参数
     *
     * @return 获取表达式参数
     */
    protected Expression[] gotArguments() {
        return new Expression[0];
    }

    /**
     * 重写克隆方法
     *
     * @return 克隆后的实体
     */
    @Override
    public QueryOp clone() {
        try {
            return (QueryOp) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆QueryOp失败", e);
        }
    }

    /**
     * 作为一个查询运算访问者，收集查询链中的包含运算（显式或隐含），并将收集到的包含链沿退化路径反向溯源到基点类型。
     */
    private static class IncludingCollector extends QueryOpVisitorWithResult<AssociationTree[]> {

        /**
         * 退化投影运算导致查询源类型沿关联关系退化的记录
         */
        private final List<AssociationTreeNode> atrophies = new ArrayList<>();

        /**
         * 初始化IncludingCollector的新实例
         *
         * @param initialType 查询基点类型
         */
        public IncludingCollector(ReferringType initialType) {
            this.result = new AssociationTree[1];
            this.result[0] = new AssociationTree(initialType);
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

            if (queryOp instanceof SelectOp) {
                SelectOp selectOp = (SelectOp) queryOp;
                //指定一个断言函数 始终返回PostExecute
                this.specify(EQueryOpName.Select, this::preVisit, op -> ESpecialPredicate.PostExecute);
                AtrophyPath atrophyPath = selectOp.getAtrophyPath();
                if (atrophyPath != null) this.atrophies.add(atrophyPath.getAssociationPath());
            } else {
                //通用前置访问逻辑
                AssociationTree targetTree;
                if (queryOp.getName() == EQueryOpName.Include)
                    targetTree = ((IncludeOp) queryOp).getIncludingTree();
                else
                    targetTree = queryOp.getImpliedIncluding();

                if (targetTree != null && this.result != null) {
                    for (AssociationTree tree : this.result) {
                        tree.grow(targetTree, this.atrophies.toArray(new AssociationTreeNode[0]));
                    }
                }
            }

            return true;
        }
    }
}
