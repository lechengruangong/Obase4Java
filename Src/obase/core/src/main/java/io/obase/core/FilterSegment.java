/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：映射筛选器片段.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 15:31:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ActionWithTwoArg;

/**
 * 映射筛选器片段
 */
public class FilterSegment {

    /**
     * 片段所属的筛选器
     */
    private final MappingFilter owner;

    /**
     * 一个委托，代表映射筛选器片段制作完成时回调的方法。
     * 该方法的第一个参数表示筛选器的依据域，第二个字段表示参考值。
     */
    private final ActionWithTwoArg<String, Object> segmentReady;

    /**
     * 依据域，即作为筛选依据的域。
     */
    private String field;

    /**
     * 参考值，即当依据域的值为该值时即判定满足条件
     */
    private Object referenceValue;

    /**
     * 创建FilterSegment实例
     *
     * @param owner        片段所属的筛选器
     * @param segmentReady 一个委托，代表映射筛选器片段制作完成时回调的方法
     */
    public FilterSegment(MappingFilter owner, ActionWithTwoArg<String, Object> segmentReady) {
        this.owner = owner;
        this.segmentReady = segmentReady;
    }

    /**
     * 获取参考值，即当依据域的值为该值时即判定满足条件
     *
     * @return 参考值，即当依据域的值为该值时即判定满足条件
     */
    public Object getReferenceValue() {
        return this.referenceValue;
    }

    /**
     * 设置筛选片段的依据域
     *
     * @param field 字段名
     * @return 当前筛选片段
     */
    public FilterSegment setField(String field) {
        this.field = field;
        return this;
    }

    /**
     * 设置筛选片段的参考值
     *
     * @param value 值
     * @return 当前片段所属的筛选器
     */
    public MappingFilter setReferenceValue(Object value) {
        this.referenceValue = value;
        //设置了值之后 视作完成
        this.segmentReady.invoke(this.field, this.referenceValue);
        return this.owner;
    }
}
