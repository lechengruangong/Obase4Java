/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户的注册器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 15:02:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.IAddonRegister;
import io.obase.core.odm.builder.ModelBuilder;

/**
 * 多租户的注册
 */
public class AddonRegister implements IAddonRegister {
    /**
     * 为某个插件注册
     *
     * @param modelBuilder 建模器
     */
    @Override
    public void registry(ModelBuilder modelBuilder) {
        modelBuilder.useTypeAnalyzer(TypeAnalyzer::new);
        modelBuilder.useProxyTypeGenerator(ProxyTypeGenerator::new);
        modelBuilder.useComplementConfigurator(ComplementConfigurator::new);
    }
}
