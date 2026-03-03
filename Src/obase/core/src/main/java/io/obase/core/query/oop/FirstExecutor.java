/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：First索引运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:39:28
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.query.oop;

import io.obase.core.query.FirstOp;

import java.util.ArrayList;
import java.util.List;

/**
 * First索引运算执行器
 */
public class FirstExecutor extends OopExecutor {

    /**
     * 要执行的查询运算
     */
    private final FirstOp op;

    /**
     * 构造FirstExecutor的新实例
     *
     * @param op 要执行的查询运算
     */
    public FirstExecutor(FirstOp op) {
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
            Object result = list.stream().findFirst().orElse(null);
            if (this.op.getReturnDefault()) {
                oopContext.setResult(result);
            } else {
                if (result == null)
                    throw new IllegalArgumentException("Sequence does not contains any matching element");
                else
                    oopContext.setResult(result);
            }
        }


        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}
