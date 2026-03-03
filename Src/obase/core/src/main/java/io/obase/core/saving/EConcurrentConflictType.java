/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举并发冲突类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:02:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 枚举并发冲突类型
 */
public enum EConcurrentConflictType {

    /**
     * 重复创建
     */
    RepeatCreation((byte) 0),

    /**
     * 版本冲突
     */
    VersionConflict((byte) 1),

    /**
     * 更新幻影
     */
    UpdatingPhantom((byte) 2);

    /**
     * 类型
     */
    private final byte type;

    /**
     * 枚举元素类型
     *
     * @param type 类型
     */
    EConcurrentConflictType(byte type) {
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