/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的构造函数参数.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:44:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

/**
 * 序列化实体的构造函数参数
 */
public class SerializationConstructorParameter extends SerializationElement {

    /**
     * 对应的构造参数索引
     * 从0开始
     */
    private final String index;

    /**
     * 是否需要存储
     */
    private final boolean needStorage;

    /**
     * 初始化序列化实体的类型元素
     *
     * @param valueType   类型元素的值类型
     * @param index       对应的构造参数索引
     * @param needStorage 是否需要存储
     */
    public SerializationConstructorParameter(String index, boolean needStorage, Class<?> valueType) {
        super(valueType);
        this.index = index;
        this.needStorage = needStorage;
    }

    /**
     * 获取对应的构造参数索引
     *
     * @return 对应的构造参数索引
     */
    public String getIndex() {
        return this.index;
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
        return this.needStorage;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "SerializationConstructorParameter{" +
                "index='" + this.index + '\'' +
                ", needStorage=" + this.needStorage +
                '}';
    }
}
