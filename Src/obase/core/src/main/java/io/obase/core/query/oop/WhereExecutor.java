/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：筛选运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:58:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.query.WhereOp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 筛选运算执行器。
 */
public class WhereExecutor extends OopExecutor {

    /**
     * Where查询
     */
    private final WhereOp op;

    /**
     * 构造筛选运算执行器
     *
     * @param op 要执行的查询运算
     */
    public WhereExecutor(WhereOp op) {
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
            oopContext.setResult(list.stream().filter((Predicate<? super Object>) ExpressionDelegates.getInstance().get(this.op.getPredicate())).collect(Collectors.toList()));
        }

        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}
