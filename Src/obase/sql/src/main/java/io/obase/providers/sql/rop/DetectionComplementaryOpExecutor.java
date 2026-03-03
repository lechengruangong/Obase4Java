/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：执行测定类运算的补充运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:22:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.oop.OopContext;
import io.obase.core.query.oop.OopExecutor;

import java.util.Iterator;

/**
 * 负责以对象运算方式执行测定类运算（AllOp, AnyOp, ContainsOp, SingleOp）的补充运算
 */
public class DetectionComplementaryOpExecutor extends OopExecutor {

    /**
     * 被执行的运算
     */
    private final DetectionComplementaryOp executedOp;

    /**
     * 初始化DetectionComplementaryOpExecutor的新实例
     *
     * @param executedOp 被执行的测定补充运算
     * @param next       运算管道中的下一个执行器
     */
    public DetectionComplementaryOpExecutor(DetectionComplementaryOp executedOp, OopExecutor next) {
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
        switch (this.executedOp.getName()) {
            case All: {
                int count = Integer.parseInt(oopContext.getResult().toString());
                oopContext.setResult(count <= 0);
            }
            break;

            case Any:
            case Contains: {
                int count = Integer.parseInt(oopContext.getResult().toString());
                oopContext.setResult(count > 0);
            }
            break;
            case Single: {
                Iterator<Object> objectIterator = oopContext.getSource().iterator();
                int multiCount = 0;
                while (objectIterator.hasNext()) {
                    Object result = objectIterator.next();
                    oopContext.setResult(result);
                    multiCount++;
                }

                if (multiCount > 1)
                    throw new IllegalArgumentException("Sequence contains more than one matching element");

            }

            break;
            default:
                throw new IllegalArgumentException("未知的测定运算: " + this.executedOp.getName());
        }

        if (this.next instanceof OopExecutor) {
            ((OopExecutor) this.next).execute(oopContext);
        }
    }
}
