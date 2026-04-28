/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:37:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

/**
 * 序列化实体的属性
 */
public class SerializationAttribute extends SerializationTypeElement {

    /**
     * 属性名称
     */
    private final String name;

    /**
     * 初始化序列化实体的类型元素
     *
     * @param valueType 类型元素的值类型
     * @param name      属性名称
     */
    public SerializationAttribute(String name, Class<?> valueType) {
        super(valueType);
        this.name = name;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取是否需要存储
     * 如果是需要存储 则在序列化时调用ValueGetter获取值并存储到序列化结果中 此时会在IValueGetter中传入当前需要序列化的对象以供获取值时使用
     * 如果不需要存储 则在反序列化时调用ValueGetter获取值并赋值到对象中 此时IValueGetter中传入的对象为null
     *
     * @return 是否需要存储
     */
    @Override
    public boolean getNeedStorage() {
        return true;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "SerializationAttribute{" +
                "name='" + this.name + '\'' +
                ", valueType='" + this.getValueType() + '\'' +
                '}';
    }
}
