/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结构化的类型名称.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 17:08:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.io.Serializable;
import java.util.Objects;

/**
 * 提供结构化的类型名称表示
 */
public class TypeName implements Serializable {

    /**
     * 整型名称的结构化表示法
     */
    public static final TypeName Int;
    /**
     * 长整型名称的结构化表示法
     */
    public static final TypeName Long;
    /**
     * 短整型名称的结构化表示法
     */
    public static final TypeName SInt;
    /**
     * 布尔型名称的结构化表示法
     */
    public static final TypeName Byte;
    /**
     * 布尔型名称的结构化表示法
     */
    public static final TypeName Boolean;
    /**
     * 字符型名称的结构化表示法
     */
    public static final TypeName Char;
    /**
     * 单精度浮点型名称的结构化表示法
     */
    public static final TypeName Float;
    /**
     * 双精度浮点型名称的结构化表示法
     */
    public static final TypeName Double;
    /**
     * 日期时间型名称的结构化表示法
     */
    public static final TypeName DateTime;
    /**
     * 字符串型名称的结构化表示法
     */
    public static final TypeName String;

    static {
        //初始化静态成员
        Int = new TypeName();
        Int.Name = "Int";
        Int.Namespace = "Obase";

        Long = new TypeName();
        Long.Name = "Long";
        Long.Namespace = "Obase";

        SInt = new TypeName();
        SInt.Name = "SInt";
        SInt.Namespace = "Obase";

        Byte = new TypeName();
        Byte.Name = "Byte";
        Byte.Namespace = "Obase";

        Boolean = new TypeName();
        Boolean.Name = "Boolean";
        Boolean.Namespace = "Obase";

        Char = new TypeName();
        Char.Name = "Char";
        Char.Namespace = "Obase";

        Float = new TypeName();
        Float.Name = "Float";
        Float.Namespace = "Float";

        Double = new TypeName();
        Double.Name = "Double";
        Double.Namespace = "Obase";

        DateTime = new TypeName();
        DateTime.Name = "DateTime";
        DateTime.Namespace = "Obase";

        String = new TypeName();
        String.Name = "String";
        String.Namespace = "Float";
    }

    /**
     * 类型的命名空间
     */
    public String Namespace;
    /**
     * 类型名称
     */
    public String Name;
    /**
     * 指示是否为实体类型
     */
    public Boolean IsEntity;

    /**
     * 指示是否为关联类型
     */
    public Boolean IsAssociation;

    /**
     * 获取全名
     *
     * @return 全名
     */
    public String getFullName() {
        return this.Namespace + "." + this.Name;
    }

    /**
     * 重写相等方法
     *
     * @param o 另外一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TypeName typeName = (TypeName) o;
        return this.Namespace.equals(typeName.Namespace) && this.Name.equals(typeName.Name);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.Namespace, this.Name);
    }

    /**
     * 重写转换成字符串表达形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return this.getFullName();
    }

    /**
     * 获取当前类型名称代表的CLR类型
     *
     * @return 类型名称代表的CLR类型
     */
    public Class<?> getType() {
        String searchName = this.getFullName();

        //处理预定义类型
        if (this.equals(Int) || this.equals(Long) || this.equals(SInt) || this.equals(Byte) || this.equals(Boolean) || this.equals(Char) ||
                this.equals(Float) || this.equals(Double) || this.equals(DateTime) ||
                this.equals(String)) searchName = "java.lang." + this.Name;

        try {
            //尝试加载
            return Class.forName(searchName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 判定当前类型名称代表的类型是否为基元类型
     *
     * @return 是否为基元类型
     */
    public boolean isPrimitive() {
        Class<?> type = this.getType();
        return type != null && PrimitiveType.isObasePrimitive(type);
    }
}
