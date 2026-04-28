/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体类型构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:42:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * 序列化实体类型构造器
 */
public class SerializationConstructor {

    /**
     * 构造函数
     */
    private final Constructor<?> constructorInfo;

    /**
     * 构造器的真实参数个数
     */
    private final int realParameterCount;

    /**
     * 获取构造函数的形式参数
     */
    private final HashMap<String, SerializationConstructorParameter> parameters;

    /**
     * 初始化序列化实体类型构造器
     *
     * @param constructorInfo 构造函数
     */
    public SerializationConstructor(Constructor<?> constructorInfo) {
        this.constructorInfo = constructorInfo;
        this.realParameterCount = constructorInfo.getParameterCount();
        this.parameters = new HashMap<>();
    }

    /**
     * 获取构造器的真实参数个数
     *
     * @return 构造器的真实参数个数
     */
    public int getRealParameterCount() {
        return this.realParameterCount;
    }

    /**
     * 获取构造函数的形式参数
     *
     * @return 构造函数的形式参数
     */
    public HashMap<String, SerializationConstructorParameter> getParameters() {
        return this.parameters;
    }

    /**
     * 构造对象
     *
     * @return 构造出的对象
     */
    public Object construct() {
        return this.construct(null);
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    public Object construct(Object[] arguments) {
        try {
            if (arguments == null)
                arguments = new Object[0];
            this.constructorInfo.setAccessible(true);
            return this.constructorInfo.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法构造要反序列化的对象,请参考内部异常.", e);
        }
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "SerializationConstructor{" +
                "constructorInfo=" + this.constructorInfo +
                ", realParameterCount=" + this.realParameterCount +
                '}';
    }
}
