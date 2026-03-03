/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:33:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.ITextSerializer;

/**
 * 序列化取值器
 */
public class SerializedValueGetter implements IValueGetter {

    /**
     * 基础取值器
     */
    private final IValueGetter baseValueGetter;

    /**
     * 序列化器
     */
    private final ITextSerializer serializer;

    /**
     * 初始化序列化取值器
     *
     * @param baseValueGetter 基础取值器
     * @param serializer      序列化器
     */
    public SerializedValueGetter(IValueGetter baseValueGetter, ITextSerializer serializer) {
        this.baseValueGetter = baseValueGetter;
        this.serializer = serializer;
    }

    /**
     * 获取基础取值器
     *
     * @return 基础取值器
     */
    public IValueGetter getBaseValueGetter() {
        return this.baseValueGetter;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        //序列化
        return this.serializer.serialize(this.baseValueGetter.getValue(obj));
    }
}
