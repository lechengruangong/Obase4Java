/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除代理类型生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 10:58:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.odm.ObjectType;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.builder.IObjectTypeConfigurator;
import io.obase.core.odm.builder.IProxyTypeGenerator;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;

/**
 * 代理类型生成器
 */
public class ProxyTypeGenerator implements IProxyTypeGenerator {

    /**
     * 获取代理类型生成管道中的下一个生成器
     */
    private final IProxyTypeGenerator next;

    /**
     * 构造代理类型生成器
     *
     * @param next 下一个生成器
     */
    public ProxyTypeGenerator(IProxyTypeGenerator next) {
        this.next = next;
    }

    /**
     * 获取代理类型生成管道中的下一个生成器
     *
     * @return 下一个类型生成器
     */
    @Override
    public IProxyTypeGenerator getNext() {
        return this.next;
    }

    /**
     * 为指定类型的代理类型定义成员
     *
     * @param typeBuilder  一个类型建造器，用于定义代理类型
     * @param objType      要为其定义代理类的类型，即代理类的基类
     * @param configurator 上述类型的配置器
     */
    @Override
    public DynamicType.Builder<?> defineMembers(DynamicType.Builder<?> typeBuilder, ObjectType objType, IObjectTypeConfigurator configurator) {
        if (this.should(objType, configurator)) {
            //如果应当生成代理类型，为该类型定义一个公有字段
            typeBuilder = typeBuilder.defineField("obase_gen_deletionMark", boolean.class, Visibility.PUBLIC);
            typeBuilder = typeBuilder.defineMethod("getObase_gen_deletionMark", boolean.class, Visibility.PUBLIC).intercept(FieldAccessor.ofField("obase_gen_deletionMark"));
        }
        return typeBuilder;
    }

    /**
     * 判定指定的类型是否需要生成代理类型
     *
     * @param objType      要判定的类型
     * @param configurator 上述类型的配置器
     * @return 是否需要生成代理类型
     */
    @Override
    public boolean should(ObjectType objType, IObjectTypeConfigurator configurator) {
        //如果已启用逻辑删除（GetExtension<LogicDeletionExtension>不为null），且_deletionMark属性未设值，则应当生成代理类型，否则不生成
        LogicDeletionExtension ext = (LogicDeletionExtension) objType.getExtension(LogicDeletionExtension.class);
        //判定当前的结果
        boolean result = ext != null && (ext.getDeletionMark() == null || ext.getDeletionMark().isEmpty());
        //向上溯源
        StructuralType baseType = objType.getDerivingFrom();
        while (baseType != null) {
            ext = (LogicDeletionExtension) baseType.getExtension(LogicDeletionExtension.class);
            result |= ext != null && (ext.getDeletionMark() == null || ext.getDeletionMark().isEmpty());
            baseType = baseType.getDerivingFrom();
        }
        return result;
    }
}
