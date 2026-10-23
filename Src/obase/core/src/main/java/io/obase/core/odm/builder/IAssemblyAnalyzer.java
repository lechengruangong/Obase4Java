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
 * 定义从程序集（Java中对应一组类型，通常以包名标识）提取类型并注册至模型的规范
 */
public interface IAssemblyAnalyzer {

    /**
     * 从指定的程序集提取类型并注册到模型
     * 参数的具体含义（程序集名称或包名）由实现类决定，DefaultAssemblyAnalyzer将其作为包名处理
     *
     * @param assembly     要解析的包名
     * @param modelBuilder 建模器
     */
    void analyze(String assembly, ModelBuilder modelBuilder);

    /**
     * 从指定的类型数组中提取类型并注册到模型
     * 由实现类决定如何筛选与推断类型
     *
     * @param types        指定的类型
     * @param modelBuilder 建模器
     */
    void analyze(Class<?>[] types, ModelBuilder modelBuilder);
}
