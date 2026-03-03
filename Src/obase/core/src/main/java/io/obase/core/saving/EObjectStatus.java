/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举对象状态.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:09:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 枚举对象状态
 */
public enum EObjectStatus {

    /**
     * 未发生更改
     */
    Unchanged((byte) 0),

    /**
     * 新增的
     */
    Added((byte) 1),

    /**
     * 已删除
     */
    Deleted((byte) 2),

    /**
     * 已修改
     */
    Modified((byte) 3);

    /**
     * 状态
     */
    private final byte status;

    /**
     * 枚举对象状态
     *
     * @param type 状态
     */
    EObjectStatus(byte type) {
        this.status = type;
    }

    /**
     * 状态
     *
     * @return 状态
     */
    public byte getStatus() {
        return this.status;
    }
}
