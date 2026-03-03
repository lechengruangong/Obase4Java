/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase内省器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-16 12:21:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Obase内省器
 */
public class ObaseIntrospector {

    /**
     * 获取Obase规定的类属性
     * 只获取当前类型与基类之间的属性
     *
     * @param beanClass  类型
     * @param superClass 基类
     * @return Obase规定的类属性
     */
    public static List<Property> getObaseBeanProperties(Class<?> beanClass, Class<?> superClass) {
        //目前使用Introspector.getBeanInfo(hostType).getPropertyDescriptors()
        //之后考虑自己制定规则

        //查询缓存
        PropertyKey key = new PropertyKey(beanClass, superClass);
        List<Property> properties = GlobalObasePropertyCache.getInstance().getProperties(key);
        //没查到 构造内省属性
        if (properties == null) {
            PropertyDescriptor[] propertyDescriptors;
            try {
                propertyDescriptors = Introspector.getBeanInfo(beanClass, superClass).getPropertyDescriptors();
            } catch (IntrospectionException e) {
                throw new RuntimeException("使用java.beans.Introspector获取" + beanClass.getName() + "的Bean信息错误.", e);
            }

            properties = Arrays.stream(propertyDescriptors).map(p -> new Property(beanClass, p.getName(), p.getReadMethod(), p.getWriteMethod())).collect(Collectors.toList());
            GlobalObasePropertyCache.getInstance().setProperties(key, properties);

        }
        return properties;
    }

    /**
     * 获取Obase规定的类属性
     * 获取当前类型直到Object类型的属性
     *
     * @param beanClass 类型
     * @return Obase规定的类属性
     */
    public static List<Property> getObaseBeanProperties(Class<?> beanClass) {
        return getObaseBeanProperties(beanClass, Object.class);
    }
}
