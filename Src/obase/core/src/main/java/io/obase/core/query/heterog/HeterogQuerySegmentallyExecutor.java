/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构查询分段执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:32:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.query.oop.OopExecutor;

/**
 * 异构查询分段执行器，提供执行异构查询分解所得片段的方案，执行该方案所得结果即为异构查询的结果
 */
public class HeterogQuerySegmentallyExecutor implements IHeterogQuerySegmentallyExecutor {
    /**
     * 执行异构查询分解所得的片段
     *
     * @param segments             对异构查询实施分解产生的片段
     * @param heterogQueryProvider 异构查询提供程序，用于执行从异构运算中分解出的附加查询
     * @param attachObject         用于将对象附加到对象上下文的委托
     * @param attachRoot           指示是否附加根对象
     * @return 执行结果
     */
    @Override
    public Object execute(HeterogQuerySegments segments, HeterogQueryProvider heterogQueryProvider, IAttachObject attachObject, boolean attachRoot) {
        HeterogOpExecutor executor = HeterogOpExecutor.create(segments.MainQuery, heterogQueryProvider.getStorageProviderCreator(),
                heterogQueryProvider.getModel(),
                heterogQueryProvider::OnPreExecuteSql, heterogQueryProvider::OnPostExecuteSql, heterogQueryProvider, heterogQueryProvider.getBaseQueryProvider());

        boolean asRoot = false;
        if (attachRoot) {
            if (segments.Complement == null) {
                asRoot = true;
            } else {
                asRoot = segments.MainTail.getResultType() == segments.Complement.getTail().getResultType();
            }
        }

        Object result = executor.execute(segments.MainTail, segments.MainQuery, segments.Including,
                heterogQueryProvider.getAttachObject(), asRoot);

        if (segments.Complement != null) {
            OopExecutor oopExecutor = segments.Complement.generatePipeline();
            //执行补充运算
            if (result instanceof Iterable) return oopExecutor.execute((Iterable<Object>) result);

            return oopExecutor.execute(result, false);
        }

        return result;
    }
}
