/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举就地修改类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:03:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.io.Serializable;

/**
 * 枚举就地修改类型
 */
public enum EDirectlyChangeType implements Serializable {

    /**
     * 删除对象
     */
    Delete((byte) 0),

    /**
     * 更新属性值
     */
    Update((byte) 1),

    /**
     * 属性值自增
     */
    Increment((byte) 2);

    /**
     * 类型
     */
    private final byte type;

    /**
     * 枚举就地修改类型
     *
     * @param type 类型
     */
    EDirectlyChangeType(byte type) {
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
