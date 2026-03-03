/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：反射建模过程中解析类型成员接口,提供解析类型成员的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 16:01:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;

/**
 * 定义在反射建模过程中解析类型成员的规范
 */
public interface ITypeMemberAnalyzer {

    /**
     * 获取类型成员解析管道中的下一个解析器
     *
     * @return 下一个成员解析器
     */
    ITypeMemberAnalyzer getNext();

    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo 类型成员
     * @param name       如果作为元素，返回元素名称 取元素[0]
     * @return 是否将作为类型元素
     */
    boolean asElement(Property memberInfo, ObjectReferencePack<String> name);

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, ITypeElementConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, IAttributeConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, IReferenceElementConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, IAssociationReferenceConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, IAssociationEndConfigurator configurator);

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    void configure(Property memberInfo, IStructuralTypeConfigurator configurator);
}
