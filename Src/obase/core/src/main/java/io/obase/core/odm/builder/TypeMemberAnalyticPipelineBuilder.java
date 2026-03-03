/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型成员解析管道建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 15:32:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型成员解析管道建造器
 */
public class TypeMemberAnalyticPipelineBuilder {

    /**
     * 寄存每次USE的委托
     */
    private final List<FunctionWithOneArg<ITypeMemberAnalyzer, ITypeMemberAnalyzer>> components = new ArrayList<>();

    /**
     * 建造类型成员解析管道
     *
     * @return 类型成员解析管道
     */
    ITypeMemberAnalyzer build() {
        ITypeMemberAnalyzer analyzer = null;
        for (int c = this.components.size() - 1; c >= 0; c--)
            analyzer = this.components.get(c).invoke(analyzer);

        return analyzer;
    }

    /**
     * 向类型成员解析管道注册中间件，该管道用于在反射建模过程中解析类型成员。
     *
     * @param middlewareDelegate 中间件委托
     * @return 自身
     */
    public TypeMemberAnalyticPipelineBuilder use(FunctionWithOneArg<ITypeMemberAnalyzer, ITypeMemberAnalyzer> middlewareDelegate) {
        this.components.add(middlewareDelegate);
        return this;
    }
}
