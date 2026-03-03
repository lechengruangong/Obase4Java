/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型扩展,存储针对结构化类型的扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:58:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 类型扩展
 */
public class TypeExtension {

    /**
     * 被扩展的类型
     */
    private StructuralType extendedType;

    /**
     * 被扩展的类型
     *
     * @return 被扩展的类型
     */
    public StructuralType getExtendedType() {
        return this.extendedType;
    }

    /**
     * 设置被扩展的类型
     *
     * @param extendedType 被扩展的类型
     */
    void setExtendedType(StructuralType extendedType) {
        this.extendedType = extendedType;
    }
}
