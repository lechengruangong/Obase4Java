/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:37:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.Date;

/**
 * 为类型提供基础实现
 */
public abstract class TypeBase {

    /**
     * 类型对应的对象系统类型
     */
    protected Class<?> clrType;

    /**
     * 类型名称
     */
    protected String name;

    /**
     * 类型所属的命名空间
     */
    protected String namespace;

    /**
     * 表示类型名称的结构体
     */
    protected TypeName typeName;

    /**
     * 构造TypeBase实例
     *
     * @param clrType 运行时类型
     */
    protected TypeBase(Class<?> clrType) {
        this.clrType = clrType;
        this.typeName = new TypeName();
        this.typeName.Name = clrType.getName();
        this.typeName.Namespace = clrType.getPackage() == null ? clrType.getName() : clrType.getPackage().getName();
        this.name = clrType.getSimpleName();
        this.namespace = clrType.getPackage() == null ? clrType.getName() : clrType.getPackage().getName();
    }

    /**
     * 初始化TypeBase的新实例，该实例还没有关联的对象系统类型，有待后续指定。
     */
    protected TypeBase() {
    }

    /**
     * 获取表示类型名称的结构体
     *
     * @return 获取表示类型名称的结构体
     */
    public TypeName getTypeName() {
        return this.typeName;
    }

    /**
     * 获取类型所属的命名空间
     *
     * @return 命名空间
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * 设置类型所属的命名空间
     *
     * @param namespace 命名空间
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置类型名称
     *
     * @param name 类型名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取类型对应的对象系统类型
     *
     * @return 获取类型对应的对象系统类型
     */
    public Class<?> getClrType() {
        return this.clrType;
    }

    /**
     * 设置对象系统类型
     *
     * @param value 对象系统类型
     */
    protected void SetClrType(Class<?> value) {
        if (this.clrType != null)
            throw new IllegalArgumentException("不能修改类型关联的对象系统类型。");
        this.clrType = value;
        this.typeName = this.generateObaseTypeName(value);
        this.name = value.getName();
        this.namespace = value.getPackage().getName();
    }

    /**
     * 获取类型的完全限定名
     *
     * @return 获取类型的完全限定名
     */
    public String getFullName() {
        return this.clrType.getTypeName();
    }

    /**
     * 生成TypeName
     * 当为基元类型时返回对应的结构化表示法
     *
     * @param type Clr类型
     * @return 类型名称
     */
    TypeName generateObaseTypeName(Class<?> type) {
        //从基础类型中寻找预制的类型
        if (type == String.class)
            return TypeName.String;
        if (type == Date.class)
            return TypeName.DateTime;
        if (type == double.class || type == Double.class)
            return TypeName.Double;
        if (type == float.class || type == Float.class)
            return TypeName.Float;
        if (type == char.class || type == Character.class)
            return TypeName.Char;
        if (type == boolean.class || type == Boolean.class)
            return TypeName.Boolean;
        if (type == short.class || type == Short.class)
            return TypeName.SInt;
        if (type == long.class || type == Long.class)
            return TypeName.Long;
        if (type == int.class || type == Integer.class)
            return TypeName.Int;
        TypeName typeName = new TypeName();
        typeName.Name = type.getName();
        typeName.Namespace = type.getPackage().getName();

        return typeName;
    }
}
