/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象集，提供对象的逻辑视图.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-4 12:37:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.FunctionWithTwoArgs;
import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.*;
import io.obase.core.odm.AssociationType;
import io.obase.core.odm.IInstanceConstructor;
import io.obase.core.odm.PrimitiveType;
import io.obase.core.odm.StructuralType;
import io.obase.core.query.QueryExpressionParser;
import io.obase.core.query.QueryProvider;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

/**
 * 对象集合 提供对象的逻辑视图
 *
 * @param <T> 元素类型
 */
public class ObjectSet<T> implements ObaseStream<T> {

    /**
     * 对象上下文
     */
    private final ObjectContext objectContext;

    /**
     * 查询提供程序
     */
    private final QueryProvider queryProvider;

    /**
     * 当前ObjectSet中的元素类型
     */
    private final Class<?> typeClass;

    /**
     * 查询表达式转换器
     */
    private QueryExpressionParser parser;

    /**
     * 原始对象集合 在执行MAP GROUPING等操作后 对象集合会发生变化 此时需要记录原始的对象集
     */
    private ObjectSet<?> originSet;

    /**
     * 创建对象集实例
     *
     * @param objectContext 该对象集所属的对象上下文
     * @param typeClass     内容类型
     */
    public ObjectSet(ObjectContext objectContext, Class<?> typeClass) {
        this(objectContext, typeClass, false);
    }

    /**
     * 创建对象集实例
     *
     * @param objectContext 该对象集所属的对象上下文
     * @param typeClass     内容类型
     * @param isByMap       是否为投影操作创建的
     */
    public ObjectSet(ObjectContext objectContext, Class<?> typeClass, boolean isByMap) {
        if (objectContext.getModel() != null) {
            AssociationType associationType = objectContext.model.getAssociationType(typeClass);
            if (associationType != null && !associationType.getVisible()) {
                throw new IllegalArgumentException("不能为隐式关联型" + typeClass.getName() + "创建对象集");
            }

            if (objectContext.model.getStructuralType(typeClass) == null && !PrimitiveType.isObasePrimitive(typeClass) && !isByMap) {
                throw new IllegalArgumentException("不能为未注册的类型" + typeClass.getName() + "创建对象集");
            }
        }

        this.objectContext = objectContext;
        this.queryProvider = objectContext.getConfigProvider().getQueryProvider();
        this.parser = new QueryExpressionParser(objectContext.getModel(), typeClass);
        this.typeClass = typeClass;
    }

    /**
     * 设置表达式转换器
     *
     * @param parser 表达式转换器
     */
    private void setExpressionParser(QueryExpressionParser parser) {
        this.parser = parser;
    }

    /**
     * 获取原始对象集合
     *
     * @return 原始对象集合
     */
    private ObjectSet<?> getOriginSet() {
        return this.originSet;
    }

    /**
     * 设置原始对象集合
     *
     * @param originSet 原始对象集合
     */
    private void setOriginSet(ObjectSet<?> originSet) {
        this.originSet = originSet;
    }

    /**
     * 获取所属的上下文
     *
     * @return 所属的上下文
     */
    public ObjectContext getObjectContext() {
        return this.objectContext;
    }

    /**
     * 将指定的对象作为新对象附加到对象上下文
     *
     * @param obj 要附加的对象
     */
    public void attach(T obj) {
        if (!this.objectContext.attached(obj)) {

            ObjectReferencePack<Object> objectObjectReferencePack = new ObjectReferencePack<>();
            objectObjectReferencePack.realValue = obj;
            this.objectContext.attach(objectObjectReferencePack, true, true);
        }
    }

    /**
     * 使用无参构造函数创建对象的新实例并附加到上下文
     * 默认使用HasNewInstanceConstructor配置的新实例构造函数 未配置时使用HasConstructor配置的构造函数
     *
     * @param typeClass 类型
     * @return 创建出的对象
     */
    public T create(Class<T> typeClass) {
        StructuralType structuralType = this.objectContext.getModel().getStructuralType(typeClass);
        IInstanceConstructor constructor = structuralType.getNewInstanceConstructor();
        if (constructor == null)
            constructor = structuralType.getConstructor();
        //创建对象
        T obj = (T) constructor.construct(null);
        //附加到上下文
        this.attach(obj);
        return obj;
    }

