/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：复杂属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:37:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 表示复杂属性
 */
public class ComplexAttribute extends Attribute {

    /**
     * 复杂属性的类型
     */
    private final ComplexType complexType;

    /**
     * 映射连接符，用于将当前复杂属性的映射目标（TargetField）与其子属性的映射目标（TargetField）串联起来，构成子属性的映射字段。
     * 术语约定
     * 当属性为复杂属性或子属性时，TargetField并非完整的字段名，而是字段名的一部分，简称映射目标。沿属性路径，以映射连接符依次将映射目标串联起来即构成完整的
     * 映射字段。
     */
    private char mappingConnectionChar = (char) -1;

    /**
     * 创建Attribute实例
     *
     * @param dataType 数据类型
     * @param name     属性名称
     */
    public ComplexAttribute(Class<?> dataType, String name, ComplexType complexType) {
        super(dataType, name);

        this.complexType = complexType;
    }

    /**
     * 获取复杂属性的类型
     *
     * @return 复杂属性的类型
     */
    public ComplexType getComplexType() {
        return this.complexType;
    }

    /**
     * 映射连接符，用于将当前复杂属性的映射目标（TargetField）与其子属性的映射目标（TargetField）串联起来，构成子属性的映射字段。
     *
     * @return 映射连接符
     */
    public char getMappingConnectionChar() {
        return this.mappingConnectionChar;
    }

    /**
     * 设置映射连接符
     *
     * @param mappingConnectionChar 映射连接符
     */
    public void setMappingConnectionChar(char mappingConnectionChar) {
        this.mappingConnectionChar = mappingConnectionChar;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "ComplexAttribute:{{Name-\"" + this.getName() + "\",DataType-\"" + this.getDataType().getName() + "\"}}";
    }
}
