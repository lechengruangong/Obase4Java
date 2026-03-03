/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：索引运算的补充运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:30:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.ElementAtOp;
import io.obase.core.query.oop.OopContext;
import io.obase.core.query.oop.OopExecutor;

import java.util.Iterator;

/**
 * 负责以对象运算方式执行索引运算（ElementAtOp）的补充运算
 */
public class IndexComplementaryOpExecutor extends OopExecutor {

    /**
     * 被执行的运算
     */
    private final IndexComplementaryOp executedOp;

    /**
     * 初始化IndexComplementaryOpExecutor的新实例
     *
     * @param executedOp 被执行的索引补充运算
     * @param next       运算管道中的下一个执行器
     */
    public IndexComplementaryOpExecutor(IndexComplementaryOp executedOp, OopExecutor next) {
        super(executedOp, next);
        this.executedOp = executedOp;
    }

    /**
     * 执行运算
     *
     * @param oopContext 运算上下文
     */
    @Override
    public void execute(OopContext oopContext) {
        Iterator<Object> tor = oopContext.getSource().iterator();
        Object result = tor.next();
        if (this.executedOp.getComplementedOp() instanceof ElementAtOp) {
            ElementAtOp elementAtOp = (ElementAtOp) this.executedOp.getComplementedOp();
            if (!elementAtOp.getReturnDefault()) {
                if (result == null)
                    throw new IllegalArgumentException("Sequence does not have any matching element");
            }
        }

        oopContext.setResult(result);
        while (tor.hasNext()) {
            tor.next();
            //取干净 防止连接未关闭
        }

        if (this.next instanceof OopExecutor) {
            ((OopExecutor) this.next).execute(oopContext);
        }
    }
}
