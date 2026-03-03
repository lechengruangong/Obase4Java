/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义为模型中的类型生成代理类的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:52:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.ObjectType;
import net.bytebuddy.dynamic.DynamicType;

/**
 * 定义为模型中的类型生成代理类的规范
 */
public interface IProxyTypeGenerator {

    /**
     * 获取代理类型生成管道中的下一个生成器
     *
     * @return 下一个类型生成器
     */
    IProxyTypeGenerator getNext();

    /**
     * 为指定类型的代理类型定义成员
     *
     * @param typeBuilder  一个类型建造器，用于定义代理类型
     * @param objType      要为其定义代理类的类型，即代理类的基类
     * @param configurator 上述类型的配置器
     */
    DynamicType.Builder<?> defineMembers(DynamicType.Builder<?> typeBuilder, ObjectType objType, IObjectTypeConfigurator configurator);

    /**
     * 判定指定的类型是否需要生成代理类型
     *
     * @param objType      要判定的类型
     * @param configurator 上述类型的配置器
     * @return 是否需要生成代理类型
     */
    boolean should(ObjectType objType, IObjectTypeConfigurator configurator);
}
