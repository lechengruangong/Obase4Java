/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举元素类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 14:51:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 枚举元素类型
 */
public enum EElementType {

    /**
     * 属性
     */
    Attribute((byte) 1),
    /**
     * 关联引用
     */
    AssociationReference((byte) 2),
    /**
     * 关联端
     */
    AssociationEnd((byte) 4),

    /**
     * 视图引用
     */
    ViewReference((byte) 5);

    /**
     * 类型
     */
    private final byte type;

    /**
     * 枚举元素类型
     *
     * @param type 类型
     */
    EElementType(byte type) {
        this.type = type;
    }

    /**
     * 类型
     *
     * @return 类型
     */
    public byte getType() {
        return this.type;
    }
}
