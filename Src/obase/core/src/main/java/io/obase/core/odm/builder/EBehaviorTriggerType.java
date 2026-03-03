/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举对象行为触发器的类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 14:52:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 枚举对象行为触发器的类型
 */
public enum EBehaviorTriggerType {

    /**
     * 方法型触发器
     */
    Method((byte) 0),

    /**
     * Get访问器型触发器
     */
    PropertyGet((byte) 1),

    /**
     * Set访问器型触发器
     */
    PropertySet((byte) 2);

    /**
     * 触发器的类型
     */
    private final byte type;

    /**
     * 构造枚举对象行为触发器的类型
     *
     * @param type 触发器的类型
     */
    EBehaviorTriggerType(byte type) {
        this.type = type;
    }

    /**
     * 触发器的类型
     *
     * @return 触发器的类型
     */
    public byte getType() {
        return this.type;
    }
}
