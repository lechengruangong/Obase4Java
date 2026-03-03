/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标注建模的注册器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 15:00:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.core.IAddonRegister;
import io.obase.core.odm.builder.ModelBuilder;

/**
 * 标注建模的注册器
 */
public class AddonRegister implements IAddonRegister {
    /**
     * 为某个插件注册
     *
     * @param modelBuilder 建模器
     */
    @Override
    public void registry(ModelBuilder modelBuilder) {
        //注册标注的解析器
        modelBuilder.useTypeAnalyzer(AnnotatedTypeAnalyzer::new);
        modelBuilder.useMemberAnalyzer(AnnotatedMemberAnalyzer::new);
    }
}
