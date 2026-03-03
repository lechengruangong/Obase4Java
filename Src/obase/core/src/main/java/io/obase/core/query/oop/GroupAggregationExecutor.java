/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：分组运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:41:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.expression.IGroupingBy;
import io.obase.core.query.GroupAggregationOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分组聚合执行器
 */
public class GroupAggregationExecutor extends OopExecutor {

    /**
     * 分组聚合函数
     */
    private final GroupAggregationOp op;

    /**
     * 初始化OpExecutor类的新实例
     *
     * @param queryOp 要执行的查询运算
     */
    protected GroupAggregationExecutor(GroupAggregationOp queryOp) {
        super(queryOp, null);
        this.op = queryOp;
    }

    /**
     * 执行运算
     *
     * @param oopContext 运算上下文
     */
    @Override
    public void execute(OopContext oopContext) {
        //源数据
        Iterable<Object> iterable = (Iterable<Object>) oopContext.getResult();
        List<Object> list = new ArrayList<>();
        iterable.forEach(list::add);
        //分组
        List<IGroupingBy<Object, Object>> grouping = new ArrayList<>();
        for (Object obj : list) {
            if (this.op.getElementSelector() == null) {
                grouping.add(new GroupingBy<>(ExpressionDelegates.getInstance().get(this.op.getKeySelector()).invoke(new Object[]{obj}), obj));

            } else {
                grouping.add(new GroupingBy<>(ExpressionDelegates.getInstance().get(this.op.getKeySelector()).invoke(new Object[]{obj}),
                        ExpressionDelegates.getInstance().get(this.op.getElementSelector()).invoke(new Object[]{obj})));
            }
        }
        //放入HashMap
        Map<Object, List<Object>> resultHashMap = new HashMap<>();

        for (IGroupingBy<Object, Object> t : grouping) {
            if (resultHashMap.containsKey(t.getKey())) {
                (resultHashMap.get(t.getKey())).add(t.getElement());
            } else {
                ArrayList<Object> container = new ArrayList<>();
                container.add(t.getElement());
                resultHashMap.put(t.getKey(), container);
            }
        }
        //最终结果
        List<Object> result = new ArrayList<>();
        //先处理IAggregation聚合方法

        for (Object key : resultHashMap.keySet()) {
            List<Object> values = resultHashMap.get(key);
            Aggregation<Object> aggregation = new Aggregation<>(values);
            Object resultValue = ExpressionDelegates.getInstance().get(this.op.getResultSelector()).invoke(new Object[]{key, aggregation});
            result.add(resultValue);
        }
        //处理实参
        oopContext.setResult(result);

        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}

