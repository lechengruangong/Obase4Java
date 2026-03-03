/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：代理类型生成管道建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 12:00:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理类型生成管道建造器
 */
public class ProxyTypeGenerationPipelineBuilder {

    /**
     * 寄存每次USE的委托
     */
    private final List<FunctionWithOneArg<IProxyTypeGenerator, IProxyTypeGenerator>> components = new ArrayList<>();

    /**
     * 建造代理类型生成管道
     * 递归到最后一个委托，调用该委托创建最后一个生成器，然后将其传入倒数第二个委托，创建倒数第二个生成器，依此类推，直到第一个。
     *
     * @return 代理类型生成管道
     */
    IProxyTypeGenerator build() {
        IProxyTypeGenerator proxyTypeGenerator = null;
        for (int c = this.components.size() - 1; c >= 0; c--)
            proxyTypeGenerator = this.components.get(c).invoke(proxyTypeGenerator);

        return proxyTypeGenerator;
    }

    /**
     * 向代理类型生成管道注册中间件，该管道用于为模型中注册的类型生成代理类。
     *
     * @param middlewareDelegate 中间件委托
     * @return 代理类型生成管道建造器
     */
    public ProxyTypeGenerationPipelineBuilder use(FunctionWithOneArg<IProxyTypeGenerator, IProxyTypeGenerator> middlewareDelegate) {
        this.components.add(middlewareDelegate);
        return this;
    }
}
