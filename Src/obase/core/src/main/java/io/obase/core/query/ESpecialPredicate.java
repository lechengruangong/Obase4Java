/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举断言是否启用特定访问逻辑返回的结果.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 15:17:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 枚举断言是否启用特定访问逻辑返回的结果
 */
public enum ESpecialPredicate {

    /**
     * 不启用
     */
    False((byte) 0),

    /**
     * 启用并替换通用访问逻辑
     */
    Substitute((byte) 1),

    /**
     * 在执行通用访问逻辑前启用特定逻辑
     */
    PreExecute((byte) 2),

    /**
     * 在执行通用访问逻辑后启用特定逻辑
     */
    PostExecute((byte) 3);

    /**
     * 特定访问逻辑返回的结果
     */
    private final byte predicate;

    /**
     * 枚举断言是否启用特定访问逻辑返回的结果
     *
     * @param predicate 特定访问逻辑返回的结果
     */
    ESpecialPredicate(byte predicate) {
        this.predicate = predicate;
    }

    /**
     * 获取特定访问逻辑返回的结果
     *
     * @return 特定访问逻辑返回的结果
     */
    public byte getPredicate() {
        return this.predicate;
    }
}
