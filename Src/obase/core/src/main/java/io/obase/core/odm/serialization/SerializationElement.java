/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的类型元素.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:28:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import io.obase.core.odm.IValueGetter;

/**
 * 序列化实体的类型元素
 */
public abstract class SerializationElement {

    /**
     * 类型元素的值类型
     */
    private final Class<?> valueType;

    /**
     * 类型元素的值获取器
     */
    private IValueGetter valueGetter;

    /**
     * 初始化序列化实体的类型元素
     *
     * @param valueType 类型元素的值类型
     */
    protected SerializationElement(Class<?> valueType) {
        this.valueType = valueType;
    }

    /**
     * 获取类型元素的值类型
     *
     * @return 类型元素的值类型
     */
    public Class<?> getValueType() {
        return this.valueType;
    }

    /**
     * 获取类型元素的值获取器
     *
     * @return 类型元素的值获取器
     */
    public IValueGetter getValueGetter() {
        return this.valueGetter;
    }

    /**
     * 设置类型元素的值获取器
     *
     * @param valueGetter 类型元素的值获取器
     */
    public void setValueGetter(IValueGetter valueGetter) {
        this.valueGetter = valueGetter;
    }

    /**
     * 获取是否需要存储
     * 如果是需要存储 则在序列化时调用ValueGetter获取值并存储到序列化结果中 此时会在IValueGetter中传入当前需要序列化的对象以供获取值时使用
     * 如果不需要存储 则在反序列化时调用ValueGetter获取值并赋值到对象中 此时IValueGetter中传入的对象为null
     *
     * @return 是否需要存储
     */
    public abstract boolean getNeedStorage();

    /**
     * 从指定对象取出当前元素的值
     *
     * @param targetObj 要取其元素值的对象
     * @return 如果元素具有多重性，返回IEnumerable{T}，否则返回object
     */
    public Object getValue(Object targetObj) {
        return this.getValueGetter().getValue(targetObj);
    }
}
