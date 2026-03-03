/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义在反射建模过程中解析类型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 16:00:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 定义在反射建模过程中解析类型的规范
 */
public interface ITypeAnalyzer {

    /**
     * 获取类型解析管道中的下一个解析器
     *
     * @return 下一个类型解析器
     */
    ITypeAnalyzer getNext();

    /**
     * 配置指定的类型
     *
     * @param type         要配置的类型
     * @param configurator 该类型的配置器
     */
    void configure(Class<?> type, IStructuralTypeConfigurator configurator);

    /**
     * 配置指定的对象类型
     *
     * @param type         要配置的对象类型
     * @param configurator 该对象类型的配置器
     */
    void configure(Class<?> type, IObjectTypeConfigurator configurator);

    /**
     * 配置指定的实体型
     *
     * @param type         要配置的实体类
     * @param configurator 该实体型的配置器
     */
    void configure(Class<?> type, IEntityTypeConfigurator configurator);

    /**
     * 配置指定的关联型
     *
     * @param type         要配置的关联型
     * @param configurator 该关联型的配置器
     */
    void configure(Class<?> type, IAssociationTypeConfigurator configurator);
}
