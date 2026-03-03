/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举属性的合并处理策略.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 14:44:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 枚举属性的合并处理策略
 */
public enum EAttributeCombinationHandlingStrategy {

    /**
     * 覆盖——强制覆盖对方版本的值
     */
    Overwrite(0),
    /**
     * 忽略——忽略当前属性，即承认冲突对方版本的值
     */
    Ignore(1),
    /**
     * 累加——将当前版本中属性值的增量累加到对方版本
     */
    Accumulate(2);

    /**
     * 策略
     */
    private final int strategy;

    /**
     * 构造枚举属性的合并处理策略
     *
     * @param strategy 策略值
     */
    EAttributeCombinationHandlingStrategy(int strategy) {
        this.strategy = strategy;
    }

    /**
     * 获取策略值
     *
     * @return 策略值
     */
    public int getStrategy() {
        return this.strategy;
    }
}
