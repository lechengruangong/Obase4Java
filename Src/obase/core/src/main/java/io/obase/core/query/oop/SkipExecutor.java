/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：略过运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:55:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.query.SkipOp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 略过运算执行器
 */
public class SkipExecutor extends OopExecutor {

    /**
     * 要执行的查询运算
     */
    private final SkipOp op;

    /**
     * 构造SkipExecutor的新实例
     *
     * @param op 要执行的查询运算
     */
    public SkipExecutor(SkipOp op) {
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
            oopContext.setResult(list.stream().skip(this.op.getCount()).collect(Collectors.toList()));
        }

        if (this.next instanceof OopExecutor) {
            OopExecutor executor = (OopExecutor) this.next;
            executor.execute(oopContext);
        }
    }
}
