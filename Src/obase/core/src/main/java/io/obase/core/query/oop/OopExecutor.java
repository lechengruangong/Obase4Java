/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象运算执行器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-26 16:22:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.query.*;

/**
 * 为对象运算执行器提供基础实现
 */
public abstract class OopExecutor extends OpExecutorWithContext<OopContext> {
    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     * @param next    运算管道中的下一个执行器
     */
    protected OopExecutor(QueryOp queryOp, OpExecutorWithContext<OopContext> next) {
        super(queryOp, next);
    }

    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     */
    protected OopExecutor(QueryOp queryOp) {
        super(queryOp, null);
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param allOp 要执行的运算
     * @param next  对象运算管道中的下一个执行器
     * @return all运算执行器
     */
    static OopExecutor create(AllOp allOp, OopExecutor next) {
        AllExecutor allExecutor = new AllExecutor(allOp);
        allExecutor.next = next;
        return allExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param anyOp 要执行的运算
     * @param next  对象运算管道中的下一个执行器
     * @return any运算执行器
     */
    static OopExecutor create(AnyOp anyOp, OopExecutor next) {
        AnyExecutor anyExecutor = new AnyExecutor(anyOp);
        anyExecutor.next = next;
        return anyExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param distinctOp 要执行的Distinct运算
     * @param next       对象运算管道中的下一个执行器
     * @return distinct运算执行器
     */
    static OopExecutor create(DistinctOp distinctOp, OopExecutor next) {
        DistinctExecutor distinctExecutor = new DistinctExecutor(distinctOp);
        distinctExecutor.next = next;
        return distinctExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param element 要执行的运算
     * @param next    对象运算管道中的下一个执行器
     * @return elementAt运算执行器
     */
    static OopExecutor create(ElementAtOp element, OopExecutor next) {
        ElementAtExecutor elementAtExecutor = new ElementAtExecutor(element);
        elementAtExecutor.next = next;
        return elementAtExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param firstOp 要执行的运算
     * @param next    对象运算管道中的下一个执行器
     * @return first运算执行器
     */
    static OopExecutor create(FirstOp firstOp, OopExecutor next) {
        FirstExecutor firstExecutor = new FirstExecutor(firstOp);
        firstExecutor.next = next;
        return firstExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param groupOp 要执行的运算
     * @param next    对象运算管道中的下一个执行器
     * @return groupAggregation运算执行器
     */
    static OopExecutor create(GroupAggregationOp groupOp, OopExecutor next) {
        GroupAggregationExecutor groupExecutor = new GroupAggregationExecutor(groupOp);
        groupExecutor.next = next;
        return groupExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param groupOp 要执行的运算
     * @param next    对象运算管道中的下一个执行器
     * @return group运算执行器
     */
    static OopExecutor create(GroupOp groupOp, OopExecutor next) {
        GroupExecutor groupExecutor = new GroupExecutor(groupOp);
        groupExecutor.next = next;
        return groupExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param lastOp 要执行的运算
     * @param next   对象运算管道中的下一个执行器
     * @return last运算执行器
     */
    static OopExecutor create(LastOp lastOp, OopExecutor next) {
        LastExecutor lastExecutor = new LastExecutor(lastOp);
        lastExecutor.next = next;
        return lastExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param selectOp 要执行的Select运算
     * @param next     对象运算管道中的下一个执行器
     * @return select运算执行器
     */
    static OopExecutor create(SelectOp selectOp, OopExecutor next) {
        SelectExecutor selectExecutor = new SelectExecutor(selectOp);
        selectExecutor.next = next;
        return selectExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param skipOp 要执行的Skip运算
     * @param next   对象运算管道中的下一个执行器
     * @return skip运算执行器
     */
    static OopExecutor create(SkipOp skipOp, OopExecutor next) {
        SkipExecutor skipExecutor = new SkipExecutor(skipOp);
        skipExecutor.next = next;
        return skipExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param takeOp 要执行的运算
     * @param next   对象运算管道中的下一个执行器
     * @return take运算执行器
     */
    static OopExecutor create(TakeOp takeOp, OopExecutor next) {
        TakeExecutor takeExecutor = new TakeExecutor(takeOp);
        takeExecutor.next = next;
        return takeExecutor;
    }

    /**
     * 为指定的查询运算创建执行器
     *
     * @param whereOp 要执行的Where运算
     * @param next    对象运算管道中的下一个执行器
     * @return where运算执行器
     */
    static OopExecutor create(WhereOp whereOp, OopExecutor next) {
        WhereExecutor whereExecutor = new WhereExecutor(whereOp);
        whereExecutor.next = next;
        return whereExecutor;
    }

    /**
     * 执行对象运算
     *
     * @param sourceObjs 查询源
     * @return 执行查询运算的结果
     */
    public Object execute(Iterable<Object> sourceObjs) {
        OopContext context = new OopContext(sourceObjs);
        //按照查询源序列处理
        this.execute(context);
        return context.getResult();
    }

    /**
     * 执行对象运算
     *
     * @param initValue  运算基点值
     * @param isIterable 是否是Iterable
     * @return 运算结果
     */
    public Object execute(Object initValue, boolean isIterable) {
        if (initValue instanceof Iterable && isIterable)
            return this.execute((Iterable<Object>) initValue);

        OopContext context = new OopContext(initValue);
        this.execute(context);
        return context.getResult();
    }
}
