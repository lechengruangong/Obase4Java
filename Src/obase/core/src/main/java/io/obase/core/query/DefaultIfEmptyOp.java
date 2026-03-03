/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示DefaultIfEmpty运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 16:56:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 表示DefaultIfEmpty运算
 */
public class DefaultIfEmptyOp extends QueryOp {

    /**
     * 序列为空时要返回的值
     */
    private final Object defaultValue;

    /**
     * 创建DefaultIfEmptyOp实例
     *
     * @param sourceType   查询源类型
     * @param defaultValue 序列为空时要返回的值
     */
    DefaultIfEmptyOp(Class<?> sourceType, Object defaultValue) {
        super(EQueryOpName.DefaultIfEmpty, sourceType);
        this.defaultValue = defaultValue;
    }

    /**
     * 获取序列为空时要返回的值
     *
     * @return 序列为空时要返回的值
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        if (this.defaultValue != null)
            return this.defaultValue.getClass();
        return null;
    }
}