    /**
     * 使用参数创建对象的新实例并附加到上下文
     * 默认使用HasNewInstanceConstructor配置的新实例构造函数 未配置时使用HasConstructor配置的构造函数
     *
     * @param typeClass 类型
     * @param parameter 构造函数参数
     * @return 创建出的对象
     */
    public T create(Class<T> typeClass, Object... parameter) {
        StructuralType structuralType = this.objectContext.getModel().getStructuralType(typeClass);
        IInstanceConstructor constructor = structuralType.getNewInstanceConstructor();
        if (constructor == null)
            constructor = structuralType.getConstructor();
        //创建对象
        T obj = (T) constructor.construct(parameter);
        //附加到上下文
        this.attach(obj);
        return obj;
    }

    /**
     * 将指定的对象标记为已删除。（标记删除（SaveChanges时才真正删除））
     *
     * @param obj 要标记为已删除的对象
     */
    public void remove(T obj) {
        this.objectContext.remove(obj);
    }

    /**
     * 根据传入的筛选条件即时删除对象 等同于Delete方法
     *
     * @param filterExpression 表达式
     * @param clazz            类型
     * @return 受影响的行数
     */
    public int removeDirectly(SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return this.delete(filterExpression, clazz);
    }

    /**
     * 根据传入的筛选条件即时删除对象
     *
     * @param filterExpression 表达式
     * @param clazz            类型
     * @return 受影响的行数
     */
    public int delete(SerializedPredicate<T> filterExpression, Class<T> clazz) {
        LambdaTranslator translator = new LambdaTranslator();

        Expression expression = translator.getLambdaExpression(filterExpression);

        //没有条件 返回
        if (expression == null) return 0;

        //删除
        return this.objectContext.getConfigProvider().getSavingProvider().delete(this.objectContext.getModel().getObjectType(clazz), expression);
    }

    /**
     * 为符合条件的对象的属性即时设置新值
     *
     * @param newValues        存储属性新值的键值对集合，其中键为属性名称，值为属性的新值
     * @param filterExpression 筛选条件
     * @param clazz            类型
     * @return 受影响的行数
     */
    public int setAttributes(Map<String, Object> newValues, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        LambdaTranslator translator = new LambdaTranslator();

        Expression expression = translator.getLambdaExpression(filterExpression);

        //没有条件 返回
        if (expression == null) return 0;

        //设值值
        return this.objectContext.getConfigProvider().getSavingProvider().setAttributes(this.objectContext.getModel().getObjectType(clazz), expression, newValues);
    }

    /**
     * 为符合条件的对象的属性即时设置新值，其中新值为原值加上增量值。属性必须为数值类型。
     *
     * @param newValues        存储增量值的键值对集合，其中键为属性名称，值为增量值
     * @param filterExpression 筛选条件
     * @param clazz            类型
     * @return 受影响的行数
     */
    public int increaseAttributes(Map<String, Object> newValues, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        LambdaTranslator translator = new LambdaTranslator();

        Expression expression = translator.getLambdaExpression(filterExpression);

        //没有条件 返回
        if (expression == null) return 0;

        //设值值
        return this.objectContext.getConfigProvider().getSavingProvider().increaseAttributes(this.objectContext.getModel().getObjectType(clazz), expression, newValues);
    }

    /**
     * 按条件筛选
     * 此方法为延迟执行方法 调用时仅进行标记 调用终结方法时结算
     *
     * @param filterExpression 筛选表达式
     * @return 自身
     */
    @Override
    public ObaseStream<T> filter(SerializedPredicate<T> filterExpression) {
        this.parser.parse("filter", filterExpression, new Object[]{this.typeClass});
        return this;
    }

    /**
     * 按条件筛选
     * 此方法为延迟执行方法 调用时仅进行标记 调用终结方法时结算
     *
     * @param filterExpression 筛选表达式
     * @return 自身
     */
    @Override
    public ObaseStream<T> filter(LambdaExpression filterExpression) {
        this.parser.addQueryOp("filter", filterExpression, new Object[]{this.typeClass});
        return this;
    }

