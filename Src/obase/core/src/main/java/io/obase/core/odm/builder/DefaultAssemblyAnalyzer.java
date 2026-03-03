/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的程序集解析器,负责从程序集中注册结构化类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 12:27:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.AssemblyUtil;
import io.obase.core.common.Utils;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认的程序集解析器
 */
public class DefaultAssemblyAnalyzer implements IAssemblyAnalyzer {

    /**
     * 存储从程序集解析类型过程中应忽略的类型
     */
    private final HashSet<Class<?>> ignoredTypes;

    /**
     * 构造默认的程序集解析器
     *
     * @param ignoredTypes 应忽略的类型
     */
    public DefaultAssemblyAnalyzer(HashSet<Class<?>> ignoredTypes) {
        this.ignoredTypes = ignoredTypes;
    }

    /**
     * 从指定的程序集提取类型并注册到模型
     *
     * @param assembly     要解析的包名
     * @param modelBuilder 建模器
     */
    @Override
    public void analyze(String assembly, ModelBuilder modelBuilder) {
        //1.显式关联推断 如果类（Class）上未定义符合“标识属性推断”约定的访问器，推断其为关联类型。
        //2.实体类型推断 如果类（Class）不能推断为显式关联，推断为实体类型。
        //3.复杂类型推断 结构体推断为复杂类型。
        Set<Class<?>> types = AssemblyUtil.getAllClassByPackageName(assembly);

        this.analyze(types.toArray(new Class<?>[0]), modelBuilder);
    }

    /**
     * 从指定的类型数组中提取类型并注册到模型
     *
     * @param types        指定的类型
     * @param modelBuilder 建模器
     */
    @Override
    public void analyze(Class<?>[] types, ModelBuilder modelBuilder) {
//解析所有的类型
        for (Class<?> type : types) {

            //忽略的类型不参与推断
            if (this.ignoredTypes.contains(type))
                continue;

            //已配置的不参与
            if (modelBuilder.findConfiguration(type) != null)
                continue;

            //枚举 接口 抽象类不参与推断
            if (type.isEnum() || type.isInterface() || Modifier.isAbstract(type.getModifiers()))
                continue;

            //如果是类
            //定义了符合“标识属性推断”约定的访问器
            if (Utils.existIdentity(type, new ObjectReferencePack<>())) {
                //推断为实体类型
                modelBuilder.entityI(type);
            }
            //没定义
            else {
                //推断为显式关联
                modelBuilder.associationI(type);
            }
            //没有结构体
        }
    }
}
