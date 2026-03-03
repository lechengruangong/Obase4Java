/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：All测定运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:14:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.query.AllOp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AllExecutor extends OopExecutor {

    /**
     * 要执行的查询运算
     */
    private final AllOp op;

    /**
     * 构造AnyExecutor的新实例
     *
     * @param op 要执行的查询运算
     */
    public AllExecutor(AllOp op) {
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
            Iterable<Object> iterable = (Iterable<Object>) oopContext.getResult();
            List<Object> list = new ArrayList<>();
            iterable.forEach(list::add);
            oopContext.setResult(list.stream().allMatch((Predicate<? super Object>) ExpressionDelegates.getInstance().get(this.op.getPredicate())));
        } else {
            int intResult = Integer.parseInt(oopContext.getResult().toString());
            oopContext.setResult(intResult <= 0);
        }


        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}
