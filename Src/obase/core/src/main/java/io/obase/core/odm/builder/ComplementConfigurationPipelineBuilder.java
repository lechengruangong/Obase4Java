/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：补充配置管道建造器,负责建造补充配置管道.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 14:23:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.FunctionWithOneArg;

import java.util.ArrayList;
import java.util.List;

/**
 * 补充配置管道建造器
 */
public class ComplementConfigurationPipelineBuilder {

    /**
     * 寄存每次USE的委托
     */
    private final List<FunctionWithOneArg<IComplementConfigurator, IComplementConfigurator>> components = new ArrayList<>();

    /**
     * 建造补充配置管道
     * 递归到最后一个委托，调用该委托创建最后一个配置器，然后将其传入倒数第二个委托，创建倒数第二个配置器，依此类推，直到第一个。
     *
     * @return 补充配置管道
     */
    IComplementConfigurator build() {
        IComplementConfigurator complementConfigurator = null;
        for (int c = this.components.size() - 1; c >= 0; c--)
            complementConfigurator = this.components.get(c).invoke(complementConfigurator);

        return complementConfigurator;
    }

    /**
     * 向补充配置管道注册中间件，该管道用于在生成模型过程中执行补充配置
     *
     * @param middlewareDelegate 中间件委托，代表创建管道中间件（即补充配置器）的方法，该方法的参数用于指定管道中的下一个配置器 返回值为生成的中间件
     * @return 自身
     */
    public ComplementConfigurationPipelineBuilder use(
            FunctionWithOneArg<IComplementConfigurator, IComplementConfigurator> middlewareDelegate) {
        this.components.add(middlewareDelegate);
        return this;
    }
}
