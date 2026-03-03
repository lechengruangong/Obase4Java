/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举并发冲突处理策略.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 17:06:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 枚举并发冲突处理策略
 */
public enum EConcurrentConflictHandlingStrategy {

    /**
     * 忽略
     */
    Ignore((byte) 0),

    /**
     * ThrowException
     */
    ThrowException((byte) 1),

    /**
     * 强制覆盖
     */
    Overwrite((byte) 2),

    /**
     * 版本合并
     */
    Combine((byte) 3),

    /**
     * 重建对象
     */
    Reconstruct((byte) 4);

    /**
     * 策略
     */
    private final byte strategy;

    /**
     * 构造枚举并发冲突处理策略
     *
     * @param strategy 策略
     */
    EConcurrentConflictHandlingStrategy(byte strategy) {
        this.strategy = strategy;
    }

    /**
     * 策略
     *
     * @return 策略
     */
    public byte getStrategy() {
        return this.strategy;
    }
}
