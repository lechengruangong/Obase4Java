/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标注建模工具类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 11:05:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.core.odm.builder.ModelBuilder;

/**
 * 标注建模工具类
 */
public class AnnotationModelingExtensions {

    /**
     * 使用标注建模
     *
     * @param modelBuilder 建模器
     * @param assembly     要注册的程序包名
     */
    public static void useAnnotationModeling(ModelBuilder modelBuilder, String[] assembly) {
        //交由AssemblyAnalyzer注册
        for (String ass : assembly) {
            modelBuilder.registerType(ass, new AssemblyAnalyzer());
        }
    }
}
