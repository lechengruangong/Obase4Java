/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举形参指代.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 11:41:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 枚举形参指代
 */
public enum EParameterReferring {

    /**
     * 查询源中的单个对象或值
     */
    Single((byte) 0),

    /**
     * 查询源序列
     */
    Sequence((byte) 1),

    /**
     * 查询源中对象或值的索引号
     */
    Index((byte) 2);

    /**
     * 形参指代的内容
     */
    private final byte referring;

    /**
     * 构造枚举形参指代 表明该形参指代的内容
     *
     * @param referring 形参指代的内容
     */
    EParameterReferring(byte referring) {
        this.referring = referring;
    }

    /**
     * 形参指代的内容
     *
     * @return 形参指代的内容
     */
    public byte getReferring() {
        return this.referring;
    }
}