    /**
     * 满足条件则将表达式加入filter
     *
     * @param condition        条件
     * @param filterExpression 表达式
     * @return 自身
     */
    @Override
    public ObaseStream<T> filterIf(boolean condition, SerializedPredicate<T> filterExpression) {
        if (condition) {
            this.parser.parse("filter", filterExpression, new Object[]{this.typeClass});
        }
        return this;
    }

    /**
     * 满足条件则将表达式加入filter
     *
     * @param condition        条件
     * @param filterExpression 表达式
     * @return 自身
     */
    @Override
    public ObaseStream<T> filterIf(boolean condition, LambdaExpression filterExpression) {
        if (condition) {
            this.parser.addQueryOp("filter", filterExpression, new Object[]{this.typeClass});
        }
        return this;
    }

    /**
     * 映射为另一元素
     *
     * @param mapExpression 映射表达式
     * @param targetClass   映射目标类型
     * @return 映射后的流
     */
    @Override
    public <R> ObaseStream<R> map(SerializedFunction<T, R> mapExpression, Class<?> targetClass) {
        if (!PrimitiveType.isObasePrimitive(targetClass) && !targetClass.isInterface())
            MethodChecker.registerClassMethod(targetClass);
        this.parser.parse("map", mapExpression, new Object[]{targetClass});
        ObjectSet<R> result = new ObjectSet<>(this.objectContext, targetClass, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 平展映射为另一元素
     *
     * @param mapExpression 映射表达式
     * @param targetClass   结果类型 注意此处的结果类型为List的泛型类型
     * @return 映射后的流
     */
    @Override
    public <R> ObaseStream<R> flatMap(SerializedFunction<T, Iterable<R>> mapExpression, Class<R> targetClass) {
        if (!PrimitiveType.isObasePrimitive(targetClass) && !targetClass.isInterface())
            MethodChecker.registerClassMethod(targetClass);
        this.parser.parse("flatmap", null, new Object[]{mapExpression, targetClass});
        ObjectSet<R> result = new ObjectSet<>(this.objectContext, targetClass, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 平展映射为另一元素
     *
     * @param getCollect 映射表达式
     * @param getResult  结果表达式
     * @param resultType 结果类型 注意此处的结果类型为List的泛型类型
     * @return 映射后的流
     */
    @Override
    public <TCollect, TResult> ObaseStream<TResult> flatMap(SerializedFunction<T, Iterable<TCollect>> getCollect, FunctionWithTwoArgs<T, TCollect, TResult> getResult, Class<TResult> resultType) {
        if (!PrimitiveType.isObasePrimitive(resultType) && !resultType.isInterface())
            MethodChecker.registerClassMethod(resultType);
        this.parser.parse("flatmap", null, new Object[]{getCollect, getResult, resultType});
        ObjectSet<TResult> result = new ObjectSet<>(this.objectContext, resultType, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 筛选为不重复的对象
     *
     * @return 自身
     */
    @Override
    public ObaseStream<T> distinct() {
        this.parser.parse("distinct", null, new Object[]{this.typeClass});
        return this;
    }

    /**
     * 排序 会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> sorted(SerializedFunction<T, R> get) {
        this.parser.parse("sorted", get, new Object[]{null});
        return this;
    }

    /**
     * 排序 会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> sorted(SerializedFunction<T, R> get, Comparator<T> comparable) {
        this.parser.parse("sorted", get, new Object[]{comparable});
        return this;
    }

    /**
     * 子排序 不会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> thenSorted(SerializedFunction<T, R> get) {
        this.parser.parse("thenSorted", get, new Object[]{null});
        return this;
    }

    /**
     * 子排序 不会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> thenSorted(SerializedFunction<T, R> get, Comparator<T> comparable) {
        this.parser.parse("thenSorted", get, new Object[]{comparable});
        return this;
    }

    /**
     * 反向排序 会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> sortedDesc(SerializedFunction<T, R> get) {
        this.parser.parse("sortedDesc", get, new Object[]{null});
        return this;
    }

    /**
     * 反向排序 会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> sortedDesc(SerializedFunction<T, R> get, Comparator<T> comparable) {
        this.parser.parse("sortedDesc", get, new Object[]{comparable});
        return this;
    }

    /**
     * 反向子排序 不会覆盖之前的排序
     *
     * @param get 要排序的依据
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> thenSortedDesc(SerializedFunction<T, R> get) {
        this.parser.parse("thenSortedDesc", get, new Object[]{null});
        return this;
    }

    /**
     * 反向子排序 不会覆盖之前的排序
     *
     * @param get        要排序的依据
     * @param comparable 比较器
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> thenSortedDesc(SerializedFunction<T, R> get, Comparator<T> comparable) {
        this.parser.parse("thenSortedDesc", get, new Object[]{comparable});
        return this;
    }

    /**
     * 反序
     *
     * @return 反序后的结果流
     */
    @Override
    public ObaseStream<T> reverse() {
        this.parser.parse("reverse", null, new Object[]{this.typeClass});
        return this;
    }

    /**
     * 提取多少个元素
     *
     * @param maxSize 最多跳过的元素数量
     * @return 自身
     */
    @Override
    public ObaseStream<T> limit(int maxSize) {
        this.parser.parse("limit", null, new Object[]{this.typeClass, maxSize});
        return this;
    }

    /**
     * 跳过多少个元素
     *
     * @param n 跳过多少个
     * @return 自身
     */
    @Override
    public ObaseStream<T> skip(int n) {
        this.parser.parse("skip", null, new Object[]{this.typeClass, n});
        return this;
    }

    /**
     * 求某个属性最小值
     *
     * @param get 某个属性
     * @return 最小值
     */
    @Override
    public <R> R min(SerializedFunction<T, R> get) {
        this.parser.parse("min", get, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        result = this.convertToTypeResult(result, this.parser.getResultTypeClass());
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (R) result;
    }

    /**
     * 求某个属性最小值
     *
     * @param get 某个属性
     * @return 最大值
     */
    @Override
    public <R> R max(SerializedFunction<T, R> get) {
        this.parser.parse("max", get, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        result = this.convertToTypeResult(result, this.parser.getResultTypeClass());
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (R) result;
    }

    /**
     * 求平均值
     *
     * @param intResult 参与的运算
     * @return 平均值
     */
    @Override
    public double avgInt(SerializedIntResult<T> intResult) {
        String result = this.realAvg(intResult);
        return Double.parseDouble(result);
    }

    /**
     * 求平均值
     *
     * @param doubleResult 参与的运算
     * @return 平均值
     */
    @Override
    public double avg(SerializedDoubleResult<T> doubleResult) {
        String result = this.realAvg(doubleResult);
        return Double.parseDouble(result);
    }

    /**
     * 求平均值
     *
     * @param floatResult 参与的运算
     * @return 平均值
     */
    @Override
    public float avg(SerializedFloatResult<T> floatResult) {
        String result = this.realAvg(floatResult);
        return Float.parseFloat(result);
    }

    /**
     * 求平均值
     *
     * @param bigDecimalResult 参与的运算
     * @return 平均值
     */
    @Override
    public BigDecimal avg(SerializedBigDecimalResult<T> bigDecimalResult) {
        String result = this.realAvg(bigDecimalResult);
        return new BigDecimal(result);
    }

    /**
     * 求和
     *
     * @param intResult 表达式
     * @return 和
     */
    @Override
    public int sumInt(SerializedIntResult<T> intResult) {
        String result = this.realSum(intResult);
        return Integer.parseInt(result);
    }

    /**
     * 求和
     *
     * @param longResult 表达式
     * @return 和
     */
    @Override
    public long sum(SerializedLongResult<T> longResult) {
        String result = this.realSum(longResult);
        return Long.parseLong(result);
    }

    /**
     * 求和
     *
     * @param doubleResult 表达式
     * @return 平均值
     */
    @Override
    public double sum(SerializedDoubleResult<T> doubleResult) {
        String result = this.realSum(doubleResult);
        return Double.parseDouble(result);
    }

    /**
     * 求和
     *
     * @param floatResult 参与的运算
     * @return 平均值
     */
    @Override
    public float sum(SerializedFloatResult<T> floatResult) {
        String result = this.realSum(floatResult);
        return Float.parseFloat(result);
    }

    /**
     * 求平均值
     *
     * @param bigDecimalResult 参与的运算
     * @return 平均值
     */
    @Override
    public BigDecimal sum(SerializedBigDecimalResult<T> bigDecimalResult) {
        String result = this.realSum(bigDecimalResult);
        return new BigDecimal(result);
    }

    /**
     * 计数
     *
     * @param predicate 计数条件
     * @return 数量
     */
    @Override
    public long count(SerializedPredicate<T> predicate) {
        this.parser.parse("count", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return Long.parseLong(result.toString());
    }

    /**
     * 计数
     *
     * @param predicate 计数条件
     * @return 数量
     */
    @Override
    public long count(LambdaExpression predicate) {
        this.parser.addQueryOp("count", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return Long.parseLong(result.toString());
    }

    /**
     * 计数
     *
     * @return 计数
     */
    @Override
    public long count() {
        this.parser.parse("count", null, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return Long.parseLong(result.toString());
    }

    /**
     * 任意匹配
     *
     * @return 是否满足任意匹配
     */
    @Override
    public boolean anyMatch() {
        this.parser.parse("anyMatch", null, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (boolean) result;
    }

    /**
     * 任意匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足任意匹配
     */
    @Override
    public boolean anyMatch(SerializedPredicate<T> predicate) {
        this.parser.parse("anyMatch", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (boolean) result;
    }

    /**
     * 任意匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足任意匹配
     */
    @Override
    public boolean anyMatch(LambdaExpression predicate) {
        this.parser.addQueryOp("anyMatch", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (boolean) result;
    }

    /**
     * 全部匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足全部匹配
     */
    @Override
    public boolean allMatch(SerializedPredicate<T> predicate) {
        this.parser.parse("allMatch", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (boolean) result;
    }

    /**
     * 全部匹配
     *
     * @param predicate 匹配条件
     * @return 是否满足全部匹配
     */
    @Override
    public boolean allMatch(LambdaExpression predicate) {
        this.parser.addQueryOp("allMatch", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return (boolean) result;
    }

    /**
     * 第一个满足条件的对象
     *
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findFirst() {
        this.parser.parse("findFirst", null, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 第一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findFirst(SerializedPredicate<T> predicate) {
        this.parser.parse("findFirst", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 第一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findFirst(LambdaExpression predicate) {
        this.parser.addQueryOp("findFirst", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 最后一个满足条件的对象
     *
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findLast() {
        this.parser.parse("findLast", null, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 最后一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findLast(SerializedPredicate<T> predicate) {
        this.parser.parse("findLast", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 最后一个满足条件的对象
     *
     * @param predicate 条件
     * @return 第一个满足条件的对象
     */
    @Override
    public Optional<T> findLast(LambdaExpression predicate) {
        this.parser.addQueryOp("findLast", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 获取指定位置的元素
     *
     * @param index 索引
     * @return 指定位置的元素
     */
    @Override
    public Optional<T> elementAt(int index) {
        this.parser.parse("elementAt", null, new Object[]{this.typeClass, index});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 取符合条件的某个单个对象
     *
     * @return 单个对象
     */
    @Override
    public Optional<T> single() {
        this.parser.parse("single", null, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 取符合条件的某个单个对象
     *
     * @param predicate 条件
     * @return 单个对象
     */
    @Override
    public Optional<T> single(SerializedPredicate<T> predicate) {
        this.parser.parse("single", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 取符合条件的某个单个对象
     *
     * @param predicate 条件
     * @return 单个对象
     */
    @Override
    public Optional<T> single(LambdaExpression predicate) {
        this.parser.addQueryOp("single", predicate, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
        return result == null ? Optional.empty() : Optional.of((T) result);
    }

    /**
     * 强制包含
     *
     * @param includeExpression 表达式
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> include(SerializedFunction<T, R> includeExpression) {
        this.parser.parse("include", includeExpression, new Object[]{this.typeClass});
        return this;
    }

    /**
     * 强制包含
     *
     * @param includeExpression 字符串形式的表达式
     * @return 自身
     */
    @Override
    public <R> ObaseStream<T> include(String includeExpression) {
        this.parser.parse("include", null, new Object[]{includeExpression, this.typeClass});
        return this;
    }

    /**
     * 分组
     *
     * @param getKey 键属性的表达式
     * @return 分组结果
     */
    @Override
    public <TKey> ObaseStream<IGroupingBy<TKey, T>> groupBy(SerializedFunction<T, TKey> getKey) {
        this.parser.parse("groupBy", null, new Object[]{getKey, null, IGroupingBy.class});
        ObjectSet<IGroupingBy<TKey, T>> result = new ObjectSet<>(this.objectContext, IGroupingBy.class, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 分组
     *
     * @param getKey     键属性的表达式
     * @param getElement 元素属性的表达式
     * @return 分组结果流
     */
    @Override
    public <TKey, TElement> ObaseStream<IGroupingBy<TKey, TElement>> groupBy(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TElement> getElement) {
        this.parser.parse("groupBy", null, new Object[]{getKey, getElement, IGroupingBy.class});
        ObjectSet<IGroupingBy<TKey, TElement>> result = new ObjectSet<>(this.objectContext, IGroupingBy.class, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 分组并投影
     *
     * @param getKey     键属性的表达式
     * @param getResult  结果的表达式
     * @param resultType 结果类型
     * @return 分组结果流
     */
    @Override
    public <TKey, TResult> ObaseStream<TResult> groupBy(SerializedFunction<T, TKey> getKey, FunctionWithTwoArgs<TKey, IAggregation<T>, TResult> getResult, Class<?> resultType) {
        MethodChecker.registerClassMethod(resultType);
        this.parser.parse("groupBy", null, new Object[]{getKey, null, getResult, resultType});
        ObjectSet<TResult> result = new ObjectSet<>(this.objectContext, resultType, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 分组并投影
     *
     * @param getKey     键属性的表达式
     * @param getElement 元素属性的表达式
     * @param getResult  结果的表达式
     * @param resultType 结果类型
     * @return 分组结果流
     */
    @Override
    public <TKey, TElement, TResult> ObaseStream<TResult> groupBy(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TElement> getElement, FunctionWithTwoArgs<TKey, IAggregation<TElement>, TResult> getResult, Class<?> resultType) {
        MethodChecker.registerClassMethod(resultType);
        this.parser.parse("groupBy", null, new Object[]{getKey, getElement, getResult, resultType});
        ObjectSet<TResult> result = new ObjectSet<>(this.objectContext, resultType, true);
        result.setExpressionParser(this.parser);
        result.setOriginSet(this);

        return result;
    }

    /**
     * 转换为数组
     * 此方法为终结方法 会结算所有的操作
     *
     * @return 数组
     */
    @Override
    public T[] toArray() {
        List<T> realResult = this.getRealListResult();

        if (realResult.size() == 0) {
            T[] resultArray = (T[]) Array.newInstance(this.parser.getQueryOp().getResultType(), realResult.size());
            this.clearParser();
            return realResult.toArray(resultArray);
        } else {
            this.clearParser();

            T[] resultArray = (T[]) Array.newInstance(realResult.get(0).getClass(), realResult.size());
            return realResult.toArray(resultArray);
        }
    }

    /**
     * 转换为列表
     * 此方法为终结方法 会结算所有的操作
     *
     * @return 列表
     */
    @Override
    public List<T> toList() {
        List<T> realResult = this.getRealListResult();
        this.clearParser();
        return realResult;
    }

    /**
     * 获取列表结果
     *
     * @return 列表结果
     */
    private List<T> getRealListResult() {
        List<T> realResult = new ArrayList<>();
        T result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        if (result instanceof Iterable) {
            Iterable<T> results = (Iterable<T>) result;
            for (T tResult : results) {
                realResult.add((T) this.convertToTypeResult(tResult, null));
            }
        } else {
            realResult.add(result);
        }
        return realResult;
    }

    /**
     * 转换为HashMap 相同键的结果保留一个
     *
     * @param getKey    键属性的表达式
     * @param getResult 结果属性的表达式
     * @return 分组结果HashMap 相同键的结果保留一个
     */
    @Override
    public <TKey, TResult> HashMap<TKey, TResult> toHashMap(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TResult> getResult) {
        List<T> realResult = this.getRealListResult();

        HashMap<TKey, TResult> resultHashMap = new HashMap<>();

        for (T t : realResult) {
            if (resultHashMap.containsKey(getKey.apply(t)))
                throw new IllegalArgumentException("重复添加分组键" + getKey.apply(t) + "的元素,如单个分组键对应多个结果,请使用toHashMapWithIterableResult方法");
            resultHashMap.put(getKey.apply(t), getResult.apply(t));
        }

        this.clearParser();

        return resultHashMap;
    }

    /**
     * 转换为HashMap 并将相同键的结果放入Iterable中
     *
     * @param getKey    键属性的表达式
     * @param getResult 结果属性的表达式
     * @return 分组结果HashMap 相同键的结果放入Iterable中
     */
    @Override
    public <TKey, TResult> HashMap<TKey, Iterable<TResult>> toHashMapWithIterableResult(SerializedFunction<T, TKey> getKey, SerializedFunction<T, TResult> getResult) {
        List<T> realResult = this.getRealListResult();

        HashMap<TKey, Iterable<TResult>> resultHashMap = new HashMap<>();

        for (T t : realResult) {
            TKey key = getKey.apply(t);
            if (resultHashMap.containsKey(key)) {
                ((ArrayList<TResult>) resultHashMap.get(key)).add(getResult.apply(t));
            } else {
                ArrayList<TResult> container = new ArrayList<>();
                container.add(getResult.apply(t));
                resultHashMap.put(key, container);
            }
        }

        this.clearParser();

        return resultHashMap;
    }

    /**
     * 求平均数的真实执行方法
     *
     * @param expression 表达式
     * @return 平均数
     */
    private String realAvg(final Serializable expression) {
        this.parser.parse("avg", expression, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);

        return result.toString();
    }

    /**
     * 求和的真实执行方法
     *
     * @param expression 表达式
     * @return 和
     */
    private String realSum(final Serializable expression) {
        this.parser.parse("sum", expression, new Object[]{this.typeClass});
        Object result = this.queryProvider.execute(this.parser.getExpression(), this.parser.getQueryOp(), this.typeClass);
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);

        return result.toString();
    }


    /**
     * 元类型转换
     *
     * @param result 原始结果
     * @return 目标类型
     */
    private Object convertToTypeResult(Object result, Class<?> typeClass) {
        if (typeClass == null)
            typeClass = this.typeClass;
        if (int.class.getName().equals(typeClass.getName()) || Integer.class.getName().equals(typeClass.getName())) {
            return Integer.parseInt(result.toString());
        } else if (short.class.getName().equals(typeClass.getName()) || Short.class.getName().equals(typeClass.getName())) {
            return Short.parseShort(result.toString());
        } else if (float.class.getName().equals(typeClass.getName()) || Float.class.getName().equals(typeClass.getName())) {
            return Float.parseFloat(result.toString());
        } else if (double.class.getName().equals(typeClass.getName()) || Double.class.getName().equals(typeClass.getName())) {
            return Double.parseDouble(result.toString());
        } else if (long.class.getName().equals(typeClass.getName()) || Long.class.getName().equals(typeClass.getName())) {
            return Long.parseLong(result.toString());
        } else if (boolean.class.getName().equals(typeClass.getName()) || Boolean.class.getName().equals(typeClass.getName())) {
            if (result.toString().equals("1")) {
                result = true;
            } else if (result.toString().equals("0")) {
                result = false;
            } else {
                result = Boolean.parseBoolean(result.toString());
            }
            return result;
        } else if (BigDecimal.class.getName().equals(typeClass.getName())) {
            return new BigDecimal(result.toString());
        }
        return result;
    }

    /**
     * 清除终结方法所用的变量
     */
    private void clearParser() {
        //终结方法 所用的变量重新初始化
        this.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);

        ObjectSet<?> set = this.getOriginSet();
        while (set != null) {
            set.parser = new QueryExpressionParser(this.objectContext.getModel(), this.typeClass);
            set = set.getOriginSet();
        }
    }
}
