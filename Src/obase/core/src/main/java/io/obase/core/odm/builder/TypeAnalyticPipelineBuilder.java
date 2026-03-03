/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型解析管道构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-18 15:39:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型解析管道构造器
 */
public class TypeAnalyticPipelineBuilder {

    /**
     * 寄存每次USE的委托
     */
    private final List<FunctionWithOneArg<ITypeAnalyzer, ITypeAnalyzer>> components = new ArrayList<>();

    /**
     * 建造类型解析管道
     * 递归到最后一个委托，调用该委托创建最后一个解析器，然后将其传入倒数第二个委托，创建倒数第二个解析器，依此类推，直到第一个。
     *
     * @return 类型解析管道
     */
    ITypeAnalyzer build() {
        ITypeAnalyzer typeAnalyzer = null;
        for (int c = this.components.size() - 1; c >= 0; c--)
            typeAnalyzer = this.components.get(c).invoke(typeAnalyzer);

        return typeAnalyzer;
    }

    /**
     * 向类型解析管道注册中间件，该管道用于在反射建模过程中解析类型。
     *
     * @param middlewareDelegate 中间件委托
     * @return 自身
     */
    public TypeAnalyticPipelineBuilder use(FunctionWithOneArg<ITypeAnalyzer, ITypeAnalyzer> middlewareDelegate) {
        this.components.add(middlewareDelegate);
        return this;
    }
}
