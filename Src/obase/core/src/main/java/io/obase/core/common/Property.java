/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase的内省属性,从Java内省机制得到.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-16 12:22:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Obase的内省属性
 */
public class Property {

    /**
     * 字段名称
     */
    private final String fieldName;
    /**
     * 内省属性的名称
     * 首字母大写
     */
    private final String name;
    /**
     * 取值方法
     */
    private final Method getterMethod;
    /**
     * 设值方法
     */
    private final Method setterMethod;
    /**
     * 内省属性对应的字段
     */
    private final Field field;

    /**
     * 初始化Obase的内省属性
     *
     * @param type         所属的类型
     * @param name         内省属性的名称
     * @param getterMethod 取值方法
     * @param setterMethod 设值方法
     */
    Property(Class<?> type, String name, Method getterMethod, Method setterMethod) {
        this.name = StringUtils.capitalize(name);
        this.fieldName = name;
        this.getterMethod = getterMethod;
        this.setterMethod = setterMethod;

        //查找对应的字段
        this.field = Utils.getFieldIncludeSuperclass(type, name);
    }

    /**
     * 获取内省属性的名称
     *
     * @return 内省属性的名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取内省属性的字段
     *
     * @return 内省属性的字段
     */
    public Field getField() {
        return this.field;
    }

    /**
     * 获取内省属性的字段名称
     * 如果没有对应的字段 则返回的是内省器返回的属性名称
     *
     * @return 内省属性的字段名称 如果没有对应的字段 则返回的是内省器返回的属性名称
     */
    public String getFieldName() {
        return this.field == null ? this.fieldName : this.field.getName();
    }

    /**
     * 获取内省属性的设值方法
     *
     * @return 取值方法
     */
    public Method getGetterMethod() {
        return this.getterMethod;
    }

    /**
     * 获取内省属性的设值方法
     *
     * @return 设值方法
     */
    public Method getSetterMethod() {
        return this.setterMethod;
    }

    /**
     * 获取内省属性的类型
     *
     * @return 内省属性的类型
     */
    public Class<?> getPropertyType() {
        return this.field != null ? this.field.getType() : this.getterMethod.getReturnType();
    }

    /**
     * 获取属性的元素类型
     * 如果属性不是泛型的 则返回本身
     *
     * @return 属性的元素类型 如果属性不是泛型的 则返回本身
     */
    public Class<?>[] getPropertyElementType() {
        if (this.field == null)
            return Utils.getMethodReturnValueGenericTypeArguments(this.getterMethod);
        else
            return Utils.getFieldGenericTypeArguments(this.field);
    }

    /**
     * 获取是否有对应的字段
     *
     * @return 是否有对应的字段
     */
    public boolean getHasField() {
        return this.field != null;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "Property{" +
                "fieldName='" + this.fieldName + '\'' +
                ", name='" + this.name + '\'' +
                ", getterMethod=" + this.getterMethod +
                ", setterMethod=" + this.setterMethod +
                ", field=" + this.field +
                '}';
    }
}
