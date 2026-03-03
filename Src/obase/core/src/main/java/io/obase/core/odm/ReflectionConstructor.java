/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于反射的构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:59:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 基于反射的构造器
 */
public class ReflectionConstructor extends InstanceConstructor {

    /**
     * 类型的构造函数信息，用于反射调用。
     */
    private final Constructor<?> constructorInfo;

    /**
     * 创建ReflectionConstructor实例
     *
     * @param constructorInfo 构造函数
     */
    public ReflectionConstructor(Constructor<?> constructorInfo) {
        this.constructorInfo = constructorInfo;
        constructorInfo.setAccessible(true);
    }


    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    @Override
    public Object construct(Object[] arguments) {

        if (arguments == null)
            arguments = new Object[0];

        this.defaultArgumentConvert(arguments);
        //解除访问限制
        this.constructorInfo.setAccessible(true);
        try {
            return this.constructorInfo.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法构造对象,请参考内部异常.", e);
        }
    }
}
