/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举Sql语句的类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:14:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

/**
 * 枚举Sql语句的类型
 */
public enum ESqlType {

    /**
     * 插入
     */
    Insert((byte) 0),

    /**
     * 删除
     */
    Delete((byte) 1),

    /**
     * 更新
     */
    Update((byte) 2),

    /**
     * 查询
     */
    Query((byte) 3);


    /**
     * 类型
     */
    private final byte type;

    /**
     * 具体值
     *
     * @param type 具体值
     */
    ESqlType(byte type) {
        this.type = type;
    }

    /**
     * 获取具体值
     *
     * @return 具体值
     */
    public byte getType() {
        return this.type;
    }
}
