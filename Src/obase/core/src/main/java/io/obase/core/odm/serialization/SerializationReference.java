/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体的引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 11:10:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

/**
 * 序列化实体的引用
 */
public class SerializationReference extends SerializationTypeElement {

    /**
     * 引用是多重的还是单值的
     */
    private final boolean multiple;

    /**
     * 引用的名称
     */
    private final String name;

    /**
     * 初始化序列化实体的类型元素
     *
     * @param valueType 类型元素的值类型
     * @param multiple  引用是多重的还是单值的
     * @param name      引用的名称
     */
    public SerializationReference(boolean multiple, String name, Class<?> valueType) {
        super(valueType);
        this.multiple = multiple;
        this.name = name;
    }

    /**
     * 获取引用是多重的还是单值的
     *
     * @return 引用是多重的还是单值的
     */
    public boolean getMultiple() {
        return this.multiple;
    }

    /**
     * 获取引用的名称
     *
     * @return 引用的名称
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
        return false;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "SerializationReference{" +
                "name=" + this.name +
                ", multiple='" + this.multiple + '\'' +
                '}';
    }
}
