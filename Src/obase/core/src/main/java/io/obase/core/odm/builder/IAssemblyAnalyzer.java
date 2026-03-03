/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义从程序集提取类型并注册至模型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:14:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 定义从程序集提取类型并注册至模型的规范
 */
public interface IAssemblyAnalyzer {

    /**
     * 从指定的程序集提取类型并注册到模型
     *
     * @param assembly     要解析的包名
     * @param modelBuilder 建模器
     */
    void analyze(String assembly, ModelBuilder modelBuilder);

    /**
     * 从指定的类型数组中提取类型并注册到模型
     *
     * @param types        指定的类型
     * @param modelBuilder 建模器
     */
    void analyze(Class<?>[] types, ModelBuilder modelBuilder);
}
