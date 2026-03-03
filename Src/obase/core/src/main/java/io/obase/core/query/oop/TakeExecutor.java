/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提取运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:57:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.query.TakeOp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提取运算执行器
 */
public class TakeExecutor extends OopExecutor {

    /**
     * 要执行的查询运算
     */
    private final TakeOp op;

    /**
     * 构造TakeExecutor的新实例
     *
     * @param op 要执行的查询运算
     */
    public TakeExecutor(TakeOp op) {
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
            oopContext.setResult(list.stream().limit(this.op.getCount()).collect(Collectors.toList()));
        }

        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}
