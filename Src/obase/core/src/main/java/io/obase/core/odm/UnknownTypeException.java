/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：未知类型异常,无法识别数据类型时抛出.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 11:49:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 未知类型异常,无法识别数据类型时抛出.
 */
public class UnknownTypeException extends RuntimeException {

    /**
     * 未识别的数据类型
     */
    private final Class<?> type;

    /**
     * 创建UnknownTypeException实例
     *
     * @param type 未识别的数据类型
     */
    public UnknownTypeException(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取异常信息
     *
     * @return 异常信息
     */
    @Override
    public String getMessage() {
        return "无法识别数据类型" + this.type.getName();
    }

    /**
     * 获取未识别的数据类型
     *
     * @return 获取未识别的数据类型
     */
    public Class<?> getType() {
        return this.type;
    }
}
