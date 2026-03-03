/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：分组运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:49:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.expression.IGroupingBy;
import io.obase.core.query.GroupOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 分组运算执行器
 */
public class GroupExecutor extends OopExecutor {

    /**
     * 运算
     */
    private final GroupOp op;

    /**
     * 构造GroupExecutor的实例
     *
     * @param op 要执行的查询运算
     */
    public GroupExecutor(GroupOp op) {
        super(op, null);
        this.op = op;
    }

    /**
     * 执行运算
     *
     * @param oopContext 运算上下文
     */
    @Override
    public void execute(OopContext oopContext) {
        if (oopContext.getResult() instanceof Iterable) {
            List<IGroupingBy<Object, Object>> grouping = new ArrayList<>();

            Iterable<Object> iterable = (Iterable<Object>) oopContext.getResult();
            List<Object> list = new ArrayList<>();
            iterable.forEach(list::add);

            for (Object obj : list) {
                if (this.op.getElementSelector() != null) {
                    grouping.add(new GroupingBy<>(ExpressionDelegates.getInstance().get(this.op.getKeySelector()).invoke(new Object[]{obj}),
                            ExpressionDelegates.getInstance().get(this.op.getElementSelector()).invoke(new Object[]{obj})));
                } else {
                    grouping.add(new GroupingBy<>(ExpressionDelegates.getInstance().get(this.op.getKeySelector()).invoke(new Object[]{obj}), obj));
                }
            }

            oopContext.setResult(grouping);
        }

        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}

