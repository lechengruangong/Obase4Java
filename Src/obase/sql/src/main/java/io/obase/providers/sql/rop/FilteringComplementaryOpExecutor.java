/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：选择类运算的补充运算执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 15:27:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.query.FirstOp;
import io.obase.core.query.LastOp;
import io.obase.core.query.oop.OopContext;
import io.obase.core.query.oop.OopExecutor;

import java.util.Iterator;

/**
 * 负责以对象运算方式执行选择类运算（FirstOp, LastOp）的补充运算
 */
public class FilteringComplementaryOpExecutor extends OopExecutor {

    /**
     * 被执行的运算
     */
    private final FilteringComplementaryOp executedOp;

    /**
     * 初始化FilteringComplementaryOpExecutor的新实例
     *
     * @param executedOp 被执行的测定补充运算
     * @param next       运算管道中的下一个执行器
     */
    public FilteringComplementaryOpExecutor(FilteringComplementaryOp executedOp, OopExecutor next) {
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
        Iterator<Object> source = oopContext.getSource().iterator();
        switch (this.executedOp.getName()) {
            case First: {
                if (this.executedOp.getComplementedOp() instanceof FirstOp) {
                    FirstOp firstOp = (FirstOp) this.executedOp.getComplementedOp();
                    if (!source.hasNext() && !firstOp.getReturnDefault()) {
                        throw new IllegalArgumentException("序列不包含任何元素");
                    }
                    if (!source.hasNext()) {
                        oopContext.setResult(null);
                    } else {
                        oopContext.setResult(source.next());
                    }

                    while (source.hasNext()) {
                        source.next();
                        //取干净 防止连接未关闭
                    }
                    break;
                }
                break;
            }
            case Last: {
                if (this.executedOp.getComplementedOp() instanceof LastOp) {
                    LastOp lastOp = (LastOp) this.executedOp.getComplementedOp();
                    if (!source.hasNext() && !lastOp.getReturnDefault()) {
                        throw new IllegalArgumentException("序列不包含任何元素");
                    }
                    if (!source.hasNext()) {
                        oopContext.setResult(null);
                    } else {
                        oopContext.setResult(source.next());
                    }
                    while (source.hasNext()) {//用下一个元素替换
                        oopContext.setResult(source.next());
                    }
                    break;
                }
                break;
            }
        }


        if (this.next instanceof OopExecutor) {
            ((OopExecutor) this.next).execute(oopContext);
        }
    }
}

